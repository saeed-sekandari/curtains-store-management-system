package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Customer;
import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.repository.CustomerRepository;
import com.royalcurtains.storemanagement.repository.OrderRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OrderController {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;

    public OrderController(
            OrderRepository orderRepository,
            StoreRepository storeRepository,
            CustomerRepository customerRepository) {

        this.orderRepository = orderRepository;
        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
    }

    // Shows orders for the selected store.
    @GetMapping("/orders")
    public String orders(
            @RequestParam(defaultValue = "royal") String store,
            Model model) {

        Store selectedStore = findStore(store);

        model.addAttribute("selectedStore", selectedStore);
        model.addAttribute(
                "orders",
                orderRepository.findByStoreId(selectedStore.getId())
        );

        return "orders";
    }

    // Opens the form for creating a new order.
    @GetMapping("/orders/new")
    public String newOrder(
            @RequestParam(defaultValue = "royal") String store,
            Model model) {

        Store selectedStore = findStore(store);

        model.addAttribute("selectedStore", selectedStore);
        model.addAttribute("customers", customerRepository.findByStoreId(selectedStore.getId()));
        model.addAttribute("order", new Order());

        return "order-form";
    }

    // Saves the new order under the selected store and customer.
    @PostMapping("/orders")
    public String saveOrder(
            @RequestParam String storeCode,
            @RequestParam Long customerId,
            @ModelAttribute Order order) {

        Store selectedStore = findStore(storeCode);

        Customer selectedCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        order.setStore(selectedStore);
        order.setCustomer(selectedCustomer);
        order.setStatus("NEW");

        orderRepository.save(order);

        return "redirect:/orders?store=" + storeCode;
    }

    // Finds a store by its short code.
    private Store findStore(String storeCode) {
        return storeRepository.findByCode(storeCode.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));
    }
}