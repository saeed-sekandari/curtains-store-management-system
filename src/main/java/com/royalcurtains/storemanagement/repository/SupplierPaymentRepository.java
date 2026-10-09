package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.SupplierPayment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierPaymentRepository
        extends JpaRepository<SupplierPayment, Long> {

    @EntityGraph(attributePaths = {
            "supplier",
            "recordedBy"
    })
    List<SupplierPayment> findBySupplierIdOrderByPaymentDateDesc(
            Long supplierId
    );

    @EntityGraph(attributePaths = {
            "supplier",
            "recordedBy"
    })
    List<SupplierPayment>
    findBySupplierStoreIdOrderByPaymentDateDesc(
            Long storeId
    );
}