package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Notification;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.NotificationRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // Marks a notification as read for its intended recipient.
    @Transactional
    @PostMapping("/notifications/{notificationId}/read")
    public String markAsRead(
            @PathVariable Long notificationId,
            Principal principal) {

        User currentUser = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notification not found"));

        if (!notification.getRecipient()
                .getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "You cannot update this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);

        return "redirect:/employee-dashboard";
    }
}