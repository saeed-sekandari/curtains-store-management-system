package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Returns customers belonging to one specific store.
    List<Customer> findByStoreId(Long storeId);

    // Helps find a customer by phone number within a store.
    List<Customer> findByStoreIdAndPhone(Long storeId, String phone);
}