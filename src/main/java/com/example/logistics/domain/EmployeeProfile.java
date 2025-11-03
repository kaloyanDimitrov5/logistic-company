package com.example.logistics.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "employee_profile")
@Getter
@Setter
public class EmployeeProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional=false) @JoinColumn(name="user_id", unique=true)
    private User user;

    @ManyToOne(optional=false) @JoinColumn(name="company_id")
    private Company company;

    @ManyToOne @JoinColumn(name="office_id")
    private Office office;

    private String position;
}
