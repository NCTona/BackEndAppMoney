package com.example.expensestracker.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class FixedTransactionListResponse {
    private List<FixedTransactionResponse> fixedTransactionResponseList;
}
