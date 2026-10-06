package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Loads the assigned store when checking login information.
    @EntityGraph(attributePaths = "assignedStore")
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    // Loads assigned stores when displaying the user accounts page.
    @EntityGraph(attributePaths = "assignedStore")
    List<User> findAll();
}