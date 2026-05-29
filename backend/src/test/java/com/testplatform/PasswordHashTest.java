package com.testplatform;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTest {
    @Test
    public void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("admin123");
        System.out.println("BCrypt hash for admin123:");
        System.out.println(hash);
        
        boolean matches = encoder.matches("admin123", hash);
        System.out.println("Verification: " + matches);
    }
}
