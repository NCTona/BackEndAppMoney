package com.example.expensestracker.util;

import java.security.SecureRandom;
import java.util.stream.Collectors;

public class OtpUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;

    public static String generateOtp() {
        return RANDOM.ints(OTP_LENGTH, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }
}
