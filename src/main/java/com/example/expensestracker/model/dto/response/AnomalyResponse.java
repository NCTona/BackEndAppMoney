package com.example.expensestracker.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Response phát hiện giao dịch bất thường từ Isolation Forest.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnomalyResponse {

    @JsonProperty("transaction_id")
    private Long transactionId;

    private double amount;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("is_anomaly")
    private boolean isAnomaly;

    @JsonProperty("anomaly_score")
    private double anomalyScore;

    private String message;
}
