package com.example.expensestracker.service;

import com.example.expensestracker.exception.DataNotFoundException;
import com.example.expensestracker.model.dto.request.ChangePasswordDTO;
import com.example.expensestracker.model.dto.request.ResetPasswordRequest;
import com.example.expensestracker.model.dto.request.UserDTO;
import com.example.expensestracker.model.dto.request.UserRegisterDTO;
import com.example.expensestracker.model.dto.response.LoginResponse;
import com.example.expensestracker.model.entity.TokenBlackList;
import com.example.expensestracker.model.entity.UserEntity;
import com.example.expensestracker.repositories.TokenBlackListRepository;
import com.example.expensestracker.repositories.UserRepository;
import com.example.expensestracker.service.InterfaceService.IUserService;
import com.example.expensestracker.util.JwtTokenUtil;
import com.example.expensestracker.util.OtpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService {
    @Value("${app.security.login.max-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.login.lock-duration-minutes:15}")
    private long lockTimeDuration;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlackListRepository tokenBlackListRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Override
    public UserEntity createUser(UserRegisterDTO userRegisterDTO) throws Exception {
        String phoneNumber = userRegisterDTO.getPhoneNumber();
        String email = userRegisterDTO.getEmail();
        if (userRepository.existsByPhoneNumber(phoneNumber) && userRepository.existsByEmail(email)) {
            throw new DataIntegrityViolationException("Số tài khoản và email đã tồn tại!");
        } else if (userRepository.existsByEmail(email)) {
            throw new DataIntegrityViolationException("Email đã tồn tại!");
        } else if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DataIntegrityViolationException("Số điện thoại đã tồn tại!");
        }
        UserEntity newUser = UserEntity.builder()
                .phoneNumber(userRegisterDTO.getPhoneNumber())
                .email(userRegisterDTO.getEmail())
                .password(passwordEncoder.encode(userRegisterDTO.getPassword()))
                .build();
        return userRepository.save(newUser);
    }

    @Override
    public UserEntity updateCategory(Long userId, UserDTO userDTO) throws Exception {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy profile"));
        if (userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())
                && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DataIntegrityViolationException("Phone number and Email already exists");
        } else if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists");
        } else if (userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setEmail(userDTO.getEmail());
        userRepository.save(user);
        return user;
    }

    @Override
    public LoginResponse login(String phoneNumber, String password) throws Exception {
        Optional<UserEntity> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("Số tài khoản hoặc mật khẩu không hợp lệ!");
        }
        UserEntity user = optionalUser.get();

        if (!user.isAccountNonLocked()) {
            if (user.getLockTime() != null &&
                    user.getLockTime().plusMinutes(lockTimeDuration).isBefore(LocalDateTime.now())) {

                user.setAccountNonLocked(true);
                user.setLockTime(null);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            } else {
                throw new BadCredentialsException("Tài khoản đang bị khóa do nhập sai nhiều lần. Vui lòng thử lại sau "
                        + lockTimeDuration + " phút.");
            }
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            int currentAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(currentAttempts);

            if (currentAttempts >= maxFailedAttempts) {
                user.setAccountNonLocked(false);
                user.setLockTime(LocalDateTime.now());
                userRepository.saveAndFlush(user);

                // Send email notification
                String subject = "Cảnh báo bảo mật: Tài khoản bị khóa";
                String body = "Tài khoản của bạn đã bị khóa do nhập sai mật khẩu quá " + maxFailedAttempts + " lần. " +
                        "Vui lòng thử lại sau " + lockTimeDuration + " phút hoặc sử dụng chức năng Quên mật khẩu.";
                try {
                    emailService.sendEmail(user.getEmail(), subject, body);
                } catch (Exception e) {
                    // Log error but don't fail the login process just because email failed
                    e.printStackTrace();
                }

                throw new BadCredentialsException("Bạn đã nhập sai quá " + maxFailedAttempts
                        + " lần. Tài khoản bị khóa trong " + lockTimeDuration + " phút.");
            } else {

                userRepository.saveAndFlush(user);
                int remaining = maxFailedAttempts - currentAttempts;
                throw new BadCredentialsException(
                        "Mật khẩu không đúng. Bạn còn " + remaining + " lần thử trước khi bị khóa.");
            }
        }
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                phoneNumber, password,
                user.getAuthorities());
        authenticationManager.authenticate(authenticationToken);
        String accessToken = jwtTokenUtil.generateToken(user);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(15));
        userRepository.save(user);
        return LoginResponse.builder()
                .status("success")
                .message("Đăng nhập thành công.")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public LoginResponse refreshToken(String requestRefreshToken) throws Exception {
        String phoneNumber = jwtTokenUtil.extractPhoneNumber(requestRefreshToken);
        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(requestRefreshToken)) {
            throw new BadCredentialsException("Refresh token không hợp lệ hoặc đã bị thu hồi");
        }
        if (user.isRefreshTokenExpired()) {
            throw new BadCredentialsException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }
        if (!jwtTokenUtil.validateToken(requestRefreshToken, user)) {
            throw new BadCredentialsException("Token lỗi");
        }
        String newAccessToken = jwtTokenUtil.generateToken(user);
        return LoginResponse.builder()
                .status("success")
                .message("Làm mới thành công.")
                .accessToken(newAccessToken)
                .refreshToken(requestRefreshToken)
                .build();
    }

    @Override
    public void changePassword(String username, ChangePasswordDTO changePasswordDTO) throws Exception {
        UserEntity user = userRepository.findByPhoneNumber(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void sendOtp(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng có email này"));

        if (user.getOtpExpiry() != null && user.getOtpExpiry().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("OTP đã được gửi và chưa hết hạn");
        }
        String otp = OtpUtil.generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(1);

        user.setOtpCode(passwordEncoder.encode(otp));
        user.setOtpExpiry(expiresAt);
        userRepository.save(user);

        String subject = "Mã OTP của bạn";
        String body = "Mã OTP của bạn là: " + otp;
        emailService.sendEmail(email, subject, body);
    }

    @Override
    public String verifyOtp(String email, String otp) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataIntegrityViolationException("Không tìm thấy người dùng có email này"));

        if (user.getOtpCode() == null || !passwordEncoder.matches(otp, user.getOtpCode())) {
            throw new DataIntegrityViolationException("OTP không hợp lệ");
        }
        if (user.isOtpExpired()) {
            throw new DataIntegrityViolationException("OTP đã hết hạn");
        }
        String rawToken = java.util.UUID.randomUUID().toString();
        String hashedToken = passwordEncoder.encode(rawToken);
        user.setResetPasswordToken(hashedToken);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(5));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        return rawToken;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new DataIntegrityViolationException("Không tìm thấy người dùng có email này"));
        if (user.getResetPasswordToken() == null
                || !passwordEncoder.matches(request.getResetToken(), user.getResetPasswordToken())) {
            throw new BadCredentialsException("Token không hợp lệ");
        }

        if (user.isResetTokenExpired()) {
            throw new BadCredentialsException("Token đổi mật khẩu đã hết hạn, vui lòng thực hiện lại từ đầu");
        }

        if (request.getNewPassword() == null || request.getConfirmPassword() == null ||
                !request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và mật khẩu xác nhận không khớp hoặc rỗng");
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);

        // Unlock account on successful password reset
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        user.setFailedLoginAttempts(0);

        userRepository.save(user);
    }

    @Override
    public void logout(String token) {
        String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);

        Date expiration = jwtTokenUtil.extractExpiration(token);
        if (expiration.after(new Date())) {
            TokenBlackList blackListToken = TokenBlackList.builder()
                    .token(token)
                    .expiryDate(expiration)
                    .build();
            tokenBlackListRepository.save(blackListToken);
        }
    }
}
