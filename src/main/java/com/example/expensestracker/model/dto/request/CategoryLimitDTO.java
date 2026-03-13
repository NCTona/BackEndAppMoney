package com.example.expensestracker.model.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryLimitDTO {
    private Long spendingLimitId;
    private Long categoryId;
    private int month;
    private int year;
    private BigDecimal limitExpense;
}
