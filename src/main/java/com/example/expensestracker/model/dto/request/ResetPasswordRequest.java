package com.example.expensestracker.model.dto.request;

import lombok.*;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    private String email;
    private String newPassword;  // Mật khẩu mới
    private String confirmPassword;  // Xác nhận mật khẩu mới

}
