package com.example.expensestracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("kynguyen29032003@gmail.com"); // Địa chỉ email gửi
        message.setTo(to); // Địa chỉ email người nhận
        message.setSubject(subject); // Tiêu đề email
        message.setText(body); // Nội dung email

        javaMailSender.send(message);
    }
}
