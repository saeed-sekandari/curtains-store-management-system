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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SupplierPaymentController {

    private final SupplierPaymentRepository paymentRepository;
    private final SupplierPurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public SupplierPaymentController(
            SupplierPaymentRepository paymentRepository,
            SupplierPurchaseRepository purchaseRepository,
            SupplierRepository supplierRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.paymentRepository = paymentRepository;
        this.purchaseRepository = purchaseRepository;
        this.supplierRepository = supplierRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    @GetMapping("/supplier-payments")
    public String payments(
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

    @GetMapping("/supplier-payments/new")
    public String newPayment(
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

    @Transactional
    @PostMapping("/supplier-payments")
    public String savePayment(
            @RequestParam String storeCode,
            @RequestParam Long supplierId,
            @RequestParam(required = false) String paymentDate,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam String receivedBy,
            @RequestParam(required = false) String notes,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Store selectedStore = findStore(storeCode);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        try {
            Supplier supplier = supplierRepository
                    .findByIdAndStoreId(
                            supplierId,
                            selectedStore.getId()
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "The selected supplier was not found."
                            )
                    );

            if (amount == null
                    || amount.compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalArgumentException(
                        "Payment amount must be greater than zero."
                );
            }

            if (receivedBy == null || receivedBy.isBlank()) {
                throw new IllegalArgumentException(
                        "Please enter the name of the person who received the money."
                );
            }

            String normalizedCurrency =
                    currency.toUpperCase();

            BigDecimal outstandingDebt =
                    calculateOutstandingDebt(
                            supplier,
                            normalizedCurrency
                    );

            if (outstandingDebt.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "This supplier has no remaining "
                                + normalizedCurrency
                                + " debt. Select USD if the debt is in USD, "
                                + "or record an AFN purchase first."
                );
            }

            if (amount.compareTo(outstandingDebt) > 0) {
                throw new IllegalArgumentException(
                        "The payment cannot be greater than the remaining "
                                + normalizedCurrency
                                + " supplier debt of "
                                + outstandingDebt
                                + " "
                                + normalizedCurrency
                                + "."
                );
            }

            User recordedBy = userRepository
                    .findByUsername(principal.getName())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "The logged-in manager was not found."
                            )
                    );

            SupplierPayment payment = new SupplierPayment();

            payment.setSupplier(supplier);
            payment.setAmount(amount);
            payment.setCurrency(normalizedCurrency);
            payment.setReceivedBy(receivedBy.trim());
            payment.setRecordedBy(recordedBy);
            payment.setNotes(notes);

            if (paymentDate != null
                    && !paymentDate.isBlank()) {

                payment.setPaymentDate(
                        LocalDateTime.parse(paymentDate)
                );
            }

            paymentRepository.save(payment);

            recalculateSupplierBalances(supplier);

            return "redirect:/suppliers?store="
                    + selectedStore.getCode();

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

            return "redirect:/suppliers?store="
                    + selectedStore.getCode();
        }
    }

    private BigDecimal calculateOutstandingDebt(
            Supplier supplier,
            String currency) {

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

        BigDecimal totalPurchases = purchases.stream()
                .filter(purchase ->
                        currency.equalsIgnoreCase(
                                purchase.getCurrency()
                        )
                )
                .map(SupplierPurchase::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPayments = payments.stream()
                .filter(payment ->
                        currency.equalsIgnoreCase(
                                payment.getCurrency()
                        )
                )
                .map(SupplierPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalPurchases
                .subtract(totalPayments)
                .max(BigDecimal.ZERO);
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
                        "Store not found."
                )
        );
    }
}