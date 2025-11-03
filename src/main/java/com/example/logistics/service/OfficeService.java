package com.example.logistics.service;

import com.example.logistics.domain.Company;
import com.example.logistics.domain.Office;
import com.example.logistics.repo.OfficeRepo;
import com.example.logistics.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfficeService {
    private final OfficeRepo officeRepo;

    public Office create(Company company, String city, String addressLine, String phone) {
        Office o = new Office();
        o.setCompany(company);
        o.setCity(city);
        o.setAddressLine(addressLine);
        o.setPhone(phone);
        return officeRepo.save(o);
    }

    public Office get(Long id) {
        return officeRepo.findById(id).orElseThrow(() -> new NotFoundException("Office not found: " + id));
    }

    public List<Office> byCity(String city) { return officeRepo.findByCity(city); }

    public List<Office> all() { return officeRepo.findAll(); }
}