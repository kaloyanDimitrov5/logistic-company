package com.example.logistics.web;

import com.example.logistics.domain.Company;
import com.example.logistics.service.CompanyService;
import com.example.logistics.service.OfficeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/offices")
@PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
public class OfficeController {

    private final OfficeService officeService;
    private final CompanyService companyService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("offices", officeService.all());
        model.addAttribute("allOffices", true);
        return "offices/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("form", new OfficeForm());
        model.addAttribute("companies", companyService.all());
        return "offices/form";
    }

    @PostMapping
    public String create(@ModelAttribute OfficeForm f) {
        Company c = companyService.get(f.getCompanyId());
        officeService.create(c, f.getCity(), f.getAddressLine(), f.getPhone());
        return "redirect:/offices";
    }

    @Data
    public static class OfficeForm {
        private Long companyId;
        private String city;
        private String addressLine;
        private String phone;
    }
}