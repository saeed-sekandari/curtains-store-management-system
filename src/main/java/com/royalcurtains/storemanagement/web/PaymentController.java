package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.Payment;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.OrderRepository;
import com.royalcurtains.storemanagement.repository.PaymentRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public PaymentController(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    // Shows payment history and the order balance.
    @Transactional
    @GetMapping("/orders/{orderId}/payments")
    public String payments(
            @PathVariable Long orderId,
            Model model,
            Principal principal) {

        Order order = findOrder(orderId);

        storeAccessService.checkStoreAccess(
                principal,
                order.getStore().getCode());

        User currentUser = findCurrentUser(principal);

        BigDecimal totalPaid =
                paymentRepository.sumActiveAfnPaymentsByOrderId(orderId);

        BigDecimal remainingBalance =
                order.getTotalAmount()
                        .subtract(totalPaid)
                        .max(BigDecimal.ZERO);

        model.addAttribute("order", order);
        model.addAttribute("currentRole", currentUser.getRole().name());
        model.addAttribute("currentUsername", currentUser.getUsername());
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("remainingBalance", remainingBalance);
        model.addAttribute(
                "payments",
                paymentRepository.findByOrderIdOrderByPaymentDateDesc(orderId));

        return "payments";
    }

    // Opens the form for recording a new payment.
    @Transactional
    @GetMapping("/orders/{orderId}/payments/new")
    public String newPayment(
            @PathVariable Long orderId,
            Model model,
            Principal principal) {

        Order order = findOrder(orderId);
        User currentUser = findCurrentUser(principal);

        storeAccessService.checkStoreAccess(
                principal,
                order.getStore().getCode());

        checkPaymentRole(currentUser);

        model.addAttribute("order", order);

        return "payment-form";
    }

    // Saves a new payment.
    @Transactional
    @PostMapping("/orders/{orderId}/payments")
    public String savePayment(
            @PathVariable Long orderId,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam String paymentType,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String notes,
            Principal principal) {

        Order order = findOrder(orderId);
        User recordedBy = findCurrentUser(principal);

        storeAccessService.checkStoreAccess(
                principal,
                order.getStore().getCode());

        checkPaymentRole(recordedBy);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStore(order.getStore());
        payment.setRecordedBy(recordedBy);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setPaymentType(paymentType);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDate.now());
        payment.setRecordedAt(LocalDateTime.now());
        payment.setStatus("ACTIVE");
        payment.setNotes(notes);

        paymentRepository.save(payment);

        return "redirect:/orders/"
                + orderId
                + "/payments";
    }

    // Opens the edit page for an existing payment.
    @Transactional
    @GetMapping("/payments/{paymentId}/edit")
    public String editPayment(
            @PathVariable Long paymentId,
            Model model,
            Principal principal) {

        Payment payment = findPayment(paymentId);

        checkPaymentEditAccess(payment, principal);

        model.addAttribute("payment", payment);

        return "payment-edit-form";
    }

    // Updates a payment without deleting it.
    @Transactional
    @PostMapping("/payments/{paymentId}/edit")
    public String updatePayment(
            @PathVariable Long paymentId,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam String paymentType,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String notes,
            Principal principal) {

        Payment payment = findPayment(paymentId);

        checkPaymentEditAccess(payment, principal);

        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setPaymentType(paymentType);
        payment.setPaymentMethod(paymentMethod);
        payment.setNotes(notes);

        paymentRepository.save(payment);

        return "redirect:/orders/"
                + payment.getOrder().getId()
                + "/payments";
    }

    // Only the manager can void a payment.
    @Transactional
    @PostMapping("/payments/{paymentId}/void")
    public String voidPayment(
            @PathVariable Long paymentId,
            @RequestParam String voidReason,
            Principal principal) {

        Payment payment = findPayment(paymentId);
        User currentUser = findCurrentUser(principal);

        storeAccessService.checkStoreAccess(
                principal,
                payment.getStore().getCode());

        if (currentUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException(
                    "Only the manager can void payments");
        }

        payment.setStatus("VOIDED");
        payment.setVoidedAt(LocalDateTime.now());
        payment.setVoidedBy(currentUser);
        payment.setVoidReason(voidReason);

        paymentRepository.save(payment);

        return "redirect:/orders/"
                + payment.getOrder().getId()
                + "/payments";
    }

    // Tailors cannot create or edit payments.
    private void checkPaymentRole(User user) {
        if (user.getRole() == Role.TAILOR) {
            throw new AccessDeniedException(
                    "Tailors are not allowed to manage payments");
        }
    }

    // Managers can edit any active payment.
    // Other authorized users can edit only their own active payment.
    private void checkPaymentEditAccess(
            Payment payment,
            Principal principal) {

        storeAccessService.checkStoreAccess(
                principal,
                payment.getStore().getCode());

        User currentUser = findCurrentUser(principal);

        checkPaymentRole(currentUser);

        if ("VOIDED".equals(payment.getStatus())) {
            throw new AccessDeniedException(
                    "Voided payments cannot be edited");
        }

        boolean isManager = currentUser.getRole() == Role.MANAGER;

        boolean recordedByCurrentUser =
                payment.getRecordedBy() != null
                        && payment.getRecordedBy()
                        .getUsername()
                        .equals(currentUser.getUsername());

        if (!isManager && !recordedByCurrentUser) {
            throw new AccessDeniedException(
                    "You can only edit payments you recorded");
        }
    }

    private User findCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));
    }

    private Payment findPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));
    }
}