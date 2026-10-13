import { useEffect, useState } from "react";

import MetricCard from "../components/MetricCard";
import Sidebar from "../components/Sidebar";
import SalesTable from "../components/SalesTable";
import DirectSaleModal from "../components/DirectSaleModal";
import { getDashboard } from "../services/api";

import type {
  DashboardData,
  Language,
} from "../types/dashboard";

export default function ManagerDashboard() {
  const [language, setLanguage] =
    useState<Language>("dari");

  const [selectedStoreCode, setSelectedStoreCode] =
    useState("all");

  const [dashboard, setDashboard] =
    useState<DashboardData | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showSaleForm, setShowSaleForm] =
    useState(false);

  const isDari = language === "dari";
  const currency = isDari ? "افغانی" : "AFN";

  const text = {
    title: isDari
      ? "پرده سرای بزرگ رویال"
      : "Royal Curtains Store",

    storeName: isDari
      ? "انتخاب فروشگاه"
      : "Choose Store",

    dashboard: isDari
      ? "داشبورد"
      : "Dashboard",

    managerWithdrawal: isDari
      ? "برداشت شخصی مدیر"
      : "Manager Withdrawal",

    welcome: isDari
      ? "به سیستم مدیریت فروشگاه خوش آمدید"
      : "Welcome to the store management system",

    orders: isDari ? "سفارش‌ها" : "Orders",

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

    todayOrders: isDari
      ? "سفارش‌های امروز"
      : "Today's Orders",

    todaySales: isDari
      ? "فروش امروز"
      : "Today's Sales",

    todayExpenses: isDari
      ? "مصارف امروز"
      : "Today's Expenses",

    monthlySales: isDari
      ? "فروش ماه"
      : "Monthly Sales",

    recordedToday: isDari
      ? "ثبت‌شده امروز"
      : "Recorded today",

    ordersAndDirectSales: isDari
      ? "شامل سفارش و فروش مستقیم"
      : "Orders and direct sales",

    expensesRecordedToday: isDari
      ? "مصارف ثبت‌شده امروز"
      : "Recorded today",

    fromStartOfMonth: isDari
      ? "از اول ماه تا امروز"
      : "From the start of the month",

    todaysSales: isDari
      ? "فروش‌های امروز"
      : "Today's Sales",

    salesDescription: isDari
      ? "تمام فروش‌های ثبت‌شده امروز"
      : "All sales recorded today",

    noSales: isDari
      ? "امروز هنوز فروشی ثبت نشده است."
      : "No sales have been recorded today.",

    noExpenses: isDari
      ? "امروز مصرفی ثبت نشده است."
      : "No expenses have been recorded today.",

    time: isDari
      ? "زمان"
      : "Time",

    saleType: isDari
      ? "نوع فروش"
      : "Sale Type",

    amount: isDari
      ? "مبلغ"
      : "Amount",

    today: isDari
      ? "امروز"
      : "Today",

    store: isDari
      ? "فروشگاه"
      : "Store",

    expense: isDari
      ? "مصرف"
      : "Expense",

    searchProduct: isDari
      ? "جستجوی محصول از انبار"
      : "Search Inventory Product",

    productCode: isDari
      ? "کد محصول"
      : "Product Code",

    available: isDari
      ? "موجود"
      : "Available",

    quantityMeters: isDari
      ? "مقدار فروش (متر)"
      : "Sale Amount (meters)",

    pricePerMeter: isDari
      ? "قیمت هر متر (افغانی)"
      : "Price per Meter (AFN)",

    totalPrice: isDari
      ? "قیمت کل (افغانی)"
      : "Total Price (AFN)",

    notes: isDari
      ? "یادداشت"
      : "Notes",

    royalCurtains: isDari
      ? "فروشگاه رویال"
      : "Royal Curtains Store",

    kabulDubai: isDari
      ? "فروشگاه کابل دبی"
      : "Kabul Dubai Curtains Store",

    allStores: isDari
      ? "همه فروشگاه‌ها"
      : "All Stores",

    noProducts: isDari
      ? "محصولی در انبار پیدا نشد."
      : "No inventory products found.",

    changeProduct: isDari
      ? "تغییر محصول"
      : "Choose another product",

    saleFormTitle: isDari
      ? "ثبت فروش مستقیم"
      : "Record Direct Sale",

    cancel: isDari
      ? "لغو"
      : "Cancel",

    saveSale: isDari
      ? "ثبت فروش"
      : "Save Sale",

    saving: isDari
      ? "در حال ثبت..."
      : "Saving...",
  };

  const getExpenseLabel = (category: string) => {
    const labels: Record<
      string,
      [string, string]
    > = {
      STORE_RENT: ["کرایه دکان", "Store Rent"],
      UTILITIES: ["برق و آب", "Utilities"],
      TAX: ["مالیات", "Tax"],
      TRANSPORTATION: ["ترانسپورت", "Transportation"],
      EMPLOYEE_PAYMENT: [
        "پرداخت کارمند",
        "Employee Payment",
      ],
      TAILOR_PAYMENT: [
        "پرداخت خیاط",
        "Tailor Payment",
      ],
      MANAGER_WITHDRAWAL: [
        "برداشت شخصی مدیر",
        "Manager Withdrawal",
      ],
      OTHER: ["مصرف دیگر", "Other Expense"],
    };

    const label = labels[category];

    if (!label) {
      return category;
    }

    return isDari ? label[0] : label[1];
  };

  const formatExpenseTime = (dateTime: string) => {
    if (dateTime.includes("T")) {
      return dateTime.substring(11, 16);
    }

    return dateTime.substring(0, 5);
  };

  const formatExpenseAmount = (
    amount: number,
    expenseCurrency: string
  ) => {
    const displayedCurrency =
      expenseCurrency.toUpperCase() === "USD"
        ? "USD"
        : isDari
          ? "افغانی"
          : "AFN";

    return `${amount.toLocaleString(
      "en-US"
    )} ${displayedCurrency}`;
  };

  const loadDashboard = async (
    storeCode: string
  ) => {
    try {
      setLoading(true);

      const data = await getDashboard(storeCode);

      setDashboard(data);
      setError("");
    } catch {
      setError(
        isDari
          ? "اطلاعات داشبورد بارگذاری نشد."
          : "Could not load dashboard data."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadDashboard(selectedStoreCode);
  }, [selectedStoreCode]);

  const handleManagerWithdrawal = () => {
    window.location.assign(
      `http://localhost:8080/expenses/new?store=${selectedStoreCode}`
    );
  };

  if (loading) {
    return (
      <div
        dir={isDari ? "rtl" : "ltr"}
        className="flex min-h-screen items-center justify-center bg-[#fbf8f1] text-xl text-[#173f38]"
      >
        {isDari
          ? "در حال بارگذاری..."
          : "Loading..."}
      </div>
    );
  }

  if (error || !dashboard) {
    return (
      <div
        dir={isDari ? "rtl" : "ltr"}
        className="flex min-h-screen items-center justify-center bg-[#fbf8f1] px-6 text-center text-xl text-red-700"
      >
        {error}
      </div>
    );
  }

  return (
    <div
      dir={isDari ? "rtl" : "ltr"}
      className="min-h-screen bg-[#fbf8f1] text-[#173f38]"
    >
      <div className="flex min-h-screen">
        <Sidebar
          labels={{
            title: text.title,
            storeName: text.storeName,
            dashboard: text.dashboard,
            managerWithdrawal:
              text.managerWithdrawal,
            orders: text.orders,
            customers: text.customers,
            products: text.products,
            directSales: text.directSales,
            wholesaleAccounts:
              text.wholesaleAccounts,
            expenses: text.expenses,
            users: text.users,
            logout: text.logout,
          }}
          selectedStoreCode={selectedStoreCode}
          storeOptions={[
            {
              code: "all",
              name: text.allStores,
            },
            {
              code: "royal",
              name: text.royalCurtains,
            },
            {
              code: "kabul",
              name: text.kabulDubai,
            },
          ]}
          onStoreChange={setSelectedStoreCode}
          onManagerWithdrawal={
            handleManagerWithdrawal
          }
          onDirectSale={() =>
            setShowSaleForm(true)
          }
        />

        <main className="min-w-0 flex-1">
          <header className="flex items-center justify-between border-b border-[#e9e1d4] bg-[#fffdf9] px-5 py-5 sm:px-8 lg:px-12">
            <div>
              <p className="mb-1 text-sm text-[#9a8063]">
                {text.dashboard}
              </p>

              <h1 className="text-2xl font-bold text-[#173f38] sm:text-3xl">
                {text.welcome}
              </h1>
            </div>

            <div className="flex rounded-full border border-[#d8cbb9] bg-white p-1 text-sm">
              <button
                type="button"
                onClick={() => setLanguage("dari")}
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
                onClick={() =>
                  setLanguage("english")
                }
                className={`rounded-full px-4 py-2 ${
                  !isDari
                    ? "bg-[#d7ad62] text-white"
                    : "text-[#64756f]"
                }`}
              >
                EN
              </button>
            </div>
          </header>

          <section className="px-5 py-7 sm:px-8 lg:px-12">
            <div className="mb-7 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
              <MetricCard
                label={text.todayOrders}
                value={dashboard.todayOrderCount.toString()}
                detail={text.recordedToday}
                icon="▤"
              />

              <MetricCard
                label={text.todaySales}
                value={`${dashboard.todaySalesAfn.toLocaleString(
                  "en-US"
                )} ${currency}`}
                detail={text.ordersAndDirectSales}
                icon="↗"
                highlighted
              />

              <MetricCard
                label={text.todayExpenses}
                value={`${dashboard.todayExpensesAfn.toLocaleString(
                  "en-US"
                )} ${currency}`}
                detail={text.expensesRecordedToday}
                icon="−"
              />

              <MetricCard
                label={text.monthlySales}
                value={`${dashboard.monthlySalesAfn.toLocaleString(
                  "en-US"
                )} ${currency}`}
                detail={text.fromStartOfMonth}
                icon="▥"
              />
            </div>

            <SalesTable
              sales={dashboard.sales}
              isDari={isDari}
              currency={currency}
              labels={{
                title: text.todaysSales,
                description:
                  text.salesDescription,
                noSales: text.noSales,
                time: text.time,
                saleType: text.saleType,
                amount: text.amount,
                today: text.today,
                store: text.store,
              }}
            />

            <section className="mt-5 rounded-3xl border border-[#e9e1d4] bg-white p-5 shadow-[0_12px_35px_rgba(61,49,35,0.06)] sm:p-7">
              <h2 className="mb-5 text-xl font-bold text-[#173f38]">
                {text.todayExpenses}
              </h2>

              {dashboard.todayExpenses.length === 0 ? (
                <p className="rounded-2xl bg-[#fbf8f1] p-6 text-center text-[#8b8175]">
                  {text.noExpenses}
                </p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[700px] border-collapse text-right">
                    <thead>
                      <tr className="border-b border-[#eee6db] text-sm text-[#8b8175]">
                        <th className="px-4 py-4 font-medium">
                          {text.expense}
                        </th>

                        <th className="px-4 py-4 font-medium">
                          {text.amount}
                        </th>

                        <th className="px-4 py-4 font-medium">
                          {text.store}
                        </th>

                        <th className="px-4 py-4 font-medium">
                          {text.time}
                        </th>
                      </tr>
                    </thead>

                    <tbody>
                      {dashboard.todayExpenses.map(
                        (expense, index) => (
                          <tr
                            key={`${expense.dateTime}-${expense.category}-${index}`}
                            className="border-b border-[#f2ece3] last:border-0"
                          >
                            <td className="px-4 py-5 font-semibold text-[#173f38]">
                              {getExpenseLabel(
                                expense.category
                              )}
                            </td>

                            <td className="px-4 py-5 font-semibold text-[#173f38]">
                              {formatExpenseAmount(
                                expense.amount,
                                expense.currency
                              )}
                            </td>

                            <td className="px-4 py-5 text-sm text-[#4f5f59]">
                              {expense.storeName}
                            </td>

                            <td className="px-4 py-5 text-sm text-[#766c60]">
                              {formatExpenseTime(
                                expense.dateTime
                              )}
                            </td>
                          </tr>
                        )
                      )}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          </section>
        </main>
      </div>

      {showSaleForm && (
        <DirectSaleModal
          isDari={isDari}
          labels={{
            title: text.saleFormTitle,
            store: text.store,
            searchProduct: text.searchProduct,
            productCode: text.productCode,
            available: text.available,
            quantityMeters:
              text.quantityMeters,
            pricePerMeter:
              text.pricePerMeter,
            totalPrice: text.totalPrice,
            notes: text.notes,
            royalCurtains:
              text.royalCurtains,
            kabulDubai: text.kabulDubai,
            noProducts: text.noProducts,
            changeProduct:
              text.changeProduct,
            cancel: text.cancel,
            saveSale: text.saveSale,
            saving: text.saving,
          }}
          onClose={() =>
            setShowSaleForm(false)
          }
          onSaved={() => {
            setShowSaleForm(false);
            void loadDashboard(
              selectedStoreCode
            );
          }}
        />
      )}
    </div>
  );
}