package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.InventoryMovement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovement, Long> {

    @EntityGraph(attributePaths = {"product", "recordedBy"})
    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(
            Long productId
    );

    @EntityGraph(attributePaths = {"product", "recordedBy"})
    List<InventoryMovement> findByProductStoreIdOrderByCreatedAtDesc(
            Long storeId
    );
}