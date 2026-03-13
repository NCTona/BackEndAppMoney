package com.example.expensestracker.service;

import com.example.expensestracker.exception.DataNotFoundException;
import com.example.expensestracker.model.dto.request.FixedTransactionDTO;
import com.example.expensestracker.model.dto.response.FixedTransactionResponse;
import com.example.expensestracker.model.entity.CategoryEntity;
import com.example.expensestracker.model.entity.FixedTransactionEntity;
import com.example.expensestracker.model.entity.TransactionEntity;
import com.example.expensestracker.model.entity.UserEntity;
import com.example.expensestracker.model.enums.RepeatFrequency;
import com.example.expensestracker.repositories.CategoryRepository;
import com.example.expensestracker.repositories.FixedTransactionRepository;
import com.example.expensestracker.repositories.TransactionRepository;
import com.example.expensestracker.repositories.UserRepository;
import com.example.expensestracker.service.InterfaceService.IFixedTransactionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FixedTransactionService implements IFixedTransactionService {
    @Autowired
    private FixedTransactionRepository fixedTransactionRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    @Override
    public FixedTransactionEntity createFixedTransaction(FixedTransactionDTO fixedTransactionDTO, Long userId) throws Exception {
        // 1. Lưu giao dịch cố định
        if(fixedTransactionDTO.getAmount().compareTo(BigDecimal.ZERO)<=0){
            throw new Exception("Số tiền không hợp lệ");
        }
            CategoryEntity existingCategory = categoryRepository
                    .findById(fixedTransactionDTO.getCategoryId())
                    .orElseThrow(() -> new DataNotFoundException(
                            "Không tìm thấy danh mục có id: " + fixedTransactionDTO.getCategoryId()));
            UserEntity existingUser = userRepository
                    .findById(userId)
                    .orElseThrow(() -> new DataNotFoundException(
                            "Không tìm thấy danh mục có id: " + userId));
            FixedTransactionEntity fixedTransactionEntity = FixedTransactionEntity.builder()
                    .user(existingUser)
                    .category(existingCategory)
                    .title(fixedTransactionDTO.getTitle())
                    .amount(fixedTransactionDTO.getAmount())
                    .repeatFrequency(RepeatFrequency.valueOf(fixedTransactionDTO.getRepeatFrequency()))
                    .startDate(fixedTransactionDTO.getStartDate())
                    .endDate(fixedTransactionDTO.getEndDate())
                    .build();
        FixedTransactionEntity savedFixedTransaction = fixedTransactionRepository.save(fixedTransactionEntity);
            // 2. Xử lý tạo giao dịch cho các ngày trong quá khứ
            generateTransactionsForPastDates(fixedTransactionEntity);
        return savedFixedTransaction;

    }
    @Transactional
    @Override
    public FixedTransactionEntity updateFixedTransaction(Long fixedTransactionId,Long userId, FixedTransactionDTO fixedtransactionDTO) throws Exception {
        FixedTransactionEntity fixedTransaction = fixedTransactionRepository.findByFixedTransactionIdAndUserId(fixedTransactionId,userId)
                .orElseThrow(() -> new DataNotFoundException("Giao dịch không tồn tại"));
        CategoryEntity existingCategory = categoryRepository
                .findById(fixedtransactionDTO.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Không tìm thấy danh mục có id: " + fixedTransaction.getCategory().getCategoryId()));
        // 2. Lưu lại giá trị `startDate` cũ
        LocalDate oldStartDate = fixedTransaction.getStartDate();

        fixedTransaction.setTitle(fixedtransactionDTO.getTitle());
        fixedTransaction.setAmount(fixedtransactionDTO.getAmount());
        fixedTransaction.setCategory(existingCategory);
        fixedTransaction.setRepeatFrequency(RepeatFrequency.valueOf(fixedtransactionDTO.getRepeatFrequency()));
        fixedTransaction.setStartDate(fixedtransactionDTO.getStartDate());
        fixedTransaction.setEndDate(fixedtransactionDTO.getEndDate());
        fixedTransactionRepository.save(fixedTransaction);

        // 3. Xóa giao dịch thực tế liên quan (trong phạm vi sửa đổi)
        transactionRepository.deleteByUserIdAndFixedTransactionIdAndTransactionDateBetween(
                fixedTransaction.getUser().getUserId(),
                fixedTransaction.getFixedTransactionId(),
                oldStartDate,
                LocalDate.now()
        );
        // 4. Tạo lại giao dịch thực tế từ thông tin mới
        generateTransactionsForPastDates(fixedTransaction);

        return fixedTransaction;
    }

    @Transactional
    @Override
    public void deleteFixedTransaction(Long fixedTransactionId, Long userId) throws Exception {
        FixedTransactionEntity fixedTransaction = fixedTransactionRepository.findByFixedTransactionIdAndUserId(fixedTransactionId,userId)
                .orElseThrow(() -> new DataNotFoundException("Không thể xóa giao dịch"));

        fixedTransactionRepository.delete(fixedTransaction);
    }
    @Override
    public List<FixedTransactionResponse> getFixedTransaction(Long userId) {
        List<FixedTransactionEntity> fixedTransactions =  fixedTransactionRepository.findByUserId(userId);
        return fixedTransactions.stream()
                .map(FixedTransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }
    private void generateTransactionsForPastDates(FixedTransactionEntity fixedTransaction) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = fixedTransaction.getStartDate();
        LocalDate endDate = fixedTransaction.getEndDate() != null ? fixedTransaction.getEndDate() : today;
        LocalDate loopEndDate = endDate.isBefore(today) ? endDate : today;
        // Duyệt qua các ngày từ `startDate` đến `today`

        UserEntity user = fixedTransaction.getUser();
        CategoryEntity category = fixedTransaction.getCategory();


        while (!startDate.isAfter(loopEndDate)) {
            if (!transactionExists(user.getUserId(), startDate, category.getCategoryId(), fixedTransaction.getFixedTransactionId())) {
                TransactionEntity transaction = TransactionEntity.builder()
                        .user(user)
                        .category(category)
                        .amount(fixedTransaction.getAmount())
                        .transactionDate(startDate)
                        .note(fixedTransaction.getTitle())
                        .fixedTransaction(fixedTransaction)
                        .build();
                transactionRepository.save(transaction);
            }
            startDate = getNextDate(String.valueOf(fixedTransaction.getRepeatFrequency()), startDate);
        }
    }

    private boolean transactionExists(Long userId, LocalDate date, Long categoryId,Long fixTransactionId) {
        // Kiểm tra xem giao dịch đã tồn tại chưa
        return transactionRepository.existsByUserIdAndTransactionDateAndCategoryIdAAndFixedTransactionId(userId, date, categoryId,fixTransactionId);
    }
    public void generateTransactionsForToday() {
        LocalDate today = LocalDate.now();

        List<FixedTransactionEntity> fixedTransactions = fixedTransactionRepository.findAll();

        for (FixedTransactionEntity fixedTransaction : fixedTransactions) {
            // Kiểm tra ngày hợp lệ: today phải nằm trong khoảng [startDate, endDate]
            if ((fixedTransaction.getStartDate().isBefore(today) || fixedTransaction.getStartDate().isEqual(today)) &&
                    (fixedTransaction.getEndDate() == null || fixedTransaction.getEndDate().isAfter(today) || fixedTransaction.getEndDate().isEqual(today))) {

                // Kiểm tra xem giao dịch đã tồn tại hay chưa
                if (!transactionExists(fixedTransaction.getUser().getUserId(), today, fixedTransaction.getCategory().getCategoryId(), fixedTransaction.getFixedTransactionId())) {
                    TransactionEntity transaction = TransactionEntity.builder()
                            .user(fixedTransaction.getUser())
                            .category(fixedTransaction.getCategory())
                            .amount(fixedTransaction.getAmount())
                            .transactionDate(today)
                            .note(fixedTransaction.getTitle())
                            .fixedTransaction(fixedTransaction)
                            .build();
                    transactionRepository.save(transaction);
                }
            }
        }
    }

    private LocalDate getNextDate(String frequency, LocalDate currentDate) {
        switch (frequency.toLowerCase()) {
            case "daily":
                return currentDate.plusDays(1);
            case "weekly":
                return currentDate.plusWeeks(1);
            case "monthly":
                return currentDate.plusMonths(1);
            case "yearly":
                return currentDate.plusYears(1);
            default:
                throw new IllegalArgumentException("Tần suất lặp lại không hợp lệ: " + frequency);
        }
    }
}
