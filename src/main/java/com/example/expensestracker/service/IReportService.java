package com.example.expensestracker.service;

import com.example.expensestracker.model.dto.response.MonthlyReportResponse;

public interface IReportService {
    MonthlyReportResponse getMonthlyReport(Long userId, int month, int year);
}
