package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.OrderItemInventoryUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemInventoryUsageRepository
        extends JpaRepository<OrderItemInventoryUsage, Long> {

    List<OrderItemInventoryUsage> findByOrderItemId(Long orderItemId);

    List<OrderItemInventoryUsage>
    findByInventoryProductId(Long inventoryProductId);
}