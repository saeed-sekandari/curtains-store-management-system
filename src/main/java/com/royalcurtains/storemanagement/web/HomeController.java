package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Customer;
import com.royalcurtains.storemanagement.model.ExpenseRecord;
import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.repository.CustomerRepository;
import com.royalcurtains.storemanagement.repository.ExpenseRecordRepository;
import com.royalcurtains.storemanagement.repository.OrderRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.util.AfghanDateUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ExpenseRecordRepository expenseRecordRepository;

    public HomeController(
            StoreRepository storeRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            ExpenseRecordRepository expenseRecordRepository) {

        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.expenseRecordRepository = expenseRecordRepository;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {

        String[] stores = storeRepository.findAll()
                .stream()
                .map(Store::getName)
                .toArray(String[]::new);

        model.addAttribute("systemName", "Curtains Store Management");
        model.addAttribute("stores", stores);

        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "all") String store,
            @RequestParam(
                    required = false,
                    defaultValue = "monthly"
            ) String period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            Model model,
            Principal principal) {

        List<Customer> customers;
        List<Order> orders;
        List<ExpenseRecord> expenses;

        String selectedStoreName;
        String selectedStoreCode;

        if (store.equalsIgnoreCase("all")) {

            selectedStoreName = "All Stores";
            selectedStoreCode = "all";

            customers = customerRepository.findAll();
            orders = orderRepository.findAll();

            expenses = expenseRecordRepository
                    .findByStatusOrderByExpenseDateDesc("ACTIVE");

        } else {

            Store selectedStore = storeRepository
                    .findByCode(store.toLowerCase())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Store not found"
                            )
                    );

            selectedStoreName = selectedStore.getName();
            selectedStoreCode = selectedStore.getCode();

            customers = customerRepository
                    .findByStoreId(selectedStore.getId());

            orders = orderRepository
                    .findByStoreId(selectedStore.getId());

            expenses = expenseRecordRepository
                    .findByStoreIdAndStatusOrderByExpenseDateDesc(
                            selectedStore.getId(),
                            "ACTIVE"
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

        LocalDate selectedDate = AfghanDateUtil.toGregorianDate(
                selectedYear,
                selectedMonth,
                selectedDay
        );

        ReportPeriod reportPeriod = calculatePeriod(
                period,
                selectedDate
        );

        List<Order> activeOrders = orders.stream()
                .filter(order ->
                        !"CANCELLED".equalsIgnoreCase(
                                order.getStatus()
                        )
                )
                .filter(order ->
                        order.getCreatedDate() != null
                                && isInsidePeriod(
                                order.getCreatedDate(),
                                reportPeriod.start(),
                                reportPeriod.end()
                        )
                )
                .toList();

        List<ExpenseRecord> periodExpenses = expenses.stream()
                .filter(expense ->
                        expense.getExpenseDate() != null
                                && isInsidePeriod(
                                expense.getExpenseDate().toLocalDate(),
                                reportPeriod.start(),
                                reportPeriod.end()
                        )
                )
                .toList();

        BigDecimal totalSales = activeOrders.stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpensesAfn = periodExpenses.stream()
                .filter(expense ->
                        "AFN".equalsIgnoreCase(
                                expense.getCurrency()
                        )
                )
                .map(ExpenseRecord::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpensesUsd = periodExpenses.stream()
                .filter(expense ->
                        "USD".equalsIgnoreCase(
                                expense.getCurrency()
                        )
                )
                .map(ExpenseRecord::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Supplier purchases are handled separately.
        BigDecimal netResultAfn = totalSales
                .subtract(totalExpensesAfn);

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

        model.addAttribute("username", principal.getName());
        model.addAttribute("selectedStore", selectedStoreName);
        model.addAttribute("selectedStoreCode", selectedStoreCode);
        model.addAttribute("period", period.toLowerCase());
        model.addAttribute("periodLabel", reportPeriod.label());

        model.addAttribute(
                "reportStart",
                AfghanDateUtil.formatDate(reportPeriod.start())
        );

        model.addAttribute(
                "reportEnd",
                AfghanDateUtil.formatDate(reportPeriod.end())
        );

        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedDay", selectedDay);
        model.addAttribute("availableYears", availableYears);
        model.addAttribute("availableMonths", availableMonths);
        model.addAttribute("availableDays", availableDays);
        model.addAttribute(
                "monthNames",
                AfghanDateUtil.getMonthNames()
        );

        model.addAttribute("orderCount", activeOrders.size());
        model.addAttribute("customerCount", customers.size());
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalExpensesAfn", totalExpensesAfn);
        model.addAttribute("totalExpensesUsd", totalExpensesUsd);
        model.addAttribute("netResultAfn", netResultAfn);

        return "dashboard";
    }

    private boolean isInsidePeriod(
            LocalDate date,
            LocalDate start,
            LocalDate end) {

        return !date.isBefore(start)
                && !date.isAfter(end);
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
                int afghanYear = AfghanDateUtil.getYear(date);

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
                int afghanYear = AfghanDateUtil.getYear(date);
                int afghanMonth = AfghanDateUtil.getMonth(date);

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

        return new ReportPeriod(start, end, label);
    }

    private record ReportPeriod(
            LocalDate start,
            LocalDate end,
            String label
    ) {
    }
}