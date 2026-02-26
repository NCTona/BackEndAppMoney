package com.example.expensestracker.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenDTO {
    @NotBlank(message = "Refresh token không được để trống")
    private String refreshToken;
}
