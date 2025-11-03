package com.example.logistics.repo;

import com.example.logistics.domain.Office;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfficeRepo extends JpaRepository<Office, Long> {
    List<Office> findByCity(String city);
    Optional<Office> findFirstByCityIgnoreCase(String city);
}