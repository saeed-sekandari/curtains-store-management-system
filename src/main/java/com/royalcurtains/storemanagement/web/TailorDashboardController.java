package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.OrderItemRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class TailorDashboardController {

    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public TailorDashboardController(
            UserRepository userRepository,
            OrderItemRepository orderItemRepository) {

        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // Shows only work assigned to the logged-in tailor.
    @GetMapping("/tailor-dashboard")
    public String tailorDashboard(
            Principal principal,
            Model model) {

        User tailor = userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        if (tailor.getRole() != Role.TAILOR) {
            throw new AccessDeniedException(
                    "Only tailors can access this page");
        }

        model.addAttribute("tailor", tailor);
        model.addAttribute(
                "assignedItems",
                orderItemRepository.findByAssignedTailorId(tailor.getId()));

        return "tailor-dashboard";
    }
}