package com.example.logistics.service;

import com.example.logistics.repo.ClientProfileRepo;
import com.example.logistics.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RegistrationServiceFullTest {

    @Autowired RegistrationService registrationService;
    @Autowired UserRepo userRepo;
    @Autowired ClientProfileRepo clientRepo;

    @Test
    void registerClientCreatesUserAndProfile() {
        String email = "reg+" + System.nanoTime() + "@mail.com";
        var u = registrationService.registerClient("Reg User", email, "pass", "0888", "Sofia", "Center 5");

        var user = userRepo.findByEmail(email).orElseThrow();
        var profile = clientRepo.findByUserId(user.getId()).orElse(null);

        assertThat(user.getId()).isNotNull();
        assertThat(profile).isNotNull();
        assertThat(profile.getUser().getId()).isEqualTo(user.getId());
    }
}