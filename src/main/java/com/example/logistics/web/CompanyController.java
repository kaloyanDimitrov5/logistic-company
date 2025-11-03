package com.example.logistics.web;

import com.example.logistics.domain.Company;
import com.example.logistics.service.CompanyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/companies")
@PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("companies", companyService.all());
        return "companies/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("form", new CompanyForm());
        return "companies/form";
    }

    @PostMapping
    public String create(@ModelAttribute CompanyForm form) {
        companyService.create(form.getName(), form.getVatNumber());
        return "redirect:/companies";
    }

    @Data
    public static class CompanyForm {
        private String name;
        private String vatNumber;
    }
}