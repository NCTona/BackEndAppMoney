package com.example.expensestracker.model.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyTransactionResponse {
    private BigDecimal totalDailyIncome;
    private BigDecimal totalDailyExpense;
}
