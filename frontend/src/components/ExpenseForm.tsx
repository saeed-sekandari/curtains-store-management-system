import { useState } from "react";
import type { FormEvent } from "react";

import ExpenseFields from "./ExpenseFields";
import ExpenseTypeSelect from "./ExpenseTypeSelect";

import {
  expenseOptions,
  getCurrentDateTime,
} from "../types/expense";

import type {
  ExpenseCategory,
  ExpenseCurrency,
  ExpenseFormData,
  ExpenseFormProps,
} from "../types/expense";

type LanguageExpenseFormProps =
  ExpenseFormProps & {
    isDari?: boolean;
  };

export default function ExpenseForm({
  storeCode = "",
  managerName = "مدیر سیستم",
  isDari = true,
  onCancel,
  onSaved,
}: LanguageExpenseFormProps) {
  const [category, setCategory] =
    useState<ExpenseCategory>("MANAGER_WITHDRAWAL");

  const [recipientName, setRecipientName] =
    useState("");

  const [amount, setAmount] =
    useState("");

  const [currency, setCurrency] =
    useState<ExpenseCurrency>("AFN");

  const [expenseDate, setExpenseDate] =
    useState(getCurrentDateTime());

  const [description, setDescription] =
    useState("");

  const [saving, setSaving] =
    useState(false);

  const [message, setMessage] =
    useState("");

  const isManagerWithdrawal =
    category === "MANAGER_WITHDRAWAL";

  const requiresWorker =
    category === "EMPLOYEE_PAYMENT" ||
    category === "TAILOR_PAYMENT";

  const visibleRecipient = isManagerWithdrawal
    ? managerName
    : recipientName;

  const handleCategoryChange = (
    nextCategory: ExpenseCategory
  ) => {
    setCategory(nextCategory);
    setMessage("");

    setRecipientName(
      nextCategory === "MANAGER_WITHDRAWAL"
        ? managerName
        : ""
    );
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();
    setMessage("");

    if (!storeCode) {
      setMessage(
        isDari
          ? "لطفاً ابتدا یک فروشگاه را انتخاب کنید."
          : "Please select a store first."
      );
      return;
    }

    const numericAmount = Number(amount);

    if (!numericAmount || numericAmount <= 0) {
      setMessage(
        isDari
          ? "لطفاً مبلغ معتبر وارد کنید."
          : "Please enter a valid amount."
      );
      return;
    }

    if (
      requiresWorker &&
      !recipientName.trim()
    ) {
      setMessage(
        isDari
          ? "لطفاً کارمند یا خیاط را انتخاب کنید."
          : "Please select an employee or tailor."
      );
      return;
    }

    const formData: ExpenseFormData = {
      storeCode,
      category,
      recipientName: visibleRecipient.trim(),
      amount: numericAmount,
      currency,
      expenseDate,
      description: description.trim(),
    };

    try {
      setSaving(true);

      if (onSaved) {
        await onSaved(formData);
      } else {
        setMessage(
          isDari
            ? "فرم آماده ثبت است."
            : "The form is ready to be saved."
        );
      }
    } catch {
      setMessage(
        isDari
          ? "ثبت مصرف انجام نشد. دوباره تلاش کنید."
          : "The expense was not saved. Please try again."
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      dir={isDari ? "rtl" : "ltr"}
      className="rounded-3xl border border-[#e5ddd0] bg-white p-4 shadow-[0_8px_24px_rgba(23,63,56,0.08)] sm:p-5"
    >
      <section className="border-b border-[#eee6da] pb-4">
        <ExpenseTypeSelect
          value={category}
          options={expenseOptions}
          onChange={handleCategoryChange}
          isDari={isDari}
        />
      </section>

      <ExpenseFields
        storeCode={storeCode}
        category={category}
        recipientName={recipientName}
        amount={amount}
        currency={currency}
        expenseDate={expenseDate}
        description={description}
        managerName={managerName}
        isDari={isDari}
        onRecipientChange={setRecipientName}
        onAmountChange={setAmount}
        onCurrencyChange={setCurrency}
        onDateChange={setExpenseDate}
        onDescriptionChange={setDescription}
      />

      {message && (
        <p className="mt-3 rounded-xl bg-[#fff1ef] px-4 py-2 text-sm font-semibold text-[#b42318]">
          {message}
        </p>
      )}

      <div
        className={`mt-4 flex flex-col-reverse gap-2 sm:flex-row ${
          isDari
            ? "sm:justify-start"
            : "sm:justify-end"
        }`}
      >
        <button
          type="button"
          onClick={onCancel}
          className="rounded-xl border border-[#b8c2bc] bg-white px-6 py-2.5 font-semibold text-[#4c6258] transition hover:bg-[#f6f7f4]"
        >
          {isDari ? "لغو" : "Cancel"}
        </button>

        <button
          type="submit"
          disabled={saving}
          className="rounded-xl bg-[#c89232] px-7 py-2.5 font-bold text-white transition hover:bg-[#ae7721] disabled:cursor-not-allowed disabled:opacity-60"
        >
          {saving
            ? isDari
              ? "در حال ثبت..."
              : "Saving..."
            : isDari
              ? "ثبت مصرف جدید"
              : "Record New Expense"}
        </button>
      </div>
    </form>
  );
}