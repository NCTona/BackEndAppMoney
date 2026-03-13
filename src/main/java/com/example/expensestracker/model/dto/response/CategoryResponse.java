package com.example.expensestracker.model.dto.response;


import com.example.expensestracker.model.entity.CategoryEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponse {
    @JsonProperty("category_id")
    private Long categoryId;
    private String categoryName;
    private String categoryType;


    public static CategoryResponse fromCategory(CategoryEntity categories) {
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .categoryId(categories.getCategoryId())
                .categoryName(categories.getCategoryName())
                .categoryType(String.valueOf(categories.getType()))
                .build();
        return categoryResponse;
    }
}
