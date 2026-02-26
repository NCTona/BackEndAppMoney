package com.example.expensestracker.service.InterfaceService;

import com.example.expensestracker.model.dto.response.MonthlyExpenseReportResponse;

public interface IReportService {
    public MonthlyExpenseReportResponse getMonthlyExpenseReport(Long userId, int month, int year);
}
