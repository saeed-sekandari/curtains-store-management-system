package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Customer;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.repository.CustomerRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;

    public CustomerController(
            CustomerRepository customerRepository,
            StoreRepository storeRepository) {
        this.customerRepository = customerRepository;
        this.storeRepository = storeRepository;
    }

    // Shows customers for the selected store.
    @GetMapping("/customers")
    public String customers(
            @RequestParam(defaultValue = "royal") String store,
            Model model) {

        Store selectedStore = storeRepository.findByCode(store)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        model.addAttribute("selectedStore", selectedStore);
        model.addAttribute(
                "customers",
                customerRepository.findByStoreId(selectedStore.getId())
        );

        return "customers";
    }

    // Opens the form for adding a new customer.
    @GetMapping("/customers/new")
    public String newCustomer(
            @RequestParam(defaultValue = "royal") String store,
            Model model) {

        Store selectedStore = storeRepository.findByCode(store)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        model.addAttribute("selectedStore", selectedStore);
        model.addAttribute("customer", new Customer());

        return "customer-form";
    }

    // Saves the new customer in the selected store.
    @PostMapping("/customers")
    public String saveCustomer(
            @RequestParam String storeCode,
            @ModelAttribute Customer customer) {

        Store selectedStore = storeRepository.findByCode(storeCode)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        customer.setStore(selectedStore);
        customerRepository.save(customer);

        return "redirect:/customers?store=" + storeCode;
    }
}