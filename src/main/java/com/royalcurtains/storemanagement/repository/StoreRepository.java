package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    // Finds a store using its short code, such as "royal" or "kabul".
    Optional<Store> findByCode(String code);
}