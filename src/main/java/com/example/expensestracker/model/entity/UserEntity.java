package com.example.expensestracker.model.entity;

import com.example.expensestracker.model.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name="users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity extends BaseEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(name="email",length = 100,nullable = false)
    private String email;
    @Column(name="phone_number",length = 20,nullable = false)
    private String phoneNumber;
    @Column(name="password",length = 255,nullable = false)
    private String password;
    @Column(name = "otp_code", length = 10)
    private String otpCode;
    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;
//    @Column(name="full_name")
//    private String fullName;
//    @Column(name="birth_date")
//    private LocalDate birthDate;
//    @Column(name="gender")
//    @Enumerated(EnumType.STRING)
//    private Gender gender;
//    @Column(name="address")
//    private String address;
//    @Column(name="finacial_goal")
//    private BigDecimal financialGoal;
//    @Column(name="preferred_report")
//    private String preferredReport;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEntity> transactions;
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<CategoryEntity> categories ;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FixedTransactionEntity> fixedTransactionEntities ;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryLimitEntity> spendingLimitEntities ;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // Không có vai trò
    }

    @Override
    public String getUsername() {return phoneNumber;}
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    // Phương thức kiểm tra OTP đã hết hạn chưa
    public boolean isOtpExpired() {
        return otpExpiry != null && otpExpiry.isBefore(LocalDateTime.now());
    }
}
