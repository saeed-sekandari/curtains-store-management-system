package com.royalcurtains.storemanagement.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Examples: CURTAIN, MATTRESS, PILLOW
    @Column(nullable = false, length = 30)
    private String productType;

    @Column(length = 100)
    private String roomName;

    // All measurements are recorded in centimeters.
    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal width;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal height;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 100)
    private String design;

    @Column(length = 500)
    private String specialNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_tailor_id")
    private User assignedTailor;

    // The date by which the tailor must complete the work.
    @Column
    private LocalDate requiredCompletionDate;

    // Added automatically when the work is assigned to a tailor.
    @Column
    private LocalDateTime receivedAt;

    // Added automatically when the tailor marks the work as completed.
    @Column
    private LocalDateTime completedAt;

    // NOT_STARTED, RECEIVED, IN_PROGRESS, COMPLETED
    @Column(nullable = false, length = 30)
    private String workStatus;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemFabric> fabrics = new ArrayList<>();

    public OrderItem() {
        this.quantity = 1;
        this.workStatus = "NOT_STARTED";
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public java.math.BigDecimal getWidth() {
        return width;
    }

    public void setWidth(java.math.BigDecimal width) {
        this.width = width;
    }

    public java.math.BigDecimal getHeight() {
        return height;
    }

    public void setHeight(java.math.BigDecimal height) {
        this.height = height;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getDesign() {
        return design;
    }

    public void setDesign(String design) {
        this.design = design;
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    public User getAssignedTailor() {
        return assignedTailor;
    }

    public void setAssignedTailor(User assignedTailor) {
        this.assignedTailor = assignedTailor;
    }

    public LocalDate getRequiredCompletionDate() {
        return requiredCompletionDate;
    }

    public void setRequiredCompletionDate(LocalDate requiredCompletionDate) {
        this.requiredCompletionDate = requiredCompletionDate;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public List<OrderItemFabric> getFabrics() {
        return fabrics;
    }

    public void setFabrics(List<OrderItemFabric> fabrics) {
        this.fabrics = fabrics;
    }
}