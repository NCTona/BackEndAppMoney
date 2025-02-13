package com.example.expensestracker.controller;


import com.example.expensestracker.model.dto.response.CategoryListResponse;
import com.example.expensestracker.model.dto.response.CategoryResponse;
import com.example.expensestracker.service.CategoryService;
import com.example.expensestracker.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

//    @PostMapping("")
//    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO category, BindingResult result,  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
//        try {
//            // Lấy token từ header Authorization
//            String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
//            // Trích xuất userId từ token
//            Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
//            if (result.hasErrors()) {
//                List<String> errorsMessages = result.getFieldErrors()
//                        .stream()
//                        .map(FieldError::getDefaultMessage)
//                        .toList();
//                return ResponseEntity.badRequest().body(new ApiResponse("error", errorsMessages));
//
//            }
//            CategoryEntity newCategory = categoryService.createCategory(category,userId);
//            return ResponseEntity.ok(new ApiResponse("success", "Insert category successfully"));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
//        }
//    }
//
//    @PutMapping("/{categoryId}")
//    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId,@Valid @RequestBody CategoryDTO categoryDTO, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
//        try{
//            // Lấy token từ header Authorization
//            String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
//            // Trích xuất userId từ token
//            Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
//            categoryService.updateCategory(categoryId,userId,categoryDTO);
//            return ResponseEntity.ok(new ApiResponse("success", "Update category successfully"));
//        }catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
//        }
//    }
//
//    @DeleteMapping("/{categoryId}")
//    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
//        try{
//            // Lấy token từ header Authorization
//            String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
//            // Trích xuất userId từ token
//            Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
//            categoryService.deleteCategory(categoryId,userId);
//            return ResponseEntity.ok(new ApiResponse("success", "Delete category successfully"));
//        }catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
//        }
//    }
    @GetMapping("/expense")
    public ResponseEntity<CategoryListResponse> getExpenseCategories() {
            // Lấy token từ header Authorization
//            String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
//            // Trích xuất userId từ token
//            Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
        List<CategoryResponse> categories = categoryService.getExpenseCategories();
        return ResponseEntity.ok(CategoryListResponse
                .builder()
                .categoryList(categories)
                .build());
    }
    @GetMapping("/income")
    public ResponseEntity<CategoryListResponse> getIncomeCategories() {
        // Lấy token từ header Authorization
//        String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
//        // Trích xuất userId từ token
//        Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
        List<CategoryResponse> categories = categoryService.getIncomeCategories();
        return ResponseEntity.ok(CategoryListResponse
                .builder()
                .categoryList(categories)
                .build());
    }
}
