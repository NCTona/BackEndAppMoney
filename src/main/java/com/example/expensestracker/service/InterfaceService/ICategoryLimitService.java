package com.example.expensestracker.service.InterfaceService;

import com.example.expensestracker.model.dto.response.CategoryLimitResponse;

import java.util.List;

public interface ICategoryLimitService {
public void saveLimits(Long userId, List<CategoryLimitResponse> limits, int month, int year);
    public List<CategoryLimitResponse> calculateRemainingPercent(Long userId) throws Exception;
}
