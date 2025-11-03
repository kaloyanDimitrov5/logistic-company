package com.example.logistics.domain;

import com.example.logistics.domain.type.DeliveryType;
import com.example.logistics.domain.type.ShipmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipment")
@Getter
@Setter
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true) private String trackingNumber;

    @ManyToOne(optional=false) @JoinColumn(name="sender_id")
    private ClientProfile sender;

    @ManyToOne(optional=false) @JoinColumn(name="recipient_id")
    private ClientProfile recipient;

    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal weightKg;

    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private ShipmentStatus status = ShipmentStatus.REGISTERED;

    @ManyToOne @JoinColumn(name="to_office_id")
    private Office toOffice; // when deliveryType=OFFICE

    // addresses snapshot
    @Column(nullable=false) private String fromCity;
    @Column(nullable=false) private String fromAddressLine;
    @Column(nullable=false) private String toCity;
    @Column(nullable=false) private String toAddressLine;

    @ManyToOne @JoinColumn(name="registered_by_employee_id")
    private EmployeeProfile registeredBy;

    private LocalDateTime registeredAt = LocalDateTime.now();
    private LocalDateTime deliveredAt;
}