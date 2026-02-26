package com.example.expensestracker.service;

import com.example.expensestracker.model.dto.response.CategoryIncomeReport;
import com.example.expensestracker.model.dto.response.CategoryExpenseReport;
import com.example.expensestracker.model.dto.response.MonthlyExpenseReportResponse;
import com.example.expensestracker.model.dto.response.MonthlyIncomeReportResponse;
import com.example.expensestracker.model.entity.CategoryLimitEntity;
import com.example.expensestracker.repositories.CategoryLimitRepository;
import com.example.expensestracker.repositories.CategoryRepository;
import com.example.expensestracker.repositories.TransactionRepository;
import com.example.expensestracker.service.InterfaceService.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService implements IReportService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryLimitRepository categoryLimitRepository;
    @Override
    public MonthlyExpenseReportResponse getMonthlyExpenseReport(Long userId, int month, int year) {
        // Tổng thu nhập
        BigDecimal totalIncome = transactionRepository.sumIncomeByUserAndMonthAndYear(userId, month, year);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        // Tổng chi tiêu
        BigDecimal totalExpense = transactionRepository.sumExpenseByUserAndMonthAndYear(userId, month, year);
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        // Tổng thu - chi
        BigDecimal netAmount = totalIncome.subtract(totalExpense);

        // Lấy danh sách giới hạn chi tiêu theo danh mục
        List<CategoryLimitEntity> categoryLimits = categoryLimitRepository.findByUserIdAndMonthAndYear(userId, month, year);

        // Tính tổng giới hạn chi tiêu
        BigDecimal totalSpendingLimit = categoryLimits.stream()
                .map(CategoryLimitEntity::getLimitExpense)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tính toán danh sách chi tiêu theo danh mục
        BigDecimal finalTotalIncome = totalIncome;
        BigDecimal finalTotalExpense = totalExpense;
        List<CategoryExpenseReport> categoryExpenseReports = categoryLimits.stream()
                .map(limit -> {
                    BigDecimal spentAmount = transactionRepository.sumSpentByCategoryAndUser(userId, limit.getCategory().getCategoryId(), month, year);
                    if (spentAmount == null) spentAmount = BigDecimal.ZERO;

                    BigDecimal limitExpense = limit.getLimitExpense();
                    BigDecimal percentLimit = totalSpendingLimit.compareTo(BigDecimal.ZERO) > 0
                            ? limitExpense.multiply(BigDecimal.valueOf(100))
                            .divide(totalSpendingLimit, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    BigDecimal percentSpent;

                    if ("Tiết kiệm".equalsIgnoreCase(limit.getCategory().getCategoryName())) {
                        // Xử lý danh mục "Tiết kiệm"
                        BigDecimal savings = finalTotalIncome.subtract(finalTotalExpense);
                        spentAmount = savings.max(BigDecimal.ZERO);
                        percentSpent = (savings.compareTo(BigDecimal.ZERO) > 0 && limitExpense.compareTo(BigDecimal.ZERO) > 0)
                                ? spentAmount.multiply(BigDecimal.valueOf(100))
                                .divide(limitExpense, 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;
                    } else {
                        // Xử lý các danh mục khác
                        percentSpent = limitExpense.compareTo(BigDecimal.ZERO) > 0
                                ? spentAmount.multiply(BigDecimal.valueOf(100))
                                .divide(limitExpense, 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;
                    }

                    return new CategoryExpenseReport(
                            limit.getCategory().getCategoryId(),
                            limit.getCategory().getCategoryName(),
                            spentAmount,
                            percentSpent,
                            percentLimit
                    );
                }).collect(Collectors.toList());

        return new MonthlyExpenseReportResponse(totalIncome, totalExpense, netAmount, categoryExpenseReports);
    }

    public MonthlyIncomeReportResponse getMonthlyIncomeReport(Long userId, int month, int year) {
        // Tổng thu nhập
        BigDecimal totalIncome = transactionRepository.sumIncomeByUserAndMonthAndYear(userId, month, year);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        // Tổng chi tiêu
        BigDecimal totalExpense = transactionRepository.sumExpenseByUserAndMonthAndYear(userId, month, year);
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        // Tổng thu - chi
        BigDecimal netAmount = totalIncome.subtract(totalExpense);

        // Lấy danh sách thu nhập theo danh mục
        List<Object[]> incomeByCategory = transactionRepository.sumIncomeByCategoryAndUserAndMonthAndYear(userId, month, year);

        // Tính toán danh sách thu nhập theo danh mục
        BigDecimal finalTotalIncome = totalIncome;
        List<CategoryIncomeReport> categoryIncomeReports = incomeByCategory.stream()
                .map(record -> {
                    Long categoryId = (Long) record[0];
                    String categoryName = (String) record[1];
                    BigDecimal categoryIncome = (BigDecimal) record[2];
                    if (categoryIncome == null) categoryIncome = BigDecimal.ZERO;

                    BigDecimal percentIncome = finalTotalIncome.compareTo(BigDecimal.ZERO) > 0
                            ? categoryIncome.multiply(BigDecimal.valueOf(100))
                            .divide(finalTotalIncome, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return new CategoryIncomeReport(
                            categoryId,
                            categoryName,
                            categoryIncome,
                            percentIncome
                    );
                }).collect(Collectors.toList());

        return new MonthlyIncomeReportResponse(totalIncome, totalExpense, netAmount, categoryIncomeReports);
    }

}
