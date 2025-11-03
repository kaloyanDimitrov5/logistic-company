package com.example.logistics.service;

import com.example.logistics.domain.type.RoleType;
import com.example.logistics.domain.User;
import com.example.logistics.repo.RoleRepo;
import com.example.logistics.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserServiceTest {

    @Autowired UserRepo userRepo;
    @Autowired RoleRepo roleRepo;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void registerClientCreatesUserWithClientRoleAndEncodedPassword() {
        var roleSvc = new RoleService(roleRepo);
        var userSvc = new UserService(userRepo, roleSvc, encoder);

        User u = userSvc.registerClient("Test User","test+"+System.nanoTime()+"@mail.com","s3cr3t");

        var fromDb = userRepo.findById(u.getId()).orElseThrow();
        assertThat(fromDb.getRoles()).anyMatch(r -> r.getName() == RoleType.CLIENT);
        assertThat(encoder.matches("s3cr3t", fromDb.getPassword())).isTrue();
    }

    @Test
    void grantRoleAddsEmployeeRole() {
        var roleSvc = new RoleService(roleRepo);
        var userSvc = new UserService(userRepo, roleSvc, encoder);

        var u = new User();
        u.setEmail("user+"+System.nanoTime()+"@mail.com");
        u.setFullName("U");
        u.setPassword(encoder.encode("x"));
        u.setEnabled(true);
        u = userRepo.save(u);

        userSvc.grantRole(u.getId(), RoleType.EMPLOYEE);

        var fromDb = userRepo.findById(u.getId()).orElseThrow();
        assertThat(fromDb.getRoles()).anyMatch(r -> r.getName() == RoleType.EMPLOYEE);
    }
}