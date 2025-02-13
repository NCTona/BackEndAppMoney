package com.example.expensestracker.model.entity;

import com.example.expensestracker.model.enums.Type;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="categories")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;
    @Column(name="category_name",length = 50,nullable = false)
    private String categoryName;
    @Column(name="type",nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="user_id")
//    private UserEntity user;
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FixedTransactionEntity> fixedTransactionEntities ;
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryLimitEntity> spendingLimitEntities ;
}
