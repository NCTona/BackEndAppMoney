package com.example.expensestracker.service;

import com.example.expensestracker.exception.DataNotFoundException;
import com.example.expensestracker.model.dto.response.CategoryLimitResponse;
import com.example.expensestracker.model.entity.CategoryEntity;
import com.example.expensestracker.model.entity.CategoryLimitEntity;
import com.example.expensestracker.model.entity.UserEntity;
import com.example.expensestracker.repositories.CategoryLimitRepository;
import com.example.expensestracker.repositories.CategoryRepository;
import com.example.expensestracker.repositories.TransactionRepository;
import com.example.expensestracker.repositories.UserRepository;
import com.example.expensestracker.service.InterfaceService.ICategoryLimitService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryLimitService implements ICategoryLimitService {
    @Autowired
    private CategoryLimitRepository categoryLimitRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    public void autoCopyLimitsToNewMonth(Long userId) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        // Kiểm tra nếu tháng hiện tại đã có dữ liệu giới hạn
        boolean currentMonthExists = categoryLimitRepository.existsByUserIdAndMonthAndYear(userId, currentMonth, currentYear);

        if (!currentMonthExists) {
            // Lấy giới hạn của tháng trước
            int previousMonth = (currentMonth == 1) ? 12 : currentMonth - 1;
            int previousYear = (currentMonth == 1) ? currentYear - 1 : currentYear;

            List<CategoryLimitEntity> previousLimits = categoryLimitRepository.findByUserIdAndMonthAndYear(userId, previousMonth, previousYear)
                    .stream()
                    .filter(limit -> "expense".equalsIgnoreCase(String.valueOf(limit.getCategory().getType()))
                            || "Tiết kiệm".equalsIgnoreCase(limit.getCategory().getCategoryName())) // Thêm điều kiện lọc tên danh mục
                    .collect(Collectors.toList());
            // Nếu không có giới hạn tháng trước, tạo giá trị mặc định 0%
            if (previousLimits.isEmpty()) {
                List<CategoryLimitEntity> defaultLimits = categoryRepository.findAll().stream()
                        .filter(category -> "expense".equalsIgnoreCase(String.valueOf(category.getType()))
                                || "Tiết kiệm".equalsIgnoreCase(category.getCategoryName())) // Thêm điều kiện lọc tên danh mục
                        .map(category -> {
                            CategoryLimitEntity defaultLimit = new CategoryLimitEntity();
                            defaultLimit.setUser(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
                            defaultLimit.setCategory(category);
                            defaultLimit.setMonth(currentMonth);
                            defaultLimit.setYear(currentYear);
                            defaultLimit.setLimitExpense(BigDecimal.ZERO);
                            return defaultLimit;
                        }).collect(Collectors.toList());
                categoryLimitRepository.saveAll(defaultLimits);
            } else {
                // Sao chép giới hạn từ tháng trước
                List<CategoryLimitEntity> newLimits = previousLimits.stream()
                        .map(limit -> {
                            CategoryLimitEntity newLimit = new CategoryLimitEntity();
                            newLimit.setUser(limit.getUser());
                            newLimit.setCategory(limit.getCategory());
                            newLimit.setMonth(currentMonth);
                            newLimit.setYear(currentYear);
                            newLimit.setLimitExpense(limit.getLimitExpense());
                            return newLimit;
                        }).collect(Collectors.toList());
                categoryLimitRepository.saveAll(newLimits);
            }
        }
    }
    @Scheduled(cron = "0 0 0 1 * ?")  // Chạy vào 00:00 ngày 1 mỗi tháng
    public void scheduleAutoCopyLimits() {
        List<Long> allUserIds = userRepository.findAllUserIds();  // Lấy danh sách tất cả userId
        allUserIds.forEach(userId -> autoCopyLimitsToNewMonth(userId));
    }

    // Gọi tự động sao chép giới hạn khi ứng dụng bắt đầu
    @PostConstruct
    public void init() {
        List<Long> allUserIds = userRepository.findAllUserIds();  // Lấy danh sách tất cả userId
        allUserIds.forEach(userId -> autoCopyLimitsToNewMonth(userId));
    }

    @Override
    // Lưu giới hạn chi tiêu cho tháng hiện tại
    public void saveLimits(Long userId, List<CategoryLimitResponse> limits, int month, int year) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        List<CategoryLimitEntity> entities = limits.stream()
                .filter(limit -> {
                    CategoryEntity category = categoryRepository
                            .findById(limit.getCategoryId())
                            .orElseThrow(() -> new DataNotFoundException("Cannot find category"));

                    // Chỉ xử lý danh mục loại "expense"
                    return "expense".equalsIgnoreCase(String.valueOf(category.getType()))
                            || "Tiết kiệm".equalsIgnoreCase(category.getCategoryName());
                })
                .map(limit -> {
                    CategoryEntity existingCategory = categoryRepository
                            .findById(limit.getCategoryId())
                            .orElseThrow(() -> new DataNotFoundException("Cannot find category"));

                    // Kiểm tra xem giới hạn đã tồn tại hay chưa
                    CategoryLimitEntity existingLimit = categoryLimitRepository
                            .findByUserIdAndCategoryIdAndMonthAndYear(user.getUserId(), existingCategory.getCategoryId(), month, year);

                    if (existingLimit != null) {
                        // Nếu đã tồn tại, cập nhật giới hạn
                        existingLimit.setLimitExpense(limit.getLimitExpense());
                        return existingLimit;
                    } else {
                        // Nếu chưa có, tạo mới
                        CategoryLimitEntity newLimit = new CategoryLimitEntity();
                        newLimit.setUser(user);
                        newLimit.setCategory(existingCategory);
                        newLimit.setMonth(month);
                        newLimit.setYear(year);
                        newLimit.setLimitExpense(limit.getLimitExpense());
                        return newLimit;
                    }
                }).collect(Collectors.toList());

        // Lưu tất cả các thay đổi (cả mới và cập nhật)
        categoryLimitRepository.saveAll(entities);
    }

