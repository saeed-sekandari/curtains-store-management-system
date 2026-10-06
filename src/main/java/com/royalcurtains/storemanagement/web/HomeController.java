package com.royalcurtains.storemanagement.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class HomeController {

    // Shows the welcome page when the application opens.
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("systemName", "Royal Curtains Store Management");

        // These store names will later come from the database.
        model.addAttribute("stores", new String[]{
                "Royal Curtains Store",
                "Kabul Dubai Curtains Store"
        });

        return "home";
    }

    // Displays the manager login page.
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Displays the dashboard after the manager logs in.
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "all") String store,
            Model model,
            Principal principal) {

        // Decide which store view the manager selected.
        String selectedStore = switch (store.toLowerCase()) {
            case "royal" -> "Royal Curtains Store";
            case "kabul" -> "Kabul Dubai Curtains Store";
            default -> "All Stores";
        };

        // Send the logged-in username and selected store to the page.
        model.addAttribute("username", principal.getName());
        model.addAttribute("selectedStore", selectedStore);

        return "dashboard";
    }
}