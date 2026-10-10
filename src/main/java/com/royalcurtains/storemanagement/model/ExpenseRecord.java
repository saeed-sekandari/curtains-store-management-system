package com.royalcurtains.storemanagement.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_records")
public class ExpenseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The store that paid this expense.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // Examples:
    // RENT, ELECTRICITY, TAX, EMPLOYEE_PAYMENT,
    // TAILOR_PAYMENT, MANAGER_WITHDRAWAL, OTHER
    @Column(nullable = false, length = 40)
    private String category;

    // Used for employee and tailor payments.
    // This stays empty for rent, electricity, and other store expenses.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private User worker;

    // Used for manager withdrawals or payments to someone
    // who does not have a system account.
    @Column(length = 150)
    private String recipientName;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    // AFN and USD are stored separately.
    @Column(nullable = false, length = 3)
    private String currency;

    // The actual date and time when the money was paid.
    @Column(nullable = false)
    private LocalDateTime expenseDate;

    @Column(length = 1000)
    private String description;

    // The user who entered this record.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_by_id", nullable = false)
    private User recordedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ExpenseRecord() {
        this.amount = BigDecimal.ZERO;
        this.currency = "AFN";
        this.expenseDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public User getWorker() {
        return worker;
    }

    public void setWorker(User worker) {
        this.worker = worker;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDateTime expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(User recordedBy) {
        this.recordedBy = recordedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}