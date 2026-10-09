package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.InventoryProduct;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @EntityGraph(attributePaths = {"store", "addedBy", "lastEditedBy"})
    @Query("""
            SELECT p
            FROM InventoryProduct p
            WHERE p.store.id = :storeId
              AND p.active = true
              AND (
                    LOWER(p.productName)
                        LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(COALESCE(p.color, ''))
                        LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(COALESCE(p.location, ''))
                        LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(p.productCode)
                        LIKE LOWER(CONCAT('%', :search, '%'))
              )
            ORDER BY p.productName ASC
            """)
    List<InventoryProduct> searchActiveProducts(
            @Param("storeId") Long storeId,
            @Param("search") String search
    );

    boolean existsByProductCodeIgnoreCase(String productCode);
}