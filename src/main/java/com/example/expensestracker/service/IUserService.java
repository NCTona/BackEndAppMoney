package com.example.expensestracker.service;

import com.example.expensestracker.model.dto.request.ChangePasswordDTO;
import com.example.expensestracker.model.dto.request.UserDTO;
import com.example.expensestracker.model.dto.request.UserRegisterDTO;
import com.example.expensestracker.model.entity.UserEntity;

public interface IUserService {
    UserEntity createUser(UserRegisterDTO userRegisterDTODTO) throws Exception;
    UserEntity updateCategory( Long userId, UserDTO userDTO) throws Exception;
    String login(String phoneNumber ,String password) throws Exception;

    void changePassword(String username, ChangePasswordDTO changePasswordDTO) throws Exception;
}
