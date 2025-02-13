package com.example.expensestracker.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("birth_date")
    private LocalDate birthDate;
    @JsonProperty("gender")
    private String gender;
    @JsonProperty("financial_goal")
    private BigDecimal financialGoal;
    @JsonProperty("preferred_report")
    private String preferredReport;
    private String address;
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
