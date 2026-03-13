package com.example.expensestracker.model.dto.response;

import com.example.expensestracker.model.entity.CategoryEntity;
import com.example.expensestracker.model.entity.FixedTransactionEntity;
import com.example.expensestracker.model.entity.TransactionEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    @JsonProperty("transaction_id")
    private Long transactionId;
    private String categoryName;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String note;
    private String type;

    public static TransactionResponse fromEntity(TransactionEntity transactionEntity) {
        TransactionResponse transactionResponse = TransactionResponse.builder()
                .transactionId(transactionEntity.getTransactionId())
                .categoryName(transactionEntity.getCategory().getCategoryName())
                .amount(transactionEntity.getAmount())
                .transactionDate(transactionEntity.getTransactionDate())
                .note(transactionEntity.getNote())
                .type(String.valueOf(transactionEntity.getCategory().getType()))
                .build();
        return transactionResponse;
    }
    public static TransactionResponse fromFixedTransaction(FixedTransactionEntity entity, LocalDate date) {
        TransactionResponse response = new TransactionResponse();
        response.transactionId = null; // Giao dịch cố định không có ID trong bảng transaction
        response.amount = entity.getAmount();
        response.transactionDate = date;
        response.note = entity.getTitle();
        response.categoryName = entity.getCategory().getCategoryName();
        return response;
    }
}
