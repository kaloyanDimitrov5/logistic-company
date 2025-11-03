package com.example.logistics.web;

import com.example.logistics.repo.ClientProfileRepo;
import com.example.logistics.service.ShipmentService;
import com.example.logistics.service.UserService;
import com.example.logistics.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my")
@PreAuthorize("hasRole('CLIENT')")
public class MyReportsController {

    private final ShipmentService shipmentService;
    private final UserService userService;
    private final ClientProfileRepo clientRepo;

    @GetMapping("/sent")
    public String mySent(@AuthenticationPrincipal UserDetails principal, Model model) {
        var user = userService.getByEmail(principal.getUsername());
        var client = clientRepo.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Client profile not found for user: " + user.getEmail()));

        model.addAttribute("title", "Shipments");
        model.addAttribute("subtitle", "Sent by me");
        model.addAttribute("shipments", shipmentService.sentByClient(client.getId()));
        return "reports/shipments";
    }

    @GetMapping("/received")
    public String myReceived(@AuthenticationPrincipal UserDetails principal, Model model) {
        var user = userService.getByEmail(principal.getUsername());
        var client = clientRepo.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Client profile not found for user: " + user.getEmail()));

        model.addAttribute("title", "Shipments");
        model.addAttribute("subtitle", "Received by me");
        model.addAttribute("shipments", shipmentService.receivedByClient(client.getId()));
        return "reports/shipments";
    }
}