package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Shows all accounts currently stored in the database.
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    // Opens the form for creating a new account.
    @GetMapping("/users/new")
    public String newUser() {
        return "user-form";
    }

    // Saves a new account with an encrypted password.
    @PostMapping("/users")
    public String saveUser(
            @RequestParam String username,
            @RequestParam String fullName,
            @RequestParam String password,
            @RequestParam String role) {

        // Do not create two accounts with the same username.
        if (userRepository.existsByUsername(username)) {
            return "redirect:/users?error=duplicate";
        }

        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);

        // Never store a user's password as plain text.
        user.setPassword(passwordEncoder.encode(password));

        user.setRole(Role.valueOf(role));
        user.setEnabled(true);

        userRepository.save(user);

        return "redirect:/users";
    }
}