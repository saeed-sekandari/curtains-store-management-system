package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.InventoryMovement;
import com.royalcurtains.storemanagement.model.InventoryProduct;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.Supplier;
import com.royalcurtains.storemanagement.model.SupplierPayment;
import com.royalcurtains.storemanagement.model.SupplierPurchase;
import com.royalcurtains.storemanagement.model.SupplierPurchaseItem;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.InventoryMovementRepository;
import com.royalcurtains.storemanagement.repository.InventoryProductRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.SupplierPaymentRepository;
import com.royalcurtains.storemanagement.repository.SupplierPurchaseRepository;
import com.royalcurtains.storemanagement.repository.SupplierRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class SupplierPurchaseController {

    private final SupplierPurchaseRepository purchaseRepository;
    private final SupplierPaymentRepository paymentRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryProductRepository inventoryProductRepository;
    private final InventoryMovementRepository movementRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public SupplierPurchaseController(
            SupplierPurchaseRepository purchaseRepository,
            SupplierPaymentRepository paymentRepository,
            SupplierRepository supplierRepository,
            InventoryProductRepository inventoryProductRepository,
            InventoryMovementRepository movementRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.purchaseRepository = purchaseRepository;
        this.paymentRepository = paymentRepository;
        this.supplierRepository = supplierRepository;
        this.inventoryProductRepository = inventoryProductRepository;
        this.movementRepository = movementRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    @GetMapping("/supplier-purchases")
    public String purchases(@RequestParam String store) {
        return "redirect:/suppliers?store=" + store;
    }

    @GetMapping("/supplier-purchases/new")
    public String newPurchase(@RequestParam String store) {
        return "redirect:/suppliers?store=" + store;
    }

    @Transactional
    @PostMapping("/supplier-purchases")
    public String savePurchase(
            @RequestParam String storeCode,
            @RequestParam Long supplierId,
            @RequestParam(required = false) String purchaseDate,
            @RequestParam BigDecimal totalAmount,
            @RequestParam String currency,
            @RequestParam(required = false) String productInformation,
            @RequestParam(required = false) String notes,

            @RequestParam(required = false)
            List<Long> inventoryProductIds,

            @RequestParam(required = false)
            List<BigDecimal> purchaseMeterages,

            @RequestParam(required = false)
            List<BigDecimal> pricePerMeters,

            @RequestParam(required = false)
            List<String> newProductNames,

            @RequestParam(required = false)
            List<String> newProductColors,

            @RequestParam(required = false)
            List<BigDecimal> newProductMeterages,

            @RequestParam(required = false)
            List<String> newProductLocations,

            @RequestParam(required = false)
            List<String> newProductCodes,

            @RequestParam(required = false)
            List<BigDecimal> newProductPricesPerMeter,

            Principal principal) {

        User manager = getLoggedInManager(principal);
        Store selectedStore = findStore(storeCode);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        Supplier supplier = supplierRepository
                .findByIdAndStoreId(
                        supplierId,
                        selectedStore.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Supplier not found"
                        ));

        List<Long> productIds = copyList(inventoryProductIds);
        List<BigDecimal> meterages = copyList(purchaseMeterages);
        List<BigDecimal> prices = copyList(pricePerMeters);

        List<String> names = copyList(newProductNames);
        List<String> colors = copyList(newProductColors);
        List<BigDecimal> newMeterages =
                copyList(newProductMeterages);
        List<String> locations =
                copyList(newProductLocations);
        List<String> codes =
                copyList(newProductCodes);
        List<BigDecimal> newPrices =
                copyList(newProductPricesPerMeter);

        validatePurchase(
                totalAmount,
                currency,
                productIds,
                meterages,
                prices,
                names,
                colors,
                newMeterages,
                locations,
                codes,
                newPrices
        );

        Set<Long> newlyCreatedProductIds =
                new HashSet<>();

        for (int index = 0; index < names.size(); index++) {

            String cleanedCode = codes.get(index).trim();

            if (inventoryProductRepository
                    .existsByProductCodeIgnoreCase(cleanedCode)) {

                throw new IllegalArgumentException(
                        "Product code is already being used: "
                                + cleanedCode
                );
            }

            InventoryProduct newProduct =
                    new InventoryProduct();

            newProduct.setProductName(
                    names.get(index).trim()
            );
            newProduct.setColor(
                    cleanValue(colors.get(index))
            );
            newProduct.setOriginalMeterage(
                    newMeterages.get(index)
            );
            newProduct.setMeterage(
                    newMeterages.get(index)
            );
            newProduct.setLocation(
                    cleanValue(locations.get(index))
            );
            newProduct.setProductCode(cleanedCode);
            newProduct.setStore(selectedStore);
            newProduct.setAddedBy(manager);

            InventoryProduct savedProduct =
                    inventoryProductRepository.save(newProduct);

            productIds.add(savedProduct.getId());
            meterages.add(newMeterages.get(index));
            prices.add(newPrices.get(index));

            newlyCreatedProductIds.add(savedProduct.getId());
        }

        SupplierPurchase purchase =
                new SupplierPurchase();

        purchase.setSupplier(supplier);
        purchase.setPurchaseDate(
                parseDate(purchaseDate)
        );
        purchase.setTotalAmount(totalAmount);
        purchase.setAmountPaid(BigDecimal.ZERO);
        purchase.setRemainingDebt(totalAmount);
        purchase.setCurrency(
                currency.trim().toUpperCase()
        );
        purchase.setProductInformation(productInformation);
        purchase.setNotes(notes);
        purchase.setRecordedBy(manager);

        purchaseRepository.save(purchase);

        for (int index = 0;
             index < productIds.size();
             index++) {

            Long productId = productIds.get(index);

            InventoryProduct product =
                    inventoryProductRepository
                            .findByIdAndStoreId(
                                    productId,
                                    selectedStore.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Inventory product not found"
                                    ));

            BigDecimal meterage =
                    meterages.get(index);

            BigDecimal pricePerMeter =
                    prices.get(index);

            if (!newlyCreatedProductIds.contains(productId)) {

                BigDecimal original =
                        product.getOriginalMeterage();

                if (original == null) {
                    original = product.getMeterage();
                }

                BigDecimal available =
                        product.getMeterage();

                if (available == null) {
                    available = BigDecimal.ZERO;
                }

                product.setOriginalMeterage(
                        original.add(meterage)
                );

                product.setMeterage(
                        available.add(meterage)
                );

                product.setLastEditedBy(manager);
                product.setLastEditedAt(LocalDateTime.now());

                inventoryProductRepository.save(product);
            }

            InventoryMovement movement =
                    new InventoryMovement();

            movement.setProduct(product);
            movement.setMovementType("ADD");
            movement.setQuantity(meterage);
            movement.setReason(
                    "Supplier purchase from "
                            + supplier.getName()
            );
            movement.setRecordedBy(manager);

            movementRepository.save(movement);

            SupplierPurchaseItem item =
                    new SupplierPurchaseItem();

            item.setPurchase(purchase);
            item.setInventoryProduct(product);
            item.setMeterage(meterage);
            item.setPricePerMeter(pricePerMeter);

            purchase.getPurchaseItems().add(item);
        }

        purchaseRepository.save(purchase);

        recalculateSupplierBalances(supplier);

        return "redirect:/suppliers?store="
                + selectedStore.getCode();
    }

    private void validatePurchase(
            BigDecimal totalAmount,
            String currency,
            List<Long> existingIds,
            List<BigDecimal> existingMeterages,
            List<BigDecimal> existingPrices,
            List<String> names,
            List<String> colors,
            List<BigDecimal> newMeterages,
            List<String> locations,
            List<String> codes,
            List<BigDecimal> newPrices) {

        if (totalAmount == null
                || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Total purchase amount must be greater than zero"
            );
        }

        if (currency == null
                || (!currency.equalsIgnoreCase("AFN")
                && !currency.equalsIgnoreCase("USD"))) {

            throw new IllegalArgumentException(
                    "Currency must be AFN or USD"
            );
        }

        if (existingIds.size() != existingMeterages.size()
                || existingIds.size() != existingPrices.size()) {

            throw new IllegalArgumentException(
                    "Each existing product must have meterage and price"
            );
        }

        for (int index = 0;
             index < existingIds.size();
             index++) {

            if (existingIds.get(index) == null) {
                throw new IllegalArgumentException(
                        "Please select an existing product"
                );
            }

            validatePositive(
                    existingMeterages.get(index),
                    "Purchased meterage"
            );

            validateNonNegative(
                    existingPrices.get(index),
                    "Price per meter"
            );
        }

        if (names.size() != colors.size()
                || names.size() != newMeterages.size()
                || names.size() != locations.size()
                || names.size() != codes.size()
                || names.size() != newPrices.size()) {

            throw new IllegalArgumentException(
                    "Each new product must have all required fields"
            );
        }

        for (int index = 0;
             index < names.size();
             index++) {

            if (names.get(index) == null
                    || names.get(index).isBlank()) {

                throw new IllegalArgumentException(
                        "New product name is required"
                );
            }

            validatePositive(
                    newMeterages.get(index),
                    "New product meterage"
            );

            if (codes.get(index) == null
                    || codes.get(index).isBlank()) {

                throw new IllegalArgumentException(
                        "New product code is required"
                );
            }

            validateNonNegative(
                    newPrices.get(index),
                    "New product price per meter"
            );
        }

        if (existingIds.isEmpty() && names.isEmpty()) {
            throw new IllegalArgumentException(
                    "Add at least one product to this purchase"
            );
        }
    }

    private void validatePositive(
            BigDecimal value,
            String fieldName) {

        if (value == null
                || value.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    fieldName + " must be greater than zero"
            );
        }
    }

    private void validateNonNegative(
            BigDecimal value,
            String fieldName) {

        if (value == null
                || value.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    fieldName + " cannot be negative"
            );
        }
    }

    private LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDateTime.now();
        }

        return LocalDateTime.parse(value);
    }

    private User getLoggedInManager(Principal principal) {
        User user = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        ));

        if (user.getRole() != Role.MANAGER) {
            throw new AccessDeniedException(
                    "Only the manager can record supplier purchases"
            );
        }

        return user;
    }

    private Store findStore(String storeCode) {
        return storeRepository.findByCode(
                storeCode.toLowerCase()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Store not found"
                ));
    }

    private String cleanValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private <T> List<T> copyList(List<T> values) {
        if (values == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(values);
    }

    private void recalculateSupplierBalances(
            Supplier supplier) {

        List<SupplierPurchase> purchases =
                purchaseRepository
                        .findBySupplierIdOrderByPurchaseDateAsc(
                                supplier.getId()
                        );

        List<SupplierPayment> payments =
                paymentRepository
                        .findBySupplierIdOrderByPaymentDateDesc(
                                supplier.getId()
                        );

        payments.sort(
                (first, second) ->
                        first.getPaymentDate()
                                .compareTo(second.getPaymentDate())
        );

        for (SupplierPurchase purchase : purchases) {
            purchase.setAmountPaid(BigDecimal.ZERO);
            purchase.setRemainingDebt(
                    purchase.getTotalAmount()
            );
        }

        for (SupplierPayment payment : payments) {

            BigDecimal remainingPayment =
                    payment.getAmount();

            for (SupplierPurchase purchase : purchases) {

                if (!purchase.getCurrency()
                        .equalsIgnoreCase(
                                payment.getCurrency()
                        )) {
                    continue;
                }

                BigDecimal debt =
                        purchase.getRemainingDebt();

                if (debt.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal applied =
                        remainingPayment.min(debt);

                purchase.setAmountPaid(
                        purchase.getAmountPaid()
                                .add(applied)
                );

                purchase.setRemainingDebt(
                        debt.subtract(applied)
                );

                remainingPayment =
                        remainingPayment.subtract(applied);

                if (remainingPayment
                        .compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
            }
        }

        purchaseRepository.saveAll(purchases);
    }
}