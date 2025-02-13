package com.example.expensestracker.repositories;

import com.example.expensestracker.model.entity.CategoryEntity;
import com.example.expensestracker.model.enums.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    @Query(value = "SELECT COUNT(c) > 0 FROM CategoryEntity c WHERE c.categoryName = ?1")
    boolean existsByCategoryNameAndUserId(String categoryName);

    // Tìm tất cả các category theo categoryName và isDefault = true
    @Query(value = "SELECT COUNT(c) > 0 FROM CategoryEntity c WHERE c.categoryName = ?1 AND c.type = ?2")
    boolean existsByCategoryNameAndType(String categoryName, Type type);

//    @Query("SELECT COUNT(c) > 0 FROM CategoryEntity c WHERE c.user.userId = ?1")
//    boolean existsByUserId(Long userId);

//    @Query("SELECT c FROM CategoryEntity c WHERE c.user.userId = :userId AND c.isDefault = false")
//    List<CategoryEntity> findUserCategories(@Param("userId") Long userId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.categoryId = ?1")
    Optional<CategoryEntity> findByCategoryIdAndUserId(Long categoryId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.type = :type ")
    List<CategoryEntity> findByType(@Param("type") Type type);
    @Query("SELECT c FROM CategoryEntity c WHERE c.type = 'expense' OR c.categoryName = 'Tiết kiệm' ")
    List<CategoryEntity> findByExpense();
}
