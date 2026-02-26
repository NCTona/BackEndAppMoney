package com.example.expensestracker.service;

import com.example.expensestracker.repositories.UserRepository;
import com.example.expensestracker.service.InterfaceService.IFixedTransactionService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AutoTransactionSchedulerService {
    @Autowired
    private IFixedTransactionService fixedTransactionService;
    @Autowired
    private UserRepository userRepository;
    /**
     * Scheduler chạy mỗi ngày lúc 00:00 để tạo giao dịch tự động.
     */
    @Scheduled(cron = "0 0 0 * * ?") // Mỗi ngày vào lúc 00:00
    public void generateDailyTransactions() {
        try {
            fixedTransactionService.generateTransactionsForToday();
            System.out.println("Giao dịch tự động đã được tạo thành công!");
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo giao dịch tự động: " + e.getMessage());
            e.printStackTrace();
        }
    }
     //Gọi tự động sao chép giới hạn khi ứng dụng bắt đầu
    @PostConstruct
    public void init() {
        fixedTransactionService.generateTransactionsForToday();
    }
}
