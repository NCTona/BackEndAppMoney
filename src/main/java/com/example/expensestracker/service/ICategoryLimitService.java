package com.example.expensestracker.service;

import com.example.expensestracker.model.dto.response.CategoryLimitResponse;

import java.util.List;

public interface ICategoryLimitService {
//    public List<CategoryLimitResponse> getOrCopyLatestLimits(Long userId);
//    public void saveLimits(Long userId, List<CategoryLimitResponse> limits, int month, int year);
//    public List<CategoryLimitResponse> calculateRemainingPercent(Long userId);
public void saveLimits(Long userId, List<CategoryLimitResponse> limits, int month, int year);
    public List<CategoryLimitResponse> calculateRemainingPercent(Long userId) throws Exception;
}
