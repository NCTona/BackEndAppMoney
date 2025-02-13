package com.example.expensestracker.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    @JsonProperty("category_id")
    private Long categoryId;
    @NotEmpty(message = "Category's name cannot be empty")
    @JsonProperty("category_name")
    private String categoryName;
    @NotEmpty(message = "Category's type cannot be empty")
    @JsonProperty("type")
    private String type;
}
