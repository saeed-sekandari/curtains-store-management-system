package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.ExpenseRecord;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.ExpenseRecordRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class VoidedExpenseController {

    private final ExpenseRecordRepository expenseRecordRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public VoidedExpenseController(
            ExpenseRecordRepository expenseRecordRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.expenseRecordRepository = expenseRecordRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    // Shows voided financial records for the manager.
    @GetMapping("/expenses/voided")
    public String voidedExpenses(
            @RequestParam String store,
            Model model,
            Principal principal) {

        User currentUser = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        if (currentUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException(
                    "Only the manager can view voided records"
            );
        }

        Store selectedStore;

        if ("all".equalsIgnoreCase(store)) {
            selectedStore = null;
        } else {
            selectedStore = storeRepository
                    .findByCode(store.toLowerCase())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Store not found"
                            )
                    );

            storeAccessService.checkStoreAccess(
                    principal,
                    selectedStore.getCode()
            );
        }

        List<ExpenseRecord> records;

        if (selectedStore == null) {
            records = expenseRecordRepository
                    .findByStatusOrderByExpenseDateDesc("VOIDED");
        } else {
            records = expenseRecordRepository
                    .findByStoreIdAndStatusOrderByExpenseDateDesc(
                            selectedStore.getId(),
                            "VOIDED"
                    );
        }

        model.addAttribute(
                "selectedStore",
                selectedStore == null
                        ? "All Stores"
                        : selectedStore.getName()
        );

        model.addAttribute(
                "selectedStoreCode",
                selectedStore == null
                        ? "all"
                        : selectedStore.getCode()
        );

        model.addAttribute(
                "records",
                records
        );

        return "voided-expenses";
    }
}