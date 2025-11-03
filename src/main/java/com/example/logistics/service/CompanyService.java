package com.example.logistics.service;

import com.example.logistics.domain.Company;
import com.example.logistics.repo.CompanyRepo;
import com.example.logistics.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepo companyRepo;

    public Company create(String name, String vatNumber) {
        Company c = new Company();
        c.setName(name);
        c.setVatNumber(vatNumber);
        return companyRepo.save(c);
    }

    public Company get(Long id) {
        return companyRepo.findById(id).orElseThrow(() -> new NotFoundException("Company not found: " + id));
    }

    public List<Company> all() { return companyRepo.findAll(); }
}