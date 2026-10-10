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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    // Shows daily, weekly, monthly, or yearly financial reports.
    @GetMapping("/expenses")
    public String expenses(
            @RequestParam(defaultValue = "all") String store,
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            Model model,
            Principal principal) {

        User manager = getManager(principal);

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

        validatePeriod(period);
        validateAfghanDate(
                selectedYear,
                selectedMonth,
                selectedDay
        );

        List<ExpenseRecord> allRecords;
        Store selectedStore = null;

        if ("all".equalsIgnoreCase(store)) {
            allRecords = expenseRecordRepository
                    .findAllByOrderByExpenseDateDesc();
        } else {
            selectedStore = findStore(store);

            storeAccessService.checkStoreAccess(
                    principal,
                    selectedStore.getCode()
            );

            allRecords = expenseRecordRepository
                    .findByStoreIdOrderByExpenseDateDesc(
                            selectedStore.getId()
                    );
        }

        List<ExpenseRecord> filteredRecords =
                filterRecords(
                        allRecords,
                        period,
                        selectedYear,
                        selectedMonth,
                        selectedDay
                );

        BigDecimal afnTotal =
                totalForCurrency(filteredRecords, "AFN");

        BigDecimal usdTotal =
                totalForCurrency(filteredRecords, "USD");

        Map<String, BigDecimal> afnTotalsByCategory =
                totalsByCategory(filteredRecords, "AFN");

        Map<String, BigDecimal> usdTotalsByCategory =
                totalsByCategory(filteredRecords, "USD");

        LocalDate referenceDate =
                AfghanDateUtil.toGregorianDate(
                        selectedYear,
                        selectedMonth,
                        selectedDay
                );

        LocalDate reportStart =
                reportStart(
                        period,
                        selectedYear,
                        selectedMonth,
                        selectedDay,
                        referenceDate
                );

        LocalDate reportEnd =
                reportEnd(
                        period,
                        selectedYear,
                        selectedMonth,
                        selectedDay,
                        referenceDate
                );

        model.addAttribute("records", filteredRecords);
        model.addAttribute("selectedStore", selectedStore);
        model.addAttribute("selectedStoreCode", store);
        model.addAttribute("username", manager.getUsername());

        model.addAttribute("period", period);
        model.addAttribute("periodLabel", periodLabel(period));

        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedDay", selectedDay);
        model.addAttribute(
                "selectedMonthName",
                AfghanDateUtil.getMonthName(selectedMonth)
        );

        model.addAttribute(
                "reportStart",
                AfghanDateUtil.formatDate(reportStart)
        );

        model.addAttribute(
                "reportEnd",
                AfghanDateUtil.formatDate(reportEnd)
        );

        model.addAttribute(
                "availableYears",
                buildAvailableYears(
                        AfghanDateUtil.getYear(today)
                )
        );

        model.addAttribute(
                "availableMonths",
                buildNumbers(1, 12)
        );

        model.addAttribute(
                "availableDays",
                buildNumbers(
                        1,
                        maximumDayForMonth(selectedMonth)
                )
        );

        model.addAttribute(
                "monthNames",
                AfghanDateUtil.getMonthNames()
        );

        model.addAttribute("afnTotal", afnTotal);
        model.addAttribute("usdTotal", usdTotal);
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

    // Opens the form for recording a financial record.
    @GetMapping("/expenses/new")
    public String newExpense(
            @RequestParam String store,
            Model model,
            Principal principal) {

        getManager(principal);

        Store selectedStore = findStore(store);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        List<User> workers = userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getAssignedStore() != null
                                && user.getAssignedStore().getId()
                                .equals(selectedStore.getId())
                                && (
                                user.getRole() == Role.EMPLOYEE
                                        || user.getRole() == Role.TAILOR
                        )
                )
                .toList();

        model.addAttribute("store", selectedStore);
        model.addAttribute("workers", workers);
        model.addAttribute("expense", new ExpenseRecord());

        return "expense-form";
    }

    // Saves a store expense, employee payment,
    // tailor payment, or manager withdrawal.
    @Transactional
    @PostMapping("/expenses")
    public String saveExpense(
            @RequestParam String storeCode,
            @RequestParam String category,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam LocalDateTime expenseDate,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String recipientName,
            @RequestParam(required = false) String description,
            Principal principal) {

        User manager = getManager(principal);

        Store selectedStore = findStore(storeCode);

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        validateAmount(amount);
        validateCurrency(currency);
        validateCategory(category);

        User worker = null;

        if (workerId != null) {
            worker = userRepository.findById(workerId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Worker not found"
                            )
                    );

            if (worker.getAssignedStore() == null
                    || !worker.getAssignedStore().getId()
                    .equals(selectedStore.getId())) {

                throw new IllegalArgumentException(
                        "The worker must belong to the selected store"
                );
            }

            if (worker.getRole() != Role.EMPLOYEE
                    && worker.getRole() != Role.TAILOR) {

                throw new IllegalArgumentException(
                        "Only employees and tailors can receive payments"
                );
            }
        }

        ExpenseRecord record = new ExpenseRecord();

        record.setStore(selectedStore);
        record.setCategory(category);
        record.setWorker(worker);
        record.setAmount(amount);
        record.setCurrency(currency.toUpperCase());
        record.setExpenseDate(expenseDate);
        record.setRecipientName(recipientName);
        record.setDescription(description);
        record.setRecordedBy(manager);

        expenseRecordRepository.save(record);

        int afghanYear =
                AfghanDateUtil.getYear(expenseDate.toLocalDate());

        int afghanMonth =
                AfghanDateUtil.getMonth(expenseDate.toLocalDate());

        int afghanDay =
                AfghanDateUtil.getDay(expenseDate.toLocalDate());

        return "redirect:/expenses?store="
                + selectedStore.getCode()
                + "&period=monthly"
                + "&year="
                + afghanYear
                + "&month="
                + afghanMonth
                + "&day="
                + afghanDay;
    }

    private List<ExpenseRecord> filterRecords(
            List<ExpenseRecord> records,
            String period,
            int year,
            int month,
            int day) {

        LocalDate referenceDate =
                AfghanDateUtil.toGregorianDate(
                        year,
                        month,
                        day
                );

        LocalDate start =
                reportStart(
                        period,
                        year,
                        month,
                        day,
                        referenceDate
                );

        LocalDate end =
                reportEnd(
                        period,
                        year,
                        month,
                        day,
                        referenceDate
                );

        return records.stream()
                .filter(record -> {
                    if (record.getExpenseDate() == null) {
                        return false;
                    }

                    LocalDate recordDate =
                            record.getExpenseDate().toLocalDate();

                    return !recordDate.isBefore(start)
                            && !recordDate.isAfter(end);
                })
                .toList();
    }

    private LocalDate reportStart(
            String period,
            int year,
            int month,
            int day,
            LocalDate referenceDate) {

        return switch (period.toLowerCase()) {
            case "daily" -> referenceDate;

            case "weekly" -> {
                int daysFromSaturday =
                        (referenceDate.getDayOfWeek().getValue() - 6 + 7)
                                % 7;

                yield referenceDate.minusDays(daysFromSaturday);
            }

            case "monthly" ->
                    AfghanDateUtil.toGregorianDate(
                            year,
                            month,
                            1
                    );

            case "yearly" ->
                    AfghanDateUtil.toGregorianDate(
                            year,
                            1,
                            1
                    );

            default -> throw new IllegalArgumentException(
                    "Invalid report period"
            );
        };
    }

    private LocalDate reportEnd(
            String period,
            int year,
            int month,
            int day,
            LocalDate referenceDate) {

        return switch (period.toLowerCase()) {
            case "daily" -> referenceDate;

            case "weekly" ->
                    reportStart(
                            period,
                            year,
                            month,
                            day,
                            referenceDate
                    ).plusDays(6);

            case "monthly" -> {
                if (month == 12) {
                    yield AfghanDateUtil.toGregorianDate(
                            year + 1,
                            1,
                            1
                    ).minusDays(1);
                }

                yield AfghanDateUtil.toGregorianDate(
                        year,
                        month + 1,
                        1
                ).minusDays(1);
            }

            case "yearly" ->
                    AfghanDateUtil.toGregorianDate(
                            year + 1,
                            1,
                            1
                    ).minusDays(1);

            default -> throw new IllegalArgumentException(
                    "Invalid report period"
            );
        };
    }

    private String periodLabel(String period) {
        return switch (period.toLowerCase()) {
            case "daily" -> "Daily Report";
            case "weekly" -> "Weekly Report";
            case "monthly" -> "Monthly Report";
            case "yearly" -> "Yearly Report";
            default -> "Expense Report";
        };
    }

    private int maximumDayForMonth(int month) {
        if (month <= 6) {
            return 31;
        }

        if (month <= 11) {
            return 30;
        }

        return 30;
    }

    private List<Integer> buildNumbers(
            int start,
            int end) {

        List<Integer> numbers = new ArrayList<>();

        for (int number = start; number <= end; number++) {
            numbers.add(number);
        }

        return numbers;
    }

    private List<Integer> buildAvailableYears(
            int currentAfghanYear) {

        List<Integer> years = new ArrayList<>();

        for (int year = currentAfghanYear;
             year >= currentAfghanYear - 5;
             year--) {

            years.add(year);
        }

        return years;
    }

    private BigDecimal totalForCurrency(
            List<ExpenseRecord> records,
            String currency) {

        return records.stream()
                .filter(record ->
                        currency.equalsIgnoreCase(
                                record.getCurrency()
                        )
                )
                .map(ExpenseRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> totalsByCategory(
            List<ExpenseRecord> records,
            String currency) {

        Map<String, BigDecimal> totals =
                new LinkedHashMap<>();

        records.stream()
                .filter(record ->
                        currency.equalsIgnoreCase(
                                record.getCurrency()
                        )
                )
                .forEach(record ->
                        totals.merge(
                                record.getCategory(),
                                record.getAmount(),
                                BigDecimal::add
                        )
                );

        return totals;
    }

    private User getManager(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (user.getRole() != Role.MANAGER) {
            throw new AccessDeniedException(
                    "Only the manager can access financial expenses"
            );
        }

        return user;
    }

    private Store findStore(String storeCode) {
        return storeRepository.findByCode(storeCode.toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException("Store not found")
                );
    }

    private void validatePeriod(String period) {
        List<String> allowedPeriods = List.of(
                "daily",
                "weekly",
                "monthly",
                "yearly"
        );

        if (!allowedPeriods.contains(period.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid report period"
            );
        }
    }

    private void validateAfghanDate(
            int year,
            int month,
            int day) {

        if (year < 1) {
            throw new IllegalArgumentException(
                    "Invalid Afghan year"
            );
        }

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Afghan month must be between 1 and 12"
            );
        }

        if (day < 1 || day > maximumDayForMonth(month)) {
            throw new IllegalArgumentException(
                    "Invalid Afghan day"
            );
        }

        AfghanDateUtil.toGregorianDate(
                year,
                month,
                day
        );
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }
    }

    private void validateCurrency(String currency) {
        if (!"AFN".equalsIgnoreCase(currency)
                && !"USD".equalsIgnoreCase(currency)) {

            throw new IllegalArgumentException(
                    "Currency must be AFN or USD"
            );
        }
    }

    private void validateCategory(String category) {
        List<String> allowedCategories = List.of(
                "RENT",
                "ELECTRICITY",
                "TAX",
                "TRANSPORTATION",
                "EMPLOYEE_PAYMENT",
                "TAILOR_PAYMENT",
                "MANAGER_WITHDRAWAL",
                "OTHER"
        );

        if (!allowedCategories.contains(category)) {
            throw new IllegalArgumentException(
                    "Invalid expense category"
            );
        }
    }
}