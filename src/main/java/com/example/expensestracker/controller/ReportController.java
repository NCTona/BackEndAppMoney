package com.example.expensestracker.controller;

import com.example.expensestracker.model.dto.response.MonthlyExpenseReportResponse;
import com.example.expensestracker.model.dto.response.MonthlyIncomeReportResponse;
import com.example.expensestracker.service.ReportService;
import com.example.expensestracker.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/report")
public class ReportController {
    @Autowired
    private ReportService reportService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/monthly_expense")
    public ResponseEntity<MonthlyExpenseReportResponse> getMonthlyExpenseReport(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam int month,
            @RequestParam int year
    ) {
        String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
        Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));

        MonthlyExpenseReportResponse report = reportService.getMonthlyExpenseReport(userId, month, year);
        return ResponseEntity.ok(report);
    }
    @GetMapping("/monthly_income")
    public ResponseEntity<MonthlyIncomeReportResponse> getMonthlyIncomeReport(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam int month,
            @RequestParam int year
    ) {
        String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
        Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));

        MonthlyIncomeReportResponse report = reportService.getMonthlyIncomeReport(userId, month, year);
        return ResponseEntity.ok(report);
    }
}
