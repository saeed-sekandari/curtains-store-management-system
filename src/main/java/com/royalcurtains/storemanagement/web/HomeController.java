package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.repository.StoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class HomeController {

    private final StoreRepository storeRepository;

    // Spring provides the store repository when the controller starts.
    public HomeController(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    // Shows the welcome page with store names from the database.
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        String[] stores = storeRepository.findAll()
                .stream()
                .map(store -> store.getName())
                .toArray(String[]::new);

        model.addAttribute("systemName", "Curtains Store Management");
        model.addAttribute("stores", stores);

        return "home";
    }

    // Displays the manager login page.
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Shows the dashboard after the manager logs in.
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "all") String store,
            Model model,
            Principal principal) {

        String selectedStore;

        if (store.equalsIgnoreCase("all")) {
            selectedStore = "All Stores";
        } else {
            selectedStore = storeRepository.findByCode(store.toLowerCase())
                    .map(foundStore -> foundStore.getName())
                    .orElse("All Stores");
        }

        model.addAttribute("username", principal.getName());
        model.addAttribute("selectedStore", selectedStore);

        return "dashboard";
    }
}