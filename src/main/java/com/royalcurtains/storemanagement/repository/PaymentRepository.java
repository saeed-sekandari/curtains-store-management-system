package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Loads all related information needed by the edit page.
    @EntityGraph(attributePaths = {
            "order",
            "order.customer",
            "order.store",
            "store",
            "recordedBy"
    })
    Optional<Payment> findById(Long id);

    // Shows payments for one order.
    List<Payment> findByOrderIdOrderByPaymentDateDesc(Long orderId);

    // Used later for store payment reports.
    List<Payment> findByStoreIdOrderByPaymentDateDesc(Long storeId);
}