package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.SupplierPurchase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierPurchaseRepository
        extends JpaRepository<SupplierPurchase, Long> {

    @EntityGraph(attributePaths = {
            "supplier",
            "recordedBy"
    })
    List<SupplierPurchase> findBySupplierIdOrderByPurchaseDateDesc(
            Long supplierId
    );

    @EntityGraph(attributePaths = {
            "supplier",
            "recordedBy"
    })
    List<SupplierPurchase>
    findBySupplierStoreIdOrderByPurchaseDateDesc(
            Long storeId
    );

    @EntityGraph(attributePaths = {
            "supplier",
            "recordedBy"
    })
    List<SupplierPurchase> findBySupplierIdOrderByPurchaseDateAsc(
            Long supplierId
    );
}