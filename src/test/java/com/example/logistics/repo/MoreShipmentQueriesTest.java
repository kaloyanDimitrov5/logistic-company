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
class MoreShipmentQueriesTest {

    @Autowired ShipmentRepo shipmentRepo;
    @Autowired UserRepo userRepo;
    @Autowired ClientProfileRepo clientRepo;

    private ClientProfile client(String n) {
        var u = new User(); u.setEmail(UUID.randomUUID()+"@mail.com"); u.setFullName(n); u.setPassword("x"); u.setEnabled(true);
        u = userRepo.save(u);
        var c = new ClientProfile(); c.setUser(u); c.setCity("Sofia"); c.setAddress("A"); return clientRepo.save(c);
    }

    @Test
    void notDeliveredAndRevenue() {
        var a = client("A"); var b = client("B");

        var s1 = new Shipment();
        s1.setTrackingNumber("S1");
        s1.setSender(a); s1.setRecipient(b);
        s1.setWeightKg(new BigDecimal("1.00"));
        s1.setPrice(new BigDecimal("5.00"));
        s1.setDeliveryType(DeliveryType.TO_ADDRESS);
        s1.setStatus(ShipmentStatus.REGISTERED);
        s1.setFromCity("Sofia"); s1.setFromAddressLine("A");
        s1.setToCity("Plovdiv"); s1.setToAddressLine("B");
        s1.setRegisteredAt(LocalDateTime.now().minusDays(3));
        shipmentRepo.save(s1);

        var s2 = new Shipment();
        s2.setTrackingNumber("S2");
        s2.setSender(a); s2.setRecipient(b);
        s2.setWeightKg(new BigDecimal("2.00"));
        s2.setPrice(new BigDecimal("8.00"));
        s2.setDeliveryType(DeliveryType.TO_ADDRESS);
        s2.setStatus(ShipmentStatus.DELIVERED);
        s2.setFromCity("Sofia"); s2.setFromAddressLine("A");
        s2.setToCity("Varna"); s2.setToAddressLine("B");
        s2.setRegisteredAt(LocalDateTime.now().minusDays(1));
        s2.setDeliveredAt(LocalDateTime.now());
        shipmentRepo.save(s2);

        assertThat(shipmentRepo.findAllNotDelivered()).extracting("trackingNumber").contains("S1").doesNotContain("S2");

        var from = LocalDateTime.now().minusDays(4);
        var to   = LocalDateTime.now().plusDays(1);
        assertThat(shipmentRepo.revenueBetween(from, to)).isEqualByComparingTo("13.00");
    }
}