package com.royalcurtains.storemanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

    // PostgreSQL creates a unique ID for each customer.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A customer belongs to one store.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    private String fullName;
    private String phone;
    private String address;

    public Customer() {
        // JPA needs an empty constructor.
    }

    public Customer(Store store, String fullName, String phone, String address) {
        this.store = store;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}