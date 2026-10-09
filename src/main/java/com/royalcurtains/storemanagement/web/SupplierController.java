package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Role;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

@Controller
public class SupplierController {

    private final SupplierRepository supplierRepository;
    private final SupplierPurchaseRepository purchaseRepository;
    private final SupplierPaymentRepository paymentRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public SupplierController(
            SupplierRepository supplierRepository,
            SupplierPurchaseRepository purchaseRepository,
            SupplierPaymentRepository paymentRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.supplierRepository = supplierRepository;
        this.purchaseRepository = purchaseRepository;
        this.paymentRepository = paymentRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    @GetMapping("/suppliers")
    public String suppliers(
            @RequestParam String store,
            @RequestParam(required = false) Long supplierId,
            Model model,
            Principal principal) {

        getLoggedInManager(principal);

        Store selectedStore = findStore(store);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        List<Supplier> suppliers =
                supplierRepository.findByStoreIdOrderByNameAsc(
                        selectedStore.getId()
                );

        List<SupplierPurchase> allPurchases =
                purchaseRepository
                        .findBySupplierStoreIdOrderByPurchaseDateDesc(
                                selectedStore.getId()
                        );

        List<SupplierPayment> allPayments =
                paymentRepository
                        .findBySupplierStoreIdOrderByPaymentDateDesc(
                                selectedStore.getId()
                        );

        Supplier selectedSupplier = null;

        List<SupplierPurchase> displayedPurchases =
                allPurchases;

        List<SupplierPayment> displayedPayments =
                allPayments;

        if (supplierId != null) {

            selectedSupplier = supplierRepository
                    .findByIdAndStoreId(
                            supplierId,
                            selectedStore.getId()
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Supplier does not belong to this store"
                            )
                    );

            displayedPurchases = allPurchases.stream()
                    .filter(purchase ->
                            purchase.getSupplier().getId()
                                    .equals(supplierId)
                    )
                    .toList();

            displayedPayments = allPayments.stream()
                    .filter(payment ->
                            payment.getSupplier().getId()
                                    .equals(supplierId)
                    )
                    .toList();
        }

        Map<Long, String> totalPurchasedBySupplier =
                new HashMap<>();

        Map<Long, String> totalOwedBySupplier =
                new HashMap<>();

        for (Supplier supplier : suppliers) {

            Map<String, BigDecimal> purchasedAmounts =
                    new TreeMap<>();

            Map<String, BigDecimal> paidAmounts =
                    new TreeMap<>();

            for (SupplierPurchase purchase : allPurchases) {

                if (purchase.getSupplier().getId()
                        .equals(supplier.getId())) {

                    String currency =
                            purchase.getCurrency().toUpperCase();

                    purchasedAmounts.put(
                            currency,
                            purchasedAmounts.getOrDefault(
                                    currency,
                                    BigDecimal.ZERO
                            ).add(purchase.getTotalAmount())
                    );
                }
            }

            for (SupplierPayment payment : allPayments) {

                if (payment.getSupplier().getId()
                        .equals(supplier.getId())) {

                    String currency =
                            payment.getCurrency().toUpperCase();

                    paidAmounts.put(
                            currency,
                            paidAmounts.getOrDefault(
                                    currency,
                                    BigDecimal.ZERO
                            ).add(payment.getAmount())
                    );
                }
            }

            Map<String, BigDecimal> owedAmounts =
                    new TreeMap<>(purchasedAmounts);

            for (Map.Entry<String, BigDecimal> entry
                    : paidAmounts.entrySet()) {

                owedAmounts.put(
                        entry.getKey(),
                        owedAmounts.getOrDefault(
                                        entry.getKey(),
                                        BigDecimal.ZERO
                                ).subtract(entry.getValue())
                                .max(BigDecimal.ZERO)
                );
            }

            totalPurchasedBySupplier.put(
                    supplier.getId(),
                    formatAmounts(purchasedAmounts)
            );

            totalOwedBySupplier.put(
                    supplier.getId(),
                    formatAmounts(owedAmounts)
            );
        }

        model.addAttribute("store", selectedStore);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("selectedSupplier", selectedSupplier);
        model.addAttribute("purchases", displayedPurchases);
        model.addAttribute("payments", displayedPayments);
        model.addAttribute(
                "totalPurchasedBySupplier",
                totalPurchasedBySupplier
        );
        model.addAttribute(
                "totalOwedBySupplier",
                totalOwedBySupplier
        );

        return "suppliers";
    }

    @GetMapping("/suppliers/new")
    public String newSupplier(
            @RequestParam String store,
            Principal principal) {

        getLoggedInManager(principal);

        Store selectedStore = findStore(store);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        return "redirect:/suppliers?store="
                + selectedStore.getCode();
    }

    @Transactional
    @PostMapping("/suppliers")
    public String saveSupplier(
            @RequestParam String storeCode,
            @RequestParam String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String notes,
            Principal principal) {

        getLoggedInManager(principal);

        Store selectedStore = findStore(storeCode);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Supplier name is required"
            );
        }

        Supplier supplier = new Supplier();

        supplier.setName(name.trim());
        supplier.setPhone(phone);
        supplier.setAddress(address);
        supplier.setNotes(notes);
        supplier.setStore(selectedStore);

        supplierRepository.save(supplier);

        return "redirect:/suppliers?store="
                + selectedStore.getCode();
    }

    private String formatAmounts(
            Map<String, BigDecimal> amounts) {

        if (amounts.isEmpty()) {
            return "0.00 AFN";
        }

        StringJoiner result = new StringJoiner(", ");

        for (Map.Entry<String, BigDecimal> entry
                : amounts.entrySet()) {

            result.add(
                    entry.getValue().toPlainString()
                            + " "
                            + entry.getKey()
            );
        }

        return result.toString();
    }

    private User getLoggedInManager(Principal principal) {

        User user = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        if (user.getRole() != Role.MANAGER) {
            throw new AccessDeniedException(
                    "Only the manager can manage suppliers"
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
                )
        );
    }
}