package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.ExpenseRecord;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.ExpenseRecordRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseApiController {

    private final ExpenseRecordRepository expenseRecordRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public ExpenseApiController(
            ExpenseRecordRepository expenseRecordRepository,
            StoreRepository storeRepository,
            UserRepository userRepository) {

        this.expenseRecordRepository = expenseRecordRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createExpense(
            @RequestBody ExpenseRequest request,
            Principal principal) {

        if (request.storeCode() == null
                || request.storeCode().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Store is required")
            );
        }

        if (request.category() == null
                || request.category().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Expense category is required")
            );
        }

        if (request.amount() == null
                || request.amount().compareTo(BigDecimal.ZERO) <= 0) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Amount must be greater than zero")
            );
        }

        if (request.currency() == null
                || request.currency().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Currency is required")
            );
        }

        Store store = storeRepository
                .findByCode(request.storeCode().toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException("Store not found")
                );

        User recordedBy = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (request.expenseDate() == null
                || request.expenseDate().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Expense date is required")
            );
        }

        LocalDateTime expenseDate;

        try {
            expenseDate = LocalDateTime.parse(
                    request.expenseDate()
            );
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Invalid expense date")
            );
        }

        ExpenseRecord expense = new ExpenseRecord();

        expense.setStore(store);
        expense.setCategory(request.category());
        expense.setRecipientName(request.recipientName());
        expense.setAmount(request.amount());
        expense.setCurrency(request.currency().toUpperCase());
        expense.setExpenseDate(expenseDate);
        expense.setDescription(request.description());
        expense.setRecordedBy(recordedBy);
        expense.setStatus("ACTIVE");

        ExpenseRecord savedExpense =
                expenseRecordRepository.save(expense);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "id", savedExpense.getId(),
                        "message", "Expense saved successfully"
                ));
    }

    public record ExpenseRequest(
            String storeCode,
            String category,
            String recipientName,
            BigDecimal amount,
            String currency,
            String expenseDate,
            String description
    ) {
    }
}