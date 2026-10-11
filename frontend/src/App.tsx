import { useState } from "react";

type Language = "dari" | "english";

type Order = {
  number: string;
  customer: string;
  amount: string;
  status: string;
};

const dariOrders: Order[] = [
  {
    number: "PR-2001",
    customer: "احمد سلیمانی",
    amount: "150,000",
    status: "تکمیل شد",
  },
  {
    number: "PR-2002",
    customer: "حامد افغان",
    amount: "180,000",
    status: "در حال اجرا",
  },
  {
    number: "PR-2003",
    customer: "نسرین احمدی",
    amount: "90,000",
    status: "جدید",
  },
  {
    number: "PR-2004",
    customer: "قدیر نوری",
    amount: "270,000",
    status: "تکمیل شد",
  },
];

const englishOrders: Order[] = [
  {
    number: "PR-2001",
    customer: "Ahmad Soleimani",
    amount: "150,000",
    status: "Completed",
  },
  {
    number: "PR-2002",
    customer: "Hamed Afghan",
    amount: "180,000",
    status: "In Progress",
  },
  {
    number: "PR-2003",
    customer: "Nasrin Ahmadi",
    amount: "90,000",
    status: "New",
  },
  {
    number: "PR-2004",
    customer: "Qadir Noori",
    amount: "270,000",
    status: "Completed",
  },
];

