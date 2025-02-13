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
//    private List<TransactionResponse> generateFixedTransactionResponses(FixedTransactionEntity fixedTransaction, LocalDate startDate, LocalDate endDate) {
//        List<TransactionResponse> responses = new ArrayList<>();
//
//        // Lặp qua các ngày từ startDate đến endDate theo tần suất của giao dịch cố định
//        LocalDate current = fixedTransaction.getStartDate().isAfter(startDate) ? fixedTransaction.getStartDate() : startDate;
//        if (fixedTransaction.getEndDate() != null && fixedTransaction.getEndDate().isBefore(endDate)) {
//            endDate = fixedTransaction.getEndDate();
//        }
//
//        while (!current.isAfter(endDate)) {
//            // Tạo TransactionResponse cho ngày hiện tại
//            TransactionResponse response = TransactionResponse.builder()
//                    .transactionId(null) // Giao dịch cố định, không có transactionId
//                    .categoryName(fixedTransaction.getCategory().getCategoryName())
//                    .amount(fixedTransaction.getAmount())
//                    .transactionDate(current)
//                    .note(fixedTransaction.getTitle()) // Giao dịch cố định không có ghi chú
//                    .build();
//
//            responses.add(response);
//
//            // Tiến tới ngày tiếp theo dựa trên tần suất lặp
//            switch (fixedTransaction.getRepeatFrequency()) {
//                case daily:
//                    current = current.plusDays(1);
//                    break;
//                case weekly:
//                    current = current.plusWeeks(1);
//                    break;
//                case monthly:
//                    current = current.plusMonths(1);
//                    break;
//                case yearly:
//                    current = current.plusYears(1);
//                    break;
//            }
//        }
//
//        return responses;
//    }
}
