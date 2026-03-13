package com.example.expensestracker.controller;

import com.example.expensestracker.model.dto.request.*;
import com.example.expensestracker.model.dto.response.ApiResponse;
import com.example.expensestracker.model.dto.response.LoginResponse;
import com.example.expensestracker.model.dto.response.VerifyOtpResponse;
import com.example.expensestracker.model.entity.UserEntity;
import com.example.expensestracker.service.InterfaceService.IUserService;
import com.example.expensestracker.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
public class UserController {
    @Autowired
    private IUserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRegisterDTO userRegisterDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorsMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(new ApiResponse("error", errorsMessages));
            }
            if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getRetypePassword())) {
                return ResponseEntity.badRequest().body(new ApiResponse("error", "Mật khẩu không khớp"));
            }
            UserEntity user = userService.createUser(userRegisterDTO);
            return ResponseEntity.ok(new ApiResponse("success", "Đăng ký thành công"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("error", ex.getMessage())); // 5
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChangePasswordDTO changePasswordDTO) throws Exception {
        if (changePasswordDTO.getCurrentPassword().equals(changePasswordDTO.getNewPassword())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Mật khẩu mới không được trùng mật khẩu cũ"));
        }
        userService.changePassword(userDetails.getUsername(), changePasswordDTO);
        return ResponseEntity.ok(new ApiResponse("success", "Mật khẩu đã được cập nhật thành công"));
    }

    @PutMapping("")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDTO userDTO,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7);
            Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
            userService.updateCategory(userId, userDTO);
            return ResponseEntity.ok(new ApiResponse("success", "Cập nhật hồ sơ thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO) {
        try {
            LoginResponse loginResponse = userService.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword());
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenDTO request) {
        try {
            LoginResponse response = userService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(new ApiResponse("error", "Token không hợp lệ"));
            }
            String token = authHeader.substring(7);
            userService.logout(token);
            return ResponseEntity.ok(new ApiResponse("success", "Đăng xuất thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody SendOtpRequest sendOtpRequest) {
        try {
            userService.sendOtp(sendOtpRequest.getEmail());
            return ResponseEntity.ok(new ApiResponse("success", "OTP đã được gửi thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {
            String token = userService.verifyOtp(request.getEmail(), request.getOtp());
            return ResponseEntity.ok(VerifyOtpResponse.builder()
                    .status("success")
                    .message("OTP hợp lệ, vui lòng dùng token này để đổi mật khẩu")
                    .resetToken(token)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse("error", "Mật khẩu mới và mật khẩu xác nhận không khớp"));
            }

            userService.resetPassword(resetPasswordRequest);
            return ResponseEntity.ok(new ApiResponse("success", "Đã đặt lại mật khẩu thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

}
