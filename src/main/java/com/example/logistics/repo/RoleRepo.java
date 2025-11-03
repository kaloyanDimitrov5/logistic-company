package com.example.logistics.repo;

import com.example.logistics.domain.Role;
import com.example.logistics.domain.type.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepo extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}