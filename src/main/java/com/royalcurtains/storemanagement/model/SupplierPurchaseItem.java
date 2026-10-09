package com.royalcurtains.storemanagement.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_purchase_items")
public class SupplierPurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private SupplierPurchase purchase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_product_id", nullable = false)
    private InventoryProduct inventoryProduct;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal meterage;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerMeter;

    public SupplierPurchaseItem() {
        this.meterage = BigDecimal.ZERO;
        this.pricePerMeter = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public SupplierPurchase getPurchase() {
        return purchase;
    }

    public void setPurchase(SupplierPurchase purchase) {
        this.purchase = purchase;
    }

    public InventoryProduct getInventoryProduct() {
        return inventoryProduct;
    }

    public void setInventoryProduct(InventoryProduct inventoryProduct) {
        this.inventoryProduct = inventoryProduct;
    }

    public BigDecimal getMeterage() {
        return meterage;
    }

    public void setMeterage(BigDecimal meterage) {
        this.meterage = meterage;
    }

    public BigDecimal getPricePerMeter() {
        return pricePerMeter;
    }

    public void setPricePerMeter(BigDecimal pricePerMeter) {
        this.pricePerMeter = pricePerMeter;
    }
}