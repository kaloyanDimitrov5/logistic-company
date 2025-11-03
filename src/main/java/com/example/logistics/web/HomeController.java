package com.example.logistics.web;

import com.example.logistics.repo.ClientProfileRepo;
import com.example.logistics.repo.EmployeeProfileRepo;
import com.example.logistics.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final EmployeeProfileRepo employeeRepo;
    private final ClientProfileRepo clientRepo;
    private final UserService userService;

    @GetMapping("/")
    public String index(Model model, Authentication auth) {
        // For staff quick-filters
        model.addAttribute("employees", employeeRepo.findAll());
        model.addAttribute("clients", clientRepo.findAll());

        // If current user is a client, expose their clientId for “My Shipments”
        if (auth != null) {
            var user = userService.getByEmail(auth.getName());
            clientRepo.findByUserId(user.getId())
                    .ifPresent(cp -> model.addAttribute("myClientId", cp.getId()));
        }
        return "index";
    }
}