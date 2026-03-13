package com.example.expensestracker.repositories;

import com.example.expensestracker.model.entity.CategoryEntity;
import com.example.expensestracker.model.entity.FixedTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FixedTransactionRepository extends JpaRepository<FixedTransactionEntity, Long> {
    @Query("SELECT f FROM FixedTransactionEntity  f WHERE f.fixedTransactionId = ?1 AND f.user.userId = ?2")
    Optional<FixedTransactionEntity> findByFixedTransactionIdAndUserId(Long fixedTransactionId, Long userId);
    @Query("SELECT f FROM FixedTransactionEntity f WHERE f.user.userId = ?1")
    List<FixedTransactionEntity> findByUserId(Long userId);
    // Truy vấn các giao dịch cố định của người dùng trong khoảng thời gian
    @Query("SELECT f FROM FixedTransactionEntity f WHERE f.user.userId = ?1 AND f.startDate <= ?2 AND f.endDate >= ?3")
    List<FixedTransactionEntity> findByUserIdAndStartDateBeforeAndEndDateAfter(
            Long userId,
            LocalDate startDate,
            LocalDate endDate);

}
