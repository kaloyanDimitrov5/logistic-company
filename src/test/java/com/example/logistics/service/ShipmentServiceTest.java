package com.example.logistics.service;

import com.example.logistics.domain.*;
import com.example.logistics.domain.type.DeliveryType;
import com.example.logistics.repo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShipmentServiceTest {

    @Autowired ShipmentRepo shipmentRepo;
    @Autowired UserRepo userRepo;
    @Autowired ClientProfileRepo clientRepo;
    @Autowired CompanyRepo companyRepo;
    @Autowired OfficeRepo officeRepo;

    private ClientProfile newClient(String name, String email) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPassword("x");
        u.setEnabled(true);
        u = userRepo.save(u);

        ClientProfile cp = new ClientProfile();
        cp.setUser(u);
        cp.setCity("Sofia");
        cp.setAddress("Adr 1");
        return clientRepo.save(cp);
    }

    @Test
    void createShipmentCalculatesPriceAndPersists() {
        ShipmentService service = new ShipmentService(shipmentRepo, officeRepo);

        var sender = newClient("Alice", "alice+"+System.nanoTime()+"@demo.com");
        var recipient = newClient("Bob", "bob+"+System.nanoTime()+"@demo.com");

        Company c = new Company(); c.setName("Demo"); companyRepo.save(c);
        Office o = new Office(); o.setCompany(c); o.setCity("Sofia"); o.setAddressLine("Office 1"); officeRepo.save(o);

        var created = service.createShipment(
                sender, recipient,
                new BigDecimal("2.00"),
                DeliveryType.TO_OFFICE,
                "Sofia", "A 1",
                "Plovdiv", "B 2",
                o,
                null
        );

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTrackingNumber()).isNotBlank();
        // OFFICE price should be discounted (8.00 for 2kg ADDRESS -> 20% off => 6.40)
        assertThat(created.getPrice()).isEqualByComparingTo("6.40");
    }
}