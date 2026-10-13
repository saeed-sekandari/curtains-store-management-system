package com.royalcurtains.storemanagement.service;

import com.royalcurtains.storemanagement.model.Customer;
import com.royalcurtains.storemanagement.model.InventoryMovement;
import com.royalcurtains.storemanagement.model.InventoryProduct;
import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.OrderItem;
import com.royalcurtains.storemanagement.model.OrderItemFabric;
import com.royalcurtains.storemanagement.model.OrderItemInventoryUsage;
import com.royalcurtains.storemanagement.model.Payment;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.CustomerRepository;
import com.royalcurtains.storemanagement.repository.InventoryMovementRepository;
import com.royalcurtains.storemanagement.repository.InventoryProductRepository;
import com.royalcurtains.storemanagement.repository.OrderItemRepository;
import com.royalcurtains.storemanagement.repository.OrderRepository;
import com.royalcurtains.storemanagement.repository.PaymentRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.web.dto.OrderCreateRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class OrderCreationService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final InventoryProductRepository inventoryProductRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public OrderCreationService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            InventoryProductRepository inventoryProductRepository,
            InventoryMovementRepository inventoryMovementRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.inventoryProductRepository =
                inventoryProductRepository;
        this.inventoryMovementRepository =
                inventoryMovementRepository;
    }

    @Transactional
    public Order createOrder(
            OrderCreateRequest request,
            Store store,
            User recordedBy) {

        validateOrderRequest(request);

        Order existingOrder =
                orderRepository.findByOrderNumber(
                        request.getOrderNumber().trim()
                );

        if (existingOrder != null) {
            throw new IllegalArgumentException(
                    "This order number already exists"
            );
        }

        Customer customer =
                resolveCustomer(request, store);

        BigDecimal totalAmount =
                request.getTotalAmount();

        BigDecimal initialPayment =
                request.getInitialPaymentAmount() == null
                        ? BigDecimal.ZERO
                        : request.getInitialPaymentAmount();

        validatePayment(
                initialPayment,
                totalAmount
        );

        Order order = new Order();

        order.setOrderNumber(
                request.getOrderNumber().trim()
        );
        order.setStore(store);
        order.setCustomer(customer);
        order.setTotalAmount(totalAmount);
        order.setDepositAmount(initialPayment);
        order.setStatus("ACTIVE");

        Order savedOrder =
                orderRepository.save(order);

        List<OrderCreateRequest.ItemRequest> itemRequests =
                request.getItems();

        if (itemRequests != null) {
            for (OrderCreateRequest.ItemRequest itemRequest :
                    itemRequests) {

                createOrderItem(
                        itemRequest,
                        savedOrder,
                        store,
                        recordedBy
                );
            }
        }

        if (initialPayment.compareTo(BigDecimal.ZERO) > 0) {
            createInitialPayment(
                    savedOrder,
                    store,
                    recordedBy,
                    initialPayment,
                    request.getInitialPaymentMethod()
            );
        }

        return savedOrder;
    }

    private void createOrderItem(
            OrderCreateRequest.ItemRequest request,
            Order order,
            Store store,
            User recordedBy) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Order item cannot be empty"
            );
        }

        if (request.getProductType() == null
                || request.getProductType().isBlank()) {
            throw new IllegalArgumentException(
                    "Product type is required"
            );
        }

        int quantity =
                request.getQuantity() == null
                        ? 1
                        : request.getQuantity();

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        OrderItem item = new OrderItem();

        item.setOrder(order);
        item.setProductType(
                request.getProductType().trim()
        );
        item.setRoomName(trim(request.getRoomName()));
        item.setWidth(request.getWidth());
        item.setHeight(request.getHeight());
        item.setQuantity(quantity);
        item.setDesign(trim(request.getDesign()));
        item.setSpecialNotes(
                trim(request.getSpecialNotes())
        );
        item.setRequiredCompletionDate(
                request.getRequiredCompletionDate()
        );

        User tailor =
                assignTailor(
                        item,
                        request.getTailorId(),
                        store
                );

        List<OrderCreateRequest.InventoryUsageRequest>
                usageRequests = request.getInventoryUsages();

        validateUsageRows(usageRequests);

        Set<Long> productIds = new HashSet<>();

        if (usageRequests != null) {
            for (OrderCreateRequest.InventoryUsageRequest usageRequest :
                    usageRequests) {

                if (!productIds.add(
                        usageRequest.getProductId()
                )) {
                    throw new IllegalArgumentException(
                            "The same inventory product cannot be added twice"
                    );
                }

                InventoryProduct product =
                        findActiveProduct(
                                usageRequest.getProductId(),
                                store
                        );

                BigDecimal meterage =
                        usageRequest.getEstimatedMeterage();

                validateInventory(
                        product,
                        meterage
                );

                OrderItemInventoryUsage usage =
                        new OrderItemInventoryUsage();

                usage.setOrderItem(item);
                usage.setInventoryProduct(product);
                usage.setEstimatedMeterage(meterage);

                if (tailor != null) {
                    deductInventory(
                            product,
                            meterage,
                            order,
                            recordedBy
                    );

                    usage.setInventoryDeducted(true);
                    usage.setInventoryDeductedAt(
                            LocalDateTime.now()
                    );
                    usage.setDeductedBy(recordedBy);
                }

                item.getInventoryUsages().add(usage);
            }
        }

        OrderItem savedItem =
                orderItemRepository.save(item);

        List<OrderCreateRequest.FabricRequest>
                fabricRequests = request.getFabrics();

        if (fabricRequests != null) {
            for (OrderCreateRequest.FabricRequest fabricRequest :
                    fabricRequests) {

                if (fabricRequest == null
                        || fabricRequest.getFabricName() == null
                        || fabricRequest.getFabricName()
                        .isBlank()) {
                    continue;
                }

                OrderItemFabric fabric =
                        new OrderItemFabric();

                fabric.setOrderItem(savedItem);
                fabric.setFabricName(
                        fabricRequest.getFabricName().trim()
                );
                fabric.setColor(
                        trim(fabricRequest.getColor())
                );
                fabric.setLining(
                        fabricRequest.isLining()
                );
                fabric.setNotes(
                        trim(fabricRequest.getNotes())
                );

                savedItem.getFabrics().add(fabric);
            }

            orderItemRepository.save(savedItem);
        }
    }

    private Customer resolveCustomer(
            OrderCreateRequest request,
            Store store) {

        if (request.getCustomerId() != null) {
            Customer customer =
                    customerRepository.findById(
                            request.getCustomerId()
                    ).orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Customer not found"
                            )
                    );

            if (customer.getStore() == null
                    || !Objects.equals(
                    customer.getStore().getId(),
                    store.getId()
            )) {
                throw new IllegalArgumentException(
                        "The selected customer does not belong to this store"
                );
            }

            return customer;
        }

        if (request.getNewCustomerName() == null
                || request.getNewCustomerName().isBlank()) {
            throw new IllegalArgumentException(
                    "New customer name is required"
            );
        }

        Customer customer = new Customer();

        customer.setStore(store);
        customer.setFullName(
                request.getNewCustomerName().trim()
        );
        customer.setPhone(
                trim(request.getNewCustomerPhone())
        );
        customer.setAddress(
                trim(request.getNewCustomerAddress())
        );

        return customerRepository.save(customer);
    }

    private User assignTailor(
            OrderItem item,
            Long tailorId,
            Store store) {

        if (tailorId == null) {
            item.setWorkStatus("NOT_STARTED");
            return null;
        }

        User tailor =
                userRepository.findById(tailorId)
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
                store.getId()
        )) {
            throw new IllegalArgumentException(
                    "The tailor must belong to the same store"
            );
        }

        item.setAssignedTailor(tailor);
        item.setReceivedAt(LocalDateTime.now());
        item.setWorkStatus("RECEIVED");

        return tailor;
    }

    private InventoryProduct findActiveProduct(
            Long productId,
            Store store) {

        if (productId == null) {
            throw new IllegalArgumentException(
                    "Inventory product is required"
            );
        }

        InventoryProduct product =
                inventoryProductRepository
                        .findByIdAndStoreId(
                                productId,
                                store.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Selected inventory product was not found"
                                )
                        );

        if (!product.isActive()) {
            throw new IllegalArgumentException(
                    "The selected inventory product is inactive"
            );
        }

        return product;
    }

    private void validateInventory(
            InventoryProduct product,
            BigDecimal requestedMeterage) {

        if (requestedMeterage == null
                || requestedMeterage
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Estimated meterage must be greater than zero"
            );
        }

        BigDecimal available =
                product.getMeterage() == null
                        ? BigDecimal.ZERO
                        : product.getMeterage();

        if (requestedMeterage.compareTo(available) > 0) {
            throw new IllegalArgumentException(
                    "Not enough inventory for "
                            + product.getProductName()
                            + ". Available: "
                            + available
                            + " meters."
            );
        }
    }

    private void deductInventory(
            InventoryProduct product,
            BigDecimal meterage,
            Order order,
            User recordedBy) {

        BigDecimal available =
                product.getMeterage() == null
                        ? BigDecimal.ZERO
                        : product.getMeterage();

        product.setMeterage(
                available.subtract(meterage)
        );
        product.setLastEditedBy(recordedBy);
        product.setLastEditedAt(
                LocalDateTime.now()
        );

        inventoryProductRepository.save(product);

        InventoryMovement movement =
                new InventoryMovement();

        movement.setProduct(product);
        movement.setMovementType("REMOVE");
        movement.setQuantity(meterage);
        movement.setReason(
                "Used for order "
                        + order.getOrderNumber()
        );
        movement.setRecordedBy(recordedBy);

        inventoryMovementRepository.save(movement);
    }

    private void createInitialPayment(
            Order order,
            Store store,
            User recordedBy,
            BigDecimal amount,
            String paymentMethod) {

        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setStore(store);
        payment.setRecordedBy(recordedBy);
        payment.setAmount(amount);
        payment.setCurrency("AFN");
        payment.setPaymentType("DEPOSIT");
        payment.setPaymentMethod(
                paymentMethod == null
                        || paymentMethod.isBlank()
                        ? "CASH"
                        : paymentMethod.trim().toUpperCase()
        );
        payment.setPaymentDate(
                java.time.LocalDate.now()
        );
        payment.setRecordedAt(
                LocalDateTime.now()
        );
        payment.setStatus("ACTIVE");
        payment.setNotes("Initial payment with order");

        paymentRepository.save(payment);
    }

    private void validateOrderRequest(
            OrderCreateRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Order request cannot be empty"
            );
        }

        if (request.getOrderNumber() == null
                || request.getOrderNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Order number is required"
            );
        }

        if (request.getTotalAmount() == null
                || request.getTotalAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Order total must be greater than zero"
            );
        }
    }

    private void validatePayment(
            BigDecimal payment,
            BigDecimal total) {

        if (payment.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Initial payment cannot be negative"
            );
        }

        if (payment.compareTo(total) > 0) {
            throw new IllegalArgumentException(
                    "Initial payment cannot be greater than the order total"
            );
        }
    }

    private void validateUsageRows(
            List<OrderCreateRequest.InventoryUsageRequest>
                    usages) {

        if (usages == null) {
            return;
        }

        for (OrderCreateRequest.InventoryUsageRequest usage :
                usages) {

            if (usage == null
                    || usage.getProductId() == null
                    || usage.getEstimatedMeterage() == null) {
                throw new IllegalArgumentException(
                        "Each inventory product must have a meterage"
                );
            }
        }
    }

    private String trim(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}