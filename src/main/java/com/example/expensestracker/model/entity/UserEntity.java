package com.example.expensestracker.model.entity;

import com.example.expensestracker.util.StringCryptoConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity extends BaseEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(name = "email", length = 255, nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String email;
    @Column(name = "phone_number", length = 255, nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String phoneNumber;
    @Column(name = "password", length = 255, nullable = false)
    private String password;
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    @Convert(converter = StringCryptoConverter.class)
    private String refreshToken;
    @Column(name = "refresh_token_expiry")
    private LocalDateTime refreshTokenExpiry;
    @Column(name = "otp_code", length = 255)
    private String otpCode;
    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;
    @Column(name = "reset_password_token")
    private String resetPasswordToken;
    @Column(name = "reset_password_token_expiry")
    private LocalDateTime resetPasswordTokenExpiry;
    @Column(name = "failed_login_attempts")
    @Builder.Default
    private int failedLoginAttempts = 0;
    @Column(name = "lock_time")
    private LocalDateTime lockTime;
    @Column(name = "account_non_locked")
    @Builder.Default
    private boolean accountNonLocked = true;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEntity> transactions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FixedTransactionEntity> fixedTransactionEntities;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryLimitEntity> spendingLimitEntities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isRefreshTokenExpired() {
        return refreshTokenExpiry != null && refreshTokenExpiry.isBefore(LocalDateTime.now());
    }

    public boolean isOtpExpired() {
        return otpExpiry != null && otpExpiry.isBefore(LocalDateTime.now());
    }

    public boolean isResetTokenExpired() {
        return resetPasswordTokenExpiry != null && resetPasswordTokenExpiry.isBefore(LocalDateTime.now());
    }
}
