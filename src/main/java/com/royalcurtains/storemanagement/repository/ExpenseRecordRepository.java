package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.ExpenseRecord;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRecordRepository
        extends JpaRepository<ExpenseRecord, Long> {

    @EntityGraph(attributePaths = {
            "store",
            "worker",
            "recordedBy"
    })
    List<ExpenseRecord> findByStoreIdOrderByExpenseDateDesc(
            Long storeId
    );

    @EntityGraph(attributePaths = {
            "store",
            "worker",
            "recordedBy"
    })
    List<ExpenseRecord> findByWorkerIdOrderByExpenseDateDesc(
            Long workerId
    );

    @EntityGraph(attributePaths = {
            "store",
            "worker",
            "recordedBy"
    })
    List<ExpenseRecord> findAllByOrderByExpenseDateDesc();
}