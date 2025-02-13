package com.example.expensestracker.repositories;

import com.example.expensestracker.model.entity.CategoryLimitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CategoryLimitRepository extends JpaRepository<CategoryLimitEntity, Long> {
    @Query("SELECT l FROM CategoryLimitEntity  l WHERE l.user.userId = ?1 AND l.month = ?2 AND l.year = ?3")
    List<CategoryLimitEntity> findByUserIdAndMonthAndYear(Long userId, int month, int year); // Lấy giới hạn cho người dùng trong một tháng, năm
    @Query("SELECT COUNT(l) > 0 FROM CategoryLimitEntity l WHERE l.user.userId = ?1 AND l.month = ?2 AND l.year = ?3")
    boolean existsByUserIdAndMonthAndYear(Long userId, int month, int year);
    @Query("SELECT l FROM CategoryLimitEntity  l WHERE l.user.userId = ?1 AND l.category.categoryId = ?2 AND l.month = ?3 AND l.year = ?4")
    CategoryLimitEntity findByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, int month, int year);

}
