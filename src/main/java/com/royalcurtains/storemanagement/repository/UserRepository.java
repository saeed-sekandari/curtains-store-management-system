package com.royalcurtains.storemanagement.repository;

import com.royalcurtains.storemanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Finds a user during the login process.
    Optional<User> findByUsername(String username);

    // Checks whether a username is already being used.
    boolean existsByUsername(String username);
}