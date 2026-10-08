package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "assignedStore")
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = "assignedStore")
    List<User> findAll();

    // Finds tailors who belong to a specific store.
    List<User> findByRoleAndAssignedStoreId(
            Role role,
            Long storeId);
}