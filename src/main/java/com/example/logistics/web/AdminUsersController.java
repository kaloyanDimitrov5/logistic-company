package com.example.logistics.web;

import com.example.logistics.domain.*;
import com.example.logistics.domain.type.RoleType;
import com.example.logistics.repo.ClientProfileRepo;
import com.example.logistics.repo.EmployeeProfileRepo;
import com.example.logistics.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsersController {

    private final UserService userService;
    private final RoleService roleService;
    private final CompanyService companyService;
    private final OfficeService officeService;
    private final AdminService adminService;
    private final ClientProfileRepo clientProfileRepo;
    private final EmployeeProfileRepo employeeProfileRepo;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.all());
        return "admin/users/list";
    }

    @GetMapping("/{id}")
    public String edit(@PathVariable Long id, Model model) {
        User u = userService.getById(id);
        EditUserForm form = EditUserForm.from(u);

        // existing profiles (if any)
        ClientProfile cp = clientProfileRepo.findByUserId(u.getId()).orElse(null);
        EmployeeProfile ep = employeeProfileRepo.findByUserId(u.getId()).orElse(null);

        model.addAttribute("user", u);
        model.addAttribute("form", form);
        model.addAttribute("hasClientProfile", cp != null);
        model.addAttribute("hasEmployeeProfile", ep != null);
        model.addAttribute("companies", companyService.all());
        model.addAttribute("offices", officeService.all());

        // helper forms for creating profiles
        model.addAttribute("clientForm", new ClientForm());
        model.addAttribute("employeeForm", new EmployeeForm());

        return "admin/users/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("form") EditUserForm f,
                         @ModelAttribute("employeeForm") EmployeeForm ef,
                         @ModelAttribute("clientForm") ClientForm cf) {

        // 1) basic fields
        userService.updateBasics(id, f.getFullName(), f.getEmail(), f.isEnabled());

        // 2) replace roles exactly with the checkboxes
        var desired = new java.util.ArrayList<RoleType>();
        if (f.isRoleAdmin())    desired.add(RoleType.ADMIN);
        if (f.isRoleEmployee()) desired.add(RoleType.EMPLOYEE);
        if (f.isRoleClient())   desired.add(RoleType.CLIENT);
        userService.replaceRoles(id, desired);

        // 3) EMPLOYEE profile management
        if (f.isRoleEmployee()) {
            if (!adminService.hasEmployeeProfile(id)) {
                var company = companyService.get(ef.getCompanyId());
                var office  = officeService.get(ef.getOfficeId());
                adminService.createEmployee(id, company, office, ef.getPosition());
            } else {
                // optional: update existing profile with the new company/office/position
                adminService.updateEmployeeByUserId(id, ef.getCompanyId(), ef.getOfficeId(), ef.getPosition());
            }
        } else {
            if (adminService.hasEmployeeProfile(id)) {
                adminService.removeEmployeeProfile(id);
            }
        }

        // 4) CLIENT profile management
        if (f.isRoleClient()) {
            if (!adminService.hasClientProfile(id)) {
                adminService.ensureClientProfile(id, cf.getPhone(), cf.getCity(), cf.getAddress());
            } else {
                adminService.updateClientProfileByUserId(id, cf.getPhone(), cf.getCity(), cf.getAddress());
            }
        } else {
            if (adminService.hasClientProfile(id)) {
                adminService.removeClientProfile(id);
            }
        }

        return "redirect:/admin/users";
    }

    // Create / Remove CLIENT profile
    @PostMapping("/{id}/client/create")
    public String createClient(@PathVariable Long id, @ModelAttribute("clientForm") ClientForm f) {
        adminService.ensureClientProfile(id, f.getPhone(), f.getCity(), f.getAddress());
        userService.grantRole(id, RoleType.CLIENT);
        return "redirect:/admin/users/{id}";
    }

    @PostMapping("/{id}/client/remove")
    public String removeClient(@PathVariable Long id) {
        adminService.removeClientProfile(id); // implement as needed (or soft-delete)
        return "redirect:/admin/users/{id}";
    }

    // Create / Remove EMPLOYEE profile
    @PostMapping("/{id}/employee/create")
    public String createEmployee(@PathVariable Long id, @ModelAttribute("employeeForm") EmployeeForm f) {
        Company c = companyService.get(f.getCompanyId());
        Office o = officeService.get(f.getOfficeId());
        adminService.createEmployee(id, c, o, f.getPosition());
        userService.grantRole(id, RoleType.EMPLOYEE);
        return "redirect:/admin/users/{id}";
    }

    @PostMapping("/{id}/employee/remove")
    public String removeEmployee(@PathVariable Long id) {
        adminService.removeEmployeeProfile(id); // implement if not present
        return "redirect:/admin/users/{id}";
    }

    // --- DTOs ---

    @Data
    public static class EditUserForm {
        private String fullName;
        private String email;
        private boolean enabled;
        private boolean roleAdmin;
        private boolean roleEmployee;
        private boolean roleClient;

        public static EditUserForm from(User u) {
            EditUserForm f = new EditUserForm();
            f.setFullName(u.getFullName());
            f.setEmail(u.getEmail());
            f.setEnabled(u.isEnabled());
            f.setRoleAdmin(u.getRoles().stream().anyMatch(r -> r.getName() == RoleType.ADMIN));
            f.setRoleEmployee(u.getRoles().stream().anyMatch(r -> r.getName() == RoleType.EMPLOYEE));
            f.setRoleClient(u.getRoles().stream().anyMatch(r -> r.getName() == RoleType.CLIENT));
            return f;
        }
    }

    @Data
    public static class ClientForm {
        private String phone;
        private String city;
        private String address;
    }

    @Data
    public static class EmployeeForm {
        private Long companyId;
        private Long officeId;
        private String position;
    }
}