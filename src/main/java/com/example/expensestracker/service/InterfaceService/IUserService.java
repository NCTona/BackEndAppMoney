package com.example.expensestracker.service.InterfaceService;

import com.example.expensestracker.model.dto.request.ChangePasswordDTO;
import com.example.expensestracker.model.dto.request.ResetPasswordRequest;
import com.example.expensestracker.model.dto.request.UserDTO;
import com.example.expensestracker.model.dto.request.UserRegisterDTO;
import com.example.expensestracker.model.dto.response.LoginResponse;
import com.example.expensestracker.model.entity.UserEntity;

public interface IUserService {
    public UserEntity createUser(UserRegisterDTO userRegisterDTODTO) throws Exception;

    public UserEntity updateCategory(Long userId, UserDTO userDTO) throws Exception;

    public LoginResponse login(String phoneNumber, String password) throws Exception;

    public LoginResponse refreshToken(String requestRefreshToken) throws Exception;

    public void logout(String token);

    public void changePassword(String username, ChangePasswordDTO changePasswordDTO) throws Exception;

    public void sendOtp(String email) throws Exception;

    public String verifyOtp(String email, String otp) throws Exception;

    public void resetPassword(ResetPasswordRequest request) throws Exception;
}
