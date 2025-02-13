package com.example.expensestracker.model.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDTO {
    @JsonProperty("phone_number")
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    private String email;
    @JsonProperty("password")
    @NotBlank(message = "Password is required")
    private String password;
    @JsonProperty("retype_password")
    private String retypePassword;

}
