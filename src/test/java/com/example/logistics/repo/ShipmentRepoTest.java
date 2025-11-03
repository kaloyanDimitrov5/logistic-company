package com.example.logistics.repo;

import com.example.logistics.domain.*;
import com.example.logistics.domain.type.DeliveryType;
import com.example.logistics.domain.type.ShipmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShipmentRepoTest {

    @Autowired UserRepo userRepo;
    @Autowired ClientProfileRepo clientRepo;
    @Autowired CompanyRepo companyRepo;
    @Autowired OfficeRepo officeRepo;
    @Autowired ShipmentRepo shipmentRepo;

    private ClientProfile newClient(String fullName, String email) {
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPassword("x");
        u.setEnabled(true);
        u = userRepo.save(u);

        ClientProfile cp = new ClientProfile();
        cp.setUser(u);
        cp.setPhone("+359");
        cp.setCity("Sofia");
        cp.setAddress("Center 1");
        return clientRepo.save(cp);
    }

    @Test
    void queriesForSenderRecipientAndNotDeliveredWork() {
        var sender = newClient("Alice", UUID.randomUUID()+"@demo.com");
        var recipient = newClient("Bob", UUID.randomUUID()+"@demo.com");

        Company c = new Company(); c.setName("Demo Co"); c.setVatNumber("BG123"); c = companyRepo.save(c);
        Office o = new Office(); o.setCompany(c); o.setCity("Sofia"); o.setAddressLine("Address 1"); officeRepo.save(o);

        Shipment s = new Shipment();
        s.setTrackingNumber("TRK-" + System.nanoTime());
        s.setSender(sender);
        s.setRecipient(recipient);
        s.setWeightKg(new BigDecimal("2.00"));
        s.setPrice(new BigDecimal("8.00"));
        s.setDeliveryType(DeliveryType.TO_ADDRESS);
        s.setStatus(ShipmentStatus.REGISTERED);
        s.setFromCity("Sofia"); s.setFromAddressLine("A");
        s.setToCity("Plovdiv"); s.setToAddressLine("B");
        s.setRegisteredAt(LocalDateTime.now());
        shipmentRepo.save(s);

        assertThat(shipmentRepo.findAllSentByClient(sender.getId())).hasSize(1);
        assertThat(shipmentRepo.findAllReceivedByClient(recipient.getId())).hasSize(1);
        assertThat(shipmentRepo.findAllNotDelivered()).extracting("status")
                .containsExactly(ShipmentStatus.REGISTERED);
    }

    @Test
    void revenueBetweenSumsPricesInPeriod() {
        var s1 = new Shipment();
        s1.setTrackingNumber("REV-" + System.nanoTime());
        s1.setSender(newClient("S1", UUID.randomUUID()+"@demo.com"));
        s1.setRecipient(newClient("R1", UUID.randomUUID()+"@demo.com"));
        s1.setWeightKg(new BigDecimal("1.00"));
        s1.setPrice(new BigDecimal("5.00"));
        s1.setDeliveryType(DeliveryType.TO_ADDRESS);
        s1.setStatus(ShipmentStatus.DELIVERED);
        s1.setFromCity("Sofia"); s1.setFromAddressLine("A");
        s1.setToCity("Sofia"); s1.setToAddressLine("B");
        s1.setRegisteredAt(LocalDateTime.now().minusDays(1));
        shipmentRepo.save(s1);

        var from = LocalDateTime.now().minusDays(2);
        var to   = LocalDateTime.now().plusDays(1);
        assertThat(shipmentRepo.revenueBetween(from, to)).isEqualByComparingTo("5.00");
    }
}