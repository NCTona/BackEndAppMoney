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
 * Service du doan chi tieu theo danh muc bang LightGBM.
 * Du doan tong chi tieu CUOI THANG HIEN TAI dua tren chi tieu dang dien ra.
 * So sanh voi ngan sach (budget) de canh bao user.
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
         * Du doan chi tieu cuoi thang cho TAT CA danh muc chi tieu.
         * Tinh features tu du lieu thang hien tai + so sanh voi budget.
         */
        public List<CategoryPredictResponse> predictAllCategories(Long userId) {
                LocalDate now = LocalDate.now();
                int daysPassed = now.getDayOfMonth();
                int totalDays = YearMonth.of(now.getYear(), now.getMonthValue()).lengthOfMonth();
                int daysRemaining = totalDays - daysPassed;

                // Lay giao dich thang HIEN TAI cua user
                LocalDate startOfMonth = now.withDayOfMonth(1);
                List<TransactionEntity> currentMonthTx = transactionRepository
                                .findByUserIdAndDateBetween(userId, startOfMonth, now)
                                .stream()
                                .filter(t -> t.getCategory().getType().name().equalsIgnoreCase("EXPENSE"))
                                .collect(Collectors.toList());

                // Tong chi tieu tat ca categories trong thang hien tai
                double totalAllCategories = currentMonthTx.stream()
                                .mapToDouble(t -> t.getAmount().doubleValue()).sum();

                // Group theo category
                Map<Long, List<TransactionEntity>> byCat = currentMonthTx.stream()
                                .collect(Collectors.groupingBy(t -> t.getCategory().getCategoryId()));

                // Lay budget cua user
                List<CategoryLimitEntity> allLimits = categoryLimitRepository.findByUserIdAndMonthAndYear(
                                userId, now.getMonthValue(), now.getYear());
                Map<Long, Double> budgetMap = allLimits.stream()
                                .filter(l -> l.getCategory().getCategoryId() != 9L) // Loai Tiet kiem
                                .collect(Collectors.toMap(
                                                l -> l.getCategory().getCategoryId(),
                                                l -> l.getLimitExpense().doubleValue(),
                                                (a, b) -> a));

                // Lay TAT CA 8 danh muc chi tieu (loai Tiet kiem - ID 9)
                List<CategoryEntity> expenseCategories = categoryRepository.findByExpense().stream()
                                .filter(c -> c.getCategoryId() != 9L)
                                .collect(Collectors.toList());

                List<CategoryPredictRequest> requests = new ArrayList<>();

                for (CategoryEntity cat : expenseCategories) {
                        Long catId = cat.getCategoryId();
                        List<TransactionEntity> catTx = byCat.getOrDefault(catId, Collections.emptyList());

                        double currentSpent = catTx.stream()
                                        .mapToDouble(t -> t.getAmount().doubleValue()).sum();
                        int currentTxCount = catTx.size();
                        double dailyRate = daysPassed > 0 ? currentSpent / daysPassed : 0;
                        double categoryRatio = totalAllCategories > 0
                                        ? currentSpent / totalAllCategories
                                        : 0;
                        double budget = budgetMap.getOrDefault(catId, 0.0);

                        requests.add(CategoryPredictRequest.builder()
                                        .categoryId(catId)
                                        .daysPassed(daysPassed)
                                        .daysRemaining(daysRemaining)
                                        .currentSpent(currentSpent)
                                        .currentTxCount(currentTxCount)
                                        .dailyRate(dailyRate)
                                        .categoryRatio(categoryRatio)
                                        .budget(budget)
                                        .build());
                }

                // Goi FastAPI predict
                return callPredictApi(requests);
        }

        /**
         * Phan tich xu huong chi tieu cua user so voi so dong cho 1 category.
         */
        public TrendAnalysisResponse analyzeTrend(Long userId, Long categoryId) {
                LocalDate now = LocalDate.now();

                // Chi tieu thang truoc da hoan tat cua user cho category nay
                LocalDate prevMonth = now.minusMonths(1);
                BigDecimal userSpent = transactionRepository.sumSpentByCategoryAndUser(
                                userId, categoryId, prevMonth.getMonthValue(), prevMonth.getYear());
                double userSpending = userSpent != null ? userSpent.doubleValue() : 0;

                // Lay chi tieu trung binh cua TAT CA users cho category nay trong 6 thang gan nhat
                List<Double> monthlyAverages = new ArrayList<>();
                for (int i = 1; i <= 6; i++) {
                        LocalDate targetMonth = now.minusMonths(i);
                        LocalDate startOfTarget = targetMonth.withDayOfMonth(1);
                        LocalDate endOfTarget = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

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

                if (monthlyAverages.isEmpty()) {
                        return TrendAnalysisResponse.builder()
                                        .categoryId(categoryId)
                                        .userSpending(userSpending)
                                        .populationAverage(0)
                                        .status("no_data")
                                        .message("Chua co du du lieu so dong de so sanh.")
                                        .build();
                }

                return callTrendApi(categoryId, monthlyAverages, userSpending);
        }

        // =========== Private helpers ===========

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
                        log.error("Loi khi goi FastAPI predict server: {}", e.getMessage());
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
                        log.error("Loi khi goi FastAPI trend analysis: {}", e.getMessage());
                        return TrendAnalysisResponse.builder()
                                        .categoryId(categoryId)
                                        .userSpending(userSpending)
                                        .status("error")
                                        .message("Khong the ket noi den AI server")
                                        .build();
                }
        }
}
