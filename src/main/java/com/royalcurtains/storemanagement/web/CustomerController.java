package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Customer;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.repository.CustomerRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;

    public CustomerController(
            CustomerRepository customerRepository,
            StoreRepository storeRepository,
            StoreAccessService storeAccessService) {
        this.customerRepository = customerRepository;
        this.storeRepository = storeRepository;
        this.storeAccessService = storeAccessService;
    }

    // Shows customers for the selected store.
    @GetMapping("/customers")
    public String customers(
            @RequestParam String store,
            Model model,
            Principal principal) {

        Store selectedStore = findStore(store);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode());

        model.addAttribute("store", selectedStore);
        model.addAttribute("selectedStore", selectedStore);

        model.addAttribute(
                "customers",
                customerRepository.findByStoreId(selectedStore.getId()));

        return "customers";
    }

    // Opens the form for creating a new customer.
    @GetMapping("/customers/new")
    public String newCustomer(
            @RequestParam String store,
            Model model,
            Principal principal) {

        Store selectedStore = findStore(store);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode());

        model.addAttribute("store", selectedStore);
        model.addAttribute("selectedStore", selectedStore);
        model.addAttribute("customer", new Customer());

        return "customer-form";
    }

    // Saves a customer under the selected store.
    @PostMapping("/customers")
    public String saveCustomer(
            @RequestParam String storeCode,
            @ModelAttribute Customer customer,
            Principal principal) {

        Store selectedStore = findStore(storeCode);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode());

        customer.setStore(selectedStore);
        customerRepository.save(customer);

        return "redirect:/customers?store="
                + selectedStore.getCode();
    }

    // Finds a store using its short code.
    private Store findStore(String storeCode) {
        return storeRepository.findByCode(storeCode.toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException("Store not found"));
    }
}