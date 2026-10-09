package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.Supplier;
import com.royalcurtains.storemanagement.model.SupplierPayment;
import com.royalcurtains.storemanagement.model.SupplierPurchase;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.SupplierPaymentRepository;
import com.royalcurtains.storemanagement.repository.SupplierPurchaseRepository;
import com.royalcurtains.storemanagement.repository.SupplierRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SupplierPurchaseController {

    private final SupplierPurchaseRepository purchaseRepository;
    private final SupplierPaymentRepository paymentRepository;
    private final SupplierRepository supplierRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public SupplierPurchaseController(
            SupplierPurchaseRepository purchaseRepository,
            SupplierPaymentRepository paymentRepository,
            SupplierRepository supplierRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.purchaseRepository = purchaseRepository;
        this.paymentRepository = paymentRepository;
        this.supplierRepository = supplierRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    // Old purchase page now redirects to the single supplier page.
    @GetMapping("/supplier-purchases")
    public String purchases(
            @RequestParam String store,
            Principal principal) {

        Store selectedStore = findStore(store);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        return "redirect:/suppliers?store="
                + selectedStore.getCode();
    }

    // Old purchase form now redirects to the single supplier page.
    @GetMapping("/supplier-purchases/new")
    public String newPurchase(
            @RequestParam String store,
            Principal principal) {

        Store selectedStore = findStore(store);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        return "redirect:/suppliers?store="
                + selectedStore.getCode();
    }

    // Saves a purchase submitted from the single supplier page.
    @Transactional
    @PostMapping("/supplier-purchases")
    public String savePurchase(
            @RequestParam String storeCode,
            @RequestParam Long supplierId,
            @RequestParam(required = false) String purchaseDate,
            @RequestParam BigDecimal totalAmount,
            @RequestParam String currency,
            @RequestParam String productInformation,
            @RequestParam(required = false) String notes,
            Principal principal) {

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
                                "Supplier does not belong to this store"
                        )
                );

        if (totalAmount == null
                || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Total purchase amount must be greater than zero"
            );
        }

        if (productInformation == null
                || productInformation.isBlank()) {

            throw new IllegalArgumentException(
                    "Product information is required"
            );
        }

        User recordedBy = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        SupplierPurchase purchase = new SupplierPurchase();

        purchase.setSupplier(supplier);
        purchase.setTotalAmount(totalAmount);
        purchase.setAmountPaid(BigDecimal.ZERO);
        purchase.setRemainingDebt(totalAmount);
        purchase.setCurrency(currency.toUpperCase());
        purchase.setProductInformation(
                productInformation.trim()
        );
        purchase.setNotes(notes);
        purchase.setRecordedBy(recordedBy);

        if (purchaseDate != null
                && !purchaseDate.isBlank()) {

            purchase.setPurchaseDate(
                    LocalDateTime.parse(purchaseDate)
            );
        }

        purchaseRepository.save(purchase);

        recalculateSupplierBalances(supplier);

        return "redirect:/suppliers?store="
                + selectedStore.getCode();
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

        Map<String, BigDecimal> remainingPayments =
                new HashMap<>();

        for (SupplierPayment payment : payments) {

            String currency =
                    payment.getCurrency().toUpperCase();

            BigDecimal currentAmount =
                    remainingPayments.getOrDefault(
                            currency,
                            BigDecimal.ZERO
                    );

            remainingPayments.put(
                    currency,
                    currentAmount.add(payment.getAmount())
            );
        }

        for (SupplierPurchase purchase : purchases) {

            String currency =
                    purchase.getCurrency().toUpperCase();

            BigDecimal availablePayment =
                    remainingPayments.getOrDefault(
                            currency,
                            BigDecimal.ZERO
                    );

            BigDecimal purchaseTotal =
                    purchase.getTotalAmount();

            BigDecimal appliedPayment =
                    availablePayment.min(purchaseTotal);

            purchase.setAmountPaid(appliedPayment);
            purchase.setRemainingDebt(
                    purchaseTotal.subtract(appliedPayment)
            );

            remainingPayments.put(
                    currency,
                    availablePayment.subtract(appliedPayment)
            );

            purchaseRepository.save(purchase);
        }
    }

    private Store findStore(String storeCode) {
        return storeRepository.findByCode(
                storeCode.toLowerCase()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Store not found"
                )
        );
    }
}