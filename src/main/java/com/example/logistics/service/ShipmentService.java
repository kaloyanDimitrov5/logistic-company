package com.example.logistics.service;

import com.example.logistics.domain.*;
import com.example.logistics.domain.type.DeliveryType;
import com.example.logistics.domain.type.ShipmentStatus;
import com.example.logistics.repo.OfficeRepo;
import com.example.logistics.repo.ShipmentRepo;
import com.example.logistics.util.NotFoundException;
import com.example.logistics.util.PriceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepo shipmentRepo;
    private final OfficeRepo officeRepo;

    @Transactional
    public Shipment createShipment(
            ClientProfile sender,
            ClientProfile recipient,
            BigDecimal weightKg,
            DeliveryType type,
            String fromCity, String fromAddress,
            String toCity, String toAddress,
            Office toOffice,
            EmployeeProfile registeredBy
    ) {
        Shipment s = new Shipment();
        s.setTrackingNumber(UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        s.setSender(sender);
        s.setRecipient(recipient);
        s.setWeightKg(weightKg);
        s.setPrice(PriceCalculator.calculate(weightKg, type));
        s.setDeliveryType(type);
        s.setFromCity(fromCity);
        s.setFromAddressLine(fromAddress);
        s.setToCity(toCity);
        s.setToAddressLine(toAddress);
        s.setToOffice(toOffice);
        s.setRegisteredBy(registeredBy);
        s.setStatus(ShipmentStatus.REGISTERED);
        s.setRegisteredAt(LocalDateTime.now());
        return shipmentRepo.save(s);
    }

    public Shipment getByTracking(String trackingNumber) {
        return shipmentRepo.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new NotFoundException("Shipment not found: " + trackingNumber));
    }

    public List<Shipment> all() { return shipmentRepo.findAll(); }

    public List<Shipment> registeredByEmployeeUser(Long userId) {
        return shipmentRepo.findAllRegisteredByEmployeeUserId(userId);
    }

    public List<Shipment> notDelivered() {
        return shipmentRepo.findAllNotDelivered();
    }

    public List<Shipment> sentByClient(Long clientId) {
        return shipmentRepo.findAllSentByClient(clientId);
    }

    public List<Shipment> receivedByClient(Long clientId) {
        return shipmentRepo.findAllReceivedByClient(clientId);
    }

    public java.math.BigDecimal revenueBetween(LocalDateTime from, LocalDateTime to) {
        return shipmentRepo.revenueBetween(from, to);
    }

    @Transactional
    public Shipment markInTransit(Long id) {
        Shipment s = shipmentRepo.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found: " + id));
        s.setStatus(ShipmentStatus.IN_TRANSIT);
        return s;
    }

    @Transactional
    public Shipment markDelivered(Long id) {
        Shipment s = shipmentRepo.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found: " + id));
        s.setStatus(ShipmentStatus.DELIVERED);
        s.setDeliveredAt(LocalDateTime.now());
        return s;
    }

    @Transactional
    public void updateDetails(Long id, BigDecimal weight, DeliveryType type,
                              String fromCity, String fromAddress,
                              String toCity, String toAddress,
                              Long toOfficeId, ShipmentStatus status) {

        Shipment s = shipmentRepo.findById(id).orElseThrow();

        s.setWeightKg(weight);
        s.setDeliveryType(type);
        s.setFromCity(fromCity);
        s.setFromAddressLine(fromAddress);
        s.setToCity(toCity);
        s.setToAddressLine(toAddress);
        s.setStatus(status);

        if (type == DeliveryType.TO_OFFICE && toOfficeId != null) {
            s.setToOffice(officeRepo.findById(toOfficeId).orElse(null));
        } else {
            s.setToOffice(null);
        }
    }

    public Optional<Shipment> findById(Long id) {
        return shipmentRepo.findById(id);
    }

    public void update(Long id, Shipment formShipment) {
        Shipment existing = shipmentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));
        existing.setStatus(formShipment.getStatus());
        existing.setDeliveryType(formShipment.getDeliveryType());
        existing.setWeightKg(formShipment.getWeightKg());
        existing.setToCity(formShipment.getToCity());
        existing.setToAddressLine(formShipment.getToAddressLine());
        shipmentRepo.save(existing);
    }

    public void delete(Long id) {
        shipmentRepo.deleteById(id);
    }
}