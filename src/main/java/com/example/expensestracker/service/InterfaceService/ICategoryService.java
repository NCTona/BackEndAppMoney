package com.example.expensestracker.service.InterfaceService;

import com.example.expensestracker.model.dto.response.CategoryResponse;


import java.util.List;

public interface ICategoryService {
//    CategoryEntity createCategory(CategoryDTO category,Long userId);
//    CategoryEntity updateCategory(Long categoryId,Long userId, CategoryDTO categoryDTO);
//    void deleteCategory(Long categoryId,Long userId);
//
//    CategoryEntity getCategoryById(Long id);
//    List<CategoryEntity> getAllCategories();

    List<CategoryResponse> getExpenseCategories();
    List<CategoryResponse> getIncomeCategories();
}
