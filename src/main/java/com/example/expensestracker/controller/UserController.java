package com.example.expensestracker.controller;

import com.example.expensestracker.model.dto.request.*;
import com.example.expensestracker.model.dto.response.ApiResponse;
import com.example.expensestracker.model.entity.UserEntity;
import com.example.expensestracker.service.InterfaceService.IUserService;
import com.example.expensestracker.util.JwtTokenUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
           UserEntity user = userService.createUser(userRegisterDTO);//return ResponseEntity.ok("Register successfully");
           return ResponseEntity.ok(new ApiResponse("success", "Đăng ký thành công"));
       }
       catch (Exception ex){
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("error", ex.getMessage())); //rule 5
       }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChangePasswordDTO changePasswordDTO) throws Exception {
        if (changePasswordDTO.getCurrentPassword().equals(changePasswordDTO.getNewPassword())) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Mật khẩu mới không được trùng mật khẩu cũ"));
        }
        userService.changePassword(userDetails.getUsername(), changePasswordDTO);
        return ResponseEntity.ok(new ApiResponse("success", "Mật khẩu đã được cập nhật thành công"));
    }

    @PutMapping("")
    public ResponseEntity<?> updateUser( @Valid @RequestBody UserDTO userDTO, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try{
            // Lấy token từ header Authorization
            String token = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
            // Trích xuất userId từ token
            Long userId = Long.valueOf(jwtTokenUtil.extractUserId(token));
            userService.updateCategory(userId,userDTO);
            return ResponseEntity.ok(new ApiResponse("success", "Cập nhật hồ sơ thành công"));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO) {
        // Kiểm tra thông tin đăng nhập và sinh token
        try {
            String token = userService.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword());
            // Trả về token trong response
            return ResponseEntity.ok(new ApiResponse("success", token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }




    // Endpoint để gửi OTP đến email
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
            userService.verifyOtp(request.getEmail(), request.getOtp());
            return ResponseEntity.ok(new ApiResponse("success", "OTP đã xác minh thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    // Endpoint thay đổi mật khẩu khi quên mật khẩu
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            // Kiểm tra xem mật khẩu mới và xác nhận mật khẩu có khớp nhau không
            if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(new ApiResponse("error", "Mật khẩu mới và mật khẩu xác nhận không khớp"));
            }

            // Gọi phương thức resetPassword trong service với đối tượng ResetPasswordRequest
            userService.resetPassword(resetPasswordRequest);
            return ResponseEntity.ok(new ApiResponse("success","Đã đặt lại mật khẩu thành công"));
        } catch (Exception e) {
            // Trả về lỗi nếu có ngoại lệ
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

}
