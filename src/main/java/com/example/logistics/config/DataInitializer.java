package com.example.logistics.config;

import com.example.logistics.domain.*;
import com.example.logistics.domain.type.DeliveryType;
import com.example.logistics.domain.type.RoleType;
import com.example.logistics.domain.type.ShipmentStatus;
import com.example.logistics.repo.*;
import com.example.logistics.service.RegistrationService;
import com.example.logistics.service.RoleService;
import com.example.logistics.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleService roleService;
    private final UserService userService;
    private final RegistrationService registrationService;

    private final UserRepo userRepo;
    private final CompanyRepo companyRepo;
    private final OfficeRepo officeRepo;
    private final ClientProfileRepo clientProfileRepo;
    private final EmployeeProfileRepo employeeProfileRepo;
    private final ShipmentRepo shipmentRepo;

    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1) Ensure roles
        roleService.getOrCreate(RoleType.ADMIN);
        roleService.getOrCreate(RoleType.EMPLOYEE);
        roleService.getOrCreate(RoleType.CLIENT);

        // 2) Admin user (admin@demo.com / admin)
        User admin = userRepo.findByEmail("admin@demo.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("admin@demo.com");
            u.setFullName("Admin User");
            u.setPassword(encoder.encode("admin"));
            u.setEnabled(true);
            u.getRoles().add(roleService.getOrCreate(RoleType.ADMIN));
            u.getRoles().add(roleService.getOrCreate(RoleType.EMPLOYEE));
            return userRepo.save(u);
        });

        // 3) Company
        Company company = companyRepo.findAll().stream().findFirst().orElseGet(() -> {
            Company c = new Company();
            c.setName("Demo Logistics");
            c.setVatNumber("BG123456789");
            return companyRepo.save(c);
        });

        // 4) Offices (Sofia, Plovdiv)
        Office sofia = officeRepo.findFirstByCityIgnoreCase("Sofia")
                .orElseGet(() -> {
                    Office o = new Office();
                    o.setCompany(company);
                    o.setCity("Sofia");
                    o.setAddressLine("Tsarigradsko 100");
                    o.setPhone("+35970012345");
                    return officeRepo.save(o);
                });

        Office plovdiv = officeRepo.findFirstByCityIgnoreCase("Plovdiv")
                .orElseGet(() -> {
                    Office o = new Office();
                    o.setCompany(company);
                    o.setCity("Plovdiv");
                    o.setAddressLine("Main St 5");
                    o.setPhone("+35970054321");
                    return officeRepo.save(o);
                });

        // 5) Clients (register only if missing)
        if (userRepo.findByEmail("alice@demo.com").isEmpty()) {
            registrationService.registerClient("Alice Client", "alice@demo.com", "alice",
                    "+359888111222", "Sofia", "Vitosha 1");
        }
        if (userRepo.findByEmail("bob@demo.com").isEmpty()) {
            registrationService.registerClient("Bob Client", "bob@demo.com", "bob",
                    "+359888333444", "Plovdiv", "Knyaz Alexander 7");
        }
        if (userRepo.findByEmail("carol@demo.com").isEmpty()) {
            registrationService.registerClient("Carol Client", "carol@demo.com", "carol",
                    "+359888555666", "Varna", "Primorski 12");
        }

        // Client profiles (we need these for shipments)
        ClientProfile alice = clientProfileRepo.findByUserEmail("alice@demo.com")
                .orElseThrow(() -> new IllegalStateException("Client Alice missing"));
        ClientProfile bob = clientProfileRepo.findByUserEmail("bob@demo.com")
                .orElseThrow(() -> new IllegalStateException("Client Bob missing"));
        ClientProfile carol = clientProfileRepo.findByUserEmail("carol@demo.com")
                .orElseThrow(() -> new IllegalStateException("Client Carol missing"));

        // 6) Make sure admin has an EmployeeProfile to appear as 'registeredBy'
        EmployeeProfile adminEmp = employeeProfileRepo.findByUserId(admin.getId())
                .orElseGet(() -> {
                    EmployeeProfile ep = new EmployeeProfile();
                    ep.setUser(admin);
                    ep.setCompany(company);
                    ep.setOffice(sofia);
                    ep.setPosition("Manager");
                    return employeeProfileRepo.save(ep);
                });

        // 7) Seed shipments with all required non-null fields
        if (shipmentRepo.count() == 0) {
            // TRK-001: Alice -> Bob, TO_OFFICE (Sofia)
            Shipment s1 = new Shipment();
            s1.setTrackingNumber("TRK-001");
            s1.setSender(alice);
            s1.setRecipient(bob);
            s1.setWeightKg(new BigDecimal("1.80"));
            s1.setPrice(new BigDecimal("8.90"));
            s1.setDeliveryType(DeliveryType.TO_OFFICE);
            s1.setStatus(ShipmentStatus.REGISTERED);
            // snapshot addresses
            s1.setFromCity(alice.getCity());
            s1.setFromAddressLine(alice.getAddress());
            s1.setToCity(bob.getCity());
            s1.setToAddressLine(bob.getAddress());
            s1.setToOffice(sofia);
            s1.setRegisteredBy(adminEmp);
            s1.setRegisteredAt(LocalDateTime.now().minusDays(3));
            shipmentRepo.save(s1);

            // TRK-002: Bob -> Carol, TO_ADDRESS (no office)
            Shipment s2 = new Shipment();
            s2.setTrackingNumber("TRK-002");
            s2.setSender(bob);
            s2.setRecipient(carol);
            s2.setWeightKg(new BigDecimal("3.20"));
            s2.setPrice(new BigDecimal("14.50"));
            s2.setDeliveryType(DeliveryType.TO_ADDRESS);
            s2.setStatus(ShipmentStatus.IN_TRANSIT);
            s2.setFromCity(bob.getCity());
            s2.setFromAddressLine(bob.getAddress());
            s2.setToCity(carol.getCity());
            s2.setToAddressLine(carol.getAddress());
            s2.setRegisteredBy(adminEmp);
            s2.setRegisteredAt(LocalDateTime.now().minusDays(2));
            shipmentRepo.save(s2);

            // TRK-003: Carol -> Alice, TO_OFFICE (Plovdiv) and already delivered
            Shipment s3 = new Shipment();
            s3.setTrackingNumber("TRK-003");
            s3.setSender(carol);
            s3.setRecipient(alice);
            s3.setWeightKg(new BigDecimal("0.90"));
            s3.setPrice(new BigDecimal("6.40"));
            s3.setDeliveryType(DeliveryType.TO_OFFICE);
            s3.setStatus(ShipmentStatus.DELIVERED);
            s3.setFromCity(carol.getCity());
            s3.setFromAddressLine(carol.getAddress());
            s3.setToCity(alice.getCity());
            s3.setToAddressLine(alice.getAddress());
            s3.setToOffice(plovdiv);
            s3.setRegisteredBy(adminEmp);
            s3.setRegisteredAt(LocalDateTime.now().minusDays(5));
            s3.setDeliveredAt(LocalDateTime.now().minusDays(1));
            shipmentRepo.save(s3);
        }
    }
}