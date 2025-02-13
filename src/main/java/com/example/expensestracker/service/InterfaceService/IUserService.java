package com.example.expensestracker.service.InterfaceService;

import com.example.expensestracker.model.dto.request.*;
import com.example.expensestracker.model.entity.UserEntity;

public interface IUserService {
    UserEntity createUser(UserRegisterDTO userRegisterDTODTO) throws Exception;
    UserEntity updateCategory( Long userId, UserDTO userDTO) throws Exception;
    String login(String phoneNumber ,String password) throws Exception;
    void changePassword(String username, ChangePasswordDTO changePasswordDTO) throws Exception;
    public void sendOtp(String email) throws Exception;
    public void verifyOtp(String email, String otp) throws Exception;
    public void resetPassword(ResetPasswordRequest request) throws Exception;
}
