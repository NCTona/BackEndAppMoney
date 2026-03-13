package com.example.expensestracker.controller;

import com.example.expensestracker.model.dto.response.MonthlyTransactionResponse;
import com.example.expensestracker.service.InterfaceService.IFinanceService;
import com.example.expensestracker.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/finance")
public class FinanceController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private IFinanceService financeService;

    @GetMapping("")
    public MonthlyTransactionResponse getMonthlyData(@RequestParam int month, @RequestParam int year,@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        // Lấy userId từ SecurityContextHolder (nếu bạn dùng JWT để xác thực người dùng)
        // Lấy token từ header Authorization
        String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
        // Trích xuất userId từ token
        Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));

        return financeService.getMonthlyData(userId, month, year);
    }
}
