package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.OrderItemFabric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemFabricRepository
        extends JpaRepository<OrderItemFabric, Long> {

    // Shows all fabric selections for one order item.
    List<OrderItemFabric> findByOrderItemId(Long orderItemId);
}