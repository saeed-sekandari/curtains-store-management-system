package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.repository.CustomerRepository;
import com.royalcurtains.storemanagement.repository.OrderRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {

    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public HomeController(
            StoreRepository storeRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository) {
        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    // Shows the public welcome page with both stores.
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        String[] stores = storeRepository.findAll()
                .stream()
                .map(Store::getName)
                .toArray(String[]::new);

        model.addAttribute("systemName", "Curtains Store Management");
        model.addAttribute("stores", stores);

        return "home";
    }

    // Displays the login page.
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Loads dashboard numbers for all stores or one selected store.
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "all") String store,
            Model model,
            Principal principal) {

        List<?> customers;
        List<Order> orders;

        String selectedStoreName;
        String selectedStoreCode = store.toLowerCase();

        if (store.equalsIgnoreCase("all")) {
            selectedStoreName = "All Stores";
            selectedStoreCode = "all";

            customers = customerRepository.findAll();
            orders = orderRepository.findAll();
        } else {
            Store selectedStore = storeRepository
                    .findByCode(store.toLowerCase())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Store not found"));

            selectedStoreName = selectedStore.getName();

            customers = customerRepository.findByStoreId(selectedStore.getId());
            orders = orderRepository.findByStoreId(selectedStore.getId());
        }

        BigDecimal totalSales = orders.stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("username", principal.getName());
        model.addAttribute("selectedStore", selectedStoreName);
        model.addAttribute("selectedStoreCode", selectedStoreCode);

        model.addAttribute("orderCount", orders.size());
        model.addAttribute("customerCount", customers.size());
        model.addAttribute("totalSales", totalSales);

        return "dashboard";
    }
}