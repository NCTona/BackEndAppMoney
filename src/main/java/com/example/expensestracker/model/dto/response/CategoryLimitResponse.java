package com.example.expensestracker.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class CategoryLimitResponse {
    private Long categoryId;
    private BigDecimal limitExpense;
    private BigDecimal remainingPercent; // Phần trăm còn lại
}
