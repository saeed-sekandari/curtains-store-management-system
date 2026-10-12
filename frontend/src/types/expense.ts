export type ExpenseCategory =
  | "STORE_RENT"
  | "UTILITIES"
  | "TAX"
  | "TRANSPORTATION"
  | "EMPLOYEE_PAYMENT"
  | "TAILOR_PAYMENT"
  | "MANAGER_WITHDRAWAL"
  | "OTHER";

export type ExpenseCurrency = "AFN" | "USD";

export type ExpenseOption = {
  value: ExpenseCategory;
  dari: string;
  english: string;
};

export type ExpenseFormData = {
  storeCode: string;
  category: ExpenseCategory;
  recipientName: string;
  amount: number;
  currency: ExpenseCurrency;
  expenseDate: string;
  description: string;
};

export type ExpenseFormProps = {
  storeCode?: string;
  storeName?: string;
  managerName?: string;
  onCancel?: () => void;
  onSaved?: (
    data: ExpenseFormData
  ) => Promise<void> | void;
};

export const expenseOptions: ExpenseOption[] = [
  {
    value: "STORE_RENT",
    dari: "کرایه دکان",
    english: "Store Rent",
  },
  {
    value: "UTILITIES",
    dari: "برق و آب",
    english: "Electricity and Water",
  },
  {
    value: "TAX",
    dari: "مالیات",
    english: "Tax",
  },
  {
    value: "TRANSPORTATION",
    dari: "ترانسپورت",
    english: "Transportation",
  },
  {
    value: "EMPLOYEE_PAYMENT",
    dari: "پرداخت کارمند",
    english: "Employee Payment",
  },
  {
    value: "TAILOR_PAYMENT",
    dari: "پرداخت خیاط",
    english: "Tailor Payment",
  },
  {
    value: "MANAGER_WITHDRAWAL",
    dari: "برداشت شخصی مدیر",
    english: "Manager Personal Withdrawal",
  },
  {
    value: "OTHER",
    dari: "مصرف دیگر",
    english: "Other Expense",
  },
];

export function getCurrentDateTime(): string {
  const now = new Date();

  const localDate = new Date(
    now.getTime() -
      now.getTimezoneOffset() * 60000
  );

  return localDate.toISOString().slice(0, 16);
}