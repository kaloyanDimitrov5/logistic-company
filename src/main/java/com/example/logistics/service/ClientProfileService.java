package com.example.logistics.service;

import com.example.logistics.domain.ClientProfile;
import com.example.logistics.domain.User;
import com.example.logistics.repo.ClientProfileRepo;
import com.example.logistics.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientProfileService {
    private final ClientProfileRepo clientRepo;

    @Transactional
    public ClientProfile createForUser(User user, String phone, String city, String address) {
        ClientProfile p = new ClientProfile();
        p.setUser(user);
        p.setPhone(phone);
        p.setCity(city);
        p.setAddress(address);
        return clientRepo.save(p);
    }

    public ClientProfile get(Long id) {
        return clientRepo.findById(id).orElseThrow(() -> new NotFoundException("ClientProfile not found: " + id));
    }
}