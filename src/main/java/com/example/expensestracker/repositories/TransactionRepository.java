package com.example.expensestracker.repositories;

import com.example.expensestracker.model.entity.CategoryEntity;
import com.example.expensestracker.model.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long>, JpaSpecificationExecutor<TransactionEntity> {
    @Query("SELECT t FROM TransactionEntity t WHERE t.transactionId = ?1 AND t.user.userId = ?2")
    Optional<TransactionEntity> findByTransactionIdAndUserId(Long transactionId, Long userId);
    
    @Query("SELECT t FROM TransactionEntity t " +
            "WHERE t.user.userId = :userId " +
            "AND (" +
            "(:categoryName IS NOT NULL AND :categoryName <> '' AND LOWER(t.category.categoryName) LIKE LOWER(CONCAT('%', :categoryName, '%'))) " +
            "OR (:note IS NOT NULL AND :note <> '' AND LOWER(t.note) LIKE LOWER(CONCAT('%', :note, '%'))) " +
            "OR (:amount IS NOT NULL AND t.amount = :amount))")
    List<TransactionEntity> searchTransactions(
            @Param("userId") Long userId,
            @Param("categoryName") String categoryName,
            @Param("note") String note,
            @Param("amount") Long amount);

    @Query("SELECT t FROM TransactionEntity t WHERE t.user.userId = ?1 AND t.transactionDate BETWEEN  ?2 AND  ?3 ORDER BY t.transactionDate ASC")
    List<TransactionEntity> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t.transactionDate, " +
            "SUM(CASE WHEN t.category.type = 'income' THEN t.amount ELSE 0 END) AS totalIncome, " +
            "SUM(CASE WHEN t.category.type = 'expense' THEN t.amount ELSE 0 END) AS totalExpense " +
            "FROM TransactionEntity t " +
            "WHERE t.user.userId = :userId AND FUNCTION('MONTH', t.transactionDate) = :month AND FUNCTION('YEAR', t.transactionDate) = :year " +
            "GROUP BY t.transactionDate" +
            " ORDER BY t.transactionDate ASC ")
    List<Object[]> findDailyIncomeAndExpenseByType(@Param("userId") Long userId,
                                                           @Param("month") int month,
                                                           @Param("year") int year);
    @Query("SELECT COUNT(t) > 0 FROM TransactionEntity t WHERE t.user.userId = ?1 AND t.transactionDate = ?2 AND t.category.categoryId = ?3 AND t.fixedTransaction.fixedTransactionId = ?4")
    boolean existsByUserIdAndTransactionDateAndCategoryIdAAndFixedTransactionId(Long userId, LocalDate transactionDate, Long categoryId,Long fixedTransactionId);
    @Modifying
    @Query("DELETE FROM TransactionEntity t WHERE t.user.userId = :userId AND t.fixedTransaction.fixedTransactionId = :fixedTransactionId AND t.transactionDate BETWEEN :startDate AND :endDate")
    void deleteByUserIdAndFixedTransactionIdAndTransactionDateBetween(Long userId, Long fixedTransactionId, LocalDate startDate, LocalDate endDate);
    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.user.userId = ?1 AND t.category.categoryId = ?2 AND MONTH(t.transactionDate) = ?3 AND YEAR(t.transactionDate) = ?4")
    BigDecimal sumSpentByCategoryAndUser(Long userId, Long categoryId, int month, int year);
    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.user.userId = ?1 AND MONTH(t.transactionDate) = ?2 AND YEAR(t.transactionDate) = ?3 AND t.category.type = 'income'")
    BigDecimal sumSpentByIncomeAndUser(Long userId, int month, int year);
    //report
    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.user.userId = ?1 AND MONTH(t.transactionDate) = ?2 AND YEAR(t.transactionDate) = ?3 AND t.category.type = 'income'")
    BigDecimal sumIncomeByUserAndMonthAndYear(Long userId, int month, int year);

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.user.userId = ?1 AND MONTH(t.transactionDate) = ?2 AND YEAR(t.transactionDate) = ?3 AND t.category.type = 'expense'")
    BigDecimal sumExpenseByUserAndMonthAndYear(Long userId, int month, int year);

    @Query("SELECT c.categoryId, c.categoryName, COALESCE(SUM(t.amount), 0) " +
            "FROM CategoryEntity c " +
            "LEFT JOIN TransactionEntity t ON c.categoryId = t.category.categoryId " +
            "AND t.user.userId = :userId AND t.category.type = 'INCOME' " +
            "AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year " +
            "WHERE c.type = 'INCOME' " +
            "GROUP BY c.categoryId, c.categoryName")
    List<Object[]> sumIncomeByCategoryAndUserAndMonthAndYear(@Param("userId") Long userId,
                                                             @Param("month") int month,
                                                             @Param("year") int year);

}
