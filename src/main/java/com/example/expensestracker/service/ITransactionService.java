package com.example.expensestracker.service;

import com.example.expensestracker.model.dto.request.CategoryDTO;
import com.example.expensestracker.model.dto.request.TransactionDTO;
import com.example.expensestracker.model.dto.response.TransactionResponse;
import com.example.expensestracker.model.entity.TransactionEntity;

import java.util.List;

public interface ITransactionService {
    TransactionEntity createTransaction(TransactionDTO transaction,Long userId) throws Exception;
    TransactionEntity updateTransaction(Long transactionId,Long userId, TransactionDTO transactionDTO) throws Exception;
    void deleteTransaction(Long transactionId, Long userId) throws Exception;
    List<TransactionResponse> searchTransactions(Long userId, String categoryName, String note, Long amount);

}
