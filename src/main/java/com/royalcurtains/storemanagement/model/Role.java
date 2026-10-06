package com.royalcurtains.storemanagement.model;

public enum Role {

    // Can manage both stores and all system features.
    MANAGER,

    // Can handle daily store work, such as customers and orders.
    EMPLOYEE,

    // Can view assigned tailoring work and update its progress.
    TAILOR,

    // Can manage payments, expenses, salaries, and financial reports.
    ACCOUNTANT
}