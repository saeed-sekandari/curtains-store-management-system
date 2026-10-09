package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.SupplierPurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierPurchaseItemRepository
        extends JpaRepository<SupplierPurchaseItem, Long> {

    List<SupplierPurchaseItem> findByPurchaseId(Long purchaseId);

    List<SupplierPurchaseItem>
    findByInventoryProductId(Long inventoryProductId);
}