package com.example.logistics.service;

import com.example.logistics.domain.*;
import com.example.logistics.domain.type.RoleType;
import com.example.logistics.repo.ClientProfileRepo;
import com.example.logistics.repo.EmployeeProfileRepo;
import com.example.logistics.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserService userService;
    private final EmployeeProfileService employeeProfileService;
    private final EmployeeProfileRepo employeeRepo;
    private final UserRepo userRepo;
    private final ClientProfileRepo clientProfileRepo;
    private final CompanyService companyService;
    private final OfficeService officeService;


    @Transactional
    public EmployeeProfile createEmployee(Long userId, Company company, Office office, String position) {
        userService.grantRole(userId, RoleType.EMPLOYEE);
        User u = userService.getById(userId);
        return employeeProfileService.create(u, company, office, position);
    }

    @Transactional
    public void ensureClientProfile(Long userId, String phone, String city, String addr) {
        User u = userRepo.findById(userId).orElseThrow();
        clientProfileRepo.findByUserId(userId).ifPresent(cp -> { throw new IllegalStateException("Client profile exists"); });
        ClientProfile cp = new ClientProfile();
        cp.setUser(u);
        cp.setPhone(phone);
        cp.setCity(city);
        cp.setAddress(addr);
        clientProfileRepo.save(cp);
    }

    @Transactional
    public void removeClientProfile(Long userId) {
        clientProfileRepo.findByUserId(userId).ifPresent(clientProfileRepo::delete);
    }

    @Transactional
    public void removeEmployeeProfile(Long userId) {
        employeeRepo.findByUserId(userId).ifPresent(employeeRepo::delete);
    }

    @Transactional
    public void updateEmployeeByUserId(Long userId, Long companyId, Long officeId, String position) {
        var emp = employeeRepo.findByUserId(userId).orElseThrow();
        emp.setCompany(companyService.get(companyId));
        emp.setOffice(officeService.get(officeId));
        emp.setPosition(position);
        employeeRepo.save(emp);
    }

    @Transactional
    public void updateClientProfileByUserId(Long userId, String phone, String city, String addr) {
        var cp = clientProfileRepo.findByUserId(userId).orElseThrow();
        cp.setPhone(phone);
        cp.setCity(city);
        cp.setAddress(addr);
        clientProfileRepo.save(cp);
    }

    public boolean hasEmployeeProfile(Long userId) {
        return employeeRepo.findByUserId(userId).isPresent();
    }

    public boolean hasClientProfile(Long userId) {
        return clientProfileRepo.findByUserId(userId).isPresent();
    }

    public EmployeeProfile getEmployee(Long employeeId) {
        return employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
    }

    public EmployeeProfile updateEmployee(Long employeeId, Company company, Office office, String position) {
        var emp = getEmployee(employeeId);
        emp.setCompany(company);
        emp.setOffice(office);
        emp.setPosition(position);
        return employeeRepo.save(emp);
    }
}