function App() {
  const [language, setLanguage] = useState<Language>("dari");

  const isDari = language === "dari";
  const currentOrders = isDari ? dariOrders : englishOrders;

  const text = {
    brand: isDari
      ? "پرده سرای بزرگ رویال"
      : "Royal Grand Curtain House",

    system: isDari
      ? "سیستم مدیریت فروشگاه"
      : "Store Management System",

    dashboard: isDari ? "داشبورد" : "Dashboard",
    orders: isDari ? "سفارش‌ها" : "Orders",
    customers: isDari ? "مشتریان" : "Customers",
    products: isDari ? "محصولات" : "Products",

    wholesaleAccounts: isDari
      ? "حساب عمده‌فروشان"
      : "Wholesale Accounts",

    expenses: isDari ? "مصارف" : "Expenses",
    users: isDari ? "کاربران" : "Users",
    logout: isDari ? "خروج" : "Log out",

    welcome: isDari ? "خوش آمدید" : "Welcome",
    manager: isDari ? "مدیریت فروشگاه" : "Store Manager",

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

    orderCount: isDari ? "سفارش" : "orders",
    afghani: isDari ? "افغانی" : "AFN",

    recentOrders: isDari
      ? "سفارش‌های امروز"
      : "Today's Orders",

    recentOrdersDescription: isDari
      ? "سفارش‌هایی که امروز ثبت شده‌اند"
      : "Orders created today",

    orderNumber: isDari
      ? "شماره سفارش"
      : "Order Number",

    customer: isDari ? "مشتری" : "Customer",
    amount: isDari ? "مبلغ" : "Amount",
    status: isDari ? "وضعیت" : "Status",
    viewAll: isDari ? "مشاهده همه" : "View all",

    completed: isDari ? "تکمیل شد" : "Completed",
    inProgress: isDari ? "در حال اجرا" : "In Progress",
  };

  function getStatusClass(status: string) {
    if (status === text.completed) {
      return "bg-emerald-100 text-emerald-700";
    }

    if (status === text.inProgress) {
      return "bg-amber-100 text-amber-700";
    }

    return "bg-blue-100 text-blue-700";
  }

  return (
    <div
      dir={isDari ? "rtl" : "ltr"}
      className="min-h-screen bg-[#fbf8f1] text-[#123b35]"
    >
      <div className="flex min-h-screen">

        <aside className="flex w-[260px] flex-col bg-[#06483f] px-5 py-7 text-white">

          <div className="mb-12 text-center">

            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-[#d5a947] text-3xl text-[#06483f]">
              ✦
            </div>

            <h1 className="text-xl font-bold">
              {text.brand}
            </h1>

            <p className="mt-2 text-sm text-emerald-100">
              {text.system}
            </p>
          </div>

          <nav className="space-y-2">

            <button className="flex w-full items-center gap-4 rounded-xl bg-[#d5a947] px-4 py-3 text-right font-bold text-[#123b35] shadow-md">
              <span className="text-xl">▦</span>
              <span>{text.dashboard}</span>
            </button>

            <button className="flex w-full items-center gap-4 rounded-xl px-4 py-3 text-right text-emerald-50 transition hover:bg-white/10">
              <span className="text-xl">🛒</span>
              <span>{text.orders}</span>
            </button>

            <button className="flex w-full items-center gap-4 rounded-xl px-4 py-3 text-right text-emerald-50 transition hover:bg-white/10">
              <span className="text-xl">👥</span>
              <span>{text.customers}</span>
            </button>

            <button className="flex w-full items-center gap-4 rounded-xl px-4 py-3 text-right text-emerald-50 transition hover:bg-white/10">
              <span className="text-xl">□</span>
              <span>{text.products}</span>
            </button>

            <button className="flex w-full items-center gap-4 rounded-xl px-4 py-3 text-right text-emerald-50 transition hover:bg-white/10">
              <span className="text-xl">🚚</span>
              <span>{text.wholesaleAccounts}</span>
            </button>

            <button className="flex w-full items-center gap-4 rounded-xl px-4 py-3 text-right text-emerald-50 transition hover:bg-white/10">
              <span className="text-xl">▤</span>
              <span>{text.expenses}</span>
            </button>

            <button className="flex w-full items-center gap-4 rounded-xl px-4 py-3 text-right text-emerald-50 transition hover:bg-white/10">
              <span className="text-xl">⚙</span>
              <span>{text.users}</span>
            </button>

          </nav>

          <button className="mt-auto flex items-center gap-4 rounded-xl px-4 py-3 text-right text-emerald-100 transition hover:bg-white/10">
            <span className="text-xl">↪</span>
            <span>{text.logout}</span>
          </button>
        </aside>

        <main className="flex-1">

          <header className="flex items-center justify-between border-b border-[#e9e2d3] bg-white px-10 py-7">

            <div>
              <h2 className="text-3xl font-bold text-[#123b35]">
                {text.welcome} 👋
              </h2>

              <p className="mt-2 text-[#71827e]">
                {text.manager}
              </p>
            </div>

            <div className="flex items-center gap-4">

              <button
                onClick={() =>
                  setLanguage(isDari ? "english" : "dari")
                }
                className="rounded-full border border-[#d9cdb8] bg-[#fbf8f1] px-5 py-2 font-semibold text-[#06483f] transition hover:bg-[#f1e8d7]"
              >
                دری | EN
              </button>

              <div className="flex h-11 w-11 items-center justify-center rounded-full bg-[#f0e9db] text-xl">
                👤
              </div>

            </div>
          </header>

          <div className="px-10 py-8">

            <section className="grid grid-cols-1 overflow-hidden rounded-2xl border border-[#eee5d5] bg-white shadow-sm md:grid-cols-2 xl:grid-cols-4">

              <SummaryCard
                title={text.todayOrders}
                value="126"
                subtitle={text.orderCount}
                icon="▤"
                iconClass="bg-emerald-100 text-emerald-700"
              />

              <SummaryCard
                title={text.todaySales}
                value="525,000"
                subtitle={text.afghani}
                icon="🛒"
                iconClass="bg-emerald-100 text-emerald-700"
              />

              <SummaryCard
                title={text.todayExpenses}
                value="86,000"
                subtitle={text.afghani}
                icon="▣"
                iconClass="bg-amber-100 text-amber-700"
              />

              <SummaryCard
                title={text.monthlySales}
                value="9,280,000"
                subtitle={text.afghani}
                icon="▥"
                iconClass="bg-red-100 text-red-600"
              />

            </section>

            <section className="mt-8 rounded-2xl border border-[#eee5d5] bg-white p-7 shadow-sm">

              <div className="mb-6 flex items-center justify-between">

                <div>
                  <h3 className="text-2xl font-bold text-[#123b35]">
                    {text.recentOrders}
                  </h3>

                  <p className="mt-1 text-sm text-[#71827e]">
                    {text.recentOrdersDescription}
                  </p>
                </div>

                <button className="font-semibold text-[#a8791e] hover:text-[#806016]">
                  {text.viewAll}
                </button>

              </div>

              <div className="overflow-x-auto">

                <table className="w-full min-w-[650px] border-collapse text-right">

                  <thead>
                    <tr className="border-b border-[#e9e2d3] bg-[#f7f5ef] text-sm text-[#60736d]">

                      <th className="px-5 py-4 font-semibold">
                        {text.orderNumber}
                      </th>

                      <th className="px-5 py-4 font-semibold">
                        {text.customer}
                      </th>

                      <th className="px-5 py-4 font-semibold">
                        {text.amount}
                      </th>

                      <th className="px-5 py-4 font-semibold">
                        {text.status}
                      </th>

                    </tr>
                  </thead>

                  <tbody>
                    {currentOrders.map((order) => (
                      <tr
                        key={order.number}
                        className="border-b border-[#f0ebe2] last:border-0 hover:bg-[#fcfaf5]"
                      >
                        <td className="px-5 py-5 font-semibold text-[#315c55]">
                          {order.number}
                        </td>

                        <td className="px-5 py-5 text-[#435c56]">
                          {order.customer}
                        </td>

                        <td className="px-5 py-5 font-semibold text-[#435c56]">
                          {order.amount} {text.afghani}
                        </td>

                        <td className="px-5 py-5">
                          <span
                            className={`rounded-full px-4 py-2 text-sm font-semibold ${getStatusClass(
                              order.status
                            )}`}
                          >
                            {order.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>

                </table>
              </div>
            </section>

          </div>
        </main>
      </div>
    </div>
  );
}

type SummaryCardProps = {
  title: string;
  value: string;
  subtitle: string;
  icon: string;
  iconClass: string;
};

function SummaryCard({
  title,
  value,
  subtitle,
  icon,
  iconClass,
}: SummaryCardProps) {
  return (
    <article className="border-b border-[#eee5d5] px-6 py-7 text-right last:border-b-0 md:border-l xl:border-b-0">

      <div className="flex items-start justify-between gap-4">

        <div>
          <p className="text-sm font-semibold text-[#71827e]">
            {title}
          </p>

          <p className="mt-5 text-3xl font-bold text-[#123b35]">
            {value}
          </p>

          <p className="mt-2 text-sm text-[#71827e]">
            {subtitle}
          </p>
        </div>

        <div
          className={`flex h-12 w-12 items-center justify-center rounded-full text-xl ${iconClass}`}
        >
          {icon}
        </div>

      </div>
    </article>
  );
}

export default App;