package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {
            "order",
            "order.customer",
            "order.store",
            "store",
            "recordedBy",
            "voidedBy"
    })
    Optional<Payment> findById(Long id);

    // Loads recordedBy so the page can check who may edit the payment.
    @EntityGraph(attributePaths = {
            "recordedBy"
    })
    List<Payment> findByOrderIdOrderByPaymentDateDesc(Long orderId);

    List<Payment> findByStoreIdOrderByPaymentDateDesc(Long storeId);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.order.id = :orderId
              AND p.currency = 'AFN'
              AND (p.status IS NULL OR p.status = 'ACTIVE')
            """)
    BigDecimal sumActiveAfnPaymentsByOrderId(
            @Param("orderId") Long orderId);
}