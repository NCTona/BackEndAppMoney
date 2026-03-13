package com.example.expensestracker.controller;


import com.example.expensestracker.model.dto.response.CategoryListResponse;
import com.example.expensestracker.model.dto.response.CategoryResponse;
import com.example.expensestracker.service.CategoryService;
import com.example.expensestracker.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/expense")
    public ResponseEntity<CategoryListResponse> getExpenseCategories() {
        List<CategoryResponse> categories = categoryService.getExpenseCategories();
        return ResponseEntity.ok(CategoryListResponse
                .builder()
                .categoryList(categories)
                .build());
    }
    @GetMapping("/income")
    public ResponseEntity<CategoryListResponse> getIncomeCategories() {
        List<CategoryResponse> categories = categoryService.getIncomeCategories();
        return ResponseEntity.ok(CategoryListResponse
                .builder()
                .categoryList(categories)
                .build());
    }
}
