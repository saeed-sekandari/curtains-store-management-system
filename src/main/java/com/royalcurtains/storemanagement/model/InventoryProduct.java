package com.royalcurtains.storemanagement.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_products")
public class InventoryProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    private String color;

    // Total amount originally added to inventory.
    @Column(precision = 12, scale = 2)
    private BigDecimal originalMeterage;

    // Amount currently available.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal meterage;

    private String location;

    @Column(nullable = false, unique = true)
    private String productCode;

    private boolean active;

    private LocalDateTime addedAt;

    private LocalDateTime lastEditedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "added_by_id", nullable = false)
    private User addedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_edited_by_id")
    private User lastEditedBy;

    public InventoryProduct() {
        this.active = true;
        this.addedAt = LocalDateTime.now();
        this.originalMeterage = BigDecimal.ZERO;
        this.meterage = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getOriginalMeterage() {
        return originalMeterage;
    }

    public void setOriginalMeterage(BigDecimal originalMeterage) {
        this.originalMeterage = originalMeterage;
    }

    public BigDecimal getMeterage() {
        return meterage;
    }

    public void setMeterage(BigDecimal meterage) {
        this.meterage = meterage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public LocalDateTime getLastEditedAt() {
        return lastEditedAt;
    }

    public void setLastEditedAt(LocalDateTime lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public User getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(User addedBy) {
        this.addedBy = addedBy;
    }

    public User getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(User lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }
}