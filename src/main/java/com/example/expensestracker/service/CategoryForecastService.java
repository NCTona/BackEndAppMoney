package com.example.expensestracker.service;

import com.example.expensestracker.model.dto.request.CategoryPredictRequest;
import com.example.expensestracker.model.dto.response.CategoryPredictResponse;
import com.example.expensestracker.model.dto.response.TrendAnalysisResponse;
import com.example.expensestracker.model.entity.CategoryEntity;
import com.example.expensestracker.model.entity.CategoryLimitEntity;
import com.example.expensestracker.model.entity.TransactionEntity;
import com.example.expensestracker.repositories.CategoryLimitRepository;
import com.example.expensestracker.repositories.CategoryRepository;
import com.example.expensestracker.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý dự đoán chi tiêu theo danh mục bằng LightGBM.
 * Đồng thời phân tích Ngân sách (Budget Limit) và Tái Phân Bổ (Redistribution).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryForecastService {

        private final TransactionRepository transactionRepository;
        private final CategoryRepository categoryRepository;
        private final CategoryLimitRepository categoryLimitRepository;

        @Value("${mlops.predict-server-url:http://localhost:8001}")
        private String predictServerUrl;

        private final RestTemplate restTemplate = new RestTemplate();

        /**
         * Lọc TẤT CẢ các danh mục chi tiêu, kết hợp Budget và tái phân bổ.
         */
        public List<CategoryPredictResponse> predictAllCategories(Long userId) {
                return predictAllCategories(userId, true);
        }

        /**
         * @param normalized true = ràng buộc budget (dùng cho Budget Screen),
         *                   false = dự đoán tự do từ ML (dùng cho Forecast Screen).
         */
        public List<CategoryPredictResponse> predictAllCategories(Long userId, boolean normalized) {
                LocalDate now = LocalDate.now();

                // Lấy 3 tháng gần nhất ĐÃ HOÀN TẤT
                LocalDate month1Date = now.minusMonths(1);
                LocalDate month2Date = now.minusMonths(2);
                LocalDate month3Date = now.minusMonths(3);

                List<TransactionEntity> month1Tx = getExpenseTransactions(userId, month1Date);
                List<TransactionEntity> month2Tx = getExpenseTransactions(userId, month2Date);
                List<TransactionEntity> month3Tx = getExpenseTransactions(userId, month3Date);

                double totalMonth1 = month1Tx.stream().mapToDouble(t -> t.getAmount().doubleValue()).sum();
                double totalMonth2 = month2Tx.stream().mapToDouble(t -> t.getAmount().doubleValue()).sum();

                Map<Long, List<TransactionEntity>> m1ByCat = groupByCategory(month1Tx);
                Map<Long, List<TransactionEntity>> m2ByCat = groupByCategory(month2Tx);
                Map<Long, List<TransactionEntity>> m3ByCat = groupByCategory(month3Tx);

                // YÊU CẦU: Đảm bảo chạy cho TẤT CẢ 8 danh mục chi tiêu (loại Tiết kiệm - ID 9)
                List<CategoryEntity> expenseCategories = categoryRepository.findByExpense().stream()
                                .filter(c -> c.getCategoryId() != 9L)
                                .collect(Collectors.toList());

                List<CategoryPredictRequest> requests = new ArrayList<>();
                Map<Long, Integer> frequencyMap = new HashMap<>(); // đếm số block giao dịch để xét mức độ thiết yếu

                for (CategoryEntity cat : expenseCategories) {
                        Long catId = cat.getCategoryId();
                        List<TransactionEntity> m1List = m1ByCat.getOrDefault(catId, Collections.emptyList());
                        List<TransactionEntity> m2List = m2ByCat.getOrDefault(catId, Collections.emptyList());
                        List<TransactionEntity> m3List = m3ByCat.getOrDefault(catId, Collections.emptyList());

                        int totalFrequency = m1List.size() + m2List.size() + m3List.size();
                        frequencyMap.put(catId, totalFrequency);

                        double monthlySpending = sumAmounts(m1List);
                        int txCount = m1List.size();
                        double avgTx = txCount > 0 ? monthlySpending / txCount : 0;
                        double maxTx = m1List.stream().mapToDouble(t -> t.getAmount().doubleValue()).max().orElse(0);
                        double avgDow = m1List.stream()
                                        .mapToDouble(t -> t.getTransactionDate().getDayOfWeek().getValue()).average()
                                        .orElse(3);
                        double avgDom = m1List.stream().mapToDouble(t -> t.getTransactionDate().getDayOfMonth())
                                        .average().orElse(15);
                        double catRatio = totalMonth1 > 0 ? monthlySpending / totalMonth1 : 0;

                        double prevSpending = sumAmounts(m2List);
                        int prevCount = m2List.size();
                        double prevRatio = totalMonth2 > 0 ? prevSpending / totalMonth2 : 0;

                        double m3Spending = sumAmounts(m3List);
                        List<Double> monthlyValues = Arrays.asList(monthlySpending, prevSpending, m3Spending);
                        long nonZeroCount = monthlyValues.stream().filter(v -> v > 0).count();
                        double avgMonthly3m = nonZeroCount > 0
                                        ? monthlyValues.stream().mapToDouble(Double::doubleValue).sum() / nonZeroCount
                                        : 0;

                        double spendingTrend = avgMonthly3m > 0 ? Math.min(monthlySpending / avgMonthly3m, 5.0) : 1.0;

                        requests.add(CategoryPredictRequest.builder()
                                        .categoryId(catId)
                                        .month(now.getMonthValue())
                                        .year(now.getYear())
                                        .monthlySpending(monthlySpending)
                                        .transactionCount(txCount)
                                        .avgTransaction(avgTx)
                                        .maxTransaction(maxTx)
                                        .avgDayOfWeek(avgDow)
                                        .avgDayOfMonth(avgDom)
                                        .totalAllCategories(totalMonth1)
                                        .categoryRatio(catRatio)
                                        .prevMonthSpending(prevSpending)
                                        .prevMonthCount(prevCount)
                                        .prevMonthRatio(prevRatio)
                                        .avgMonthlySpending3m(avgMonthly3m)
                                        .spendingTrend(spendingTrend)
                                        .build());
                }

                // Gọi ML Model
                List<CategoryPredictResponse> mlResponses = callPredictApi(requests);

                // ===== NORMALIZE: Ràng buộc tổng gợi ý = tổng budget hiện tại =====
                if (normalized) {
                        // Lấy tổng budget user đã thiết lập
                        List<CategoryLimitEntity> allLimits = categoryLimitRepository.findByUserIdAndMonthAndYear(
                                        userId, now.getMonthValue(), now.getYear());
                        // Chỉ tính budget của các danh mục chi tiêu (loại Tiết kiệm - ID 9)
                        double totalBudget = allLimits.stream()
                                        .filter(l -> l.getCategory().getCategoryId() != 9L)
                                        .mapToDouble(l -> l.getLimitExpense().doubleValue()).sum();

                        if (totalBudget > 0 && !mlResponses.isEmpty()) {
                                double totalPredicted = mlResponses.stream()
                                                .mapToDouble(CategoryPredictResponse::getPredictedSpending).sum();

                                if (totalPredicted > 0) {
                                        // Scale tỷ lệ: predicted_i = (predicted_i / totalPredicted) * totalBudget
                                        for (CategoryPredictResponse res : mlResponses) {
                                                double ratio = res.getPredictedSpending() / totalPredicted;
                                                double scaledValue = Math.round(ratio * totalBudget);
                                                res.setPredictedSpending(scaledValue);
                                        }

                                        // Bù sai số làm tròn vào danh mục lớn nhất
                                        double sumAfterScale = mlResponses.stream()
                                                        .mapToDouble(CategoryPredictResponse::getPredictedSpending)
                                                        .sum();
                                        double diff = totalBudget - sumAfterScale;
                                        if (diff != 0) {
                                                mlResponses.stream()
                                                                .max(Comparator.comparingDouble(
                                                                                CategoryPredictResponse::getPredictedSpending))
                                                                .ifPresent(r -> r.setPredictedSpending(
                                                                                r.getPredictedSpending() + diff));
                                        }
                                }
                        }

                        // Hậu xử lý logic Budget Re-distribution
                        return applyBudgetRedistribution(userId, mlResponses, frequencyMap);
                }

                // Dự đoán tự do: trả kết quả ML trực tiếp
                return mlResponses;
        }

        private List<CategoryPredictResponse> applyBudgetRedistribution(Long userId,
                        List<CategoryPredictResponse> responses, Map<Long, Integer> frequencyMap) {
                LocalDate now = LocalDate.now();
                int daysRemaining = YearMonth.of(now.getYear(), now.getMonthValue()).lengthOfMonth()
                                - now.getDayOfMonth() + 1;

                boolean needsRescue = false;
                double availableRescueFunds = 0;

                for (CategoryPredictResponse mlRes : responses) {
                        Long catId = mlRes.getCategoryId();

                        CategoryLimitEntity limitEntity = categoryLimitRepository
                                        .findByUserIdAndCategoryIdAndMonthAndYear(
                                                        userId, catId, now.getMonthValue(), now.getYear());
                        double limit = limitEntity != null ? limitEntity.getLimitExpense().doubleValue() : 0;

                        BigDecimal spentBd = transactionRepository.sumSpentByCategoryAndUser(
                                        userId, catId, now.getMonthValue(), now.getYear());
                        double currentSpent = spentBd != null ? spentBd.doubleValue() : 0;

                        double remaining = limit - currentSpent;

                        // Tiêu chí "Thiết yếu": có tối thiểu 10 giao dịch trong vòng 3 tháng qua
                        // (Khoảng 3-4 giao dịch/tháng)
                        boolean isEssential = frequencyMap.getOrDefault(catId, 0) >= 10;
                        mlRes.setBudgetLimit(limit);
                        mlRes.setEssential(isEssential);

                        double daily = limit > 0 ? Math.max(0, remaining) / daysRemaining : 0;
                        mlRes.setRecommendedDailyAllocation(daily);

                        // Nếu thiết yếu nhưng ngân sách còn lại < 15% tổng hạn mức
                        if (isEssential && limit > 0 && remaining <= limit * 0.15) {
                                needsRescue = true;
                        }

                        // Những danh mục không thiết yếu sẽ là quỹ giải cứu nếu còn tiền
                        if (!isEssential && remaining > 0) {
                                availableRescueFunds += remaining;
                        }
                }

                // Tái phân bổ vắt kiệt tiền Quỹ Phụ (Linh Hoạt) đẩy sang Quỹ Chính (Thiết yếu)
                // do Quỹ Chính sắp âm tiền.
                if (needsRescue && availableRescueFunds > 0) {
                        long essentialRescueCount = responses.stream()
                                        .filter(r -> r.isEssential() && r.getBudgetLimit() > 0 && (r.getBudgetLimit()
                                                        - (r.getBudgetLimit() - r.getRecommendedDailyAllocation()
                                                                        * daysRemaining) <= r.getBudgetLimit() * 0.15))
                                        .count();

                        double distributePerEssential = essentialRescueCount > 0
                                        ? availableRescueFunds / essentialRescueCount
                                        : 0;

                        for (CategoryPredictResponse mlRes : responses) {
                                if (!mlRes.isEssential()) {
                                        mlRes.setRecommendedDailyAllocation(0);
                                        mlRes.setReason("Đóng quỹ ngày do mục thiết yếu khác đang cạn kiệt.");
                                } else if (mlRes.isEssential() && mlRes.getBudgetLimit() > 0 && (mlRes.getBudgetLimit()
                                                - (mlRes.getBudgetLimit() - mlRes.getRecommendedDailyAllocation()
                                                                * daysRemaining) <= mlRes.getBudgetLimit() * 0.15)) {
                                        double extraDaily = distributePerEssential / daysRemaining;
                                        mlRes.setRecommendedDailyAllocation(
                                                        mlRes.getRecommendedDailyAllocation() + extraDaily);
                                        mlRes.setReason("Hệ thống tự động cắt giảm danh mục phụ để ưu tiên quỹ cho mục này.");
                                } else {
                                        mlRes.setReason("Duy trì chi tiêu hợp lý theo kế hoạch.");
                                }
                        }
                } else {
                        // Không cần giải cứu
                        for (CategoryPredictResponse mlRes : responses) {
                                double limit = mlRes.getBudgetLimit();
                                // Note: We need currentSpent to check if they overspent. It can be derived:
                                // limit - (dailyAlloc * daysRemaining)
                                double currentSpent = limit - (mlRes.getRecommendedDailyAllocation() * daysRemaining);

                                if (limit == 0) {
                                        mlRes.setReason("Chưa thiết lập Ngân sách (Budget).");
                                } else if (currentSpent > limit) {
                                        mlRes.setReason("Cảnh báo: Bạn đã chi tiêu quá giới hạn tháng này!");
                                } else {
                                        mlRes.setReason("Ngân sách giữ an toàn.");
                                }
                        }
                }

                return responses;
        }

        /**
         * Phân tích xu hướng chi tiêu của user so với số đông cho 1 category.
         */
        public TrendAnalysisResponse analyzeTrend(Long userId, Long categoryId) {
                LocalDate now = LocalDate.now();
                int currentMonth = now.getMonthValue();
                int currentYear = now.getYear();

                // Chi tiêu tháng trước đã hoàn tất của user cho category này
                LocalDate prevMonth = now.minusMonths(1);
                BigDecimal userSpent = transactionRepository.sumSpentByCategoryAndUser(
                                userId, categoryId, prevMonth.getMonthValue(), prevMonth.getYear());
                double userSpending = userSpent != null ? userSpent.doubleValue() : 0;

                // Lấy chi tiêu trung bình của TẤT CẢ users cho category này trong 6 tháng gần
                // nhất
                List<Double> monthlyAverages = new ArrayList<>();
                for (int i = 1; i <= 6; i++) {
                        LocalDate targetMonth = now.minusMonths(i);
                        LocalDate startOfTarget = targetMonth.withDayOfMonth(1);
                        LocalDate endOfTarget = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

                        // Lấy tất cả giao dịch của hệ thống (không filter user)
                        List<TransactionEntity> allTx = transactionRepository.findAllByDateBetween(
                                        startOfTarget, endOfTarget);

                        List<TransactionEntity> categoryTx = allTx.stream()
                                        .filter(t -> t.getCategory().getCategoryId().equals(categoryId))
                                        .filter(t -> t.getCategory().getType().name().equalsIgnoreCase("EXPENSE"))
                                        .collect(Collectors.toList());

                        if (!categoryTx.isEmpty()) {
                                Map<Long, Double> perUserSpending = categoryTx.stream()
                                                .collect(Collectors.groupingBy(
                                                                t -> t.getUser().getUserId(),
                                                                Collectors.summingDouble(
                                                                                t -> t.getAmount().doubleValue())));
                                double avg = perUserSpending.values().stream()
                                                .mapToDouble(Double::doubleValue).average().orElse(0);
                                monthlyAverages.add(avg);
                        }
                }

                // Nếu không có dữ liệu trung bình, trả về response mặc định
                if (monthlyAverages.isEmpty()) {
                        return TrendAnalysisResponse.builder()
                                        .categoryId(categoryId)
                                        .userSpending(userSpending)
                                        .populationAverage(0)
                                        .status("no_data")
                                        .message("Chua co du du lieu so dong de so sanh.")
                                        .build();
                }

                // Gọi FastAPI Trend Analysis
                return callTrendApi(categoryId, monthlyAverages, userSpending);
        }

        // =========== Private helpers ===========

        private List<TransactionEntity> getExpenseTransactions(Long userId, LocalDate monthDate) {
                LocalDate start = monthDate.withDayOfMonth(1);
                LocalDate end = monthDate.withDayOfMonth(monthDate.lengthOfMonth());
                return transactionRepository.findByUserIdAndDateBetween(userId, start, end)
                                .stream()
                                .filter(t -> t.getCategory().getType().name().equalsIgnoreCase("EXPENSE"))
                                .collect(Collectors.toList());
        }

        private Map<Long, List<TransactionEntity>> groupByCategory(List<TransactionEntity> transactions) {
                return transactions.stream()
                                .collect(Collectors.groupingBy(t -> t.getCategory().getCategoryId()));
        }

        private double sumAmounts(List<TransactionEntity> transactions) {
                return transactions.stream().mapToDouble(t -> t.getAmount().doubleValue()).sum();
        }

        private List<CategoryPredictResponse> callPredictApi(List<CategoryPredictRequest> requests) {
                try {
                        String url = predictServerUrl + "/predict/bulk";
                        Map<String, Object> body = new HashMap<>();
                        body.put("predictions", requests);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                        ResponseEntity<CategoryPredictResponse[]> response = restTemplate.exchange(
                                        url, HttpMethod.POST, entity, CategoryPredictResponse[].class);

                        if (response.getBody() != null) {
                                return Arrays.asList(response.getBody());
                        }
                } catch (Exception e) {
                        log.error("Lỗi khi gọi FastAPI predict server: {}", e.getMessage());
                }
                return Collections.emptyList();
        }

        private TrendAnalysisResponse callTrendApi(Long categoryId, List<Double> monthlyAverages, double userSpending) {
                try {
                        String url = predictServerUrl + "/predict/trend";
                        Map<String, Object> body = new HashMap<>();
                        body.put("category_id", categoryId);
                        body.put("monthly_averages", monthlyAverages);
                        body.put("user_current_spending", userSpending);

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                        ResponseEntity<TrendAnalysisResponse> response = restTemplate.exchange(
                                        url, HttpMethod.POST, entity, TrendAnalysisResponse.class);

                        return response.getBody();
                } catch (Exception e) {
                        log.error("Lỗi khi gọi FastAPI trend analysis: {}", e.getMessage());
                        return TrendAnalysisResponse.builder()
                                        .categoryId(categoryId)
                                        .userSpending(userSpending)
                                        .status("error")
                                        .message("Không thể kết nối đến AI server")
                                        .build();
                }
        }
}
