package com.example.expensestracker.model.dto.request;

import lombok.*;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SendOtpRequest {
    private String email;
}
