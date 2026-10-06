package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Loads the customer when showing store orders.
    @EntityGraph(attributePaths = {"customer", "store"})
    List<Order> findByStoreId(Long storeId);

    // Loads the customer and store when opening payment pages.
    @EntityGraph(attributePaths = {"customer", "store"})
    Optional<Order> findById(Long id);

    Order findByOrderNumber(String orderNumber);

    List<Order> findByCustomerId(Long customerId);
}