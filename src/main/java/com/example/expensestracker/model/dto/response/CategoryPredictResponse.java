package com.example.expensestracker.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Response tu FastAPI LightGBM prediction server.
 * Du doan tong chi tieu cuoi thang va canh bao ngan sach.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryPredictResponse {

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("predicted_spending")
    private double predictedSpending;

    @JsonProperty("current_spent")
    private double currentSpent;

    private double budget;

    @JsonProperty("budget_used_pct")
    private double budgetUsedPct;

    @JsonProperty("forecast_usage_pct")
    private double forecastUsagePct;

    /** safe | warning | over_budget | no_budget */
    private String status;

    private String suggestion;

    @JsonProperty("suggested_daily")
    private double suggestedDaily;
}
