package com.example.expensestracker.service;

import com.example.expensestracker.model.dto.response.MLOpsTransactionDTO;
import com.example.expensestracker.model.entity.TransactionEntity;
import com.example.expensestracker.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MLOpsInternalService {
    private final TransactionRepository transactionRepository;

    @Value("${mlops.model-dir:models}")
    private String modelDir;

    /**
     * Lấy toàn bộ transactions từ DB để gửi cho MLOps training.
     */
    public List<MLOpsTransactionDTO> getAllTransactionsForMLOps() {
        List<TransactionEntity> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(MLOpsTransactionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lưu file model .tflite mà MLOps gửi lên server.
     */
    public String saveModel(MultipartFile modelFile) throws IOException {
        Path modelDirPath = Paths.get(modelDir);
        if (!Files.exists(modelDirPath)) {
            Files.createDirectories(modelDirPath);
        }

        String originalFilename = modelFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".tflite")) {
            throw new IllegalArgumentException("File phải có định dạng .tflite");
        }

        Path targetPath = modelDirPath.resolve(originalFilename);
        Files.copy(modelFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toAbsolutePath().toString();
    }
}
