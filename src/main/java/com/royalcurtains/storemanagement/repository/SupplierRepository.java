package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByStoreIdOrderByNameAsc(Long storeId);

    Optional<Supplier> findByIdAndStoreId(Long supplierId, Long storeId);
}