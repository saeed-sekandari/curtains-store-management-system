import { useEffect, useState } from "react";

type Language = "dari" | "english";

type SaleRow = {
  dateTime: string;
  type: string;
  description: string;
  amount: number;
  currency: string;
};

type DashboardData = {
  storeName: string;
  storeCode: string;
  todayOrderCount: number;
  customerCount: number;
  todayOrderSalesAfn: number;
  todayDirectSalesAfn: number;
  todaySalesAfn: number;
  todaySalesUsd: number;
  todayExpensesAfn: number;
  todayExpensesUsd: number;
  monthlySalesAfn: number;
  sales: SaleRow[];
};

function App() {
  const [language, setLanguage] = useState<Language>("dari");
  const [dashboard, setDashboard] =
    useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const isDari = language === "dari";
  const currency = isDari ? "افغانی" : "AFN";

  useEffect(() => {
    fetch("/api/dashboard?store=all", {
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Could not load dashboard data");
        }

        return response.json();
      })
      .then((data: DashboardData) => {
        setDashboard(data);
        setLoading(false);
      })
      .catch(() => {
        setError(
          isDari
            ? "اطلاعات داشبورد بارگذاری نشد."
            : "Could not load dashboard data."
        );
        setLoading(false);
      });
  }, [isDari]);

  const formatMoney = (
    amount: number | undefined,
    selectedCurrency = currency
  ) => {
    return `${(amount ?? 0).toLocaleString("en-US")} ${selectedCurrency}`;
  };

  const getSaleType = (type: string) => {
    if (type === "CUSTOMER_ORDER") {
      return isDari ? "سفارش مشتری" : "Customer Order";
    }

    return isDari ? "فروش مستقیم" : "Direct Sale";
  };

  const getSaleTime = (dateTime: string) => {
    if (dateTime.includes("T")) {
      return dateTime.substring(11, 16);
    }

    return dateTime.substring(0, 5);
  };

  const text = {
    title: isDari ? "پرده سرای بزرگ رویال" : "Royal Curtains Store",
    dashboard: isDari ? "داشبورد" : "Dashboard",
    welcome: isDari
      ? "به سیستم مدیریت فروشگاه خوش آمدید"
      : "Welcome to the store management system",

    orders: isDari ? "سفارش‌ها" : "Orders",
    customers: isDari ? "مشتریان" : "Customers",
    products: isDari ? "محصولات" : "Products",
    wholesaleAccounts: isDari
      ? "عمده‌فروشان"
      : "Wholesale Accounts",
    expenses: isDari ? "مصارف" : "Expenses",
    users: isDari ? "کاربران" : "Users",
    logout: isDari ? "خروج" : "Logout",

    todayOrders: isDari ? "سفارش‌های امروز" : "Today's Orders",
    todaySales: isDari ? "فروش امروز" : "Today's Sales",
    todayExpenses: isDari ? "مصارف امروز" : "Today's Expenses",
    monthlySales: isDari ? "فروش ماه" : "Monthly Sales",

    todaysSales: isDari ? "فروش‌های امروز" : "Today's Sales",
    noSales: isDari
      ? "امروز هنوز فروشی ثبت نشده است."
      : "No sales have been recorded today.",

    time: isDari ? "زمان" : "Time",
    saleType: isDari ? "نوع فروش" : "Sale Type",
    description: isDari ? "توضیحات" : "Description",
    amount: isDari ? "مبلغ" : "Amount",

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

    today: isDari ? "امروز" : "Today",

    royalStore: isDari
      ? "فروشگاه پرده سرای رویال"
      : "Royal Curtains Store",
  };

  if (loading) {
    return (
      <div
        dir={isDari ? "rtl" : "ltr"}
        className="flex min-h-screen items-center justify-center bg-[#fbf8f1] text-xl text-[#173f38]"
      >
        {isDari ? "در حال بارگذاری..." : "Loading..."}
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
        <aside className="hidden w-72 shrink-0 flex-col bg-[#123b35] px-5 py-6 text-white lg:flex">
          <div className="mb-10 border-b border-white/10 pb-6">
            <div className="mb-2 text-2xl font-bold">
              {text.title}
            </div>

            <div className="text-sm text-emerald-100/70">
              {text.royalStore}
            </div>
          </div>

          <nav className="space-y-2">
            <MenuItem active label={text.dashboard} icon="⌂" />
            <MenuItem label={text.orders} icon="▤" />
            <MenuItem label={text.customers} icon="♙" />
            <MenuItem label={text.products} icon="▦" />
            <MenuItem
              label={text.wholesaleAccounts}
              icon="◈"
            />
            <MenuItem label={text.expenses} icon="−" />
            <MenuItem label={text.users} icon="◎" />
          </nav>

          <div className="mt-auto">
            <button className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-right text-emerald-100 transition hover:bg-white/10">
              <span className="text-lg">↪</span>
              <span>{text.logout}</span>
            </button>
          </div>
        </aside>

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
                onClick={() => setLanguage("dari")}
                className={`rounded-full px-4 py-2 transition ${
                  isDari
                    ? "bg-[#d7ad62] text-white"
                    : "text-[#64756f]"
                }`}
              >
                دری
              </button>

              <button
                onClick={() => setLanguage("english")}
                className={`rounded-full px-4 py-2 transition ${
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
                value={formatMoney(dashboard.todaySalesAfn)}
                detail={text.ordersAndDirectSales}
                icon="↗"
                highlighted
              />

              <MetricCard
                label={text.todayExpenses}
                value={formatMoney(dashboard.todayExpensesAfn)}
                detail={text.expensesRecordedToday}
                icon="−"
              />

              <MetricCard
                label={text.monthlySales}
                value={formatMoney(dashboard.monthlySalesAfn)}
                detail={text.fromStartOfMonth}
                icon="▥"
              />
            </div>

            <section className="rounded-3xl border border-[#e9e1d4] bg-white p-5 shadow-[0_12px_35px_rgba(61,49,35,0.06)] sm:p-7">
              <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
                <div>
                  <h2 className="text-xl font-bold text-[#173f38]">
                    {text.todaysSales}
                  </h2>

                  <p className="mt-1 text-sm text-[#8b8175]">
                    {isDari
                      ? "تمام فروش‌های ثبت‌شده امروز"
                      : "All sales recorded today"}
                  </p>
                </div>

                <div className="rounded-full bg-[#f7f1e7] px-4 py-2 text-sm text-[#987348]">
                  {text.today}
                </div>
              </div>

              {dashboard.sales.length === 0 ? (
                <p className="rounded-2xl bg-[#fbf8f1] p-6 text-center text-[#8b8175]">
                  {text.noSales}
                </p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[650px] border-collapse text-right">
                    <thead>
                      <tr className="border-b border-[#eee6db] text-sm text-[#8b8175]">
                        <th className="px-4 py-4 font-medium">
                          {text.time}
                        </th>

                        <th className="px-4 py-4 font-medium">
                          {text.saleType}
                        </th>

                        <th className="px-4 py-4 font-medium">
                          {text.description}
                        </th>

                        <th className="px-4 py-4 font-medium">
                          {text.amount}
                        </th>
                      </tr>
                    </thead>

                    <tbody>
                      {dashboard.sales.map((sale) => (
                        <tr
                          key={`${sale.dateTime}-${sale.description}`}
                          className="border-b border-[#f2ece3] last:border-0"
                        >
                          <td className="px-4 py-5 text-sm text-[#766c60]">
                            {getSaleTime(sale.dateTime)}
                          </td>

                          <td className="px-4 py-5">
                            <span className="rounded-full bg-[#eef6f1] px-3 py-1.5 text-sm text-[#267052]">
                              {getSaleType(sale.type)}
                            </span>
                          </td>

                          <td className="px-4 py-5 text-sm text-[#4f5f59]">
                            {sale.description}
                          </td>

                          <td className="px-4 py-5 font-semibold text-[#173f38]">
                            {formatMoney(
                              sale.amount,
                              isDari
                                ? "افغانی"
                                : sale.currency
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          </section>
        </main>
      </div>
    </div>
  );
}

function MenuItem({
  label,
  icon,
  active = false,
}: {
  label: string;
  icon: string;
  active?: boolean;
}) {
  return (
    <button
      className={`flex w-full items-center gap-3 rounded-xl px-4 py-3 text-right transition ${
        active
          ? "bg-[#d7ad62] font-semibold text-white"
          : "text-emerald-50 hover:bg-white/10"
      }`}
    >
      <span className="w-6 text-center text-lg">{icon}</span>
      <span>{label}</span>
    </button>
  );
}

function MetricCard({
  label,
  value,
  detail,
  icon,
  highlighted = false,
}: {
  label: string;
  value: string;
  detail: string;
  icon: string;
  highlighted?: boolean;
}) {
  return (
    <article
      className={`rounded-3xl border p-5 shadow-[0_10px_30px_rgba(61,49,35,0.05)] ${
        highlighted
          ? "border-[#d7ad62] bg-[#fff8e9]"
          : "border-[#e9e1d4] bg-white"
      }`}
    >
      <div className="mb-5 flex items-center justify-between">
        <span className="text-sm font-medium text-[#776d61]">
          {label}
        </span>

        <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#f4ecdf] text-lg text-[#a47739]">
          {icon}
        </span>
      </div>

      <div className="text-2xl font-bold text-[#173f38]">
        {value}
      </div>

      <p className="mt-2 text-xs text-[#958b7d]">{detail}</p>
    </article>
  );
}

export default App;