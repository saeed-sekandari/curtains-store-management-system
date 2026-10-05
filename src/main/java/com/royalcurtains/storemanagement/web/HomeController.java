package com.royalcurtains.storemanagement.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("systemName", "Royal Curtains Store Management");
        model.addAttribute("stores", new String[]{
                "Royal Curtains Store",
                "Kabul Dubai Curtains Store"
        });
        return "home";
    }
}
