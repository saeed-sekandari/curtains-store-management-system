package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.InventoryProduct;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryProductRepository
        extends JpaRepository<InventoryProduct, Long> {

    @EntityGraph(attributePaths = {"store", "addedBy", "lastEditedBy"})
    List<InventoryProduct> findByStoreIdAndActiveTrueOrderByProductNameAsc(
            Long storeId
    );

    @EntityGraph(attributePaths = {"store", "addedBy", "lastEditedBy"})
    List<InventoryProduct> findByStoreIdOrderByProductNameAsc(
            Long storeId
    );

    @EntityGraph(attributePaths = {"store", "addedBy", "lastEditedBy"})
    Optional<InventoryProduct> findByIdAndStoreId(
            Long productId,
            Long storeId
    );

    boolean existsByProductCodeIgnoreCase(String productCode);
}