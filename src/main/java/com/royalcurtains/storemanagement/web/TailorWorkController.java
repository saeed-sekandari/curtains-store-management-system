package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.OrderItem;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.OrderItemRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class TailorWorkController {

    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    public TailorWorkController(
            OrderItemRepository orderItemRepository,
            UserRepository userRepository) {

        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    // Starts work assigned to the logged-in tailor.
    @Transactional
    @PostMapping("/tailor/work/{itemId}/start")
    public String startWork(
            @PathVariable Long itemId,
            Principal principal) {

        User tailor = getLoggedInTailor(principal);
        OrderItem item = findItem(itemId);

        checkAssignedTailor(item, tailor);

        if (!"RECEIVED".equals(item.getWorkStatus())) {
            throw new IllegalStateException(
                    "Only received work can be started");
        }

        item.setWorkStatus("IN_PROGRESS");
        orderItemRepository.save(item);

        return "redirect:/tailor-dashboard";
    }

    // Marks the assigned work as completed.
    @Transactional
    @PostMapping("/tailor/work/{itemId}/complete")
    public String completeWork(
            @PathVariable Long itemId,
            Principal principal) {

        User tailor = getLoggedInTailor(principal);
        OrderItem item = findItem(itemId);

        checkAssignedTailor(item, tailor);

        if (!"IN_PROGRESS".equals(item.getWorkStatus())) {
            throw new IllegalStateException(
                    "Work must be in progress before it can be completed");
        }

        item.setWorkStatus("COMPLETED");
        item.setCompletedAt(LocalDateTime.now());

        orderItemRepository.save(item);

        return "redirect:/tailor-dashboard";
    }

    private User getLoggedInTailor(Principal principal) {
        User tailor = userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        if (tailor.getRole() != Role.TAILOR) {
            throw new AccessDeniedException(
                    "Only tailors can update work status");
        }

        return tailor;
    }

    private OrderItem findItem(Long itemId) {
        return orderItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Work item not found"));
    }

    private void checkAssignedTailor(
            OrderItem item,
            User loggedInTailor) {

        if (item.getAssignedTailor() == null
                || !item.getAssignedTailor()
                .getId()
                .equals(loggedInTailor.getId())) {

            throw new AccessDeniedException(
                    "This work is not assigned to you");
        }
    }
}