
package com.example.expo.Config;

import com.example.expo.Model.User;
import com.example.expo.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PHONE = "0500000000";
    private static final String ADMIN_PASSWORD = "ChangeThisPassword123!";

    @Override
    public void run(String... args) {

        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            System.out.println("Admin account already exists.");
            return;
        }

        if (userRepository.existsByUsername(ADMIN_USERNAME)
                || userRepository.existsByPhone(ADMIN_PHONE)) {
            throw new IllegalStateException(
                    "Admin username or phone already exists."
            );
        }

        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setEmail(ADMIN_EMAIL);
        admin.setPhone(ADMIN_PHONE);
        admin.setPassword(ADMIN_PASSWORD);
        admin.setRole("ADMIN");
        admin.setStatus("ACTIVE");

        userRepository.save(admin);

        System.out.println("Initial admin account created successfully.");
    }
}
