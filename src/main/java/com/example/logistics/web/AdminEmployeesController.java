package com.example.logistics.web;

import com.example.logistics.domain.Company;
import com.example.logistics.domain.Office;
import com.example.logistics.domain.User;
import com.example.logistics.domain.type.RoleType;
import com.example.logistics.service.AdminService;
import com.example.logistics.service.CompanyService;
import com.example.logistics.service.OfficeService;
import com.example.logistics.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/employees")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmployeesController {

    private final UserService userService;
    private final CompanyService companyService;
    private final OfficeService officeService;
    private final AdminService adminService;

    @GetMapping("/new")
    public String promoteForm(Model model) {
        model.addAttribute("form", new PromoteForm());
        model.addAttribute("companies", companyService.all());
        model.addAttribute("offices", officeService.all());
        model.addAttribute("users", userService.all());
        return "admin/employees/form";
    }

    @PostMapping
    public String promote(@ModelAttribute PromoteForm f, RedirectAttributes ra) {
        User u = userService.getByEmail(f.getEmail());
        Company c = companyService.get(f.getCompanyId());
        Office o = officeService.get(f.getOfficeId());
        adminService.createEmployee(u.getId(), c, o, f.getPosition());
        ra.addFlashAttribute("success", "User promoted to employee.");
        return "redirect:/reports/employees";
    }

    // Create-Employee
    @GetMapping("/create")
    public String createEmployeeForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CreateEmployeeForm());
        }
        model.addAttribute("companies", companyService.all());
        model.addAttribute("offices", officeService.all());
        return "admin/employees/create"; // new template
    }

    @PostMapping("/create")
    public String createEmployee(@ModelAttribute CreateEmployeeForm f, RedirectAttributes ra) {
        try {
            // 1) Create user
            User u = userService.createUser(f.getFullName(), f.getEmail(), f.getPassword(), true);
            // 2) Grant EMPLOYEE role
            userService.grantRole(u.getId(), RoleType.EMPLOYEE);
            // 3) Create employee profile
            Company c = companyService.get(f.getCompanyId());
            Office o = officeService.get(f.getOfficeId());
            adminService.createEmployee(u.getId(), c, o, f.getPosition());

            ra.addFlashAttribute("success", "Employee created successfully.");
            return "redirect:/reports/employees";
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "Email already exists. Please use another one.");
            ra.addFlashAttribute("form", f);
            return "redirect:/admin/employees/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var emp = adminService.getEmployee(id);
        var form = new PromoteForm();
        form.setEmail(emp.getUser().getEmail());
        form.setCompanyId(emp.getCompany() != null ? emp.getCompany().getId() : null);
        form.setOfficeId(emp.getOffice() != null ? emp.getOffice().getId() : null);
        form.setPosition(emp.getPosition());

        model.addAttribute("form", form);
        model.addAttribute("employeeId", id);
        model.addAttribute("companies", companyService.all());
        model.addAttribute("offices", officeService.all());
        model.addAttribute("users", userService.all());
        return "admin/employees/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute PromoteForm f, RedirectAttributes ra) {
        Company c = f.getCompanyId() != null ? companyService.get(f.getCompanyId()) : null;
        Office o  = f.getOfficeId()  != null ? officeService.get(f.getOfficeId())   : null;
        adminService.updateEmployee(id, c, o, f.getPosition());
        ra.addFlashAttribute("success", "Employee updated.");
        return "redirect:/reports/employees";
    }

    @Data
    public static class PromoteForm {
        private String email;
        private Long companyId;
        private Long officeId;
        private String position;
    }

    @Data
    public static class CreateEmployeeForm {
        private String fullName;
        private String email;
        private String password;
        private String position;
        private Long companyId;
        private Long officeId;
    }
}