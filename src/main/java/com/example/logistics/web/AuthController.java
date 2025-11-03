package com.example.logistics.web;

import com.example.logistics.service.RegistrationService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final RegistrationService registrationService;

    @GetMapping("/login")
    public String loginPage() { return "auth/login"; }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute("form") @Validated RegisterForm f,
                             org.springframework.validation.BindingResult br,
                             org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        if (br.hasErrors()) return "auth/register";
        try {
            registrationService.registerClient(
                    f.getFullName(), f.getEmail(), f.getPassword(),
                    f.getPhone(), f.getCity(), f.getAddress()
            );
            ra.addFlashAttribute("registered", true);
            return "redirect:/auth/login";
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            br.rejectValue("email", "duplicate", "Email is already in use");
            return "auth/register";
        }
    }

    @Data
    public static class RegisterForm {
        @NotBlank
        private String fullName;
        @Email
        @NotBlank private String email;
        @Size(min = 4, max = 100) private String password;
        private String phone;
        private String city;
        private String address;
    }
}