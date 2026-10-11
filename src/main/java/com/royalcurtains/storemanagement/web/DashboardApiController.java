package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.DirectSale;
import com.royalcurtains.storemanagement.model.ExpenseRecord;
import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.CustomerRepository;
import com.royalcurtains.storemanagement.repository.DirectSaleRepository;
import com.royalcurtains.storemanagement.repository.ExpenseRecordRepository;
import com.royalcurtains.storemanagement.repository.OrderRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import com.royalcurtains.storemanagement.util.AfghanDateUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
public class DashboardApiController {

    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ExpenseRecordRepository expenseRecordRepository;
    private final DirectSaleRepository directSaleRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public DashboardApiController(
            StoreRepository storeRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            ExpenseRecordRepository expenseRecordRepository,
            DirectSaleRepository directSaleRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.expenseRecordRepository = expenseRecordRepository;
        this.directSaleRepository = directSaleRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    @Transactional
    @GetMapping("/api/dashboard")
    public DashboardResponse dashboard(
            @RequestParam(defaultValue = "all") String store,
            Principal principal) {

        User currentUser = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        )
                );

        List<Order> orders;
        List<ExpenseRecord> expenses;
        List<DirectSale> directSales;
        String storeName;
        String storeCode;

        if ("all".equalsIgnoreCase(store)) {

            if (currentUser.getRole() != Role.MANAGER) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Only the manager can view all stores"
                );
            }

            storeName = "All Stores";
            storeCode = "all";

            orders = orderRepository.findAll();

            expenses = expenseRecordRepository
                    .findByStatusOrderByExpenseDateDesc("ACTIVE");

            directSales = directSaleRepository
                    .findByStatusAndSaleDateBetweenOrderBySaleDateDesc(
                            "ACTIVE",
                            LocalDateTime.of(2000, 1, 1, 0, 0),
                            LocalDateTime.now().plusDays(1)
                    );

        } else {

            Store selectedStore = storeRepository
                    .findByCode(store.toLowerCase())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Store not found"
                            )
                    );

            storeAccessService.checkStoreAccess(
                    principal,
                    selectedStore.getCode()
            );

            storeName = selectedStore.getName();
            storeCode = selectedStore.getCode();

            orders = orderRepository
                    .findByStoreId(selectedStore.getId());

            expenses = expenseRecordRepository
                    .findByStoreIdAndStatusOrderByExpenseDateDesc(
                            selectedStore.getId(),
                            "ACTIVE"
                    );

            directSales = directSaleRepository
                    .findByStoreIdAndStatusOrderBySaleDateDesc(
                            selectedStore.getId(),
                            "ACTIVE"
                    );
        }

        LocalDate today = LocalDate.now();

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today
                .plusDays(1)
                .atStartOfDay();

        LocalDate afghanMonthStart = getAfghanMonthStart(today);
        LocalDate afghanMonthEnd = getAfghanMonthEnd(today);

        List<Order> activeOrders = orders.stream()
                .filter(order ->
                        !"CANCELLED".equalsIgnoreCase(
                                order.getStatus()
                        )
                )
                .toList();

        List<Order> todayOrders = activeOrders.stream()
                .filter(order ->
                        order.getCreatedDate() != null
                                && order.getCreatedDate().equals(today)
                )
                .toList();

        BigDecimal todayOrderSalesAfn = todayOrders.stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyOrderSalesAfn = activeOrders.stream()
                .filter(order ->
                        order.getCreatedDate() != null
                                && isBetween(
                                order.getCreatedDate(),
                                afghanMonthStart,
                                afghanMonthEnd
                        )
                )
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ExpenseRecord> todayExpenses = expenses.stream()
                .filter(expense ->
                        expense.getExpenseDate() != null
                                && !expense.getExpenseDate()
                                .isBefore(todayStart)
                                && expense.getExpenseDate()
                                .isBefore(tomorrowStart)
                )
                .toList();

        BigDecimal todayExpensesAfn = sumExpenses(
                todayExpenses,
                "AFN"
        );

        BigDecimal todayExpensesUsd = sumExpenses(
                todayExpenses,
                "USD"
        );

        List<DirectSale> todayDirectSales = directSales.stream()
                .filter(sale ->
                        sale.getSaleDate() != null
                                && !sale.getSaleDate()
                                .isBefore(todayStart)
                                && sale.getSaleDate()
                                .isBefore(tomorrowStart)
                )
                .toList();

        BigDecimal todayDirectSalesAfn = sumDirectSales(
                todayDirectSales,
                "AFN"
        );

        BigDecimal todayDirectSalesUsd = sumDirectSales(
                todayDirectSales,
                "USD"
        );

        BigDecimal monthlyDirectSalesAfn = directSales.stream()
                .filter(sale ->
                        sale.getSaleDate() != null
                                && isBetween(
                                sale.getSaleDate().toLocalDate(),
                                afghanMonthStart,
                                afghanMonthEnd
                        )
                )
                .filter(sale ->
                        "AFN".equalsIgnoreCase(
                                sale.getCurrency()
                        )
                )
                .map(DirectSale::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal todaySalesAfn = todayOrderSalesAfn
                .add(todayDirectSalesAfn);

        BigDecimal todaySalesUsd = todayDirectSalesUsd;

        BigDecimal monthlySalesAfn = monthlyOrderSalesAfn
                .add(monthlyDirectSalesAfn);

        List<SaleRow> sales = new ArrayList<>();

        for (Order order : todayOrders) {
            sales.add(new SaleRow(
                    order.getCreatedDate()
                            .atStartOfDay()
                            .toString(),
                    "CUSTOMER_ORDER",
                    "Order #" + order.getOrderNumber(),
                    order.getTotalAmount(),
                    "AFN"
            ));
        }

        for (DirectSale sale : todayDirectSales) {
            sales.add(new SaleRow(
                    sale.getSaleDate().toString(),
                    "DIRECT_SALE",
                    sale.getProductName(),
                    sale.getTotalAmount(),
                    sale.getCurrency()
            ));
        }

        sales.sort(
                Comparator.comparing(
                        SaleRow::dateTime
                ).reversed()
        );

        int customerCount;

        if ("all".equalsIgnoreCase(store)) {
            customerCount = customerRepository
                    .findAll()
                    .size();
        } else {
            Store selectedStore = storeRepository
                    .findByCode(store.toLowerCase())
                    .orElseThrow();

            customerCount = customerRepository
                    .findByStoreId(selectedStore.getId())
                    .size();
        }

        return new DashboardResponse(
                storeName,
                storeCode,
                todayOrders.size(),
                customerCount,
                todayOrderSalesAfn,
                todayDirectSalesAfn,
                todaySalesAfn,
                todaySalesUsd,
                todayExpensesAfn,
                todayExpensesUsd,
                monthlySalesAfn,
                sales
        );
    }

    private BigDecimal sumExpenses(
            List<ExpenseRecord> expenses,
            String currency) {

        return expenses.stream()
                .filter(expense ->
                        currency.equalsIgnoreCase(
                                expense.getCurrency()
                        )
                )
                .map(ExpenseRecord::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumDirectSales(
            List<DirectSale> sales,
            String currency) {

        return sales.stream()
                .filter(sale ->
                        currency.equalsIgnoreCase(
                                sale.getCurrency()
                        )
                )
                .map(DirectSale::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isBetween(
            LocalDate date,
            LocalDate start,
            LocalDate end) {

        return !date.isBefore(start)
                && !date.isAfter(end);
    }

    private LocalDate getAfghanMonthStart(LocalDate date) {

        int year = AfghanDateUtil.getYear(date);
        int month = AfghanDateUtil.getMonth(date);

        return AfghanDateUtil.toGregorianDate(
                year,
                month,
                1
        );
    }

    private LocalDate getAfghanMonthEnd(LocalDate date) {

        int year = AfghanDateUtil.getYear(date);
        int month = AfghanDateUtil.getMonth(date);

        if (month == 12) {
            return AfghanDateUtil.toGregorianDate(
                    year + 1,
                    1,
                    1
            ).minusDays(1);
        }

        return AfghanDateUtil.toGregorianDate(
                year,
                month + 1,
                1
        ).minusDays(1);
    }

    public record DashboardResponse(
            String storeName,
            String storeCode,
            int todayOrderCount,
            int customerCount,
            BigDecimal todayOrderSalesAfn,
            BigDecimal todayDirectSalesAfn,
            BigDecimal todaySalesAfn,
            BigDecimal todaySalesUsd,
            BigDecimal todayExpensesAfn,
            BigDecimal todayExpensesUsd,
            BigDecimal monthlySalesAfn,
            List<SaleRow> sales
    ) {
    }

    public record SaleRow(
            String dateTime,
            String type,
            String description,
            BigDecimal amount,
            String currency
    ) {
    }
}