package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.InventoryMovement;
import com.royalcurtains.storemanagement.model.InventoryProduct;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.InventoryMovementRepository;
import com.royalcurtains.storemanagement.repository.InventoryProductRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Transactional
public class InventoryApiController {

    private final InventoryProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public InventoryApiController(
            InventoryProductRepository productRepository,
            InventoryMovementRepository movementRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.productRepository = productRepository;
        this.movementRepository = movementRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    @GetMapping
    public List<InventoryProductResponse> getProducts(
            @RequestParam String store,
            @RequestParam(required = false, defaultValue = "") String search,
            Principal principal) {

        if ("all".equalsIgnoreCase(store)) {
            User user = findUser(principal);

            if (user.getRole() != Role.MANAGER) {
                throw new AccessDeniedException(
                        "Only managers can view all stores"
                );
            }

            List<InventoryProduct> products =
                    productRepository
                            .findByActiveTrueOrderByProductNameAsc();

            if (search != null && !search.isBlank()) {
                String searchText = search.trim().toLowerCase();

                products = products.stream()
                        .filter(product ->
                                contains(
                                        product.getProductName(),
                                        searchText
                                )
                                        || contains(
                                        product.getColor(),
                                        searchText
                                )
                                        || contains(
                                        product.getLocation(),
                                        searchText
                                )
                                        || contains(
                                        product.getProductCode(),
                                        searchText
                                )
                        )
                        .toList();
            }

            return products.stream()
                    .map(this::toProductResponse)
                    .toList();
        }

        Store selectedStore = findStore(store);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        List<InventoryProduct> products;

        if (search == null || search.isBlank()) {
            products = productRepository
                    .findByStoreIdAndActiveTrueOrderByProductNameAsc(
                            selectedStore.getId()
                    );
        } else {
            products = productRepository.searchActiveProducts(
                    selectedStore.getId(),
                    search.trim()
            );
        }

        return products.stream()
                .map(this::toProductResponse)
                .toList();
    }

    @GetMapping("/{productId}")
    public InventoryProductResponse getProduct(
            @PathVariable Long productId,
            @RequestParam String store,
            Principal principal) {

        InventoryProduct product =
                findAccessibleProduct(
                        productId,
                        store,
                        principal
                );

        return toProductResponse(product);
    }

    @GetMapping("/{productId}/movements")
    public List<InventoryMovementResponse> getMovements(
            @PathVariable Long productId,
            @RequestParam String store,
            Principal principal) {

        InventoryProduct product =
                findAccessibleProduct(
                        productId,
                        store,
                        principal
                );

        return movementRepository
                .findByProductIdOrderByCreatedAtDesc(product.getId())
                .stream()
                .map(this::toMovementResponse)
                .toList();
    }

    @Transactional
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryProductResponse createProduct(
            @RequestBody InventoryProductRequest request,
            Principal principal) {

        User currentUser = findUser(principal);

        requireInventoryEditor(currentUser);
        validateRequest(request);

        Store selectedStore = findStore(request.storeCode());

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        String cleanedCode = request.productCode().trim();

        if (productRepository.existsByProductCodeIgnoreCase(
                cleanedCode
        )) {
            throw new IllegalArgumentException(
                    "Product code already exists"
            );
        }

        InventoryProduct product = new InventoryProduct();

        product.setProductName(request.productName().trim());
        product.setColor(clean(request.color()));
        product.setOriginalMeterage(request.meterage());
        product.setMeterage(request.meterage());
        product.setLocation(clean(request.location()));
        product.setProductCode(cleanedCode);
        product.setStore(selectedStore);
        product.setAddedBy(currentUser);
        product.setActive(true);

        InventoryProduct savedProduct =
                productRepository.save(product);

        InventoryMovement movement = new InventoryMovement();

        movement.setProduct(savedProduct);
        movement.setMovementType("INITIAL_STOCK");
        movement.setQuantity(request.meterage());
        movement.setReason(
                "Initial inventory product created with "
                        + request.meterage()
                        + " meters"
        );
        movement.setRecordedBy(currentUser);

        movementRepository.save(movement);

        return toProductResponse(savedProduct);
    }

    @Transactional
    @PutMapping("/{productId}")
    public InventoryProductResponse updateProduct(
            @PathVariable Long productId,
            @RequestParam String store,
            @RequestBody InventoryProductRequest request,
            Principal principal) {

        User currentUser = findUser(principal);

        requireInventoryEditor(currentUser);
        validateRequest(request);

        InventoryProduct product =
                findAccessibleProduct(
                        productId,
                        store,
                        principal
                );

        Store oldStore = product.getStore();
        Store newStore = findStore(request.storeCode());

        storeAccessService.checkStoreAccess(
                principal,
                newStore.getCode()
        );

        if (!product.isActive()) {
            throw new IllegalStateException(
                    "This inventory product is no longer active"
            );
        }

        String cleanedCode = request.productCode().trim();

        if (productRepository
                .existsByProductCodeIgnoreCaseAndIdNot(
                        cleanedCode,
                        productId
                )) {

            throw new IllegalArgumentException(
                    "Product code already exists"
            );
        }

        List<String> changes = new ArrayList<>();

        BigDecimal oldMeterage =
                valueOrZero(product.getMeterage());

        BigDecimal newMeterage =
                request.meterage();

        if (!equalsText(
                product.getProductName(),
                request.productName()
        )) {
            changes.add(
                    "Product name changed from '"
                            + safe(product.getProductName())
                            + "' to '"
                            + request.productName().trim()
                            + "'"
            );
        }

        if (!equalsText(
                product.getColor(),
                request.color()
        )) {
            changes.add(
                    "Color changed from '"
                            + safe(product.getColor())
                            + "' to '"
                            + safe(request.color())
                            + "'"
            );
        }

        if (!equalsText(
                product.getLocation(),
                request.location()
        )) {
            changes.add(
                    "Location changed from '"
                            + safe(product.getLocation())
                            + "' to '"
                            + safe(request.location())
                            + "'"
            );
        }

        if (!equalsText(
                product.getProductCode(),
                request.productCode()
        )) {
            changes.add(
                    "Product code changed from '"
                            + safe(product.getProductCode())
                            + "' to '"
                            + cleanedCode
                            + "'"
            );
        }

        if (oldMeterage.compareTo(newMeterage) != 0) {
            changes.add(
                    "Available meterage changed from "
                            + oldMeterage
                            + " meters to "
                            + newMeterage
                            + " meters"
            );
        }

        if (!oldStore.getCode()
                .equalsIgnoreCase(newStore.getCode())) {

            changes.add(
                    "Store changed from '"
                            + oldStore.getName()
                            + "' to '"
                            + newStore.getName()
                            + "'"
            );
        }

        product.setProductName(request.productName().trim());
        product.setColor(clean(request.color()));
        product.setMeterage(newMeterage);
        product.setLocation(clean(request.location()));
        product.setProductCode(cleanedCode);
        product.setStore(newStore);
        product.setLastEditedBy(currentUser);
        product.setLastEditedAt(LocalDateTime.now());

        InventoryProduct savedProduct =
                productRepository.save(product);

        if (!changes.isEmpty()) {
            InventoryMovement movement =
                    new InventoryMovement();

            movement.setProduct(savedProduct);
            movement.setMovementType("CORRECTION");
            movement.setQuantity(
                    newMeterage.subtract(oldMeterage)
            );
            movement.setReason(
                    String.join("; ", changes)
            );
            movement.setRecordedBy(currentUser);

            movementRepository.save(movement);
        }

        return toProductResponse(savedProduct);
    }

    private InventoryProduct findAccessibleProduct(
            Long productId,
            String storeCode,
            Principal principal) {

        if ("all".equalsIgnoreCase(storeCode)) {
            User currentUser = findUser(principal);

            if (currentUser.getRole() != Role.MANAGER) {
                throw new AccessDeniedException(
                        "Only managers can access all stores"
                );
            }

            return productRepository
                    .findById(productId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Inventory product not found"
                            )
                    );
        }

        Store selectedStore =
                findStore(storeCode);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        return productRepository
                .findByIdAndStoreId(
                        productId,
                        selectedStore.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory product not found"
                        )
                );
    }

    private void validateRequest(
            InventoryProductRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Product information is required"
            );
        }

        if (request.productName() == null
                || request.productName().isBlank()) {

            throw new IllegalArgumentException(
                    "Product name is required"
            );
        }

        if (request.productCode() == null
                || request.productCode().isBlank()) {

            throw new IllegalArgumentException(
                    "Product code is required"
            );
        }

        if (request.meterage() == null
                || request.meterage()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Meterage cannot be negative"
            );
        }

        if (request.storeCode() == null
                || request.storeCode().isBlank()
                || "all".equalsIgnoreCase(
                request.storeCode()
        )) {

            throw new IllegalArgumentException(
                    "A specific store is required"
            );
        }
    }

    private void requireInventoryEditor(User user) {
        if (user.getRole() != Role.MANAGER
                && user.getRole() != Role.EMPLOYEE) {

            throw new AccessDeniedException(
                    "Only managers and employees can edit inventory"
            );
        }
    }

    private Store findStore(String storeCode) {
        return storeRepository
                .findByCode(storeCode.toLowerCase())
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

    private InventoryProductResponse toProductResponse(
            InventoryProduct product) {

        return new InventoryProductResponse(
                product.getId(),
                product.getProductName(),
                safe(product.getColor()),
                valueOrZero(
                        product.getOriginalMeterage()
                ),
                valueOrZero(
                        product.getMeterage()
                ),
                safe(product.getLocation()),
                product.getProductCode(),
                product.getStore().getCode(),
                product.getStore().getName(),
                fullName(product.getAddedBy()),
                product.getAddedAt(),
                fullName(product.getLastEditedBy()),
                product.getLastEditedAt(),
                product.isActive()
        );
    }

    private InventoryMovementResponse toMovementResponse(
            InventoryMovement movement) {

        return new InventoryMovementResponse(
                movement.getId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getReason(),
                movement.getCreatedAt(),
                fullName(movement.getRecordedBy())
        );
    }

    private boolean contains(
            String value,
            String search) {

        return value != null
                && value.toLowerCase().contains(search);
    }

    private BigDecimal valueOrZero(
            BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String clean(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private String safe(String value) {
        return value == null
                ? ""
                : value;
    }

    private boolean equalsText(
            String first,
            String second) {

        return safe(first)
                .trim()
                .equals(
                        safe(second).trim()
                );
    }

    private String fullName(User user) {
        return user == null
                ? ""
                : user.getFullName();
    }

    public record InventoryProductRequest(
            String storeCode,
            String productName,
            String color,
            BigDecimal meterage,
            String location,
            String productCode
    ) {
    }

    public record InventoryProductResponse(
            Long id,
            String productName,
            String color,
            BigDecimal originalMeterage,
            BigDecimal availableMeterage,
            String location,
            String productCode,
            String storeCode,
            String storeName,
            String addedBy,
            LocalDateTime addedAt,
            String lastEditedBy,
            LocalDateTime lastEditedAt,
            boolean active
    ) {
    }

    public record InventoryMovementResponse(
            Long id,
            String movementType,
            BigDecimal quantity,
            String reason,
            LocalDateTime createdAt,
            String recordedBy
    ) {
    }
}