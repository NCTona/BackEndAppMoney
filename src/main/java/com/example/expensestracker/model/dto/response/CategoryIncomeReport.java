package com.example.expensestracker.model.dto.response;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class CategoryIncomeReport {
    private Long categoryId;
    private String categoryName;
    private BigDecimal categoryIncome;
    private BigDecimal percentIncome;
}
