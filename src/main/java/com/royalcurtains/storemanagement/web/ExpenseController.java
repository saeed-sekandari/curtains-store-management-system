package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.ExpenseRecord;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.ExpenseRecordRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import com.royalcurtains.storemanagement.util.AfghanDateUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ExpenseController {

    private final ExpenseRecordRepository expenseRecordRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public ExpenseController(
            ExpenseRecordRepository expenseRecordRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.expenseRecordRepository = expenseRecordRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    @GetMapping("/expenses")
    public String expenses(
            @RequestParam String store,
            @RequestParam(required = false, defaultValue = "monthly")
            String period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            Model model,
            Principal principal) {

        User currentUser = findUser(principal);
        requireManager(currentUser);

        Store selectedStore = findSelectedStore(store);

        if (selectedStore != null) {
            storeAccessService.checkStoreAccess(
                    principal,
                    selectedStore.getCode()
            );
        }

        LocalDate today = LocalDate.now();

        int selectedYear = year != null
                ? year
                : AfghanDateUtil.getYear(today);

        int selectedMonth = month != null
                ? month
                : AfghanDateUtil.getMonth(today);

        int selectedDay = day != null
                ? day
                : AfghanDateUtil.getDay(today);

        LocalDate reportDate = AfghanDateUtil.toGregorianDate(
                selectedYear,
                selectedMonth,
                selectedDay
        );

        ReportPeriod reportPeriod = calculatePeriod(
                period,
                reportDate
        );

        List<ExpenseRecord> activeRecords;

        if (selectedStore == null) {
            activeRecords = expenseRecordRepository
                    .findByStatusOrderByExpenseDateDesc("ACTIVE");
        } else {
            activeRecords = expenseRecordRepository
                    .findByStoreIdAndStatusOrderByExpenseDateDesc(
                            selectedStore.getId(),
                            "ACTIVE"
                    );
        }

        List<ExpenseRecord> filteredRecords = activeRecords.stream()
                .filter(record ->
                        isInsidePeriod(
                                record,
                                reportPeriod.start(),
                                reportPeriod.end()
                        )
                )
                .toList();

        List<ExpenseRecord> voidedRecords;

        if (selectedStore == null) {
            voidedRecords = expenseRecordRepository
                    .findByStatusOrderByExpenseDateDesc("VOIDED");
        } else {
            voidedRecords = expenseRecordRepository
                    .findByStoreIdAndStatusOrderByExpenseDateDesc(
                            selectedStore.getId(),
                            "VOIDED"
                    );
        }

        BigDecimal afnTotal = BigDecimal.ZERO;
        BigDecimal usdTotal = BigDecimal.ZERO;

        Map<String, BigDecimal> afnTotalsByCategory =
                new HashMap<>();

        Map<String, BigDecimal> usdTotalsByCategory =
                new HashMap<>();

        for (ExpenseRecord record : filteredRecords) {
            BigDecimal amount = record.getAmount() == null
                    ? BigDecimal.ZERO
                    : record.getAmount();

            if ("USD".equalsIgnoreCase(record.getCurrency())) {
                usdTotal = usdTotal.add(amount);

                usdTotalsByCategory.merge(
                        record.getCategory(),
                        amount,
                        BigDecimal::add
                );
            } else {
                afnTotal = afnTotal.add(amount);

                afnTotalsByCategory.merge(
                        record.getCategory(),
                        amount,
                        BigDecimal::add
                );
            }
        }

        List<Integer> availableYears = new ArrayList<>();
        int currentAfghanYear = AfghanDateUtil.getYear(today);

        for (int i = currentAfghanYear - 5;
             i <= currentAfghanYear;
             i++) {

            availableYears.add(i);
        }

        List<Integer> availableMonths = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            availableMonths.add(i);
        }

        List<Integer> availableDays = new ArrayList<>();

        for (int i = 1; i <= 31; i++) {
            availableDays.add(i);
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
                "period",
                period.toLowerCase()
        );

        model.addAttribute(
                "periodLabel",
                reportPeriod.label()
        );

        model.addAttribute(
                "reportStart",
                AfghanDateUtil.formatDate(
                        reportPeriod.start()
                )
        );

        model.addAttribute(
                "reportEnd",
                AfghanDateUtil.formatDate(
                        reportPeriod.end()
                )
        );

        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedDay", selectedDay);

        model.addAttribute(
                "selectedMonthName",
                AfghanDateUtil.getMonthName(selectedMonth)
        );

        model.addAttribute(
                "availableYears",
                availableYears
        );

        model.addAttribute(
                "availableMonths",
                availableMonths
        );

        model.addAttribute(
                "availableDays",
                availableDays
        );

        model.addAttribute(
                "monthNames",
                AfghanDateUtil.getMonthNames()
        );

        model.addAttribute(
                "records",
                filteredRecords
        );

        model.addAttribute(
                "voidedRecords",
                voidedRecords
        );

        model.addAttribute(
                "afnTotal",
                afnTotal
        );

        model.addAttribute(
                "usdTotal",
                usdTotal
        );

        model.addAttribute(
                "afnTotalsByCategory",
                afnTotalsByCategory
        );

        model.addAttribute(
                "usdTotalsByCategory",
                usdTotalsByCategory
        );

        return "expenses";
    }

    @GetMapping("/expenses/new")
    public String newExpense(
            @RequestParam String store,
            Model model,
            Principal principal) {

        User currentUser = findUser(principal);
        requireManager(currentUser);

        Store selectedStore = findStore(store);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        List<User> workers = userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getRole() == Role.EMPLOYEE
                                || user.getRole() == Role.TAILOR
                )
                .sorted(
                        Comparator.comparing(
                                User::getFullName,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                )
                .toList();

        model.addAttribute("store", selectedStore);
        model.addAttribute("workers", workers);
        model.addAttribute("expense", new ExpenseRecord());

        return "expense-form";
    }

    @Transactional
    @PostMapping("/expenses")
    public String saveExpense(
            @RequestParam String storeCode,
            @RequestParam String category,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String recipientName,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam String expenseDate,
            @RequestParam(required = false) String description,
            Principal principal) {

        User recordedBy = findUser(principal);
        requireManager(recordedBy);

        Store selectedStore = findStore(storeCode);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        ExpenseRecord record = new ExpenseRecord();

        record.setStore(selectedStore);
        record.setCategory(category);
        record.setAmount(amount);
        record.setCurrency(currency.toUpperCase());
        record.setExpenseDate(
                LocalDateTime.parse(expenseDate)
        );
        record.setDescription(description);
        record.setRecordedBy(recordedBy);
        record.setStatus("ACTIVE");

        if (recipientName != null
                && !recipientName.isBlank()) {

            record.setRecipientName(
                    recipientName.trim()
            );
        }

        if (workerId != null) {
            User worker = userRepository.findById(workerId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Selected worker was not found"
                            )
                    );

            record.setWorker(worker);

            if (record.getRecipientName() == null
                    || record.getRecipientName().isBlank()) {

                record.setRecipientName(
                        worker.getFullName()
                );
            }
        }

        expenseRecordRepository.save(record);

        return "redirect:/expenses?store="
                + selectedStore.getCode();
    }

    @Transactional
    @PostMapping("/expenses/{expenseId}/void")
    public String voidExpense(
            @PathVariable Long expenseId,
            @RequestParam String voidReason,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User currentUser = findUser(principal);
        requireManager(currentUser);

        ExpenseRecord record = expenseRecordRepository
                .findById(expenseId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Expense record not found"
                        )
                );

        Store recordStore = record.getStore();

        storeAccessService.checkStoreAccess(
                principal,
                recordStore.getCode()
        );

        if ("VOIDED".equals(record.getStatus())) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "This record has already been voided."
            );

            return "redirect:/expenses?store="
                    + recordStore.getCode();
        }

        if (voidReason == null
                || voidReason.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please enter a reason before voiding the record."
            );

            return "redirect:/expenses?store="
                    + recordStore.getCode();
        }

        record.setStatus("VOIDED");
        record.setVoidedAt(LocalDateTime.now());
        record.setVoidedBy(currentUser);
        record.setVoidReason(voidReason.trim());

        expenseRecordRepository.save(record);

        redirectAttributes.addFlashAttribute(
                "success",
                "The expense record was voided successfully."
        );

        return "redirect:/expenses?store="
                + recordStore.getCode();
    }

    private Store findSelectedStore(String storeCode) {
        if ("all".equalsIgnoreCase(storeCode)) {
            return null;
        }

        return findStore(storeCode);
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

    private void requireManager(User user) {
        if (user.getRole() != Role.MANAGER) {
            throw new AccessDeniedException(
                    "Only the manager can manage expense records"
            );
        }
    }

    private boolean isInsidePeriod(
            ExpenseRecord record,
            LocalDate start,
            LocalDate end) {

        if (record.getExpenseDate() == null) {
            return false;
        }

        LocalDate recordDate =
                record.getExpenseDate().toLocalDate();

        return !recordDate.isBefore(start)
                && !recordDate.isAfter(end);
    }

    private ReportPeriod calculatePeriod(
            String period,
            LocalDate date) {

        String selectedPeriod = period == null
                ? "monthly"
                : period.toLowerCase();

        LocalDate start;
        LocalDate end;
        String label;

        switch (selectedPeriod) {

            case "daily" -> {
                start = date;
                end = date;
                label = "Daily Report";
            }

            case "weekly" -> {
                int daysFromSaturday =
                        (date.getDayOfWeek().getValue() + 1) % 7;

                start = date.minusDays(daysFromSaturday);
                end = start.plusDays(6);
                label = "Weekly Report";
            }

            case "yearly" -> {
                int afghanYear =
                        AfghanDateUtil.getYear(date);

                start = AfghanDateUtil.toGregorianDate(
                        afghanYear,
                        1,
                        1
                );

                end = AfghanDateUtil.toGregorianDate(
                        afghanYear + 1,
                        1,
                        1
                ).minusDays(1);

                label = "Yearly Report";
            }

            default -> {
                int afghanYear =
                        AfghanDateUtil.getYear(date);

                int afghanMonth =
                        AfghanDateUtil.getMonth(date);

                start = AfghanDateUtil.toGregorianDate(
                        afghanYear,
                        afghanMonth,
                        1
                );

                if (afghanMonth == 12) {
                    end = AfghanDateUtil.toGregorianDate(
                            afghanYear + 1,
                            1,
                            1
                    ).minusDays(1);
                } else {
                    end = AfghanDateUtil.toGregorianDate(
                            afghanYear,
                            afghanMonth + 1,
                            1
                    ).minusDays(1);
                }

                label = "Monthly Report";
            }
        }

        return new ReportPeriod(
                start,
                end,
                label
        );
    }

    private record ReportPeriod(
            LocalDate start,
            LocalDate end,
            String label
    ) {
    }
}