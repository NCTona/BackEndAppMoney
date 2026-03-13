package com.example.expensestracker.model.dto.response;

import com.example.expensestracker.model.entity.TransactionEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MLOpsTransactionDTO {
    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonProperty("note")
    private String note;

    public static MLOpsTransactionDTO fromEntity(TransactionEntity entity) {
        return MLOpsTransactionDTO.builder()
                .transactionId(entity.getTransactionId())
                .userId(entity.getUser().getUserId())
                .categoryId(entity.getCategory().getCategoryId())
                .amount(entity.getAmount())
                .date(entity.getTransactionDate())
                .note(entity.getNote())
                .build();
    }
}
