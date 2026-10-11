package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.DirectSale;
import com.royalcurtains.storemanagement.model.InventoryMovement;
import com.royalcurtains.storemanagement.model.InventoryProduct;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.DirectSaleRepository;
import com.royalcurtains.storemanagement.repository.InventoryMovementRepository;
import com.royalcurtains.storemanagement.repository.InventoryProductRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class DirectSaleApiController {

    private final DirectSaleRepository directSaleRepository;
    private final InventoryProductRepository inventoryProductRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public DirectSaleApiController(
            DirectSaleRepository directSaleRepository,
            InventoryProductRepository inventoryProductRepository,
            InventoryMovementRepository inventoryMovementRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.directSaleRepository = directSaleRepository;
        this.inventoryProductRepository = inventoryProductRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    @GetMapping("/api/inventory/products")
    public List<InventoryProductResponse> availableProducts(
            @RequestParam String store,
            @RequestParam(required = false, defaultValue = "") String search,
            Principal principal) {

        Store selectedStore = findStore(store);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        List<InventoryProduct> products;

        if (search.isBlank()) {
            products = inventoryProductRepository
                    .findByStoreIdAndActiveTrueOrderByProductNameAsc(
                            selectedStore.getId()
                    );
        } else {
            products = inventoryProductRepository
                    .searchActiveProducts(
                            selectedStore.getId(),
                            search.trim()
                    );
        }

        return products.stream()
                .filter(InventoryProduct::isActive)
                .filter(product ->
                        product.getMeterage() != null
                                && product.getMeterage()
                                .compareTo(BigDecimal.ZERO) > 0
                )
                .map(product ->
                        new InventoryProductResponse(
                                product.getId(),
                                product.getProductName(),
                                product.getColor(),
                                product.getProductCode(),
                                product.getMeterage(),
                                product.getLocation()
                        )
                )
                .toList();
    }

    @Transactional
    @PostMapping("/api/direct-sales")
    public DirectSaleResponse createDirectSale(
            @RequestBody DirectSaleRequest request,
            Principal principal) {

        if (request.storeCode() == null
                || request.storeCode().isBlank()) {
            throw badRequest("Store is required");
        }

        if (request.productId() == null) {
            throw badRequest("Inventory product is required");
        }

        if (request.quantity() == null
                || request.quantity()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequest(
                    "Quantity in meters must be greater than zero"
            );
        }

        if (request.unitPrice() == null
                || request.unitPrice()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequest(
                    "Price per meter must be greater than zero"
            );
        }

        User recordedBy = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        )
                );

        if (recordedBy.getRole() == Role.TAILOR) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tailors cannot record direct sales"
            );
        }

        Store selectedStore = findStore(request.storeCode());

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        InventoryProduct product =
                inventoryProductRepository
                        .findByIdAndStoreId(
                                request.productId(),
                                selectedStore.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Inventory product not found"
                                )
                        );

        if (!product.isActive()) {
            throw badRequest(
                    "This inventory product is inactive"
            );
        }

        BigDecimal availableMeterage = product.getMeterage();

        if (availableMeterage == null
                || request.quantity()
                .compareTo(availableMeterage) > 0) {
            throw badRequest(
                    "The requested amount is greater than the available meterage"
            );
        }

        BigDecimal totalAmount = request.quantity()
                .multiply(request.unitPrice());

        DirectSale sale = new DirectSale();

        sale.setStore(selectedStore);
        sale.setProductName(product.getProductName());
        sale.setQuantity(request.quantity());
        sale.setUnitPrice(request.unitPrice());
        sale.setTotalAmount(totalAmount);
        sale.setCurrency("AFN");
        sale.setSaleDate(
                request.saleDate() == null
                        ? LocalDateTime.now()
                        : request.saleDate()
        );
        sale.setRecordedBy(recordedBy);
        sale.setStatus("ACTIVE");
        sale.setNotes(request.notes());

        DirectSale savedSale =
                directSaleRepository.save(sale);

        product.setMeterage(
                availableMeterage.subtract(request.quantity())
        );
        product.setLastEditedAt(LocalDateTime.now());
        product.setLastEditedBy(recordedBy);

        inventoryProductRepository.save(product);

        InventoryMovement movement = new InventoryMovement();

        movement.setProduct(product);
        movement.setMovementType("DIRECT_SALE");
        movement.setQuantity(request.quantity());
        movement.setReason(
                "Direct sale #" + savedSale.getId()
        );
        movement.setRecordedBy(recordedBy);

        inventoryMovementRepository.save(movement);

        return new DirectSaleResponse(
                savedSale.getId(),
                product.getId(),
                product.getProductName(),
                product.getColor(),
                savedSale.getQuantity(),
                savedSale.getUnitPrice(),
                savedSale.getTotalAmount(),
                savedSale.getCurrency(),
                savedSale.getSaleDate(),
                product.getMeterage(),
                selectedStore.getCode()
        );
    }

    private Store findStore(String storeCode) {
        return storeRepository
                .findByCode(storeCode.toLowerCase())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Store not found"
                        )
                );
    }

    private ResponseStatusException badRequest(
            String message) {

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    public record InventoryProductResponse(
            Long id,
            String productName,
            String color,
            String productCode,
            BigDecimal availableMeterage,
            String location
    ) {
    }

    public record DirectSaleRequest(
            String storeCode,
            Long productId,
            BigDecimal quantity,
            BigDecimal unitPrice,
            LocalDateTime saleDate,
            String notes
    ) {
    }

    public record DirectSaleResponse(
            Long id,
            Long productId,
            String productName,
            String color,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal totalAmount,
            String currency,
            LocalDateTime saleDate,
            BigDecimal remainingMeterage,
            String storeCode
    ) {
    }
}