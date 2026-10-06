package com.royalcurtains.storemanagement.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class EmployeeController {

    // Shows the page employees will use after logging in.
    @GetMapping("/employee-dashboard")
    public String employeeDashboard(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        return "employee-dashboard";
    }
}