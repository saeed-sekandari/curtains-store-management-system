package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Customer;
import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.OrderItem;
import com.royalcurtains.storemanagement.model.Payment;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.CustomerRepository;
import com.royalcurtains.storemanagement.repository.OrderItemRepository;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Controller
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public OrderController(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            StoreRepository storeRepository,
            CustomerRepository customerRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    @GetMapping("/orders")
    public String orders(
            @RequestParam String store,
            Model model,
            Principal principal) {

        Store selectedStore = findStore(store);
        checkStoreAccess(principal, selectedStore);

        User currentUser = findUser(principal);

        List<Order> orders =
                orderRepository.findByStoreId(
                        selectedStore.getId()
                );

        Map<Long, BigDecimal> paidAmounts = new HashMap<>();
        Map<Long, BigDecimal> remainingAmounts = new HashMap<>();
        Map<Long, String> workStatuses = new HashMap<>();

        for (Order order : orders) {
            BigDecimal paid =
                    paymentRepository.sumActiveAfnPaymentsByOrderId(
                            order.getId()
                    );

            if (paid == null) {
                paid = BigDecimal.ZERO;
            }

            BigDecimal total =
                    order.getTotalAmount() == null
                            ? BigDecimal.ZERO
                            : order.getTotalAmount();

            BigDecimal remaining =
                    total.subtract(paid).max(BigDecimal.ZERO);

            paidAmounts.put(order.getId(), paid);
            remainingAmounts.put(order.getId(), remaining);
            workStatuses.put(
                    order.getId(),
                    calculateWorkStatus(order.getId())
            );
        }

        model.addAttribute("store", selectedStore);
        model.addAttribute("selectedStore", selectedStore);
        model.addAttribute(
                "currentRole",
                currentUser.getRole().name()
        );
        model.addAttribute("orders", orders);
        model.addAttribute("paidAmounts", paidAmounts);
        model.addAttribute(
                "remainingAmounts",
                remainingAmounts
        );
        model.addAttribute("workStatuses", workStatuses);

        return "orders";
    }

    @GetMapping("/orders/new")
    public String newOrder(
            @RequestParam String store,
            Model model,
            Principal principal) {

        Store selectedStore = findStore(store);
        checkStoreAccess(principal, selectedStore);

        model.addAttribute("store", selectedStore);
        model.addAttribute("selectedStore", selectedStore);
        model.addAttribute(
                "customers",
                customerRepository.findByStoreId(
                        selectedStore.getId()
                )
        );
        model.addAttribute("order", new Order());

        return "order-form";
    }

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
        checkStoreAccess(principal, selectedStore);

        validateOrderTotal(order);

        Customer customer;

        if (customerId != null) {
            customer = customerRepository.findById(customerId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Customer not found"
                            )
                    );

            ensureCustomerBelongsToStore(
                    customer,
                    selectedStore
            );
        } else {
            customer = createCustomer(
                    newCustomerName,
                    newCustomerPhone,
                    newCustomerAddress,
                    selectedStore
            );
        }

        BigDecimal paymentAmount =
                initialPaymentAmount == null
                        ? BigDecimal.ZERO
                        : initialPaymentAmount;

        validateInitialPayment(
                paymentAmount,
                order.getTotalAmount()
        );

        User recordedBy = findUser(principal);

        order.setStore(selectedStore);
        order.setCustomer(customer);
        order.setStatus("ACTIVE");
        order.setDepositAmount(paymentAmount);

        Order savedOrder = orderRepository.save(order);

        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            Payment payment = new Payment();

            payment.setOrder(savedOrder);
            payment.setStore(selectedStore);
            payment.setRecordedBy(recordedBy);
            payment.setAmount(paymentAmount);
            payment.setCurrency("AFN");
            payment.setPaymentType("DEPOSIT");
            payment.setPaymentMethod(
                    normalizePaymentMethod(initialPaymentMethod)
            );
            payment.setPaymentDate(LocalDate.now());
            payment.setRecordedAt(LocalDateTime.now());
            payment.setStatus("ACTIVE");
            payment.setNotes("Initial payment with order");

            paymentRepository.save(payment);
        }

        return "redirect:/orders?store="
                + selectedStore.getCode();
    }

    @Transactional
    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(
            @PathVariable Long orderId,
            @RequestParam String cancellationReason,
            Principal principal) {

        Order order = findOrder(orderId);
        User currentUser = findUser(principal);

        checkStoreAccess(principal, order.getStore());

        if (currentUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException(
                    "Only the manager can cancel orders"
            );
        }

        if (cancellationReason == null
                || cancellationReason.isBlank()) {

            throw new IllegalArgumentException(
                    "Cancellation reason is required"
            );
        }

        if (!"CANCELLED".equalsIgnoreCase(
                order.getStatus()
        )) {
            order.setStatus("CANCELLED");
            order.setCancelledBy(currentUser);
            order.setCancelledAt(LocalDateTime.now());
            order.setCancellationReason(
                    cancellationReason.trim()
            );

            orderRepository.save(order);
        }

        return "redirect:/orders?store="
                + order.getStore().getCode();
    }

    private Customer createCustomer(
            String name,
            String phone,
            String address,
            Store store) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "New customer name is required"
            );
        }

        Customer customer = new Customer();
        customer.setFullName(name.trim());
        customer.setPhone(
                phone == null ? null : phone.trim()
        );
        customer.setAddress(
                address == null ? null : address.trim()
        );
        customer.setStore(store);

        return customerRepository.save(customer);
    }

    private void validateOrderTotal(Order order) {
        if (order.getTotalAmount() == null
                || order.getTotalAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Order total must be greater than zero"
            );
        }
    }

    private void validateInitialPayment(
            BigDecimal paymentAmount,
            BigDecimal totalAmount) {

        if (paymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Initial payment cannot be negative"
            );
        }

        if (paymentAmount.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException(
                    "Initial payment cannot be greater than the order total"
            );
        }
    }

    private String normalizePaymentMethod(
            String paymentMethod) {

        if (paymentMethod == null
                || paymentMethod.isBlank()) {
            return "CASH";
        }

        return paymentMethod.trim().toUpperCase(
                Locale.ROOT
        );
    }

    private void ensureCustomerBelongsToStore(
            Customer customer,
            Store selectedStore) {

        if (customer.getStore() == null
                || !Objects.equals(
                customer.getStore().getId(),
                selectedStore.getId()
        )) {
            throw new IllegalArgumentException(
                    "The selected customer does not belong to this store"
            );
        }
    }

    private String calculateWorkStatus(Long orderId) {
        List<OrderItem> items =
                orderItemRepository.findByOrderId(orderId);

        if (items.isEmpty()) {
            return "NOT_STARTED";
        }

        boolean anyInProgress = items.stream()
                .anyMatch(item ->
                        "IN_PROGRESS".equals(
                                item.getWorkStatus()
                        )
                );

        boolean allCompleted = items.stream()
                .allMatch(item ->
                        "COMPLETED".equals(
                                item.getWorkStatus()
                        )
                );

        if (allCompleted) {
            return "COMPLETED";
        }

        if (anyInProgress) {
            return "IN_PROGRESS";
        }

        return "NOT_STARTED";
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found"
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
                .findByCode(
                        storeCode.trim().toLowerCase(
                                Locale.ROOT
                        )
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Store not found"
                        )
                );
    }

    private User findUser(Principal principal) {
        return userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
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