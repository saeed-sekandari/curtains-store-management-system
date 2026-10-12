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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseReportApiController {

    private final ExpenseRecordRepository expenseRecordRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public ExpenseReportApiController(
            ExpenseRecordRepository expenseRecordRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.expenseRecordRepository = expenseRecordRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    @GetMapping("/report")
    public ExpenseReportResponse report(
            @RequestParam(defaultValue = "all")
            String store,

            @RequestParam
            String date,

            @RequestParam(defaultValue = "today")
            String period,

            Principal principal) {

        User currentUser = findUser(principal);

        if (currentUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException(
                    "Only managers can view expense reports"
            );
        }

        Store selectedStore = findSelectedStore(store);

        if (selectedStore != null) {
            storeAccessService.checkStoreAccess(
                    principal,
                    selectedStore.getCode()
            );
        }

        LocalDate selectedDate =
                LocalDate.parse(date);

        DateRange range = calculateDateRange(
                selectedDate,
                period
        );

        List<ExpenseRecord> records;

        if (selectedStore == null) {
            records = expenseRecordRepository
                    .findByStatusOrderByExpenseDateDesc(
                            "ACTIVE"
                    );
        } else {
            records = expenseRecordRepository
                    .findByStoreIdAndStatusOrderByExpenseDateDesc(
                            selectedStore.getId(),
                            "ACTIVE"
                    );
        }

        List<ExpenseReportRecord> filteredRecords =
                records.stream()
                        .filter(record ->
                                isInsideRange(
                                        record,
                                        range.start(),
                                        range.end()
                                )
                        )
                        .map(this::toReportRecord)
                        .toList();

        BigDecimal totalAfn =
                filteredRecords.stream()
                        .filter(record ->
                                "AFN".equalsIgnoreCase(
                                        record.currency()
                                )
                        )
                        .map(ExpenseReportRecord::amount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalUsd =
                filteredRecords.stream()
                        .filter(record ->
                                "USD".equalsIgnoreCase(
                                        record.currency()
                                )
                        )
                        .map(ExpenseReportRecord::amount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return new ExpenseReportResponse(
                filteredRecords.size(),
                totalAfn,
                totalUsd,
                filteredRecords
        );
    }

    private ExpenseReportRecord toReportRecord(
            ExpenseRecord record) {

        String recipientName =
                record.getRecipientName();

        if (recipientName == null
                || recipientName.isBlank()) {

            if (record.getWorker() != null) {
                recipientName =
                        record.getWorker().getFullName();
            } else {
                recipientName = "";
            }
        }

        String storeName =
                record.getStore() == null
                        ? ""
                        : record.getStore().getName();

        BigDecimal amount =
                record.getAmount() == null
                        ? BigDecimal.ZERO
                        : record.getAmount();

        String currency =
                record.getCurrency() == null
                        ? "AFN"
                        : record.getCurrency().toUpperCase();

        return new ExpenseReportRecord(
                record.getId(),
                record.getExpenseDate(),
                storeName,
                record.getCategory(),
                recipientName,
                amount,
                currency
        );
    }

    private boolean isInsideRange(
            ExpenseRecord record,
            LocalDate start,
            LocalDate end) {

        LocalDateTime expenseDate =
                record.getExpenseDate();

        if (expenseDate == null) {
            return false;
        }

        LocalDate recordDate =
                expenseDate.toLocalDate();

        return !recordDate.isBefore(start)
                && !recordDate.isAfter(end);
    }

    private DateRange calculateDateRange(
            LocalDate date,
            String period) {

        String selectedPeriod =
                period == null
                        ? "today"
                        : period.toLowerCase();

        return switch (selectedPeriod) {
            case "week" -> {
                LocalDate start =
                        date.with(
                                DayOfWeek.MONDAY
                        );

                yield new DateRange(
                        start,
                        start.plusDays(6)
                );
            }

            case "month" -> {
                LocalDate start =
                        date.withDayOfMonth(1);

                yield new DateRange(
                        start,
                        date.withDayOfMonth(
                                date.lengthOfMonth()
                        )
                );
            }

            case "year" -> {
                LocalDate start =
                        date.withDayOfYear(1);

                yield new DateRange(
                        start,
                        date.withDayOfYear(
                                date.lengthOfYear()
                        )
                );
            }

            default -> new DateRange(
                    date,
                    date
            );
        };
    }

    private Store findSelectedStore(
            String storeCode) {

        if ("all".equalsIgnoreCase(storeCode)) {
            return null;
        }

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

    private record DateRange(
            LocalDate start,
            LocalDate end
    ) {
    }

    public record ExpenseReportRecord(
            Long id,
            LocalDateTime dateTime,
            String storeName,
            String category,
            String recipientName,
            BigDecimal amount,
            String currency
    ) {
    }

    public record ExpenseReportResponse(
            int totalRecords,
            BigDecimal totalAfn,
            BigDecimal totalUsd,
            List<ExpenseReportRecord> records
    ) {
    }
}