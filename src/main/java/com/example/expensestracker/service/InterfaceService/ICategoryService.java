package com.example.expensestracker.service.InterfaceService;

import com.example.expensestracker.model.dto.response.CategoryResponse;


import java.util.List;

public interface ICategoryService {
    List<CategoryResponse> getExpenseCategories();
    List<CategoryResponse> getIncomeCategories();
}
