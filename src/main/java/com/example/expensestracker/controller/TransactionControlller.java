package com.example.expensestracker.controller;

import com.example.expensestracker.model.dto.request.TransactionDTO;
import com.example.expensestracker.model.dto.response.ApiResponse;
import com.example.expensestracker.model.dto.response.TransactionResponse;
import com.example.expensestracker.model.entity.TransactionEntity;
import com.example.expensestracker.service.TransactionService;
import com.example.expensestracker.util.JwtTokenUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/transactions")
public class TransactionControlller {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @PostMapping("")
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO, BindingResult bindingResult, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7);
            Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(new ApiResponse("error", errors));
            }
            TransactionEntity transactionEntity = transactionService.createTransaction(transactionDTO,userId);
            TransactionResponse transactionResponse = TransactionResponse.fromEntity(transactionEntity);
            return ResponseEntity.ok(transactionResponse);
        } catch (Exception e){
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long transactionId, @Valid @RequestBody TransactionDTO transactionDTO, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try{
            // Lấy token từ header Authorization
            String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
            // Trích xuất userId từ token
            Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
            transactionService.updateTransaction(transactionId,userId,transactionDTO);
            return ResponseEntity.ok(new ApiResponse("success", "Cập nhật giao dịch thành công"));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long transactionId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try{
            // Lấy token từ header Authorization
            String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
            // Trích xuất userId từ token
            Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
            transactionService.deleteTransaction(transactionId,userId);
            return ResponseEntity.ok(new ApiResponse("success", "Xóa danh mục thành công"));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<TransactionResponse>> searchTransactions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) Long amount) {

        // Lấy token từ header Authorization
        String token = authorizationHeader.substring(7); // Bỏ tiền tố "Bearer "

        // Trích xuất userId từ token
        Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));

        // Sử dụng userId và các tiêu chí tìm kiếm để lấy danh sách giao dịch
        List<TransactionResponse> transactions = transactionService.searchTransactions(
                userId, categoryName, note, amount);

        return ResponseEntity.ok(transactions);
    }
    


}
