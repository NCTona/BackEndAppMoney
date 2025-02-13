package com.example.expensestracker.service;

import com.example.expensestracker.model.dto.response.CategoryResponse;
import com.example.expensestracker.model.entity.CategoryEntity;
import com.example.expensestracker.model.enums.Type;
import com.example.expensestracker.repositories.CategoryRepository;
import com.example.expensestracker.repositories.UserRepository;
import com.example.expensestracker.service.InterfaceService.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class CategoryService implements ICategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;


//    @Override
//    public CategoryEntity createCategory(CategoryDTO category,Long userId) {
//        UserEntity user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        if (categoryRepository.existsByCategoryNameAndUserId(category.getCategoryName(),userId)) {
//            throw new RuntimeException("Category name already exists");
//        }
//        // Kiểm tra nếu categoryName đã tồn tại và có isDefault = true
//        if (categoryRepository.existsByCategoryNameAndTypeAndIsDefault(category.getCategoryName(), Type.valueOf(category.getType()),true)) {
//            throw new RuntimeException("Category name already exists with default status");
//        }
//        CategoryEntity newCategory = CategoryEntity.builder()
//                .categoryName(category.getCategoryName())
//                .type(Type.valueOf(category.getType()))
//                .user(user)
//                .build();
//        return categoryRepository.save(newCategory);
//    }

//    @Override
//    public CategoryEntity getCategoryById(Long id) {
//        return categoryRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Category not found"));
//    }

//    @Override
//    public List<CategoryEntity> getAllCategories() {
//        return categoryRepository.findAll();
//    }

//    @Override
//    public CategoryEntity updateCategory(Long categoryId,Long userId, CategoryDTO categoryDTO) {
//        CategoryEntity category = categoryRepository.findByCategoryIdAndUserId(categoryId,userId)
//                .orElseThrow(() -> new DataNotFoundException("Không thể cập nhật danh mục"));
//        // Kiểm tra nếu danh mục là mặc định và không thuộc về người dùng cụ thể
//        if (category.isDefault()) {
//            throw new RuntimeException("Không thể cập nhật danh mục mặc định.");
//        }
//        // Thực hiện cập nhật nếu danh mục không phải là mặc định
//        category.setCategoryName(categoryDTO.getCategoryName());
//        category.setType(Type.valueOf(categoryDTO.getType()));
//        categoryRepository.save(category);
//        return category;
//    }
//
//    @Override
//    public void deleteCategory(Long categoryId,Long userId) {
//        CategoryEntity category = categoryRepository.findByCategoryIdAndUserId(categoryId,userId)
//                .orElseThrow(() -> new DataNotFoundException("Không thể xóa danh mục"));
//        if (category.isDefault()) {
//            throw new RuntimeException("Không thể xóa danh mục mặc định.");
//        }
//        categoryRepository.delete(category);
//    }

    @Override
    public List<CategoryResponse> getExpenseCategories() {
        // Tìm tất cả danh mục 'expense' cho người dùng và các danh mục mặc định
        List<CategoryEntity> categories =  categoryRepository.findByType(Type.expense);
        return categories.stream()
                .map(CategoryResponse::fromCategory)
                .collect(Collectors.toList());
    }
    @Override
    public List<CategoryResponse> getIncomeCategories() {
        // Tìm tất cả danh mục 'expense' cho người dùng và các danh mục mặc định
        List<CategoryEntity> categories =  categoryRepository.findByType(Type.income);
        return categories.stream()
                .map(CategoryResponse::fromCategory)
                .collect(Collectors.toList());
    }
}
