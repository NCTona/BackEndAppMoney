package com.example.expensestracker.service;

import com.example.expensestracker.exception.DataNotFoundException;
import com.example.expensestracker.model.dto.request.*;
import com.example.expensestracker.model.entity.UserEntity;
import com.example.expensestracker.repositories.UserRepository;
import com.example.expensestracker.service.InterfaceService.IUserService;
import com.example.expensestracker.util.JwtTokenUtil;
import com.example.expensestracker.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Override
    public UserEntity createUser(UserRegisterDTO userRegisterDTO) throws Exception {
        //register user
        String phoneNumber = userRegisterDTO.getPhoneNumber();
        String email = userRegisterDTO.getEmail();
        //kiểm tra xem số điện thoại đã tồn tại hay chưa
        if (userRepository.existsByPhoneNumber(phoneNumber) && userRepository.existsByEmail(email)) {
            throw new DataIntegrityViolationException("Số tài khoản và email đã tồn tại!");
        }else if(userRepository.existsByEmail(email)) {
            throw new DataIntegrityViolationException("Email đã tồn tại!");
        }else if(userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DataIntegrityViolationException("Số điện thoại đã tồn tại!");
        }
        //convert from userDTO -> userEntity
        UserEntity newUser = UserEntity.builder()
                .phoneNumber(userRegisterDTO.getPhoneNumber())
                .email(userRegisterDTO.getEmail())
                .password(passwordEncoder.encode(userRegisterDTO.getPassword()))
                .build();
        return userRepository.save(newUser);
    }
    @Override
    public UserEntity updateCategory( Long userId, UserDTO userDTO) throws Exception{
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy profile"));
        //kiểm tra xem số điện thoại đã tồn tại hay chưa
        if (userRepository.existsByPhoneNumber(userDTO.getPhoneNumber()) && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DataIntegrityViolationException("Phone number and Email already exists");
        }else if(userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists");
        }else if(userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        // Thực hiện cập nhật nếu danh mục không phải là mặc định
       // user.setFullName(userDTO.getFullName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setEmail(userDTO.getEmail());
//        user.setGender(Gender.valueOf(userDTO.getGender()));
//        user.setBirthDate(userDTO.getBirthDate());
//        user.setAddress(userDTO.getAddress());
        userRepository.save(user);
        return user;
    }
    @Override
    public String login(String phoneNumber, String password) throws Exception {
        Optional<UserEntity> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if (optionalUser.isEmpty()) {
            throw new  UsernameNotFoundException("Số tài khoản hoặc mật khẩu không hợp lệ!");
        }
        //return optionalUser.get();//muốn trả JWT token ?
        UserEntity existingUser = optionalUser.get();
        //check password
        if (!passwordEncoder.matches(password, existingUser.getPassword())) {
            throw new BadCredentialsException("Số tài khoản hoặc mật khẩu không đúng!");
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                phoneNumber, password,
                existingUser.getAuthorities()
        );
        //authenticate with Java Spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }

    @Override
    public void changePassword(String username, ChangePasswordDTO changePasswordDTO) throws Exception {
        UserEntity user = userRepository.findByPhoneNumber(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void sendOtp(String email) {
        // Kiểm tra xem người dùng có tồn tại không
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng có email này"));

        // Kiểm tra xem OTP đã tồn tại và còn hiệu lực chưa
        if (user.getOtpExpiry() != null && user.getOtpExpiry().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("OTP đã được gửi và chưa hết hạn");
        }

        // Tạo mã OTP
        String otp = OtpUtil.generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(1); // Đặt thời gian hết hạn là 1 phút

        // Lưu OTP và thời gian hết hạn vào UserEntity
        user.setOtpCode(otp);
        user.setOtpExpiry(expiresAt);
        userRepository.save(user); // Lưu vào cơ sở dữ liệu

        // Gửi OTP qua email
        String subject = "Mã OTP của bạn";
        String body = "Mã OTP của bạn là: " + otp;
        emailService.sendEmail(email, subject, body); // Gửi email
    }
    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataIntegrityViolationException("Không tìm thấy người dùng có email này"));

        // Kiểm tra OTP có khớp không và đã hết hạn chưa
        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            throw new DataIntegrityViolationException("OTP không hợp lệ");
        }
        if (user.isOtpExpired()) {
            throw new DataIntegrityViolationException("OTP đã hết hạn");
        }

        // Xóa OTP sau khi xác minh thành công
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user); // Lưu lại sau khi xóa OTP
    }
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // Kiểm tra người dùng có tồn tại không
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new DataIntegrityViolationException("Không tìm thấy người dùng có email này"));

        // Kiểm tra mật khẩu mới không phải null hoặc rỗng
        if (request.getNewPassword() == null || request.getConfirmPassword() == null ||
                !request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và mật khẩu xác nhận không khớp hoặc rỗng");
        }

        // Mã hóa mật khẩu mới
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        // Cập nhật mật khẩu đã mã hóa vào người dùng
        user.setPassword(encodedPassword);

        // Lưu lại người dùng với mật khẩu mới đã mã hóa
        userRepository.save(user);
    }

}
