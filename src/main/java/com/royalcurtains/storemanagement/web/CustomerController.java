package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Customer;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.CustomerRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Locale;

@Controller
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public CustomerController(
            CustomerRepository customerRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.customerRepository = customerRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    @GetMapping("/customers")
    public String customers(
            @RequestParam String store,
            Model model,
            Principal principal) {

        Store selectedStore = findStore(store);

        checkStoreAccess(principal, selectedStore);

        User currentUser = findCurrentUser(principal);

        model.addAttribute("store", selectedStore);
        model.addAttribute("selectedStore", selectedStore);
        model.addAttribute(
                "currentRole",
                currentUser.getRole().name()
        );
        model.addAttribute(
                "customers",
                customerRepository.findByStoreId(
                        selectedStore.getId()
                )
        );

        return "customers";
    }

    @GetMapping("/customers/new")
    public String newCustomer(
            @RequestParam String store,
            Model model,
            Principal principal) {

        Store selectedStore = findStore(store);

        checkStoreAccess(principal, selectedStore);

        model.addAttribute("store", selectedStore);
        model.addAttribute("selectedStore", selectedStore);
        model.addAttribute("customer", new Customer());

        return "customer-form";
    }

    @PostMapping("/customers")
    public String saveCustomer(
            @RequestParam String storeCode,
            @ModelAttribute Customer customer,
            Principal principal) {

        Store selectedStore = findStore(storeCode);

        checkStoreAccess(principal, selectedStore);

        if (customer.getFullName() == null
                || customer.getFullName().isBlank()) {

            throw new IllegalArgumentException(
                    "Customer name is required"
            );
        }

        customer.setFullName(
                customer.getFullName().trim()
        );

        if (customer.getPhone() != null) {
            customer.setPhone(customer.getPhone().trim());
        }

        if (customer.getAddress() != null) {
            customer.setAddress(customer.getAddress().trim());
        }

        customer.setStore(selectedStore);

        customerRepository.save(customer);

        return "redirect:/customers?store="
                + selectedStore.getCode();
    }

    private User findCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }

    private Store findStore(String storeCode) {
        if (storeCode == null || storeCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Store code is required"
            );
        }

        return storeRepository
                .findByCode(storeCode.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Store not found"
                        )
                );
    }

    private void checkStoreAccess(
            Principal principal,
            Store store) {

        storeAccessService.checkStoreAccess(
                principal,
                store.getCode()
        );
    }
}