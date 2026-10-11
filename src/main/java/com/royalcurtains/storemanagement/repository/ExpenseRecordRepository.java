package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.ExpenseRecord;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRecordRepository
        extends JpaRepository<ExpenseRecord, Long> {

    // Active records are used in financial reports.
    @EntityGraph(attributePaths = {"store", "worker", "recordedBy"})
    List<ExpenseRecord> findByStoreIdAndStatusOrderByExpenseDateDesc(
            Long storeId,
            String status
    );

    @EntityGraph(attributePaths = {"store", "worker", "recordedBy"})
    List<ExpenseRecord> findByStatusOrderByExpenseDateDesc(
            String status
    );

    // Used for the employee or tailor payment history.
    @EntityGraph(attributePaths = {"store", "worker", "recordedBy"})
    List<ExpenseRecord> findByWorkerIdAndStatusOrderByExpenseDateDesc(
            Long workerId,
            String status
    );

    // Used when the manager opens or voids one specific record.
    @EntityGraph(attributePaths = {
            "store",
            "worker",
            "recordedBy",
            "voidedBy"
    })
    Optional<ExpenseRecord> findByIdAndStoreId(
            Long expenseId,
            Long storeId
    );

    // Keeps all records available for audit history.
    @EntityGraph(attributePaths = {
            "store",
            "worker",
            "recordedBy",
            "voidedBy"
    })
    List<ExpenseRecord> findByStoreIdOrderByExpenseDateDesc(
            Long storeId
    );

    @EntityGraph(attributePaths = {
            "store",
            "worker",
            "recordedBy",
            "voidedBy"
    })
    List<ExpenseRecord> findAllByOrderByExpenseDateDesc();
}