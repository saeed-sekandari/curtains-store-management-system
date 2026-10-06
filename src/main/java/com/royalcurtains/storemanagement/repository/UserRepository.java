package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    // Loads the assigned store together with each user.
    @EntityGraph(attributePaths = "assignedStore")
    List<User> findAll();
}