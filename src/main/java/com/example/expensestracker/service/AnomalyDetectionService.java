package com.example.expensestracker.service;

import com.example.expensestracker.model.dto.response.AnomalyResponse;
import com.example.expensestracker.model.entity.CategoryLimitEntity;
import com.example.expensestracker.model.entity.TransactionEntity;
import com.example.expensestracker.repositories.CategoryLimitRepository;
import com.example.expensestracker.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service phát hiện giao dịch bất thường (Gatekeeper/Gác cổng).
 * Kết hợp hai phương pháp:
 * 1. AI (Isolation Forest) để phát hiện sai lệch Pattern (bất thường so với
 * thói quen).
 * 2. Rule-based: Bất thường khi 1 giao dịch vượt xa số tiền còn lại trong ngân
 * sách.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionService {

    private final TransactionRepository transactionRepository;
    private final CategoryLimitRepository categoryLimitRepository;

    @Value("${mlops.predict-server-url:http://localhost:8001}")
    private String predictServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Dò tìm các giao dịch bất thường trong thời gian gần đây (30 ngày).
     */
    public List<AnomalyResponse> checkRecentTransactions(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusDays(30); // Lấy 30 ngày gần nhất

        // CHI LOC EXPENSE - loai bo income, chi kiem tra bat thuong tren chi tieu
        List<TransactionEntity> recentTx = transactionRepository
                .findByUserIdAndDateBetween(userId, startDate, now)
                .stream()
                .filter(t -> t.getCategory().getType().name().equalsIgnoreCase("EXPENSE"))
                .collect(Collectors.toList());

        if (recentTx.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Chuẩn bị tính toán Trung bình để gửi cho iForest
        Map<Long, Double> categoryAvg = recentTx.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getCategoryId(),
                        Collectors.averagingDouble(t -> t.getAmount().doubleValue())));

        List<Map<String, Object>> transactions = new ArrayList<>();
        for (TransactionEntity tx : recentTx) {
            double catAvg = categoryAvg.getOrDefault(tx.getCategory().getCategoryId(), 1.0);
            Map<String, Object> txMap = new HashMap<>();
            txMap.put("transaction_id", tx.getTransactionId());
            txMap.put("amount", tx.getAmount().doubleValue());
            txMap.put("category_id", tx.getCategory().getCategoryId());
            txMap.put("day_of_week", tx.getTransactionDate().getDayOfWeek().getValue() - 1);
            txMap.put("day_of_month", tx.getTransactionDate().getDayOfMonth());
            txMap.put("amount_vs_category_avg", catAvg > 0 ? tx.getAmount().doubleValue() / catAvg : 1.0);
            transactions.add(txMap);
        }

        // 2. Lấy Limit Ngân sách trong tháng hiện tại để chạy rule-based
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        List<CategoryLimitEntity> limits = categoryLimitRepository.findByUserIdAndMonthAndYear(userId, currentMonth,
                currentYear);
        Map<Long, Double> limitMap = limits.stream()
                .collect(
                        Collectors.toMap(l -> l.getCategory().getCategoryId(), l -> l.getLimitExpense().doubleValue()));

        // Tính tiền đã tiêu từng danh mục tháng này - CHI LOC EXPENSE
        List<TransactionEntity> currentMonthTx = transactionRepository.findByUserIdAndDateBetween(userId,
                        now.withDayOfMonth(1), now)
                .stream()
                .filter(t -> t.getCategory().getType().name().equalsIgnoreCase("EXPENSE"))
                .collect(Collectors.toList());
        Map<Long, Double> spentMap = currentMonthTx.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().getCategoryId(),
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())));

        List<AnomalyResponse> results = new ArrayList<>();

        // 3. Gọi FastAPI iForest
        try {
            String url = predictServerUrl + "/predict/anomaly";
            Map<String, Object> body = new HashMap<>();
            body.put("transactions", transactions);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map[].class);

            if (response.getBody() == null) {
                return Collections.emptyList();
            }

            Map<Long, String> txCategoryNames = recentTx.stream()
                    .collect(Collectors.toMap(TransactionEntity::getTransactionId,
                            t -> t.getCategory().getCategoryName(), (a, b) -> a));

            for (Map resultMap : response.getBody()) {
                Long txId = ((Number) resultMap.get("transaction_id")).longValue();
                boolean isAnomalyByAI = (boolean) resultMap.get("is_anomaly");
                double score = ((Number) resultMap.get("anomaly_score")).doubleValue();
                String mlMessage = (String) resultMap.get("message");

                TransactionEntity originalTx = recentTx.stream().filter(t -> t.getTransactionId().equals(txId))
                        .findFirst().orElse(null);
                if (originalTx == null)
                    continue;

                double amount = originalTx.getAmount().doubleValue();
                Long catId = originalTx.getCategory().getCategoryId();

                // Rule-based check logic (Chỉ check với các giao dịch cùng tháng hiện tại)
                boolean isAnomalyByRule = false;
                String ruleMessage = mlMessage;
                if (originalTx.getTransactionDate().getMonthValue() == currentMonth) {
                    double limit = limitMap.getOrDefault(catId, 0.0);
                    if (limit > 0) {
                        double totalSpent = spentMap.getOrDefault(catId, 0.0);
                        double remainingBeforeTx = limit - (totalSpent - amount); // Hoàn lại tiền từ tx này để xem dư
                        // bao nhiêu

                        // Chỉ cảnh báo "nguốn 80% còn lại" nếu số tiền tiêu VỪA CHIẾM 80% phần dư, VỪA PHẢI ĐỦ LỚN (> 10% tổng ngân sách)
                        if (limit > 0 && remainingBeforeTx > 0 && amount > remainingBeforeTx * 0.8 && amount > limit * 0.1) {
                            isAnomalyByRule = true;
                            ruleMessage = "Báo động: Khoản chi này đã ngốn quá 80% số dư ngân sách còn lại của danh mục!";

                        } else if (amount > limit * 0.5) { // 1 nhát chiếm 50% limit cả tháng
                            isAnomalyByRule = true;
                            ruleMessage = "Cảnh báo: Khoản chi quá lớn, chiếm hơn một nửa tổng ngân sách tháng của bạn!";
                        }
                    }
                }

                // Gộp kết quả
                if (isAnomalyByAI || isAnomalyByRule) {
                    results.add(AnomalyResponse.builder()
                            .transactionId(txId)
                            .amount(amount)
                            .categoryName(txCategoryNames.getOrDefault(txId, "Không rõ"))
                            .isAnomaly(true)
                            .anomalyScore(isAnomalyByAI ? score : 0.9) // Nếu rule-based bắt được, cho score cao
                            .message(isAnomalyByRule ? ruleMessage : mlMessage)
                            .build());
                }
            }

            // Xếp hạng: ưu tiên rule-based hoặc score cao nhất lên trước
            results.sort((a, b) -> Double.compare(b.getAnomalyScore(), a.getAnomalyScore()));
            return results;

        } catch (Exception e) {
            log.error("Lỗi khi gọi FastAPI anomaly detection: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
