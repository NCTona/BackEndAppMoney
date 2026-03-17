package com.example.expensestracker.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Request body gửi đến FastAPI LightGBM prediction server.
 * Không dùng userId làm feature — thay bằng behavioral features.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryPredictRequest {

    @JsonProperty("category_id")
    private Long categoryId;

    private int month;
    private int year;

    @JsonProperty("monthly_spending")
    private double monthlySpending;

    @JsonProperty("transaction_count")
    private int transactionCount;

    @JsonProperty("avg_transaction")
    private double avgTransaction;

    @JsonProperty("max_transaction")
    private double maxTransaction;

    @JsonProperty("avg_day_of_week")
    private double avgDayOfWeek;

    @JsonProperty("avg_day_of_month")
    private double avgDayOfMonth;

    @JsonProperty("total_all_categories")
    private double totalAllCategories;

    @JsonProperty("category_ratio")
    private double categoryRatio;

    @JsonProperty("prev_month_spending")
    private double prevMonthSpending;

    @JsonProperty("prev_month_count")
    private int prevMonthCount;

    @JsonProperty("prev_month_ratio")
    private double prevMonthRatio;

    @JsonProperty("avg_monthly_spending_3m")
    private double avgMonthlySpending3m;

    @JsonProperty("spending_trend")
    private double spendingTrend;
}
