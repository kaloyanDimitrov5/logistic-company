package com.example.logistics.repo;

import com.example.logistics.domain.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientProfileRepo extends JpaRepository<ClientProfile, Long> {
    Optional<ClientProfile> findByUserId(Long userId);
    Optional<ClientProfile> findByUserEmail(String email);
}