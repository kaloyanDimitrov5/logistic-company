package com.example.logistics.service;

import com.example.logistics.domain.Company;
import com.example.logistics.domain.EmployeeProfile;
import com.example.logistics.domain.Office;
import com.example.logistics.domain.User;
import com.example.logistics.repo.EmployeeProfileRepo;
import com.example.logistics.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeProfileService {
    private final EmployeeProfileRepo employeeRepo;

    @Transactional
    public EmployeeProfile create(User user, Company company, Office office, String position) {
        EmployeeProfile ep = new EmployeeProfile();
        ep.setUser(user);
        ep.setCompany(company);
        ep.setOffice(office);
        ep.setPosition(position);
        return employeeRepo.save(ep);
    }

    public EmployeeProfile get(Long id) {
        return employeeRepo.findById(id).orElseThrow(() -> new NotFoundException("EmployeeProfile not found: " + id));
    }
}