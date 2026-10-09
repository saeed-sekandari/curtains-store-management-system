package com.royalcurtains.storemanagement.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_item_inventory_usage")
public class OrderItemInventoryUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_product_id", nullable = false)
    private InventoryProduct inventoryProduct;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedMeterage;

    @Column(nullable = false)
    private boolean inventoryDeducted;

    private LocalDateTime inventoryDeductedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deducted_by_id")
    private User deductedBy;

    public OrderItemInventoryUsage() {
        this.estimatedMeterage = BigDecimal.ZERO;
        this.inventoryDeducted = false;
    }

    public Long getId() {
        return id;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public InventoryProduct getInventoryProduct() {
        return inventoryProduct;
    }

    public void setInventoryProduct(InventoryProduct inventoryProduct) {
        this.inventoryProduct = inventoryProduct;
    }

    public BigDecimal getEstimatedMeterage() {
        return estimatedMeterage;
    }

    public void setEstimatedMeterage(BigDecimal estimatedMeterage) {
        this.estimatedMeterage = estimatedMeterage;
    }

    public boolean isInventoryDeducted() {
        return inventoryDeducted;
    }

    public void setInventoryDeducted(boolean inventoryDeducted) {
        this.inventoryDeducted = inventoryDeducted;
    }

    public LocalDateTime getInventoryDeductedAt() {
        return inventoryDeductedAt;
    }

    public void setInventoryDeductedAt(LocalDateTime inventoryDeductedAt) {
        this.inventoryDeductedAt = inventoryDeductedAt;
    }

    public User getDeductedBy() {
        return deductedBy;
    }

    public void setDeductedBy(User deductedBy) {
        this.deductedBy = deductedBy;
    }
}