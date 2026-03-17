package com.example.expensestracker.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Response tổng hợp kết quả phân tích AI:
 * - Weekly Analysis: dữ liệu đầu vào cho TFLite (Android)
 * - LightGBM: dự đoán theo danh mục
 * - Isolation Forest: phát hiện bất thường
 * - Pattern: cảnh báo ngày chi cao
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AISummaryResponse {

    // === Weekly Data for TFLite ===
    @JsonProperty("weekly_forecast")
    private WeeklyForecast weeklyForecast;

    // === LightGBM Category Forecasts ===
    @JsonProperty("category_forecasts")
    private List<CategoryPredictResponse> categoryForecasts;

    // === Isolation Forest Anomalies ===
    @JsonProperty("anomaly_count")
    private int anomalyCount;

    @JsonProperty("top_anomalies")
    private List<AnomalyResponse> topAnomalies;

    // === Spending Pattern Alerts ===
    @JsonProperty("upcoming_alerts")
    private List<SpendingPatternResponse> upcomingAlerts;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WeeklyForecast {
        @JsonProperty("predicted_spending")
        private double predictedSpending; // Dự đoán chi tiêu cho TẤT CẢ số ngày còn lại

        @JsonProperty("input_weeks")
        private List<Double> inputWeeks;

        private String trend;

        @JsonProperty("change_percent")
        private double changePercent;

        @JsonProperty("remaining_days")
        private int remainingDays;

        @JsonProperty("is_over_budget")
        private boolean isOverBudget;

        @JsonProperty("warning_message")
        private String warningMessage;
    }
}
