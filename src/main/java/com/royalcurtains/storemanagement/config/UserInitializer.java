package com.royalcurtains.storemanagement.config;

import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserInitializer {

    @Bean
    public CommandLineRunner createInitialManager(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // Prevents the manager account from being created again.
            if (userRepository.existsByUsername("manager")) {
                return;
            }

            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("ChangeMe123!"));
            manager.setFullName("System Manager");
            manager.setRole(Role.MANAGER);
            manager.setEnabled(true);

            userRepository.save(manager);

            System.out.println("Initial manager account created.");
            System.out.println("Username: manager");
            System.out.println("Password: ChangeMe123!");
        };
    }
}