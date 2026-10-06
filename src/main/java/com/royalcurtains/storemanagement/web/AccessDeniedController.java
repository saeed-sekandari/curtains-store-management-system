package com.royalcurtains.storemanagement.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessDeniedController {

    // Shows a friendly page when a user opens a store they cannot access.
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}