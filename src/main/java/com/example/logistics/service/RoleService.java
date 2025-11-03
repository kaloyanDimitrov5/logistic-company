package com.example.logistics.service;

import com.example.logistics.domain.Role;
import com.example.logistics.domain.type.RoleType;
import com.example.logistics.repo.RoleRepo;
import com.example.logistics.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepo roleRepo;

    public Role getOrCreate(RoleType type) {
        return roleRepo.findByName(type).orElseGet(() -> {
            Role r = new Role();
            r.setName(type);
            return roleRepo.save(r);
        });
    }

    public Role get(RoleType type) {
        return roleRepo.findByName(type)
                .orElseThrow(() -> new NotFoundException("Role not found: " + type));
    }

    /** Optional helper: list all roles. */
    public List<Role> getAll() {
        return roleRepo.findAll();
    }

    /** Optional helper: check if role exists. */
    public boolean exists(RoleType type) {
        return roleRepo.findByName(type).isPresent();
    }

    /** Optional helper: ensure all enum roles exist in DB. */
    public void ensureAllRolesExist() {
        for (RoleType rt : RoleType.values()) {
            getOrCreate(rt);
        }
    }
}