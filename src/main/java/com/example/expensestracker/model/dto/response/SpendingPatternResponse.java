package com.example.expensestracker.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Response cảnh báo ngày chi tiêu cao sắp tới.
 * Dựa trên phân tích thống kê pattern chi tiêu lịch sử.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpendingPatternResponse {

    @JsonProperty("alert_date")
    private String alertDate;

    @JsonProperty("day_of_month")
    private int dayOfMonth;

    @JsonProperty("expected_spending")
    private double expectedSpending;

    @JsonProperty("times_higher")
    private double timesHigher;

    @JsonProperty("category_name")
    private String categoryName;

    private String message;

    private String suggestion;
}
