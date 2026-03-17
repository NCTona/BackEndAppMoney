package com.example.expensestracker.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Response phân tích xu hướng chi tiêu so với số đông.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrendAnalysisResponse {
    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("population_average")
    private double populationAverage;

    @JsonProperty("user_spending")
    private double userSpending;

    @JsonProperty("deviation_percent")
    private double deviationPercent;

    private String status;

    private String message;
}
