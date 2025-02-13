package com.example.expensestracker.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    @JsonProperty("transaction_id")
    private Long transactionId;
    @JsonProperty("category_id")
    private Long categoryId;
    @JsonProperty("amount")
    private BigDecimal amount;
    @JsonProperty("transaction_date")
    private LocalDate transactionDate;
    @JsonProperty("note")
    private String note;
    @JsonProperty("is_auto_import")
    private boolean isAutoImport;
}
