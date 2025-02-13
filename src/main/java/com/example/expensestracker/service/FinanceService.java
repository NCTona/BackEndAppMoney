package com.example.expensestracker.service;

import com.example.expensestracker.model.dto.response.DailyTransactionResponse;
import com.example.expensestracker.model.dto.response.MonthlyTransactionResponse;
import com.example.expensestracker.model.dto.response.TransactionResponse;
import com.example.expensestracker.model.entity.TransactionEntity;
import com.example.expensestracker.repositories.TransactionRepository;
import com.example.expensestracker.service.InterfaceService.IFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FinanceService implements IFinanceService {
    @Autowired
    private TransactionRepository transactionRepository;

    public MonthlyTransactionResponse getMonthlyData(Long userId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<TransactionEntity> transactions = transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        // Tính tổng thu nhập và chi tiêu
        for (TransactionEntity transaction : transactions) {
            if (transaction.getCategory() != null && transaction.getCategory().getType() != null) {
                switch (transaction.getCategory().getType()) {
                    case income -> totalIncome = totalIncome.add(transaction.getAmount());
                    case expense -> totalExpense = totalExpense.add(transaction.getAmount().abs());
                }
            }
        }

        // Lấy dữ liệu thu chi hàng ngày
        List<Object[]> results = transactionRepository.findDailyIncomeAndExpenseByType(userId, month, year);

        Map<LocalDate, DailyTransactionResponse> dailyTransactions = new HashMap<>();
        for (Object[] result : results) {
            if (result[0] instanceof LocalDate date) {
                BigDecimal totalDailyIncome = result[1] != null ? (BigDecimal) result[1] : BigDecimal.ZERO;
                BigDecimal totalDailyExpense = result[2] != null ? (BigDecimal) result[2] : BigDecimal.ZERO;

                dailyTransactions.put(date, new DailyTransactionResponse(totalDailyIncome, totalDailyExpense));
            }
        }

        // Tính số dư
        BigDecimal balance = totalIncome.subtract(totalExpense);

        // Chuyển đổi danh sách giao dịch
        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());

        // Kết quả cuối cùng
        return new MonthlyTransactionResponse(dailyTransactions, totalIncome, totalExpense, balance, transactionResponses);
    }
}