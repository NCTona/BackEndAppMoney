package com.example.expensestracker.controller;

import com.example.expensestracker.model.dto.request.FixedTransactionDTO;

import com.example.expensestracker.model.dto.response.ApiResponse;
import com.example.expensestracker.model.dto.response.FixedTransactionListResponse;
import com.example.expensestracker.model.dto.response.FixedTransactionResponse;
import com.example.expensestracker.model.entity.FixedTransactionEntity;
import com.example.expensestracker.service.InterfaceService.IFixedTransactionService;
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
@RequestMapping("api/fixed-transactions")
public class FixedTransactionController {
    @Autowired
    private IFixedTransactionService fixedTransactionService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("")
    public ResponseEntity<?> createTransaction(@Valid @RequestBody FixedTransactionDTO fixedTransactionDTO, BindingResult bindingResult, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
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
            FixedTransactionEntity fixedtransactionEntity = fixedTransactionService.createFixedTransaction(fixedTransactionDTO,userId);

            return ResponseEntity.ok(new ApiResponse("success","Lưu giao dịch cố định thành công" ));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PutMapping("/{fixedTransactionId}")
    public ResponseEntity<?> updateFixedTransaction(@PathVariable Long fixedTransactionId, @Valid @RequestBody FixedTransactionDTO fixedTransactionDTO, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try{
            // Lấy token từ header Authorization
            String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
            // Trích xuất userId từ token
            Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
            fixedTransactionService.updateFixedTransaction(fixedTransactionId,userId,fixedTransactionDTO);
            return ResponseEntity.ok(new ApiResponse("success", "Cập nhật giao dịch cố định thành công"));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{fixedTransactionId}")
    public ResponseEntity<?> deleteFixedTransaction(@PathVariable Long fixedTransactionId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try{
            // Lấy token từ header Authorization
            String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
            // Trích xuất userId từ token
            Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
            fixedTransactionService.deleteFixedTransaction(fixedTransactionId,userId);
            return ResponseEntity.ok(new ApiResponse("success", "Xóa giao dịch cố định thành công"));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getFixedTransaction(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        // Lấy token từ header Authorization
        String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
        // Trích xuất userId từ token
        Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
        List<FixedTransactionResponse> fixedTransactionResponses = fixedTransactionService.getFixedTransaction(userId);
        return ResponseEntity.ok(FixedTransactionListResponse
                .builder()
                .fixedTransactionResponseList(fixedTransactionResponses)
                .build());
    }
}
