package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.OrderItem;
import com.royalcurtains.storemanagement.model.OrderItemFabric;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.OrderItemFabricRepository;
import com.royalcurtains.storemanagement.repository.OrderItemRepository;
import com.royalcurtains.storemanagement.repository.OrderRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import jakarta.transaction.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.util.List;

@Controller
public class OrderItemController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemFabricRepository fabricRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public OrderItemController(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderItemFabricRepository fabricRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.fabricRepository = fabricRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    // Shows complete order details for employees, managers, and tailors.
    @Transactional
    @GetMapping("/orders/{orderId}/items")
    public String orderItems(
            @PathVariable Long orderId,
            Model model,
            Principal principal) {

        Order order = findOrder(orderId);

        checkStoreAccess(order, principal);

        User currentUser = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        model.addAttribute("order", order);
        model.addAttribute(
                "items",
                orderItemRepository.findByOrderId(orderId));
        model.addAttribute(
                "currentRole",
                currentUser.getRole().name());

        return "order-items";
    }

    // Opens the form for adding measurements and product information.
    @Transactional
    @GetMapping("/orders/{orderId}/items/new")
    public String newItem(
            @PathVariable Long orderId,
            Model model,
            Principal principal) {

        Order order = findOrder(orderId);

        checkStoreAccess(order, principal);

        List<User> tailors = userRepository
                .findByRoleAndAssignedStoreId(
                        Role.TAILOR,
                        order.getStore().getId());

        model.addAttribute("order", order);
        model.addAttribute("tailors", tailors);

        return "order-item-form";
    }

    // Saves one product, measurements, and fabric information.
    @Transactional
    @PostMapping("/orders/{orderId}/items")
    public String saveItem(
            @PathVariable Long orderId,
            @RequestParam String productType,
            @RequestParam(required = false) String roomName,
            @RequestParam(required = false) BigDecimal width,
            @RequestParam(required = false) BigDecimal height,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String design,
            @RequestParam(required = false) String specialNotes,
            @RequestParam(required = false) Long tailorId,
            @RequestParam(required = false) String fabricName,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String fabricNotes,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate requiredCompletionDate,
            Principal principal) {

        Order order = findOrder(orderId);

        checkStoreAccess(order, principal);

        OrderItem item = new OrderItem();

        item.setOrder(order);
        item.setProductType(productType);
        item.setRoomName(roomName);
        item.setWidth(width);
        item.setHeight(height);
        item.setQuantity(quantity);
        item.setDesign(design);
        item.setSpecialNotes(specialNotes);
        item.setRequiredCompletionDate(requiredCompletionDate);

        if (tailorId != null) {
            User tailor = userRepository.findById(tailorId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Tailor not found"));

            if (tailor.getRole() != Role.TAILOR) {
                throw new IllegalArgumentException(
                        "Selected user is not a tailor");
            }

            if (tailor.getAssignedStore() == null
                    || !tailor.getAssignedStore()
                    .getId()
                    .equals(order.getStore().getId())) {

                throw new IllegalArgumentException(
                        "This tailor is not assigned to this store");
            }

            item.setAssignedTailor(tailor);

            // Assigned work is automatically received.
            item.setReceivedAt(LocalDateTime.now());
            item.setWorkStatus("RECEIVED");

        } else {
            item.setWorkStatus("NOT_STARTED");
        }

        OrderItem savedItem = orderItemRepository.save(item);

        if (fabricName != null && !fabricName.isBlank()) {
            OrderItemFabric fabric = new OrderItemFabric();

            fabric.setOrderItem(savedItem);
            fabric.setFabricName(fabricName);
            fabric.setColor(color);
            fabric.setNotes(fabricNotes);

            fabricRepository.save(fabric);
        }

        return "redirect:/orders/" + orderId + "/items";
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));
    }

    private void checkStoreAccess(
            Order order,
            Principal principal) {

        Store store = order.getStore();

        storeAccessService.checkStoreAccess(
                principal,
                store.getCode());
    }
}