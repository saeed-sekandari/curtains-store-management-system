package com.royalcurtains.storemanagement.service;

import com.royalcurtains.storemanagement.model.Notification;
import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.NotificationRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // Sends a simple message to all employees assigned to the store.
    @Transactional
    public void notifyEmployeesOrderCompleted(Order order) {

        List<User> employees =
                userRepository.findByRoleAndAssignedStoreId(
                        Role.EMPLOYEE,
                        order.getStore().getId());

        String message =
                "Order #" + order.getOrderNumber()
                        + " for "
                        + order.getCustomer().getFullName()
                        + " was completed.";

        for (User employee : employees) {
            Notification notification = new Notification();
            notification.setRecipient(employee);
            notification.setMessage(message);

            notificationRepository.save(notification);
        }
    }
}