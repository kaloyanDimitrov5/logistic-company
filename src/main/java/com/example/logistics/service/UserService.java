package com.example.logistics.service;

import com.example.logistics.domain.Role;
import com.example.logistics.domain.User;
import com.example.logistics.domain.type.RoleType;
import com.example.logistics.repo.UserRepo;
import com.example.logistics.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public User getById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    public User getByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    @Transactional
    public User registerClient(String fullName, String email, String rawPassword) {
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setEnabled(true);

        Role client = roleService.getOrCreate(RoleType.CLIENT);
        u.getRoles().add(client);

        return userRepo.save(u);
    }

    @Transactional
    public User grantRole(Long userId, RoleType roleType) {
        User u = getById(userId);
        Role role = roleService.getOrCreate(roleType);
        boolean hasIt = u.getRoles().stream().anyMatch(r -> r.getName() == roleType);
        if (!hasIt) {
            u.getRoles().add(role);
            userRepo.save(u);
        }
        return u;
    }

    @Transactional
    public User grantRoles(Long userId, Iterable<RoleType> roles) {
        for (RoleType rt : roles) grantRole(userId, rt);
        return getById(userId);
    }

    @Transactional
    public User revokeRole(Long userId, RoleType roleType) {
        User u = getById(userId);
        u.getRoles().removeIf(r -> r.getName() == roleType);
        return userRepo.save(u);
    }
    @Transactional
    public User replaceRoles(Long userId, Iterable<RoleType> desired) {
        User u = getById(userId);
        u.getRoles().clear();
        for (RoleType rt : desired) {
            Role r = roleService.getOrCreate(rt);
            u.getRoles().add(r);
        }
        return userRepo.save(u);
    }

    @Transactional
    public void updatePassword(Long userId, String rawPassword) {
        User u = getById(userId);
        u.setPassword(passwordEncoder.encode(rawPassword));
        userRepo.save(u);
    }

    @Transactional
    public void updateBasics(Long id, String fullName, String email, boolean enabled) {
        User u = getById(id);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setEnabled(enabled);
        userRepo.save(u);
    }

    @Transactional
    public void clearRoles(Long id) {
        User u = getById(id);
        u.getRoles().clear();
        userRepo.save(u);
    }

    @Transactional
    public User createUser(String fullName, String email, String rawPassword, boolean enabled) {
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setEnabled(enabled);
        return userRepo.save(u);
    }

    public List<User> all() { return userRepo.findAll(); }

    public List<User> allNonEmployees() {
        return userRepo.findAll().stream()
                .filter(u -> u.getRoles().stream().noneMatch(r -> r.getName() == RoleType.EMPLOYEE))
                .collect(Collectors.toList());
    }
}