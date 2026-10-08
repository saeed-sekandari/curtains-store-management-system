package com.royalcurtains.storemanagement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_item_fabrics")
public class OrderItemFabric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The order item that uses this fabric.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    // Examples: Velvet, Sheer, Cotton, or Silk.
    @Column(nullable = false, length = 100)
    private String fabricName;

    // Examples: Blue, White, Cream, or Gray.
    @Column(length = 50)
    private String color;

    // True when this fabric is used as lining.
    @Column(nullable = false)
    private boolean lining;

    // Optional notes about this particular fabric.
    @Column(length = 300)
    private String notes;

    public OrderItemFabric() {
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

    public String getFabricName() {
        return fabricName;
    }

    public void setFabricName(String fabricName) {
        this.fabricName = fabricName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isLining() {
        return lining;
    }

    public void setLining(boolean lining) {
        this.lining = lining;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}