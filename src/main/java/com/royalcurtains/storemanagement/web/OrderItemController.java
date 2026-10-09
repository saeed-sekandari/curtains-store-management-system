package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.InventoryMovement;
import com.royalcurtains.storemanagement.model.InventoryProduct;
import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.OrderItem;
import com.royalcurtains.storemanagement.model.OrderItemFabric;
import com.royalcurtains.storemanagement.model.OrderItemInventoryUsage;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.InventoryMovementRepository;
import com.royalcurtains.storemanagement.repository.InventoryProductRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Controller
public class OrderItemController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemFabricRepository fabricRepository;
    private final InventoryProductRepository inventoryProductRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public OrderItemController(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderItemFabricRepository fabricRepository,
            InventoryProductRepository inventoryProductRepository,
            InventoryMovementRepository inventoryMovementRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.fabricRepository = fabricRepository;
        this.inventoryProductRepository = inventoryProductRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
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
        model.addAttribute(
                "inventoryProducts",
                inventoryProductRepository
                        .findByStoreIdAndActiveTrueOrderByProductNameAsc(
                                order.getStore().getId()
                        )
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
            @RequestParam(required = false) List<Long> inventoryProductIds,
            @RequestParam(required = false) List<BigDecimal> estimatedMeterages,
            Principal principal) {

        Order order = findOrder(orderId);
        checkStoreAccess(order, principal);

        User currentUser = findUser(principal);

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

        validateInventoryRows(
                inventoryProductIds,
                estimatedMeterages
        );

        validateInventoryAvailability(
                order,
                inventoryProductIds,
                estimatedMeterages
        );

        OrderItem savedItem = orderItemRepository.save(item);

        addInventoryUsages(
                savedItem,
                order,
                inventoryProductIds,
                estimatedMeterages,
                tailorId != null,
                currentUser
        );

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
        rejectAlreadySentItem(item);

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
        User currentUser = findUser(principal);

        verifyItemBelongsToOrder(item, order);
        rejectCompletedItem(item);
        rejectAlreadySentItem(item);

        assignTailor(item, tailorId, order);

        OrderItem savedItem = orderItemRepository.save(item);

        if (tailorId != null) {
            validateExistingUsageAvailability(savedItem);
            deductExistingUsages(savedItem, order, currentUser);
        }

        return "redirect:/orders/" + orderId + "/items";
    }

    private void addInventoryUsages(
            OrderItem item,
            Order order,
            List<Long> productIds,
            List<BigDecimal> meterages,
            boolean deductNow,
            User currentUser) {

        if (productIds == null || productIds.isEmpty()) {
            return;
        }

        for (int index = 0; index < productIds.size(); index++) {

            Long productId = productIds.get(index);
            BigDecimal meterage = meterages.get(index);

            InventoryProduct product =
                    findInventoryProduct(
                            productId,
                            order.getStore()
                    );

            OrderItemInventoryUsage usage =
                    new OrderItemInventoryUsage();

            usage.setOrderItem(item);
            usage.setInventoryProduct(product);
            usage.setEstimatedMeterage(meterage);

            if (deductNow) {
                deductInventory(
                        product,
                        meterage,
                        order,
                        currentUser
                );

                usage.setInventoryDeducted(true);
                usage.setInventoryDeductedAt(LocalDateTime.now());
                usage.setDeductedBy(currentUser);
            }

            item.getInventoryUsages().add(usage);
        }

        orderItemRepository.save(item);
    }

    private void deductExistingUsages(
            OrderItem item,
            Order order,
            User currentUser) {

        for (OrderItemInventoryUsage usage
                : item.getInventoryUsages()) {

            if (usage.isInventoryDeducted()) {
                continue;
            }

            InventoryProduct product =
                    usage.getInventoryProduct();

            BigDecimal meterage =
                    usage.getEstimatedMeterage();

            deductInventory(
                    product,
                    meterage,
                    order,
                    currentUser
            );

            usage.setInventoryDeducted(true);
            usage.setInventoryDeductedAt(LocalDateTime.now());
            usage.setDeductedBy(currentUser);
        }

        orderItemRepository.save(item);
    }

    private void deductInventory(
            InventoryProduct product,
            BigDecimal meterage,
            Order order,
            User currentUser) {

        BigDecimal available = product.getMeterage();

        if (available == null) {
            available = BigDecimal.ZERO;
        }

        if (meterage.compareTo(available) > 0) {
            throw new IllegalArgumentException(
                    "Not enough inventory for "
                            + product.getProductName()
                            + ". Available: "
                            + available
                            + " meters."
            );
        }

        product.setMeterage(available.subtract(meterage));
        product.setLastEditedBy(currentUser);
        product.setLastEditedAt(LocalDateTime.now());

        inventoryProductRepository.save(product);

        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setMovementType("REMOVE");
        movement.setQuantity(meterage);
        movement.setReason(
                "Used for order " + order.getOrderNumber()
        );
        movement.setRecordedBy(currentUser);

        inventoryMovementRepository.save(movement);
    }

    private void validateInventoryAvailability(
            Order order,
            List<Long> productIds,
            List<BigDecimal> meterages) {

        if (productIds == null || productIds.isEmpty()) {
            return;
        }

        for (int index = 0; index < productIds.size(); index++) {

            InventoryProduct product =
                    findInventoryProduct(
                            productIds.get(index),
                            order.getStore()
                    );

            BigDecimal available = product.getMeterage();

            if (available == null) {
                available = BigDecimal.ZERO;
            }

            if (meterages.get(index).compareTo(available) > 0) {
                throw new IllegalArgumentException(
                        "Not enough inventory for "
                                + product.getProductName()
                                + ". Available: "
                                + available
                                + " meters."
                );
            }
        }
    }

    private void validateExistingUsageAvailability(
            OrderItem item) {

        for (OrderItemInventoryUsage usage
                : item.getInventoryUsages()) {

            if (usage.isInventoryDeducted()) {
                continue;
            }

            InventoryProduct product =
                    usage.getInventoryProduct();

            BigDecimal available = product.getMeterage();

            if (available == null) {
                available = BigDecimal.ZERO;
            }

            if (usage.getEstimatedMeterage()
                    .compareTo(available) > 0) {

                throw new IllegalArgumentException(
                        "Not enough inventory for "
                                + product.getProductName()
                                + ". Available: "
                                + available
                                + " meters."
                );
            }
        }
    }

    private void validateInventoryRows(
            List<Long> productIds,
            List<BigDecimal> meterages) {

        List<Long> safeProductIds = productIds == null
                ? Collections.emptyList()
                : productIds;

        List<BigDecimal> safeMeterages = meterages == null
                ? Collections.emptyList()
                : meterages;

        if (safeProductIds.size() != safeMeterages.size()) {
            throw new IllegalArgumentException(
                    "Each selected product must have an estimated meterage"
            );
        }

        for (int index = 0; index < safeMeterages.size(); index++) {

            BigDecimal meterage = safeMeterages.get(index);

            if (safeProductIds.get(index) == null) {
                throw new IllegalArgumentException(
                        "Please select an inventory product"
                );
            }

            if (meterage == null
                    || meterage.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Estimated meterage must be greater than zero"
                );
            }
        }
    }

    private InventoryProduct findInventoryProduct(
            Long productId,
            Store store) {

        return inventoryProductRepository
                .findByIdAndStoreId(productId, store.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Selected inventory product was not found"
                        )
                );
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
                        new IllegalArgumentException(
                                "Tailor not found"
                        )
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

    private void rejectAlreadySentItem(OrderItem item) {
        if (item.getReceivedAt() != null) {
            throw new IllegalStateException(
                    "This work was already sent to the tailor"
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
                        new IllegalArgumentException(
                                "Order not found"
                        )
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
                        new IllegalArgumentException(
                                "User not found"
                        )
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