import { useMemo, useState } from "react";
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
} from "../types/expense";

type ExpenseFormProps = {
  storeCode?: string;
  storeName?: string;
  managerName?: string;
  isDari?: boolean;
  onCancel?: () => void;
  onSaved?: (data: ExpenseFormData) => Promise<void>;
};

export default function ExpenseForm({
  storeCode = "",
  managerName = "مدیر سیستم",
  isDari = true,
  onCancel,
  onSaved,
}: ExpenseFormProps) {
  const [category, setCategory] =
    useState<ExpenseCategory>("MANAGER_WITHDRAWAL");

  const [recipientName, setRecipientName] = useState("");
  const [amount, setAmount] = useState("");

  const [currency, setCurrency] =
    useState<ExpenseCurrency>("AFN");

  const [expenseDate, setExpenseDate] = useState(
    getCurrentDateTime()
  );

  const [description, setDescription] = useState("");
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [success, setSuccess] = useState(false);

  const selectedOption = useMemo(() => {
    return expenseOptions.find(
      (option) => option.value === category
    );
  }, [category]);

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
    setSuccess(false);

    if (nextCategory === "MANAGER_WITHDRAWAL") {
      setRecipientName(managerName);
    } else {
      setRecipientName("");
    }
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setMessage("");
    setSuccess(false);

    const numericAmount = Number(amount);

    if (!storeCode) {
      setMessage(
        isDari
          ? "لطفاً ابتدا فروشگاه را انتخاب کنید."
          : "Please select a store first."
      );
      return;
    }

    if (!numericAmount || numericAmount <= 0) {
      setMessage(
        isDari
          ? "لطفاً مبلغ معتبر وارد کنید."
          : "Please enter a valid amount."
      );
      return;
    }

    if (requiresWorker && !recipientName.trim()) {
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

        setSuccess(true);
        setMessage(
          isDari
            ? "مصرف با موفقیت ثبت شد."
            : "Expense saved successfully."
        );

        setAmount("");
        setRecipientName("");
        setDescription("");
        setExpenseDate(getCurrentDateTime());
      } else {
        setMessage(
          isDari
            ? "فرم آماده ثبت است."
            : "The form is ready to submit."
        );
      }
    } catch {
      setSuccess(false);
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
      className="rounded-3xl border border-[#e5ddd0] bg-white p-5 shadow-[0_12px_35px_rgba(23,63,56,0.08)] sm:p-7"
    >
      <section className="border-b border-[#eee6da] pb-5">
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
        <p
          className={`mt-5 rounded-xl px-4 py-3 text-sm font-semibold ${
            success
              ? "bg-[#edf8ef] text-[#276044]"
              : "bg-[#fff1ef] text-[#b42318]"
          }`}
        >
          {message}
        </p>
      )}

      <div className="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
        <button
          type="button"
          onClick={onCancel}
          className="rounded-xl border border-[#b8c2bc] bg-white px-7 py-3 font-semibold text-[#4c6258] transition hover:bg-[#f6f7f4]"
        >
          {isDari ? "لغو" : "Cancel"}
        </button>

        <button
          type="submit"
          disabled={saving}
          className="rounded-xl bg-[#c89232] px-8 py-3 font-bold text-white transition hover:bg-[#ae7721] disabled:cursor-not-allowed disabled:opacity-60"
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