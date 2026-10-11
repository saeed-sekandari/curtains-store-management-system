package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.ExpenseRecord;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.ExpenseRecordRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
public class MyPaymentsController {

    private final ExpenseRecordRepository expenseRecordRepository;
    private final UserRepository userRepository;

    public MyPaymentsController(
            ExpenseRecordRepository expenseRecordRepository,
            UserRepository userRepository) {

        this.expenseRecordRepository = expenseRecordRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-payments")
    public String myPayments(
            Model model,
            Principal principal) {

        User currentUser = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        if (currentUser.getRole() != Role.EMPLOYEE
                && currentUser.getRole() != Role.TAILOR) {

            throw new AccessDeniedException(
                    "Only employees and tailors can view their payments"
            );
        }

        List<ExpenseRecord> payments =
                expenseRecordRepository
                        .findByWorkerIdAndStatusOrderByExpenseDateDesc(
                                currentUser.getId(),
                                "ACTIVE"
                        );

        BigDecimal totalAfn = payments.stream()
                .filter(payment ->
                        "AFN".equalsIgnoreCase(
                                payment.getCurrency()
                        )
                )
                .map(ExpenseRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUsd = payments.stream()
                .filter(payment ->
                        "USD".equalsIgnoreCase(
                                payment.getCurrency()
                        )
                )
                .map(ExpenseRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("user", currentUser);
        model.addAttribute("payments", payments);
        model.addAttribute("totalAfn", totalAfn);
        model.addAttribute("totalUsd", totalUsd);

        return "my-payments";
    }
}