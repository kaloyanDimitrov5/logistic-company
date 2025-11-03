package com.example.logistics.web;

import com.example.logistics.domain.ClientProfile;
import com.example.logistics.repo.ClientProfileRepo;
import com.example.logistics.service.RegistrationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/clients")
@PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
public class ClientsController {

    private final ClientProfileRepo clientRepo;
    private final RegistrationService registrationService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("clients", clientRepo.findAll());
        return "clients/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("form", new ClientForm());
        model.addAttribute("isEdit", false);
        return "clients/form";
    }

    @PostMapping
    public String create(@ModelAttribute("form") @Validated ClientForm f) {
        registrationService.registerClient(
                f.getFullName(), f.getEmail(), f.getPassword(),
                f.getPhone(), f.getCity(), f.getAddress()
        );
        return "redirect:/clients?created";
    }

    // EDIT
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ClientProfile c = clientRepo.findById(id).orElseThrow();
        ClientForm form = new ClientForm();
        form.setFullName(c.getUser().getFullName());
        form.setEmail(c.getUser().getEmail());
        form.setPhone(c.getPhone());
        form.setCity(c.getCity());
        form.setAddress(c.getAddress());
        model.addAttribute("form", form);
        model.addAttribute("clientId", id);
        model.addAttribute("isEdit", true);
        return "clients/form";
    }

    @PostMapping("/edit/{id}")
    public String editSave(@PathVariable Long id, @ModelAttribute("form") ClientForm f) {
        ClientProfile c = clientRepo.findById(id).orElseThrow();
        c.getUser().setFullName(f.getFullName());
        c.setPhone(f.getPhone());
        c.setCity(f.getCity());
        c.setAddress(f.getAddress());
        clientRepo.save(c);
        return "redirect:/clients?updated";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        clientRepo.deleteById(id);
        return "redirect:/clients?deleted";
    }

    @Data
    public static class ClientForm {
        private String fullName;
        private String email;
        private String password;
        private String phone;
        private String city;
        private String address;
    }
}