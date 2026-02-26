package com.example.expensestracker.model.dto.response;


import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyExpenseReportResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netAmount;
    private List<CategoryExpenseReport> categoryExpenseReports;

}
