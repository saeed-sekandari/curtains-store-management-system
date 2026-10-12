import { useEffect, useState } from "react";

import Sidebar from "../components/Sidebar";
import ExpenseForm from "../components/ExpenseForm";
import ExpenseReportFilters, {
  type ReportPeriod,
} from "../components/ExpenseReportFilters";
import ExpenseSummaryCards from "../components/ExpenseSummaryCards";
import ExpenseTable, {
  type ExpenseReportRecord,
} from "../components/ExpenseTable";

import { createExpense } from "../services/expenseApi";

import type { ExpenseFormData } from "../types/expense";

type Language = "dari" | "english";

type ExpensePageProps = {
  storeCode?: string;
  managerName?: string;
};

type ExpenseReportData = {
  totalRecords: number;
  totalAfn: number;
  totalUsd: number;
  records: ExpenseReportRecord[];
};

const storeOptions = [
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

const formStoreOptions = storeOptions.filter(
  (store) => store.code !== "all"
);

const emptyReport: ExpenseReportData = {
  totalRecords: 0,
  totalAfn: 0,
  totalUsd: 0,
  records: [],
};

function getToday(): string {
  return new Date().toISOString().slice(0, 10);
}

async function loadExpenseReport(
  storeCode: string,
  date: string,
  period: ReportPeriod
): Promise<ExpenseReportData> {
  const params = new URLSearchParams();

  params.set("store", storeCode);
  params.set("date", date);
  params.set("period", period);

  const response = await fetch(
    `/api/expenses/report?${params.toString()}`,
    {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Expense report could not be loaded");
  }

  return response.json() as Promise<ExpenseReportData>;
}

export default function ExpensePage({
  storeCode = "royal",
  managerName = "مدیر سیستم",
}: ExpensePageProps) {
  const [language, setLanguage] =
    useState<Language>("dari");

  const [formStoreCode, setFormStoreCode] =
    useState(
      storeCode === "all" ? "" : storeCode
    );

  const [reportStoreCode, setReportStoreCode] =
    useState(storeCode);

  const [reportDate, setReportDate] =
    useState(getToday());

  const [reportPeriod, setReportPeriod] =
    useState<ReportPeriod>("today");

  const [report, setReport] =
    useState<ExpenseReportData>(emptyReport);

  const [reportLoading, setReportLoading] =
    useState(true);

  const [reportError, setReportError] =
    useState("");

  const isDari = language === "dari";

  const text = {
    title: isDari
      ? "پرده‌سرای بزرگ رویال"
      : "Royal Curtains Store",

    storeSelector: isDari
      ? "انتخاب فروشگاه"
      : "Select Store",

    storeForExpense: isDari
      ? "فروشگاه مصرف"
      : "Expense Store",

    chooseStore: isDari
      ? "لطفاً فروشگاه را انتخاب کنید"
      : "Please select a store",

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

    pageTitle: isDari
      ? "ثبت مصرف یا برداشت"
      : "Record Expense or Withdrawal",

    reportTitle: isDari
      ? "گزارش مصارف"
      : "Expense Report",

    loading: isDari
      ? "در حال بارگذاری گزارش..."
      : "Loading report...",

    reportError: isDari
      ? "اطلاعات گزارش مصارف بارگذاری نشد."
      : "Expense report could not be loaded.",

    retry: isDari
      ? "تلاش دوباره"
      : "Try Again",

    dari: "دری",
    english: "EN",
  };

  const handleCancel = () => {
    window.history.back();
  };

  const handleSave = async (data: ExpenseFormData) => {
    if (!formStoreCode) {
      throw new Error("A store must be selected");
    }

    await createExpense({
      ...data,
      storeCode: formStoreCode,
    });

    window.location.assign(
      `/expenses/new?store=${formStoreCode}`
    );
  };

  const refreshReport = async () => {
    try {
      setReportLoading(true);
      setReportError("");

      const data = await loadExpenseReport(
        reportStoreCode,
        reportDate,
        reportPeriod
      );

      setReport(data);
    } catch {
      setReportError(text.reportError);
    } finally {
      setReportLoading(false);
    }
  };

  useEffect(() => {
    refreshReport();
  }, [
    reportStoreCode,
    reportDate,
    reportPeriod,
  ]);

  return (
    <div
      dir={isDari ? "rtl" : "ltr"}
      className="min-h-screen bg-[#fbf8f1] text-[#173f38]"
    >
      <div className="flex min-h-screen">
        <Sidebar
          isDari={isDari}
          labels={{
            title: text.title,
            storeName: text.storeSelector,
            dashboard: text.dashboard,
            managerWithdrawal: text.managerWithdrawal,
            orders: text.orders,
            customers: text.customers,
            products: text.products,
            directSales: text.directSales,
            wholesaleAccounts: text.wholesaleAccounts,
            expenses: text.expenses,
            users: text.users,
            logout: text.logout,
          }}
          selectedStoreCode={storeCode}
          storeOptions={storeOptions.map((store) => ({
            code: store.code,
            name: isDari
              ? store.dari
              : store.english,
          }))}
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
              `/dashboard?store=${storeCode}`
            );
          }}
        />

        <main className="min-w-0 flex-1">
          <header className="border-b border-[#e9e1d4] bg-[#fffdf9] px-5 py-4 sm:px-8 lg:px-12">
            <div className="mx-auto flex max-w-6xl items-center justify-between gap-4">
              <h1 className="text-2xl font-bold text-[#173f38] sm:text-3xl">
                {text.pageTitle}
              </h1>

              <div className="flex rounded-full border border-[#d8cbb9] bg-white p-1 text-sm">
                <button
                  type="button"
                  onClick={() =>
                    setLanguage("dari")
                  }
                  className={`rounded-full px-4 py-2 ${
                    isDari
                      ? "bg-[#d7ad62] text-white"
                      : "text-[#64756f]"
                  }`}
                >
                  {text.dari}
                </button>

                <button
                  type="button"
                  onClick={() =>
                    setLanguage("english")
                  }
                  className={`rounded-full px-4 py-2 ${
                    !isDari
                      ? "bg-[#d7ad62] text-white"
                      : "text-[#64756f]"
                  }`}
                >
                  {text.english}
                </button>
              </div>
            </div>
          </header>

          <section className="px-5 py-4 sm:px-8 lg:px-12">
            <div className="mx-auto max-w-6xl">
              <div className="mb-4 rounded-2xl border border-[#dfe8df] bg-[#f4faf4] px-5 py-3">
                <label
                  htmlFor="expense-store"
                  className="mb-2 block text-sm font-bold text-[#173f38]"
                >
                  {text.storeForExpense}
                </label>

                <select
                  id="expense-store"
                  value={formStoreCode}
                  onChange={(event) =>
                    setFormStoreCode(
                      event.target.value
                    )
                  }
                  className="w-full rounded-xl border border-[#cbd8cc] bg-white px-4 py-3 font-bold text-[#173f38] outline-none focus:border-[#d7ad62] focus:ring-2 focus:ring-[#d7ad62]/20"
                >
                  <option value="">
                    {text.chooseStore}
                  </option>

                  {formStoreOptions.map((store) => (
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
                managerName={managerName}
                isDari={isDari}
                onCancel={handleCancel}
                onSaved={handleSave}
              />

              <section className="mt-6">
                <h2 className="mb-4 text-2xl font-bold text-[#173f38] sm:text-3xl">
                  {text.reportTitle}
                </h2>

                <ExpenseReportFilters
                  selectedStoreCode={reportStoreCode}
                  date={reportDate}
                  period={reportPeriod}
                  storeOptions={storeOptions.map(
                    (store) => ({
                      code: store.code,
                      name: isDari
                        ? store.dari
                        : store.english,
                    })
                  )}
                  isDari={isDari}
                  onStoreChange={setReportStoreCode}
                  onDateChange={setReportDate}
                  onPeriodChange={setReportPeriod}
                />

                {reportLoading ? (
                  <div className="rounded-2xl border border-[#eadfce] bg-white px-6 py-10 text-center text-[#71817a] shadow-sm">
                    {text.loading}
                  </div>
                ) : reportError ? (
                  <div className="rounded-2xl border border-red-200 bg-red-50 px-6 py-10 text-center text-red-700 shadow-sm">
                    <p>{reportError}</p>

                    <button
                      type="button"
                      onClick={refreshReport}
                      className="mt-4 rounded-xl bg-[#173f38] px-5 py-2.5 font-bold text-white"
                    >
                      {text.retry}
                    </button>
                  </div>
                ) : (
                  <>
                    <ExpenseSummaryCards
                      totalRecords={
                        report.totalRecords
                      }
                      totalAfn={report.totalAfn}
                      totalUsd={report.totalUsd}
                      isDari={isDari}
                    />

                    <ExpenseTable
                      records={report.records}
                      isDari={isDari}
                    />
                  </>
                )}
              </section>
            </div>
          </section>
        </main>
      </div>
    </div>
  );
}