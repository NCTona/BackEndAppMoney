package com.example.expensestracker.controller;

import com.example.expensestracker.model.dto.response.ApiResponse;
import com.example.expensestracker.model.dto.response.MLOpsTransactionDTO;
import com.example.expensestracker.service.MLOpsInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("api/internal")
@RequiredArgsConstructor
public class MLOpsInternalController {

    private final MLOpsInternalService mlOpsInternalService;

    @Value("${mlops.api-key}")
    private String expectedApiKey;

    @Value("${mlops.model-dir:models}")
    private String modelDir;

    /**
     * GET /api/internal/transactions
     * Trả về toàn bộ transactions cho MLOps pipeline training.
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactionsForMLOps(
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        if (!isValidApiKey(apiKey)) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse("error", "API Key không hợp lệ hoặc thiếu"));
        }

        List<MLOpsTransactionDTO> transactions = mlOpsInternalService.getAllTransactionsForMLOps();
        return ResponseEntity.ok(transactions);
    }

    /**
     * POST /api/internal/model/update
     * Nhận file model .tflite mới từ MLOps pipeline.
     */
    @PostMapping("/model/update")
    public ResponseEntity<?> uploadModel(
            @RequestParam("model_file") MultipartFile modelFile,
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        if (!isValidApiKey(apiKey)) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse("error", "API Key không hợp lệ hoặc thiếu"));
        }

        if (modelFile.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "File model không được để trống"));
        }

        try {
            String savedPath = mlOpsInternalService.saveModel(modelFile);
            return ResponseEntity.ok(new ApiResponse("success",
                    "Model đã được lưu thành công tại: " + savedPath));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("error", "Lỗi khi lưu model: " + e.getMessage()));
        }
    }

    /**
     * GET /api/internal/model/download
     * App mobile gọi API này để tải file model .tflite mới nhất.
     * Endpoint này yêu cầu user đã đăng nhập (JWT Bearer token).
     */
    @GetMapping("/model/download")
    public ResponseEntity<?> downloadModel() {
        try {
            Path modelPath = Paths.get(modelDir, "expense_model.tflite");
            if (!Files.exists(modelPath)) {
                return ResponseEntity.status(404)
                        .body(new ApiResponse("error", "Chưa có model nào được huấn luyện"));
            }

            Resource resource = new FileSystemResource(modelPath.toFile());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"expense_model.tflite\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("error", "Lỗi khi tải model: " + e.getMessage()));
        }
    }

    private boolean isValidApiKey(String apiKey) {
        return apiKey != null && apiKey.equals(expectedApiKey);
    }
}
