package com.mahesh.fna.rest.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderUtil {

    public static String encryptPassword(String password) {
        PasswordEncoder encoder =  new BCryptPasswordEncoder(12);
        // Generate BCrypt hash
        String encryptedPassword = encoder.encode(password);
        System.out.println("Original Password: " + password);
        System.out.println("Encrypted Password: " + encryptedPassword);
        // Verify password
        boolean matches = encoder.matches(
                password,
                encryptedPassword
        );
        System.out.println("Password Matches: " + matches);
        return encryptedPassword;
    }
}
