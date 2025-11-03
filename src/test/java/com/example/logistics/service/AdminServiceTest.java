package com.example.logistics.service;

import com.example.logistics.domain.*;
import com.example.logistics.domain.type.RoleType;
import com.example.logistics.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AdminServiceTest {

    @Autowired UserRepo userRepo;
    @Autowired RoleRepo roleRepo;
    @Autowired EmployeeProfileRepo employeeRepo;
    @Autowired ClientProfileRepo clientProfileRepo;
    @Autowired CompanyRepo companyRepo;
    @Autowired OfficeRepo officeRepo;

    // services under test
    RoleService roleService;
    UserService userService;
    EmployeeProfileService employeeProfileService;
    AdminService adminService;
    CompanyService companyService;
    OfficeService officeService;

    @BeforeEach
    void setup() {
        roleService = new RoleService(roleRepo);
        userService = new UserService(userRepo, roleService, new BCryptPasswordEncoder());
        employeeProfileService = new EmployeeProfileService(employeeRepo);
        adminService = new AdminService(userService, employeeProfileService, employeeRepo, userRepo, clientProfileRepo, companyService, officeService);
    }

    private User newUser(String email) {
        User u = new User();
        u.setEmail(email);
        u.setFullName("Test User");
        u.setPassword(new BCryptPasswordEncoder().encode("x"));
        u.setEnabled(true);
        return userRepo.save(u);
    }

    private Company newCompany(String name) {
        Company c = new Company();
        c.setName(name);
        c.setVatNumber("BG" + System.nanoTime());
        return companyRepo.save(c);
    }

    private Office newOffice(Company c, String city, String addr) {
        Office o = new Office();
        o.setCompany(c);
        o.setCity(city);
        o.setAddressLine(addr);
        return officeRepo.save(o);
    }

    @Test
    void promoteCreatesEmployeeProfileAndRole() {
        User u = newUser("promote+" + System.nanoTime() + "@mail.com");
        Company c = newCompany("DemoCo");
        Office o = newOffice(c, "Sofia", "Center 1");

        EmployeeProfile profile = adminService.createEmployee(u.getId(), c, o, "Courier");

        assertThat(profile.getId()).isNotNull();
        User reloaded = userRepo.findById(u.getId()).orElseThrow();
        assertThat(reloaded.getRoles())
                .anySatisfy(r -> assertThat(r.getName()).isEqualTo(RoleType.EMPLOYEE));
        assertThat(employeeRepo.findByUserId(u.getId())).isPresent();
    }

    @Test
    void updateEmployeeUpdatesCompanyOfficeAndPosition() {
        // arrange
        User u = newUser("emp+" + System.nanoTime() + "@mail.com");
        Company c1 = newCompany("Co1");
        Company c2 = newCompany("Co2");
        Office o1 = newOffice(c1, "Varna", "Addr 1");
        Office o2 = newOffice(c2, "Plovdiv", "Addr 2");
        EmployeeProfile created = adminService.createEmployee(u.getId(), c1, o1, "Courier");

        // act
        EmployeeProfile updated = adminService.updateEmployee(created.getId(), c2, o2, "Dispatcher");

        // assert
        assertThat(updated.getCompany().getId()).isEqualTo(c2.getId());
        assertThat(updated.getOffice().getId()).isEqualTo(o2.getId());
        assertThat(updated.getPosition()).isEqualTo("Dispatcher");
    }

    @Test
    void ensureClientProfileCreatesProfile() {
        User u = newUser("client+" + System.nanoTime() + "@mail.com");

        adminService.ensureClientProfile(u.getId(), "0888 000 111", "Sofia", "Main 5");

        Optional<ClientProfile> cp = clientProfileRepo.findByUserId(u.getId());
        assertThat(cp).isPresent();
        assertThat(cp.get().getPhone()).isEqualTo("0888 000 111");
        assertThat(cp.get().getCity()).isEqualTo("Sofia");
        assertThat(cp.get().getAddress()).isEqualTo("Main 5");
    }

    @Test
    void removeClientProfileDeletesIt() {
        User u = newUser("client2+" + System.nanoTime() + "@mail.com");
        adminService.ensureClientProfile(u.getId(), "123", "Burgas", "Somewhere");

        adminService.removeClientProfile(u.getId());

        assertThat(clientProfileRepo.findByUserId(u.getId())).isEmpty();
    }

    @Test
    void removeEmployeeProfileDeletesIt() {
        User u = newUser("emp2+" + System.nanoTime() + "@mail.com");
        Company c = newCompany("Co3");
        Office o = newOffice(c, "Ruse", "Addr X");
        adminService.createEmployee(u.getId(), c, o, "Courier");
        assertThat(employeeRepo.findByUserId(u.getId())).isPresent();

        adminService.removeEmployeeProfile(u.getId());

        assertThat(employeeRepo.findByUserId(u.getId())).isEmpty();
    }
}