package com.example.expensestracker.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FixedTransactionDTO {
    @JsonProperty("fixed_transaction_id")
    private Long fixedTransactionId;
    @JsonProperty("category_id")
    private Long categoryId;
    @JsonProperty("title")
    private String title;
    @JsonProperty("amount")
    private BigDecimal amount;
    @JsonProperty("repeat_frequency")
    private String repeatFrequency;
    @JsonProperty("start_date")
    private LocalDate startDate;
    @JsonProperty("end_date")
    private LocalDate endDate;
}
