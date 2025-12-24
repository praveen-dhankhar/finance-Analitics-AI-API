package com.financeapp.testsupport;

import com.financeapp.entity.User;
import com.financeapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public final class TestDataUtil {

    private TestDataUtil() {}

    public static String generateUniqueUsername(String base) {
        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static User seedUser(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                String baseName) {
        String username = generateUniqueUsername(baseName);
        String email = username + "@example.com";
        return seedUserWithFixed(userRepository, passwordEncoder, username, email);
    }

    public static User seedUserWithEmail(UserRepository userRepository,
                                         PasswordEncoder passwordEncoder,
                                         String baseName,
                                         String baseEmailDomain) {
        String username = generateUniqueUsername(baseName);
        String email = username + "@" + baseEmailDomain;
        return seedUserWithFixed(userRepository, passwordEncoder, username, email);
    }

    public static User seedOrGetUser(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     String username,
                                     String email) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            return existing.get();
        }
        return seedUserWithFixed(userRepository, passwordEncoder, username, email);
    }

    private static User seedUserWithFixed(UserRepository userRepository,
                                          PasswordEncoder passwordEncoder,
                                          String username,
                                          String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password@123"));
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return userRepository.save(user);
    }
}


