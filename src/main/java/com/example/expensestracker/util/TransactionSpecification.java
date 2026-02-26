package com.example.expensestracker.util;

import com.example.expensestracker.model.entity.TransactionEntity;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {
    public static Specification<TransactionEntity> filterTransactions(
            Long userId,  String categoryName, String note, Long amount) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Thêm điều kiện cho userId
            predicates.add(criteriaBuilder.equal(root.get("user").get("userId"), userId));

            if (categoryName != null && !categoryName.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("category").get("categoryName"), "%" + categoryName + "%"));
            }
            if (note != null && !note.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("note"), "%" + note + "%"));
            }
            if (amount != null) {
                predicates.add(criteriaBuilder.equal(root.get("amount"), amount));
            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
