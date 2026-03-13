package com.example.expensestracker.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    @ManyToOne
    @JoinColumn(name="user_id",nullable = false)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id",nullable = false)
    private CategoryEntity category;
    @ManyToOne
    @JoinColumn(name = "fixed_transaction_id" )
    private FixedTransactionEntity fixedTransaction;
    @Column(name="amount")
    private BigDecimal amount;
    @Column(name="transaction_date")
    private LocalDate transactionDate;
    @Column(name="note")
    private String note;

}
