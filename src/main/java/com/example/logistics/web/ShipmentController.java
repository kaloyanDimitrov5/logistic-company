package com.example.logistics.web;

import com.example.logistics.domain.*;
import com.example.logistics.domain.type.DeliveryType;
import com.example.logistics.domain.type.ShipmentStatus;
import com.example.logistics.repo.ClientProfileRepo;
import com.example.logistics.repo.OfficeRepo;
import com.example.logistics.service.ShipmentService;
import com.example.logistics.service.UserService;
import com.example.logistics.util.NotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shipments")
public class ShipmentController {
    private final ShipmentService shipmentService;
    private final UserService userService;
    private final ClientProfileRepo clientProfileRepo;
    private final OfficeRepo officeRepo;

    @GetMapping
    public String list(Model model, Authentication auth) {
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE") || a.getAuthority().equals("ROLE_ADMIN"));
        List<Shipment> shipments;
        if (isStaff) {
            shipments = shipmentService.all();
        } else {
            var user = userService.getByEmail(auth.getName());
            var client = clientProfileRepo.findByUserId(user.getId()).orElse(null);
            shipments = (client == null) ? List.of() :
                    Stream.concat(
                            shipmentService.sentByClient(client.getId()).stream(),
                            shipmentService.receivedByClient(client.getId()).stream()
                    ).distinct().toList();
        }
        model.addAttribute("shipments", shipments);
        return "shipments/list";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new CreateShipmentForm());
        model.addAttribute("offices", officeRepo.findAll());
        model.addAttribute("clients", clientProfileRepo.findAll());
        return "shipments/form";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping
    public String create(@ModelAttribute("form") CreateShipmentForm f, Authentication auth) {
        var user = userService.getByEmail(auth.getName());
        // very simple: sender/recipient are provided as IDs from the form
        ClientProfile sender = clientProfileRepo.findById(f.getSenderClientId()).orElseThrow();
        ClientProfile recipient = clientProfileRepo.findById(f.getRecipientClientId()).orElseThrow();

        Office toOffice = null;
        if (f.getDeliveryType() == DeliveryType.TO_OFFICE && f.getToOfficeId() != null) {
            toOffice = officeRepo.findById(f.getToOfficeId()).orElseThrow();
        }

        // For demo we skip storing employee profile — we only need user on registeredBy
        EmployeeProfile registeredBy = null; // keep null if you haven't created profiles UI yet

        shipmentService.createShipment(
                sender, recipient,
                f.getWeightKg(),
                f.getDeliveryType(),
                f.getFromCity(), f.getFromAddress(),
                f.getToCity(), f.getToAddress(),
                toOffice,
                registeredBy
        );
        return "redirect:/shipments";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/{id}/in-transit")
    public String markInTransit(@PathVariable Long id) {
        shipmentService.markInTransit(id);
        return "redirect:/shipments";
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/{id}/delivered")
    public String markDelivered(@PathVariable Long id) {
        shipmentService.markDelivered(id);
        return "redirect:/shipments";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public String editShipmentForm(@PathVariable Long id, Model model) {
        Shipment s = shipmentService.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));

        EditShipmentForm f = new EditShipmentForm();
        f.setWeightKg(s.getWeightKg());
        f.setDeliveryType(s.getDeliveryType());
        f.setFromCity(s.getFromCity());
        f.setFromAddress(s.getFromAddressLine());
        f.setToCity(s.getToCity());
        f.setToAddress(s.getToAddressLine());
        f.setStatus(s.getStatus());
        if (s.getToOffice() != null) f.setToOfficeId(s.getToOffice().getId());

        model.addAttribute("form", f);
        model.addAttribute("shipment", s);
        model.addAttribute("offices", officeRepo.findAll());
        model.addAttribute("deliveryTypes", DeliveryType.values());
        model.addAttribute("statuses", ShipmentStatus.values());
        return "shipments/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public String updateShipment(@PathVariable Long id,
                                 @ModelAttribute("form") EditShipmentForm f) {

        shipmentService.updateDetails(id, f.getWeightKg(), f.getDeliveryType(),
                f.getFromCity(), f.getFromAddress(),
                f.getToCity(), f.getToAddress(),
                f.getToOfficeId(),
                f.getStatus());

        return "redirect:/shipments";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public String deleteShipment(@PathVariable Long id) {
        shipmentService.delete(id);
        return "redirect:/shipments";
    }

    @Data
    public static class CreateShipmentForm {
        private Long senderClientId;
        private Long recipientClientId;
        private BigDecimal weightKg;
        private DeliveryType deliveryType = DeliveryType.TO_ADDRESS;
        private String fromCity;
        private String fromAddress;
        private String toCity;
        private String toAddress;
        private Long toOfficeId; // used when deliveryType=OFFICE
    }

    @Data
    public static class EditShipmentForm {
        private BigDecimal weightKg;
        private DeliveryType deliveryType;
        private String fromCity;
        private String fromAddress;
        private String toCity;
        private String toAddress;
        private Long toOfficeId;
        private ShipmentStatus status;
    }
}