package com.example.expensestracker.service;

import com.example.expensestracker.exception.DataNotFoundException;
import com.example.expensestracker.model.dto.request.TransactionDTO;
import com.example.expensestracker.model.dto.response.TransactionResponse;
import com.example.expensestracker.model.entity.CategoryEntity;
import com.example.expensestracker.model.entity.TransactionEntity;
import com.example.expensestracker.model.entity.UserEntity;
import com.example.expensestracker.repositories.CategoryRepository;
import com.example.expensestracker.repositories.FixedTransactionRepository;
import com.example.expensestracker.repositories.TransactionRepository;
import com.example.expensestracker.repositories.UserRepository;
import com.example.expensestracker.service.InterfaceService.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TransactionService implements ITransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FixedTransactionRepository fixedTransactionRepository;

    @Override
    public TransactionEntity createTransaction(TransactionDTO transactionDTO,Long userId) throws Exception {
        if(transactionDTO.getAmount().compareTo(BigDecimal.ZERO)<=0){
            throw new Exception("Số tiền không hợp lệ");
        }else if(transactionDTO.getTransactionDate().isAfter(LocalDate.now())){
            throw  new Exception("thời gian không hợp lệ");
        } else {
            CategoryEntity existingCategory = categoryRepository
                    .findById(transactionDTO.getCategoryId())
                    .orElseThrow(() -> new DataNotFoundException(
                            "Cannot find category with id " + transactionDTO.getCategoryId()));
            UserEntity existingUser = userRepository
                    .findById(userId)
                    .orElseThrow(() -> new DataNotFoundException(
                            "Cannot find category with id " + userId));
                TransactionEntity transactionEntity = TransactionEntity.builder()
                        .transactionDate(transactionDTO.getTransactionDate())
                        .amount(transactionDTO.getAmount())
                        .category(existingCategory)
                        .user(existingUser)
                        .note(transactionDTO.getNote())
                        .build();
                return transactionRepository.save(transactionEntity);
        }
    }
    @Override
    public List<TransactionResponse> searchTransactions(Long userId, String categoryName, String note, Long amount) {
        List<TransactionEntity> transactions = transactionRepository.searchTransactions(userId, categoryName, note, amount);
        return transactions.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }
    @Override
    public TransactionEntity updateTransaction(Long transactionId,Long userId, TransactionDTO transactionDTO) throws Exception {
        TransactionEntity transaction = transactionRepository.findByTransactionIdAndUserId(transactionId,userId)
                .orElseThrow(() -> new DataNotFoundException("Giao dịch không tồn tại"));
        // Thực hiện cập nhật nếu danh mục không phải là mặc định
        CategoryEntity category = categoryRepository.findById(transactionDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục có id: " + transactionDTO.getCategoryId()));
        transaction.setCategory(category);
        transaction.setTransactionDate(transactionDTO.getTransactionDate());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setNote(transactionDTO.getNote());
        transactionRepository.save(transaction);
        return transaction;
    }

    @Override
    public void deleteTransaction(Long transactionId, Long userId) throws Exception {
        TransactionEntity transaction = transactionRepository.findByTransactionIdAndUserId(transactionId,userId)
                .orElseThrow(() -> new DataNotFoundException("Không thể xóa giao dịch"));
        transactionRepository.delete(transaction);
    }




//    @Scheduled(cron = "0 0 0 * * ?") // Chạy mỗi ngày lúc 00:00
//    public void generateTransactionsFromFixed() {
//        List<FixedTransactionEntity> fixedTransactions = fixedTransactionRepository.findByUserId(null); // Lấy tất cả
//        LocalDate today = LocalDate.now();
//
//        for (FixedTransactionEntity fixedTransaction : fixedTransactions) {
//            if (shouldGenerateTransaction(fixedTransaction, today)) {
//                TransactionEntity transaction = new TransactionEntity();
//                transaction.setUser(fixedTransaction.getUser());
//                transaction.setCategory(fixedTransaction.getCategory());
//                transaction.setAmount(fixedTransaction.getAmount());
//                transaction.setTransactionDate(today);
//                transaction.setFixedTransaction(fixedTransaction);
//                transactionRepository.save(transaction);
//            }
//        }
//    }
//
//    private boolean shouldGenerateTransaction(FixedTransactionEntity fixedTransaction, LocalDate date) {
//        if (fixedTransaction.getEndDate() != null && date.isAfter(fixedTransaction.getEndDate())) {
//            return false;
//        }
//        // Kiểm tra tần suất
//        switch (fixedTransaction.getRepeatFrequency()) {
//            case daily:
//                return true;
//            case weekly:
//                return date.getDayOfWeek().getValue() == fixedTransaction.getStartDate().getDayOfWeek().getValue();
//            case monthly:
//                return date.getDayOfMonth() == fixedTransaction.getStartDate().getDayOfMonth();
//            case yearly:
//                return date.getDayOfYear() == fixedTransaction.getStartDate().getDayOfYear();
//            default:
//                return false;
//        }
//    }
}
