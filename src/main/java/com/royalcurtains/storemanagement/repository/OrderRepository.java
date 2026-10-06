package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Loads the customer together with each order.
    // This allows the orders page to display the customer's name safely.
    @EntityGraph(attributePaths = "customer")
    List<Order> findByStoreId(Long storeId);

    // Finds one order using its order number.
    Order findByOrderNumber(String orderNumber);

    // Returns all orders for one customer.
    List<Order> findByCustomerId(Long customerId);
}