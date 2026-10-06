package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Customer;
import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.repository.CustomerRepository;
import com.royalcurtains.storemanagement.repository.OrderRepository;
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
public class OrderController {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final StoreAccessService storeAccessService;

    public OrderController(
            OrderRepository orderRepository,
            StoreRepository storeRepository,
            CustomerRepository customerRepository,
            StoreAccessService storeAccessService) {
        this.orderRepository = orderRepository;
        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
        this.storeAccessService = storeAccessService;
    }

    // Shows orders for the selected store.
    @GetMapping("/orders")
    public String orders(
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
                "orders",
                orderRepository.findByStoreId(selectedStore.getId()));

        return "orders";
    }

    // Opens the form for creating a new order.
    @GetMapping("/orders/new")
    public String newOrder(
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

        model.addAttribute("order", new Order());

        return "order-form";
    }

    // Saves an order under the selected store.
    @PostMapping("/orders")
    public String saveOrder(
            @RequestParam String storeCode,
            @RequestParam Long customerId,
            @ModelAttribute Order order,
            Principal principal) {

        Store selectedStore = findStore(storeCode);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode());

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found"));

        order.setStore(selectedStore);
        order.setCustomer(customer);
        order.setStatus("NEW");

        orderRepository.save(order);

        return "redirect:/orders?store="
                + selectedStore.getCode();
    }

    // Finds a store using its short code.
    private Store findStore(String storeCode) {
        return storeRepository.findByCode(storeCode.toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException("Store not found"));
    }
}