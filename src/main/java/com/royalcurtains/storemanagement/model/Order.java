package com.royalcurtains.storemanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "customer_orders")
public class Order {

    // Database ID for this order.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Every order belongs to one store.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // The customer who placed the order.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Human-readable order number, such as 1001.
    @Column(nullable = false, unique = true)
    private String orderNumber;

    // Total order price in whole Afghanis.
    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Amount paid by the customer at the beginning.
    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    // Current stage of the order.
    private String status = "NEW";

    // Date when the order was created.
    @Column(nullable = false)
    private LocalDate createdDate = LocalDate.now();

    public Order() {
        // JPA needs an empty constructor.
    }

    public Order(
            Store store,
            Customer customer,
            String orderNumber,
            BigDecimal totalAmount,
            BigDecimal depositAmount) {

        this.store = store;
        this.customer = customer;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.depositAmount = depositAmount;
    }

    // Calculates what the customer still needs to pay.
    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(depositAmount);
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public BigDecimal getRemainingAmountValue() {
        return getRemainingAmount();
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
}