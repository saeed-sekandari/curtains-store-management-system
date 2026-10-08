package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.Notification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // Shows the newest notifications first.
    @EntityGraph(attributePaths = "recipient")
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    // Used to display the unread notification count.
    long countByRecipientIdAndReadFalse(Long recipientId);
}