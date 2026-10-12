import { useEffect, useState } from "react";

import Sidebar from "../components/Sidebar";
import ExpenseForm from "../components/ExpenseForm";
import ExpenseReportFilters from "../components/ExpenseReportFilters";
import ExpenseSummaryCards from "../components/ExpenseSummaryCards";
import ExpenseTable from "../components/ExpenseTable";

import { createExpense } from "../services/expenseApi";

import type { ExpenseFormData } from "../types/expense";
import type { ReportPeriod } from "../components/ExpenseReportFilters";

type ExpensePageProps = {
  storeCode?: string;
  managerName?: string;
};

type StoreOption = {
  code: string;
  dari: string;
  english: string;
};

const stores: StoreOption[] = [
  {
    code: "all",
    dari: "همه فروشگاه‌ها",
    english: "All Stores",
  },
  {
    code: "royal",
    dari: "فروشگاه پرده‌سرای رویال",
    english: "Royal Curtains Store",
  },
  {
    code: "kabul",
    dari: "فروشگاه کابل دبی",
    english: "Kabul Dubai Curtains Store",
  },
];

export default function ExpensePage({
  storeCode = "royal",
  managerName = "مدیر سیستم",
}: ExpensePageProps) {
  const [isDari, setIsDari] = useState(true);

  const [formStoreCode, setFormStoreCode] = useState(
    storeCode === "all" ? "" : storeCode
  );

  const [reportStoreCode, setReportStoreCode] =
    useState(storeCode);

  const [reportDate, setReportDate] = useState(
    new Date().toISOString().slice(0, 10)
  );

  const [reportPeriod, setReportPeriod] =
    useState<ReportPeriod>("month");

  const [report, setReport] = useState<{
    totalRecords: number;
    totalAfn: number;
    totalUsd: number;
    records: Array<{
      id: number;
      dateTime: string;
      storeName: string;
      category: string;
      recipientName: string;
      amount: number;
      currency: string;
    }>;
  } | null>(null);

  const [reportLoading, setReportLoading] =
    useState(true);

  const [reportError, setReportError] =
    useState("");

  const currentStore = stores.find(
    (store) => store.code === formStoreCode
  );

  const selectedReportStore = stores.find(
    (store) => store.code === reportStoreCode
  );

  const loadReport = async () => {
    try {
      setReportLoading(true);
      setReportError("");

      const params = new URLSearchParams({
        store: reportStoreCode,
        date: reportDate,
        period: reportPeriod,
      });

      const response = await fetch(
        `http://localhost:8080/api/expenses/report?${params}`,
        {
          credentials: "include",
        }
      );

      if (!response.ok) {
        throw new Error("Report request failed");
      }

      const data = await response.json();
      setReport(data);
    } catch {
      setReportError(
        isDari
          ? "اطلاعات گزارش مصارف بارگذاری نشد."
          : "Expense report could not be loaded."
      );
    } finally {
      setReportLoading(false);
    }
  };

  useEffect(() => {
    loadReport();
  }, [reportStoreCode, reportDate, reportPeriod]);

  const handleSave = async (
    data: ExpenseFormData
  ): Promise<void> => {
    await createExpense(data);

    await loadReport();
  };

  const storeOptionsForSidebar = stores.map((store) => ({
    code: store.code,
    name: isDari ? store.dari : store.english,
  }));

  return (
    <div
      dir={isDari ? "rtl" : "ltr"}
      className="min-h-screen bg-[#fbf8f1] text-[#173f38]"
    >
      <div className="flex min-h-screen">
        <Sidebar
          isDari={isDari}
          labels={{
            title: "پرده‌سرای بزرگ رویال",
            storeName: isDari
              ? "انتخاب فروشگاه"
              : "Select Store",
            dashboard: isDari
              ? "داشبورد"
              : "Dashboard",
            managerWithdrawal: isDari
              ? "برداشت شخصی مدیر"
              : "Manager Withdrawal",
            orders: isDari
              ? "سفارش‌ها"
              : "Orders",
            customers: isDari
              ? "مشتریان"
              : "Customers",
            products: isDari
              ? "محصولات"
              : "Products",
            directSales: isDari
              ? "فروش مستقیم"
              : "Direct Sales",
            wholesaleAccounts: isDari
              ? "عمده‌فروشان"
              : "Wholesale Accounts",
            expenses: isDari
              ? "مصارف"
              : "Expenses",
            users: isDari
              ? "کاربران"
              : "Users",
            logout: isDari
              ? "خروج"
              : "Logout",
          }}
          selectedStoreCode={reportStoreCode}
          storeOptions={storeOptionsForSidebar}
          onStoreChange={(nextStoreCode) => {
            window.location.assign(
              `/expenses/new?store=${nextStoreCode}`
            );
          }}
          onManagerWithdrawal={() => {
            window.scrollTo({
              top: 0,
              behavior: "smooth",
            });
          }}
          onDirectSale={() => {
            window.location.assign(
              `/dashboard?store=${reportStoreCode}`
            );
          }}
        />

        <main className="min-w-0 flex-1">
          <header className="border-b border-[#e9e1d4] bg-[#fffdf9] px-5 py-5 sm:px-8 lg:px-12">
            <div className="mx-auto flex max-w-6xl items-center justify-between gap-5">
              <div>
                <p className="mb-1 text-sm text-[#9a8063]">
                  {isDari
                    ? "خانه ← مصارف"
                    : "Home ← Expenses"}
                </p>

                <h1 className="text-2xl font-bold text-[#173f38] sm:text-3xl">
                  {isDari
                    ? "ثبت مصرف یا برداشت"
                    : "Record Expense or Withdrawal"}
                </h1>
              </div>

              <div className="flex rounded-full border border-[#d8cbb9] bg-white p-1 text-sm">
                <button
                  type="button"
                  onClick={() => setIsDari(true)}
                  className={`rounded-full px-4 py-2 ${
                    isDari
                      ? "bg-[#d7ad62] text-white"
                      : "text-[#64756f]"
                  }`}
                >
                  دری
                </button>

                <button
                  type="button"
                  onClick={() => setIsDari(false)}
                  className={`rounded-full px-4 py-2 ${
                    !isDari
                      ? "bg-[#d7ad62] text-white"
                      : "text-[#64756f]"
                  }`}
                >
                  EN
                </button>
              </div>
            </div>
          </header>

          <section className="px-5 py-6 sm:px-8 lg:px-12">
            <div className="mx-auto max-w-6xl">
              <div className="mb-5 rounded-2xl border border-[#dfe8df] bg-[#f4faf4] px-5 py-4">
                <label
                  htmlFor="expense-store"
                  className="mb-2 block text-sm font-bold text-[#173f38]"
                >
                  {isDari
                    ? "فروشگاه مصرف"
                    : "Expense Store"}
                </label>

                <select
                  id="expense-store"
                  value={formStoreCode}
                  onChange={(event) => {
                    setFormStoreCode(event.target.value);
                    setReportStoreCode(event.target.value);
                  }}
                  className="form-input"
                  required
                >
                  <option value="">
                    {isDari
                      ? "لطفاً فروشگاه را انتخاب کنید"
                      : "Please select a store"}
                  </option>

                  {stores
                    .filter((store) => store.code !== "all")
                    .map((store) => (
                      <option
                        key={store.code}
                        value={store.code}
                      >
                        {isDari
                          ? store.dari
                          : store.english}
                      </option>
                    ))}
                </select>
              </div>

              <ExpenseForm
                storeCode={formStoreCode}
                storeName={
                  currentStore
                    ? isDari
                      ? currentStore.dari
                      : currentStore.english
                    : ""
                }
                managerName={managerName}
                isDari={isDari}
                onCancel={() => window.history.back()}
                onSaved={handleSave}
              />

              <section className="mt-7 rounded-3xl border border-[#e5ddd0] bg-white p-5 shadow-[0_12px_35px_rgba(23,63,56,0.07)] sm:p-7">
                <h2 className="mb-5 text-2xl font-bold text-[#173f38]">
                  {isDari
                    ? "گزارش مصارف"
                    : "Expense Report"}
                </h2>

                <ExpenseReportFilters
                  isDari={isDari}
                  storeOptions={stores}
                  selectedStoreCode={reportStoreCode}
                  selectedDate={reportDate}
                  period={reportPeriod}
                  onStoreChange={(value) => {
                    setReportStoreCode(value);
                    setFormStoreCode(
                      value === "all" ? "" : value
                    );
                  }}
                  onDateChange={setReportDate}
                  onPeriodChange={setReportPeriod}
                />

                {reportLoading && (
                  <p className="mt-6 text-[#6d7b75]">
                    {isDari
                      ? "در حال بارگذاری..."
                      : "Loading..."}
                  </p>
                )}

                {reportError && !reportLoading && (
                  <div className="mt-6 rounded-xl bg-[#fff1ef] px-4 py-3 text-sm font-semibold text-[#b42318]">
                    <p>{reportError}</p>

                    <button
                      type="button"
                      onClick={loadReport}
                      className="mt-2 underline"
                    >
                      {isDari
                        ? "تلاش دوباره"
                        : "Try again"}
                    </button>
                  </div>
                )}

                {report && !reportLoading && !reportError && (
                  <>
                    <ExpenseSummaryCards
                      isDari={isDari}
                      totalRecords={report.totalRecords}
                      totalAfn={report.totalAfn}
                      totalUsd={report.totalUsd}
                    />

                    <ExpenseTable
                      isDari={isDari}
                      records={report.records}
                    />
                  </>
                )}

                {selectedReportStore && (
                  <p className="mt-5 text-sm text-[#89948c]">
                    {isDari
                      ? `فروشگاه: ${selectedReportStore.dari}`
                      : `Store: ${selectedReportStore.english}`}
                  </p>
                )}
              </section>
            </div>
          </section>
        </main>
      </div>
    </div>
  );
}