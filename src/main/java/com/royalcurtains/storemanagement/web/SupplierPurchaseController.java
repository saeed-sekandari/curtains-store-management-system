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
import java.util.List;

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
            @RequestParam(required = false) List<Long> inventoryProductIds,
            @RequestParam(required = false) List<BigDecimal> purchaseMeterages,
            @RequestParam(required = false) List<BigDecimal> pricePerMeters,
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

        validatePurchase(
                totalAmount,
                currency,
                inventoryProductIds,
                purchaseMeterages,
                pricePerMeters
        );

        SupplierPurchase purchase = new SupplierPurchase();
        purchase.setSupplier(supplier);
        purchase.setPurchaseDate(parseDate(purchaseDate));
        purchase.setTotalAmount(totalAmount);
        purchase.setAmountPaid(BigDecimal.ZERO);
        purchase.setRemainingDebt(totalAmount);
        purchase.setCurrency(currency.trim().toUpperCase());
        purchase.setProductInformation(productInformation);
        purchase.setNotes(notes);
        purchase.setRecordedBy(manager);

        purchaseRepository.save(purchase);

        for (int index = 0;
             index < inventoryProductIds.size();
             index++) {

            InventoryProduct product =
                    inventoryProductRepository
                            .findByIdAndStoreId(
                                    inventoryProductIds.get(index),
                                    selectedStore.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Inventory product not found"
                                    ));

            BigDecimal meterage =
                    purchaseMeterages.get(index);

            BigDecimal pricePerMeter =
                    pricePerMeters.get(index);

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

            SupplierPurchaseItem item =
                    new SupplierPurchaseItem();

            item.setPurchase(purchase);
            item.setInventoryProduct(product);
            item.setMeterage(meterage);
            item.setPricePerMeter(pricePerMeter);

            purchase.getPurchaseItems().add(item);

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
        }

        purchaseRepository.save(purchase);

        recalculateSupplierBalances(supplier);

        return "redirect:/suppliers?store="
                + selectedStore.getCode();
    }

    private void validatePurchase(
            BigDecimal totalAmount,
            String currency,
            List<Long> productIds,
            List<BigDecimal> meterages,
            List<BigDecimal> prices) {

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

        if (productIds == null
                || productIds.isEmpty()
                || meterages == null
                || prices == null
                || productIds.size() != meterages.size()
                || productIds.size() != prices.size()) {

            throw new IllegalArgumentException(
                    "Every purchase product must have meterage and price"
            );
        }

        for (int index = 0;
             index < productIds.size();
             index++) {

            if (productIds.get(index) == null) {
                throw new IllegalArgumentException(
                        "Please select an inventory product"
                );
            }

            if (meterages.get(index) == null
                    || meterages.get(index)
                    .compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalArgumentException(
                        "Purchased meterage must be greater than zero"
                );
            }

            if (prices.get(index) == null
                    || prices.get(index)
                    .compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "Price per meter cannot be negative"
                );
            }
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
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Store not found"
                        ));
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