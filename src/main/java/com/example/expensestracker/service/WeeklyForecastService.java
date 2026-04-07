package com.example.expensestracker.service;

import com.example.expensestracker.model.dto.response.AISummaryResponse;
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

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service tong hop du lieu chi tieu AN UONG theo tuan de phuc vu du bao.
 * Cung cap du lieu dau vao (input_weeks) cho model TFLite tren thiet bi di dong.
 *
 * LSTM chi train tren category An uong (categoryId=2) vi:
 * - Cac category khac co chi tieu dot ngot, bat thuong -> LSTM khong on dinh
 * - An uong co tan suat deu, amount on dinh -> phu hop voi LSTM forecasting
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({ "null", "unchecked" })
public class WeeklyForecastService {

    // Category An uong - dong bo voi LSTM_FOOD_CATEGORY_ID trong MLOps config
    private static final Long FOOD_CATEGORY_ID = 2L;

    private final TransactionRepository transactionRepository;
    private final CategoryLimitRepository categoryLimitRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public AISummaryResponse.WeeklyForecast predictNextWeek(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusDays(27); // 28 ngày (bao gồm hôm nay)

        // Chi lay category An uong (dong bo voi LSTM model chi train tren food data)
        List<TransactionEntity> transactions = transactionRepository
                .findByUserIdAndDateBetween(userId, startDate, now)
                .stream()
                .filter(t -> t.getCategory().getType().name().equalsIgnoreCase("EXPENSE"))
                .filter(t -> t.getCategory().getCategoryId().equals(FOOD_CATEGORY_ID))
                .collect(Collectors.toList());

        // Tổng hợp theo ngày
        Map<LocalDate, Double> dailyMap = new HashMap<>();
        for (TransactionEntity tx : transactions) {
            dailyMap.merge(tx.getTransactionDate(), tx.getAmount().doubleValue(), Double::sum);
        }

        List<Double> weeklySpending = new ArrayList<>();
        for (int w = 0; w < 4; w++) {
            double weekSum = 0;
            for (int d = 0; d < 7; d++) {
                LocalDate date = startDate.plusDays(w * 7 + d);
                weekSum += dailyMap.getOrDefault(date, 0.0);
            }
            weeklySpending.add(weekSum);
        }

        // CHUẨN BỊ DỮ LIỆU ĐỂ ANDROID TỰ DỰ BÁO BẰNG TFLITE
        int daysInMonth = YearMonth.of(now.getYear(), now.getMonthValue()).lengthOfMonth();
        int remainingDays = daysInMonth - now.getDayOfMonth() + 1;

        // Tính tạm dự báo bằng trung bình cũ (Server-side fallback)
        double avgWeekly = weeklySpending.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double predictedRemaining = (avgWeekly / 7) * remainingDays;

        // Tinh ngan sach va da chi CHI cho category An uong
        List<CategoryLimitEntity> limits = categoryLimitRepository.findByUserIdAndMonthAndYear(
                userId, now.getMonthValue(), now.getYear());
        double totalBudget = limits.stream()
                .filter(l -> l.getCategory().getCategoryId().equals(FOOD_CATEGORY_ID))
                .mapToDouble(l -> l.getLimitExpense().doubleValue())
                .sum();

        // Lay chi tieu An uong da tieu thang nay
        List<TransactionEntity> currentMonthTx = transactionRepository.findByUserIdAndDateBetween(
                userId, now.withDayOfMonth(1), now);
        double totalSpent = currentMonthTx.stream()
                .filter(t -> t.getCategory().getType().name().equalsIgnoreCase("EXPENSE"))
                .filter(t -> t.getCategory().getCategoryId().equals(FOOD_CATEGORY_ID))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        double remainingBudget = totalBudget - totalSpent;
        boolean isOverBudget = false;
        String warningMessage = "";

        if (totalBudget > 0) {
            if (remainingBudget <= 0) {
                isOverBudget = true;
                warningMessage = "Báo động Đỏ: Bạn đã TIÊU HẾT tổng ngân sách tháng này!";
            } else if (predictedRemaining > remainingBudget) {
                isOverBudget = true;
                warningMessage = String.format(
                        "Báo động Đỏ: Với mức chi tiêu trung bình, bạn có nguy cơ vượt ngân sách %.0f₫.",
                        (predictedRemaining - remainingBudget));
            } else {
                warningMessage = "An toàn: Chi tiêu của bạn đang nằm trong giới hạn ngân sách.";
            }
        } else {
            warningMessage = "Hãy thiết lập ngân sách để nhận cảnh báo chi tiêu.";
        }

        return AISummaryResponse.WeeklyForecast.builder()
                .predictedSpending(Math.round(predictedRemaining))
                .inputWeeks(weeklySpending)
                .trend("stable")
                .changePercent(0)
                .remainingDays(remainingDays)
                .isOverBudget(isOverBudget)
                .warningMessage(warningMessage)
                .build();
    }
}
