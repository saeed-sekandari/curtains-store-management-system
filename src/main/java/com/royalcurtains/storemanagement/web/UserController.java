package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository,
            StoreRepository storeRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Shows all accounts stored in the database.
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    // Opens the form for creating a new account.
    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("stores", storeRepository.findAll());
        return "user-form";
    }

    // Saves a new account with an encrypted password and assigned store.
    @PostMapping("/users")
    public String saveUser(
            @RequestParam String username,
            @RequestParam String fullName,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam String storeCode) {

        if (userRepository.existsByUsername(username)) {
            return "redirect:/users?error=duplicate";
        }

        Store assignedStore = storeRepository.findByCode(storeCode)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.valueOf(role));
        user.setAssignedStore(assignedStore);
        user.setEnabled(true);

        userRepository.save(user);

        return "redirect:/users";
    }

    // Opens the edit page for an existing employee account.
    @GetMapping("/users/{id}/edit")
    public String editUser(
            @PathVariable Long id,
            Model model) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("stores", storeRepository.findAll());

        return "user-edit-form";
    }

    // Updates an employee without deleting the employee's history.
    @PostMapping("/users/{id}/edit")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam String role,
            @RequestParam String storeCode,
            @RequestParam(required = false, defaultValue = "") String password) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // The manager account is protected from employee editing.
        if (user.getRole() == Role.MANAGER) {
            return "redirect:/users";
        }

        Store assignedStore = storeRepository.findByCode(storeCode)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        user.setFullName(fullName);
        user.setRole(Role.valueOf(role));
        user.setAssignedStore(assignedStore);

        // Only replace the password if a new one was entered.
        if (!password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(user);

        return "redirect:/users";
    }

    // Disables or re-enables an employee without deleting their records.
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {

            if (user.getRole() != Role.MANAGER) {
                user.setEnabled(!user.isEnabled());
                userRepository.save(user);
            }
        });

        return "redirect:/users";
    }
}