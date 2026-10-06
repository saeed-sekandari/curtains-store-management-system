package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class EmployeeController {

    private final UserRepository userRepository;

    public EmployeeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Shows the employee's assigned store after login.
    @GetMapping("/employee-dashboard")
    public String employeeDashboard(
            Model model,
            Principal principal) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        String assignedStoreName = user.getAssignedStore() != null
                ? user.getAssignedStore().getName()
                : "No store assigned";

        String assignedStoreCode = user.getAssignedStore() != null
                ? user.getAssignedStore().getCode()
                : "";

        model.addAttribute("username", user.getUsername());
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", user.getRole());
        model.addAttribute("assignedStoreName", assignedStoreName);
        model.addAttribute("assignedStoreCode", assignedStoreCode);

        return "employee-dashboard";
    }
}