package com.example.logistics.service;

import com.example.logistics.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final UserService userService;
    private final ClientProfileService clientProfileService;

    @Transactional
    public User registerClient(String fullName, String email, String rawPassword,
                               String phone, String city, String address) {
        User u = userService.registerClient(fullName, email, rawPassword);
        clientProfileService.createForUser(u, phone, city, address);
        return u;
    }
}