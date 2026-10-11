package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.DirectSale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DirectSaleRepository
        extends JpaRepository<DirectSale, Long> {

    @EntityGraph(attributePaths = {"store", "recordedBy"})
    List<DirectSale>
    findByStoreIdAndStatusAndSaleDateBetweenOrderBySaleDateDesc(
            Long storeId,
            String status,
            LocalDateTime start,
            LocalDateTime end
    );

    @EntityGraph(attributePaths = {"store", "recordedBy"})
    List<DirectSale>
    findByStatusAndSaleDateBetweenOrderBySaleDateDesc(
            String status,
            LocalDateTime start,
            LocalDateTime end
    );

    @EntityGraph(attributePaths = {"store", "recordedBy"})
    List<DirectSale>
    findByStoreIdAndStatusOrderBySaleDateDesc(
            Long storeId,
            String status
    );
}