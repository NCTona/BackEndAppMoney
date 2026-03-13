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
    private String resetToken;
    private String newPassword;
    private String confirmPassword;

}
