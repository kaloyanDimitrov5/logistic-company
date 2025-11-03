package com.example.logistics.web;

import com.example.logistics.repo.ClientProfileRepo;
import com.example.logistics.repo.EmployeeProfileRepo;
import com.example.logistics.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
@PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
public class ReportsController {

    private final EmployeeProfileRepo employeeRepo;
    private final ClientProfileRepo clientRepo;
    private final ShipmentService shipmentService;

    // 1) Employees
    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("employees", employeeRepo.findAll());
        return "reports/employees";
    }

    // 2) Clients
    @GetMapping("/clients")
    public String clients(Model model) {
        model.addAttribute("clients", clientRepo.findAll());
        return "reports/clients";
    }

    // 3) All shipments
    @GetMapping("/shipments")
    public String shipments(Model model) {
        model.addAttribute("title", "Shipments");
        model.addAttribute("subtitle", "Report of all registered shipments");
        model.addAttribute("shipments", shipmentService.all());
        return "reports/shipments";
    }

    // 4) Not delivered
    @GetMapping("/shipments/not-delivered")
    public String notDelivered(Model model) {
        model.addAttribute("title", "Shipments");
        model.addAttribute("subtitle", "Not delivered yet");
        model.addAttribute("shipments", shipmentService.notDelivered());
        return "reports/shipments";
    }

    // 5) By employee (registered by the employee's USER id)
    @GetMapping("/shipments/by-employee/{userId}")
    public String shipmentsByEmployee(@PathVariable Long userId, Model model) {
        model.addAttribute("title", "Shipments");
        model.addAttribute("subtitle", "Registered by employee (userId=" + userId + ")");
        model.addAttribute("shipments", shipmentService.registeredByEmployeeUser(userId));
        model.addAttribute("userId", userId);
        return "reports/shipments";
    }

    // 6) Sent by client
    @GetMapping("/shipments/sent/{clientId}")
    public String sentByClient(@PathVariable Long clientId, Model model) {
        model.addAttribute("title", "Shipments");
        model.addAttribute("subtitle", "Sent by client (id=" + clientId + ")");
        model.addAttribute("shipments", shipmentService.sentByClient(clientId));
        model.addAttribute("clientId", clientId);
        return "reports/shipments";
    }

    // 7) Received by client
    @GetMapping("/shipments/received/{clientId}")
    public String receivedByClient(@PathVariable Long clientId, Model model) {
        model.addAttribute("title", "Shipments");
        model.addAttribute("subtitle", "Received by client (id=" + clientId + ")");
        model.addAttribute("shipments", shipmentService.receivedByClient(clientId));
        model.addAttribute("clientId", clientId);
        return "reports/shipments";
    }

    // 8) Revenue in period
    @GetMapping("/revenue")
    public String revenue(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Model model) {

        model.addAttribute("title", "Revenue");
        if (from != null && to != null) {
            model.addAttribute("from", from);
            model.addAttribute("to", to);
            model.addAttribute("revenue", shipmentService.revenueBetween(from, to));
        }
        return "reports/revenue";
    }
}