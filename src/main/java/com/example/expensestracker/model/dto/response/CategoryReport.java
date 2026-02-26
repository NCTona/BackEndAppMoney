package com.example.expensestracker.model.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryReport {
    private Long categoryId;
    private String categoryName;
    private BigDecimal spentAmount;
    private BigDecimal percentSpent;
    private BigDecimal percentLimit;
}
