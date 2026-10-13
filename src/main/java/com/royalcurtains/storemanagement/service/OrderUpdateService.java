package com.royalcurtains.storemanagement.service;

import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.OrderItem;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.OrderItemRepository;
import com.royalcurtains.storemanagement.repository.OrderRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.web.dto.OrderUpdateRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderUpdateService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    public OrderUpdateService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order updateOrder(
            Long orderId,
            OrderUpdateRequest request,
            Store store) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Order update data is required"
            );
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found"
                        )
                );

        if (!order.getStore().getId().equals(store.getId())) {
            throw new IllegalArgumentException(
                    "This order does not belong to this store"
            );
        }

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalArgumentException(
                    "Cancelled orders cannot be edited"
            );
        }

        User tailor = null;

        if (request.getTailorId() != null) {
            tailor = userRepository
                    .findByRoleAndAssignedStoreId(
                            Role.TAILOR,
                            store.getId()
                    )
                    .stream()
                    .filter(user ->
                            user.getId().equals(
                                    request.getTailorId()
                            )
                    )
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Selected tailor does not belong to this store"
                            )
                    );
        }

        List<OrderItem> items =
                orderItemRepository.findByOrderId(order.getId());

        if (items.isEmpty()) {
            OrderItem newItem = new OrderItem();

            newItem.setOrder(order);
            newItem.setProductType("CURTAIN");
            newItem.setQuantity(1);
            newItem.setWorkStatus("NOT_STARTED");

            items = List.of(newItem);
        }

        String details = request.getDetails() == null
                ? null
                : request.getDetails().trim();

        for (OrderItem item : items) {
            item.setRequiredCompletionDate(
                    request.getCompletionDate()
            );
            item.setAssignedTailor(tailor);
            item.setSpecialNotes(details);

            orderItemRepository.save(item);
        }

        return order;
    }
}