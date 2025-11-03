package com.example.logistics.service;

import com.example.logistics.domain.User;
import com.example.logistics.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RegistrationServiceTest {

    @Autowired RegistrationService registrationService;
    @Autowired UserRepo userRepo;
    @Autowired PasswordEncoder encoder;

    @Test
    void registersClientAndCreatesHashedPassword() {
        String email = "test+" + System.nanoTime() + "@demo.com";

        User u = registrationService.registerClient(
                "Test User", email, "secret", "0888123456", "Sofia", "Center 1");

        var fromDb = userRepo.findByEmail(email).orElseThrow();
        assertThat(encoder.matches("secret", fromDb.getPassword())).isTrue();
        assertThat(fromDb.getRoles()).isNotEmpty();
    }
}