public List<CategoryLimitResponse> calculateRemainingPercent(Long userId) throws Exception {
    // Lấy tất cả giới hạn chi tiêu của người dùng cho tháng và năm hiện tại
    int currentMonth = LocalDate.now().getMonthValue();
    int currentYear = LocalDate.now().getYear();

    List<CategoryLimitEntity> categoryLimits = categoryLimitRepository
            .findByUserIdAndMonthAndYear(userId, currentMonth, currentYear);
    if (categoryLimits.isEmpty()) {
        throw new DataNotFoundException("No category limits found for the current month.");
    }
    // Tính toán phần trăm còn lại
    return categoryLimits.stream()
            .map(limit -> {
                // Tính tổng chi tiêu cho danh mục hiện tại
                BigDecimal totalSpent = transactionRepository
                        .sumSpentByCategoryAndUser(userId, limit.getCategory().getCategoryId(), currentMonth, currentYear);

                // Đảm bảo giá trị chi tiêu không null
                totalSpent = (totalSpent != null) ? totalSpent : BigDecimal.ZERO;

                // Lấy giới hạn chi tiêu
                BigDecimal limitExpense = limit.getLimitExpense();

                // Nếu giới hạn là 0%, không cần tính toán
                if (limitExpense.compareTo(BigDecimal.ZERO) == 0) {
                    return new CategoryLimitResponse(
                            limit.getCategory().getCategoryId(),
                            limitExpense,
                            BigDecimal.ZERO
                    );
                }

                // Tính toán phần trăm chi tiêu đã sử dụng so với giới hạn
                BigDecimal spentPercent = totalSpent.multiply(BigDecimal.valueOf(100))
                        .divide(limitExpense, 2, RoundingMode.HALF_UP);

                BigDecimal remainingPercent = BigDecimal.valueOf(100).subtract(spentPercent);

                // Nếu phần trăm còn lại âm, gán giá trị 0
                if (remainingPercent.compareTo(BigDecimal.ZERO) < 0) {
                    remainingPercent = BigDecimal.ZERO;
                }

                // Trả về thông tin
                return new CategoryLimitResponse(
                        limit.getCategory().getCategoryId(),
                        limitExpense,
                        remainingPercent
                );
            })
            .collect(Collectors.toList());
}

}
