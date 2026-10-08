package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Loads the tailor and fabric information with each order item.
    @EntityGraph(attributePaths = {
            "assignedTailor",
            "fabrics"
    })
    List<OrderItem> findByOrderId(Long orderId);

    // Loads order information for the tailor's assigned work page.
    @EntityGraph(attributePaths = {
            "order",
            "order.customer",
            "order.store",
            "fabrics"
    })
    List<OrderItem> findByAssignedTailorId(Long tailorId);
}