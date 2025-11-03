package com.example.logistics.repo;

import com.example.logistics.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepo extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    @Query("select s from Shipment s where s.registeredBy.user.id = :userId")
    List<Shipment> findAllRegisteredByEmployeeUserId(@Param("userId") Long userId);

    @Query("select s from Shipment s where s.status <> 'DELIVERED'")
    List<Shipment> findAllNotDelivered();

    @Query("select s from Shipment s where s.sender.id = :clientId")
    List<Shipment> findAllSentByClient(@Param("clientId") Long clientId);

    @Query("select s from Shipment s where s.recipient.id = :clientId")
    List<Shipment> findAllReceivedByClient(@Param("clientId") Long clientId);

    @Query("select coalesce(sum(s.price),0) from Shipment s where s.registeredAt between :from and :to")
    BigDecimal revenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}