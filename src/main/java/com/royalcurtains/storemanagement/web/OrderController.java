package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Customer;
import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.Payment;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.CustomerRepository;
import com.royalcurtains.storemanagement.repository.OrderRepository;
import com.royalcurtains.storemanagement.repository.PaymentRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public OrderController(
            OrderRepository orderRepository,
            StoreRepository storeRepository,
            CustomerRepository customerRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {
        this.orderRepository = orderRepository;
        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
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

        User currentUser = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        List<Order> orders =
                orderRepository.findByStoreId(selectedStore.getId());

        Map<Long, BigDecimal> paidAmounts = new HashMap<>();
        Map<Long, BigDecimal> remainingAmounts = new HashMap<>();

        for (Order order : orders) {
            BigDecimal paid =
                    paymentRepository
                            .sumActiveAfnPaymentsByOrderId(order.getId());

            BigDecimal remaining =
                    order.getTotalAmount()
                            .subtract(paid)
                            .max(BigDecimal.ZERO);

            paidAmounts.put(order.getId(), paid);
            remainingAmounts.put(order.getId(), remaining);
        }

        model.addAttribute("store", selectedStore);
        model.addAttribute("selectedStore", selectedStore);
        model.addAttribute("currentRole", currentUser.getRole().name());
        model.addAttribute("orders", orders);
        model.addAttribute("paidAmounts", paidAmounts);
        model.addAttribute("remainingAmounts", remainingAmounts);

        return "orders";
    }

    // Opens the combined customer and order form.
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

    // Creates a customer, order, and optional first payment together.
    @Transactional
    @PostMapping("/orders")
    public String saveOrder(
            @RequestParam String storeCode,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String newCustomerName,
            @RequestParam(required = false) String newCustomerPhone,
            @RequestParam(required = false) String newCustomerAddress,
            @RequestParam(required = false)
            BigDecimal initialPaymentAmount,
            @RequestParam(
                    required = false,
                    defaultValue = "CASH"
            )
            String initialPaymentMethod,
            @ModelAttribute Order order,
            Principal principal) {

        Store selectedStore = findStore(storeCode);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode());

        Customer customer;

        if (customerId != null) {
            customer = customerRepository.findById(customerId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Customer not found"));
        } else {
            if (newCustomerName == null
                    || newCustomerName.isBlank()) {
                throw new IllegalArgumentException(
                        "New customer name is required");
            }

            customer = new Customer();
            customer.setFullName(newCustomerName);
            customer.setPhone(newCustomerPhone);
            customer.setAddress(newCustomerAddress);
            customer.setStore(selectedStore);

            customer = customerRepository.save(customer);
        }

        order.setStore(selectedStore);
        order.setCustomer(customer);
        order.setStatus("NEW");

        if (initialPaymentAmount == null) {
            order.setDepositAmount(BigDecimal.ZERO);
        } else {
            order.setDepositAmount(initialPaymentAmount);
        }

        Order savedOrder = orderRepository.save(order);

        if (initialPaymentAmount != null
                && initialPaymentAmount.compareTo(BigDecimal.ZERO) > 0) {

            User recordedBy = userRepository
                    .findByUsername(principal.getName())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "User not found"));

            Payment payment = new Payment();
            payment.setOrder(savedOrder);
            payment.setStore(selectedStore);
            payment.setRecordedBy(recordedBy);
            payment.setAmount(initialPaymentAmount);
            payment.setCurrency("AFN");
            payment.setPaymentType("DEPOSIT");
            payment.setPaymentMethod(initialPaymentMethod);
            payment.setPaymentDate(LocalDate.now());
            payment.setRecordedAt(LocalDateTime.now());
            payment.setStatus("ACTIVE");
            payment.setNotes("Initial payment with order");

            paymentRepository.save(payment);
        }

        return "redirect:/orders?store="
                + selectedStore.getCode();
    }

    // Only the manager can cancel an order.
    @Transactional
    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(
            @PathVariable Long orderId,
            @RequestParam String cancellationReason,
            Principal principal) {

        Order order = findOrder(orderId);

        User currentUser = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        storeAccessService.checkStoreAccess(
                principal,
                order.getStore().getCode());

        if (currentUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException(
                    "Only the manager can cancel orders");
        }

        if (!"CANCELLED".equals(order.getStatus())) {
            order.setStatus("CANCELLED");
            order.setCancelledBy(currentUser);
            order.setCancelledAt(LocalDateTime.now());
            order.setCancellationReason(cancellationReason);

            orderRepository.save(order);
        }

        return "redirect:/orders?store="
                + order.getStore().getCode();
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));
    }

    private Store findStore(String storeCode) {
        return storeRepository.findByCode(storeCode.toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException("Store not found"));
    }
}