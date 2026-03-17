package com.example.expensestracker.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Response từ FastAPI LightGBM prediction server.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryPredictResponse {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("predicted_spending")
    private double predictedSpending;

    @JsonProperty("current_spending")
    private double currentSpending;

    private String trend;

    @JsonProperty("change_percent")
    private double changePercent;

    // Các trường phân bổ ngân sách (AI đề xuất)
    @JsonProperty("budget_limit")
    private double budgetLimit;

    @JsonProperty("is_essential")
    private boolean essential;

    @JsonProperty("recommended_daily_allocation")
    private double recommendedDailyAllocation;

    @JsonProperty("reason")
    private String reason;
}
