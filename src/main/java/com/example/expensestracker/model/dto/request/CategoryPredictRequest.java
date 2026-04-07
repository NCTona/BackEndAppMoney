package com.example.expensestracker.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Request body gui den FastAPI LightGBM prediction server.
 * Du doan tong chi tieu cuoi thang hien tai dua tren chi tieu dang dien ra.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryPredictRequest {

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("days_passed")
    private int daysPassed;

    @JsonProperty("days_remaining")
    private int daysRemaining;

    @JsonProperty("current_spent")
    private double currentSpent;

    @JsonProperty("current_tx_count")
    private int currentTxCount;

    @JsonProperty("daily_rate")
    private double dailyRate;

    @JsonProperty("category_ratio")
    private double categoryRatio;

    /** Ngan sach phan bo cho category (chi dung post-processing, khong la feature). */
    private double budget;
}
