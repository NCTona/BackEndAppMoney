package com.example.expensestracker.controller;

import com.example.expensestracker.model.dto.response.ApiResponse;
import com.example.expensestracker.model.dto.response.CategoryLimitResponse;
import com.example.expensestracker.model.entity.CategoryLimitEntity;
import com.example.expensestracker.repositories.CategoryLimitRepository;
import com.example.expensestracker.repositories.CategoryRepository;
import com.example.expensestracker.service.InterfaceService.ICategoryLimitService;
import com.example.expensestracker.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/category-limits")
public class CategoryLimitController {
    @Autowired
    private ICategoryLimitService categoryLimitService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private CategoryLimitRepository categoryLimitRepository;
    @Autowired
    private CategoryRepository categoryRepository;


    @PutMapping("/save")
    public ResponseEntity<ApiResponse> updateCategoryLimits(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody List<CategoryLimitResponse> limits) {
        String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
        // Trích xuất userId từ token
        Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        categoryLimitService.saveLimits(userId, limits, currentMonth, currentYear);
        return ResponseEntity.ok(new ApiResponse("success", "Cập nhật giới hạn chi tiêu thành công"));
    }

    // Tính toán phần trăm còn lại của giới hạn chi tiêu
//    @GetMapping("/remaining")
//    public List<CategoryLimitResponse> getRemainingPercent(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
//        String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
//        // Trích xuất userId từ token
//        Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
//        return categoryLimitService.calculateRemainingPercent(userId);
//    }
    @GetMapping("/remaining")
    public ResponseEntity<?> getRemainingPercent(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
            // Trích xuất userId từ token
            Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
            // Lấy phần trăm còn lại của giới hạn chi tiêu theo danh mục
            List<CategoryLimitResponse> remainingPercent = categoryLimitService.calculateRemainingPercent(userId);

            return ResponseEntity.ok(remainingPercent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }

    }
    @GetMapping("/current")
    public ResponseEntity<List<CategoryLimitResponse>> getCurrentMonthLimits(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
        // Trích xuất userId từ token
        Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
        // Lấy giới hạn tháng hiện tại
        List<CategoryLimitEntity> currentLimits = categoryLimitRepository.findByUserIdAndMonthAndYear(userId, currentMonth, currentYear);

        // Nếu không có, tạo giá trị mặc định
        if (currentLimits.isEmpty()) {
            List<CategoryLimitResponse> defaultLimits = categoryRepository.findByExpense().stream()
                    .map(category -> new CategoryLimitResponse(category.getCategoryId(), BigDecimal.ZERO, BigDecimal.ZERO))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(defaultLimits);
        }

        // Nếu có, trả về danh sách giới hạn
        List<CategoryLimitResponse> response = currentLimits.stream()
                .map(limit -> new CategoryLimitResponse(limit.getCategory().getCategoryId(), limit.getLimitExpense(), limit.getLimitExpense()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
