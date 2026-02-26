package com.example.expensestracker.service;

import com.example.expensestracker.model.dto.response.CategoryResponse;
import com.example.expensestracker.model.entity.CategoryEntity;
import com.example.expensestracker.model.enums.Type;
import com.example.expensestracker.repositories.CategoryRepository;
import com.example.expensestracker.repositories.UserRepository;
import com.example.expensestracker.service.InterfaceService.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<CategoryResponse> getExpenseCategories() {
        List<CategoryEntity> categories = categoryRepository.findByType(Type.expense);
        return categories.stream()
                .map(CategoryResponse::fromCategory)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getIncomeCategories() {
        List<CategoryEntity> categories = categoryRepository.findByType(Type.income);
        return categories.stream()
                .map(CategoryResponse::fromCategory)
                .collect(Collectors.toList());
    }
}
