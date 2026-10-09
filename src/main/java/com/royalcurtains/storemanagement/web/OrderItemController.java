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
import java.util.Objects;

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

    @Transactional
    @GetMapping("/orders/{orderId}/items")
    public String orderItems(
            @PathVariable Long orderId,
            Model model,
            Principal principal) {

        Order order = findOrder(orderId);
        checkStoreAccess(order, principal);

        User currentUser = findUser(principal);

        model.addAttribute("order", order);
        model.addAttribute(
                "items",
                orderItemRepository.findByOrderId(orderId)
        );
        model.addAttribute("currentRole", currentUser.getRole().name());
        model.addAttribute(
                "tailors",
                findTailorsForStore(order.getStore())
        );

        return "order-items";
    }

    @Transactional
    @GetMapping("/orders/{orderId}/items/new")
    public String newItem(
            @PathVariable Long orderId,
            Model model,
            Principal principal) {

        Order order = findOrder(orderId);
        checkStoreAccess(order, principal);

        model.addAttribute("order", order);
        model.addAttribute(
                "tailors",
                findTailorsForStore(order.getStore())
        );

        return "order-item-form";
    }

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
            @RequestParam(required = false) String requiredCompletionDate,
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

        if (requiredCompletionDate != null
                && !requiredCompletionDate.isBlank()) {

            item.setRequiredCompletionDate(
                    LocalDate.parse(requiredCompletionDate)
            );
        }

        assignTailor(item, tailorId, order);

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

    @Transactional
    @GetMapping("/orders/{orderId}/items/{itemId}/edit")
    public String editAssignment(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            Model model,
            Principal principal) {

        Order order = findOrder(orderId);
        checkStoreAccess(order, principal);

        OrderItem item = findItem(itemId);

        verifyItemBelongsToOrder(item, order);
        rejectCompletedItem(item);

        model.addAttribute("order", order);
        model.addAttribute("item", item);
        model.addAttribute(
                "tailors",
                findTailorsForStore(order.getStore())
        );

        return "order-item-assignment-form";
    }

    @Transactional
    @PostMapping("/orders/{orderId}/items/{itemId}/assignment")
    public String updateAssignment(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestParam(required = false) Long tailorId,
            Principal principal) {

        Order order = findOrder(orderId);
        checkStoreAccess(order, principal);

        OrderItem item = findItem(itemId);

        verifyItemBelongsToOrder(item, order);
        rejectCompletedItem(item);

        assignTailor(item, tailorId, order);

        orderItemRepository.save(item);

        return "redirect:/orders/" + orderId + "/items";
    }

    private void assignTailor(
            OrderItem item,
            Long tailorId,
            Order order) {

        if (tailorId == null) {
            item.setAssignedTailor(null);
            item.setWorkStatus("NOT_STARTED");
            return;
        }

        User tailor = userRepository.findById(tailorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Tailor not found")
                );

        if (tailor.getRole() != Role.TAILOR) {
            throw new IllegalArgumentException(
                    "The selected user is not a tailor"
            );
        }

        if (tailor.getAssignedStore() == null
                || !Objects.equals(
                tailor.getAssignedStore().getId(),
                order.getStore().getId())) {

            throw new IllegalArgumentException(
                    "The tailor must belong to the same store"
            );
        }

        item.setAssignedTailor(tailor);

        if (item.getReceivedAt() == null) {
            item.setReceivedAt(LocalDateTime.now());
        }

        if (item.getWorkStatus() == null
                || "NOT_STARTED".equals(item.getWorkStatus())) {

            item.setWorkStatus("RECEIVED");
        }
    }

    private void rejectCompletedItem(OrderItem item) {
        if ("COMPLETED".equals(item.getWorkStatus())) {
            throw new IllegalStateException(
                    "Completed work cannot be reassigned"
            );
        }
    }

    private List<User> findTailorsForStore(Store store) {
        return userRepository.findByRoleAndAssignedStoreId(
                Role.TAILOR,
                store.getId()
        );
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found")
                );
    }

    private OrderItem findItem(Long itemId) {
        return orderItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product or measurement not found"
                        )
                );
    }

    private User findUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );
    }

    private void verifyItemBelongsToOrder(
            OrderItem item,
            Order order) {

        if (item.getOrder() == null
                || !Objects.equals(
                item.getOrder().getId(),
                order.getId())) {

            throw new IllegalArgumentException(
                    "This product does not belong to the selected order"
            );
        }
    }

    private void checkStoreAccess(
            Order order,
            Principal principal) {

        storeAccessService.checkStoreAccess(
                principal,
                order.getStore().getCode()
        );
    }
}