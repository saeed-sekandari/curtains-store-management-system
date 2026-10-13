package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Customer;
import com.royalcurtains.storemanagement.model.InventoryProduct;
import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.OrderItem;
import com.royalcurtains.storemanagement.model.OrderItemFabric;
import com.royalcurtains.storemanagement.model.OrderItemInventoryUsage;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.CustomerRepository;
import com.royalcurtains.storemanagement.repository.InventoryProductRepository;
import com.royalcurtains.storemanagement.repository.OrderItemRepository;
import com.royalcurtains.storemanagement.repository.OrderRepository;
import com.royalcurtains.storemanagement.repository.PaymentRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import com.royalcurtains.storemanagement.service.OrderCreationService;
import com.royalcurtains.storemanagement.web.dto.OrderCreateRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final InventoryProductRepository inventoryProductRepository;
    private final PaymentRepository paymentRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;
    private final OrderCreationService orderCreationService;

    public OrderApiController(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository,
            InventoryProductRepository inventoryProductRepository,
            PaymentRepository paymentRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService,
            OrderCreationService orderCreationService) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.inventoryProductRepository =
                inventoryProductRepository;
        this.paymentRepository = paymentRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
        this.orderCreationService = orderCreationService;
    }

    @Transactional
    @GetMapping
    public Map<String, Object> getOrders(
            @RequestParam String store,
            Principal principal) {

        Store selectedStore = getStore(store);
        checkStoreAccess(principal, selectedStore);

        List<Order> orders =
                orderRepository.findByStoreId(
                        selectedStore.getId()
                );

        List<Map<String, Object>> orderResponses =
                new ArrayList<>();

        for (Order order : orders) {
            orderResponses.add(toOrderResponse(order));
        }

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("storeCode", selectedStore.getCode());
        response.put("storeName", selectedStore.getName());
        response.put("orders", orderResponses);

        return response;
    }

    @Transactional
    @GetMapping("/{orderId}")
    public Map<String, Object> getOrder(
            @PathVariable Long orderId,
            @RequestParam String store,
            Principal principal) {

        Store selectedStore = getStore(store);
        checkStoreAccess(principal, selectedStore);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"
                        )
                );

        if (!order.getStore().getId()
                .equals(selectedStore.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This order does not belong to this store"
            );
        }

        Map<String, Object> response =
                toOrderResponse(order);

        List<OrderItem> items =
                orderItemRepository.findByOrderId(
                        order.getId()
                );

        List<Map<String, Object>> itemResponses =
                new ArrayList<>();

        for (OrderItem item : items) {
            itemResponses.add(toItemResponse(item));
        }

        response.put("items", itemResponses);

        return response;
    }

    @Transactional
    @PostMapping
    public Map<String, Object> createOrder(
            @RequestBody OrderCreateRequest request,
            Principal principal) {

        Store selectedStore = getStore(
                request.getStoreCode()
        );

        checkStoreAccess(
                principal,
                selectedStore
        );

        User currentUser = findUser(principal);

        Order savedOrder =
                orderCreationService.createOrder(
                        request,
                        selectedStore,
                        currentUser
                );

        return getOrder(
                savedOrder.getId(),
                selectedStore.getCode(),
                principal
        );
    }

    @Transactional
    @GetMapping("/form-data")
    public Map<String, Object> getFormData(
            @RequestParam String store,
            Principal principal) {

        Store selectedStore = getStore(store);
        checkStoreAccess(principal, selectedStore);

        List<Map<String, Object>> customers =
                customerRepository.findByStoreId(
                                selectedStore.getId()
                        )
                        .stream()
                        .map(this::toCustomerResponse)
                        .toList();

        List<Map<String, Object>> products =
                inventoryProductRepository
                        .findByStoreIdAndActiveTrueOrderByProductNameAsc(
                                selectedStore.getId()
                        )
                        .stream()
                        .map(this::toInventoryResponse)
                        .toList();

        List<Map<String, Object>> tailors =
                userRepository.findByRoleAndAssignedStoreId(
                                Role.TAILOR,
                                selectedStore.getId()
                        )
                        .stream()
                        .map(this::toUserResponse)
                        .toList();

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("storeCode", selectedStore.getCode());
        response.put("storeName", selectedStore.getName());
        response.put("customers", customers);
        response.put("inventoryProducts", products);
        response.put("tailors", tailors);

        return response;
    }

    private Map<String, Object> toOrderResponse(
            Order order) {

        BigDecimal total =
                order.getTotalAmount() == null
                        ? BigDecimal.ZERO
                        : order.getTotalAmount();

        BigDecimal paid =
                paymentRepository.sumActiveAfnPaymentsByOrderId(
                        order.getId()
                );

        if (paid == null) {
            paid = BigDecimal.ZERO;
        }

        BigDecimal remaining =
                total.subtract(paid).max(BigDecimal.ZERO);

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("id", order.getId());
        response.put("orderNumber", order.getOrderNumber());
        response.put(
                "customerName",
                order.getCustomer() == null
                        ? ""
                        : order.getCustomer().getFullName()
        );
        response.put("totalAmount", total);
        response.put("depositAmount", order.getDepositAmount());
        response.put("paidAmount", paid);
        response.put("remainingAmount", remaining);
        response.put("status", order.getStatus());
        response.put("createdDate", order.getCreatedDate());
        response.put(
                "workStatus",
                calculateWorkStatus(order.getId())
        );

        return response;
    }

    private Map<String, Object> toItemResponse(
            OrderItem item) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("id", item.getId());
        response.put("productType", item.getProductType());
        response.put("roomName", item.getRoomName());
        response.put("width", item.getWidth());
        response.put("height", item.getHeight());
        response.put("quantity", item.getQuantity());
        response.put("design", item.getDesign());
        response.put("specialNotes", item.getSpecialNotes());
        response.put("workStatus", item.getWorkStatus());
        response.put(
                "requiredCompletionDate",
                item.getRequiredCompletionDate()
        );
        response.put("receivedAt", item.getReceivedAt());
        response.put("completedAt", item.getCompletedAt());

        User tailor = item.getAssignedTailor();

        response.put(
                "assignedTailor",
                tailor == null
                        ? null
                        : toUserResponse(tailor)
        );

        List<Map<String, Object>> fabrics =
                new ArrayList<>();

        for (OrderItemFabric fabric : item.getFabrics()) {
            Map<String, Object> fabricResponse =
                    new LinkedHashMap<>();

            fabricResponse.put("id", fabric.getId());
            fabricResponse.put(
                    "fabricName",
                    fabric.getFabricName()
            );
            fabricResponse.put("color", fabric.getColor());
            fabricResponse.put("lining", fabric.isLining());
            fabricResponse.put("notes", fabric.getNotes());

            fabrics.add(fabricResponse);
        }

        response.put("fabrics", fabrics);

        List<Map<String, Object>> usages =
                new ArrayList<>();

        for (OrderItemInventoryUsage usage :
                item.getInventoryUsages()) {

            InventoryProduct product =
                    usage.getInventoryProduct();

            Map<String, Object> usageResponse =
                    new LinkedHashMap<>();

            usageResponse.put("id", usage.getId());
            usageResponse.put(
                    "estimatedMeterage",
                    usage.getEstimatedMeterage()
            );
            usageResponse.put(
                    "inventoryDeducted",
                    usage.isInventoryDeducted()
            );
            usageResponse.put(
                    "inventoryDeductedAt",
                    usage.getInventoryDeductedAt()
            );

            if (product != null) {
                usageResponse.put(
                        "inventoryProductId",
                        product.getId()
                );
                usageResponse.put(
                        "productName",
                        product.getProductName()
                );
                usageResponse.put(
                        "productCode",
                        product.getProductCode()
                );
                usageResponse.put(
                        "color",
                        product.getColor()
                );
                usageResponse.put(
                        "availableMeterage",
                        product.getMeterage()
                );
            }

            usages.add(usageResponse);
        }

        response.put("inventoryUsages", usages);

        return response;
    }

    private Map<String, Object> toCustomerResponse(
            Customer customer) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("id", customer.getId());
        response.put("fullName", customer.getFullName());
        response.put("phone", customer.getPhone());
        response.put("address", customer.getAddress());

        return response;
    }

    private Map<String, Object> toInventoryResponse(
            InventoryProduct product) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("id", product.getId());
        response.put("productName", product.getProductName());
        response.put("color", product.getColor());
        response.put(
                "availableMeterage",
                product.getMeterage()
        );
        response.put(
                "originalMeterage",
                product.getOriginalMeterage()
        );
        response.put("productCode", product.getProductCode());
        response.put("location", product.getLocation());

        return response;
    }

    private Map<String, Object> toUserResponse(
            User user) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("id", user.getId());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole().name());

        return response;
    }

    private String calculateWorkStatus(Long orderId) {
        List<OrderItem> items =
                orderItemRepository.findByOrderId(orderId);

        if (items.isEmpty()) {
            return "NOT_STARTED";
        }

        boolean allCompleted = items.stream()
                .allMatch(item ->
                        "COMPLETED".equals(
                                item.getWorkStatus()
                        )
                );

        if (allCompleted) {
            return "COMPLETED";
        }

        boolean anyInProgress = items.stream()
                .anyMatch(item ->
                        "IN_PROGRESS".equals(
                                item.getWorkStatus()
                        )
                );

        if (anyInProgress) {
            return "IN_PROGRESS";
        }

        return "NOT_STARTED";
    }

    private Store getStore(String storeCode) {
        if (storeCode == null
                || storeCode.isBlank()
                || "all".equalsIgnoreCase(storeCode)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A specific store is required"
            );
        }

        return storeRepository.findByCode(
                storeCode.trim().toLowerCase(Locale.ROOT)
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Store not found"
                )
        );
    }

    private User findUser(Principal principal) {
        return userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
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