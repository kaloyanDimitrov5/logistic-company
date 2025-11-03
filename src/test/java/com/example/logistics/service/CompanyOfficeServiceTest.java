package com.example.logistics.service;

import com.example.logistics.repo.CompanyRepo;
import com.example.logistics.repo.OfficeRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CompanyOfficeServiceTest {

    @Autowired CompanyRepo companyRepo;
    @Autowired OfficeRepo officeRepo;

    @Test
    void createCompanyAndOffice() {
        var companyService = new CompanyService(companyRepo);
        var officeService  = new OfficeService(officeRepo);

        var c = companyService.create("ACME Logistics", "BG999");
        var o = officeService.create(c, "Sofia", "Bul. Bulgaria 1", "+35970012345");

        assertThat(companyService.all()).extracting("name").contains("ACME Logistics");
        assertThat(officeService.all()).extracting("city").contains("Sofia");
        assertThat(o.getCompany().getId()).isEqualTo(c.getId());
    }
}