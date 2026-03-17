package com.example.expensestracker.controller;

import com.example.expensestracker.model.dto.response.AISummaryResponse;
import com.example.expensestracker.model.dto.response.AnomalyResponse;
import com.example.expensestracker.model.dto.response.ApiResponse;
import com.example.expensestracker.model.dto.response.CategoryPredictResponse;
import com.example.expensestracker.model.dto.response.SpendingPatternResponse;
import com.example.expensestracker.model.dto.response.TrendAnalysisResponse;
import com.example.expensestracker.model.entity.UserEntity;
import com.example.expensestracker.service.AnomalyDetectionService;
import com.example.expensestracker.service.CategoryForecastService;
import com.example.expensestracker.service.SpendingPatternService;
import com.example.expensestracker.service.WeeklyForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller phục vụ API dự đoán chi tiêu và phân tích AI.
 * Bao gồm: dự đoán danh mục (LightGBM), dự đoán tuần (LSTM),
 * phát hiện bất thường (Isolation Forest), cảnh báo pattern chi tiêu (thống
 * kê).
 */
@RestController
@RequestMapping("api/forecast")
@RequiredArgsConstructor
public class CategoryForecastController {

    private final CategoryForecastService categoryForecastService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final SpendingPatternService spendingPatternService;
    private final WeeklyForecastService weeklyForecastService;

    /**
     * GET /api/forecast/summary
     * Tổng hợp kết quả từ TẤT CẢ 3 model AI + thống kê trong 1 response duy nhất.
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getAISummary(@AuthenticationPrincipal UserEntity user) {
        try {
            Long userId = user.getUserId();

            // Gọi 4 service
            AISummaryResponse.WeeklyForecast weeklyForecast = weeklyForecastService.predictNextWeek(userId);
            List<CategoryPredictResponse> categoryForecasts = categoryForecastService.predictAllCategories(userId,
                    false); // Không normalize để đồng bộ với Xu hướng chi tiêu
            List<AnomalyResponse> anomalies = anomalyDetectionService.checkRecentTransactions(userId);
            List<SpendingPatternResponse> alerts = spendingPatternService.getUpcomingAlerts(userId);

            // Giới hạn top 3 anomalies để response gọn
            List<AnomalyResponse> topAnomalies = anomalies.size() > 3
                    ? anomalies.subList(0, 3)
                    : anomalies;

            AISummaryResponse summary = AISummaryResponse.builder()
                    .weeklyForecast(weeklyForecast)
                    .categoryForecasts(categoryForecasts)
                    .anomalyCount(anomalies.size())
                    .topAnomalies(topAnomalies)
                    .upcomingAlerts(alerts)
                    .build();

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("error", "Lỗi tổng hợp AI: " + e.getMessage()));
        }
    }

    /**
     * GET /api/forecast/categories
     * Dự đoán chi tiêu tháng tiếp theo cho TẤT CẢ danh mục chi tiêu của user.
     */
    @GetMapping("/categories")
    public ResponseEntity<?> predictAllCategories(
            @AuthenticationPrincipal UserEntity user,
            @RequestParam(name = "normalized", defaultValue = "false") boolean normalized) {
        try {
            List<CategoryPredictResponse> predictions = categoryForecastService.predictAllCategories(user.getUserId(),
                    normalized);
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("error", "Lỗi dự đoán: " + e.getMessage()));
        }
    }

    /**
     * GET /api/forecast/trend/{categoryId}
     * Phân tích xu hướng chi tiêu của user so với số đông cho 1 danh mục cụ thể.
     */
    @GetMapping("/trend/{categoryId}")
    public ResponseEntity<?> analyzeTrend(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long categoryId) {
        try {
            TrendAnalysisResponse result = categoryForecastService.analyzeTrend(user.getUserId(), categoryId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("error", "Lỗi phân tích: " + e.getMessage()));
        }
    }

    /**
     * GET /api/forecast/anomalies
     * Phát hiện giao dịch bất thường gần đây bằng Isolation Forest.
     */
    @GetMapping("/anomalies")
    public ResponseEntity<?> checkAnomalies(@AuthenticationPrincipal UserEntity user) {
        try {
            List<AnomalyResponse> anomalies = anomalyDetectionService.checkRecentTransactions(user.getUserId());
            return ResponseEntity.ok(anomalies);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("error", "Lỗi kiểm tra bất thường: " + e.getMessage()));
        }
    }

    /**
     * GET /api/forecast/alerts
     * Cảnh báo ngày chi tiêu cao sắp tới (dựa trên phân tích thống kê pattern lịch
     * sử).
     */
    @GetMapping("/alerts")
    public ResponseEntity<?> getSpendingAlerts(@AuthenticationPrincipal UserEntity user) {
        try {
            List<SpendingPatternResponse> alerts = spendingPatternService.getUpcomingAlerts(user.getUserId());
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("error", "Lỗi phân tích pattern: " + e.getMessage()));
        }
    }
}
