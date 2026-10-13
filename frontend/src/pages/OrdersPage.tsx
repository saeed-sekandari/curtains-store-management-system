import { useEffect, useState } from "react";

import Sidebar from "../components/Sidebar";
import OrderForm from "../components/OrderForm";
import OrdersTable from "../components/OrdersTable";

import {
  createOrder,
  getOrderFormData,
  getOrders,
} from "../services/orderApi";

import type {
  CreateOrderRequest,
  OrderFormData,
  OrdersResponse,
} from "../types/order";

type OrdersPageProps = {
  storeCode?: string;
};

const storeOptions = [
  {
    code: "all",
    name: "همه فروشگاه‌ها",
  },
  {
    code: "royal",
    name: "فروشگاه پرده‌سرای رویال",
  },
  {
    code: "kabul",
    name: "فروشگاه کابل دبی",
  },
];

export default function OrdersPage({
  storeCode = "royal",
}: OrdersPageProps) {
  const [isDari, setIsDari] = useState(true);

  const [formData, setFormData] =
    useState<OrderFormData | null>(null);

  const [ordersData, setOrdersData] =
    useState<OrdersResponse | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const text = {
    title: isDari ? "سفارش‌ها" : "Orders",
    dashboard: isDari ? "داشبورد" : "Dashboard",
    newOrder: isDari
      ? "ثبت سفارش جدید"
      : "New Order",
    orderList: isDari
      ? "فهرست سفارش‌ها"
      : "Orders List",
    language: isDari ? "دری" : "EN",
    loading: isDari
      ? "در حال بارگذاری..."
      : "Loading...",
    loadError: isDari
      ? "اطلاعات سفارش‌ها بارگذاری نشد."
      : "Orders could not be loaded.",
    retry: isDari
      ? "تلاش دوباره"
      : "Try Again",
    saved: isDari
      ? "سفارش با موفقیت ثبت شد."
      : "Order saved successfully.",
  };

  async function loadPage() {
    try {
      setLoading(true);
      setError("");

      const [loadedFormData, loadedOrders] =
        await Promise.all([
          getOrderFormData(storeCode),
          getOrders(storeCode),
        ]);

      setFormData(loadedFormData);
      setOrdersData(loadedOrders);
    } catch (loadError) {
      setError(
        loadError instanceof Error
          ? loadError.message
          : text.loadError
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (storeCode === "all") {
      window.location.assign(
        "/orders?store=royal"
      );
      return;
    }

    loadPage();
  }, [storeCode]);

  async function handleSubmitOrder(
    request: CreateOrderRequest
  ) {
    await createOrder(request);
  }

  function handleStoreChange(nextStoreCode: string) {
    if (nextStoreCode === "all") {
      window.location.assign(
        "/dashboard?store=all"
      );
      return;
    }

    window.location.assign(
      `/orders?store=${nextStoreCode}`
    );
  }

  function handleLogout() {
    window.location.assign("/logout");
  }

  if (loading) {
    return (
      <div
        dir={isDari ? "rtl" : "ltr"}
        className="flex min-h-screen items-center justify-center bg-[#fbf8f1] text-xl text-[#173f38]"
      >
        {text.loading}
      </div>
    );
  }

  if (error || !formData || !ordersData) {
    return (
      <div
        dir={isDari ? "rtl" : "ltr"}
        className="flex min-h-screen flex-col items-center justify-center gap-5 bg-[#fbf8f1] px-6 text-center"
      >
        <p className="rounded-xl bg-[#fff0ed] px-6 py-4 font-semibold text-[#b42318]">
          {error || text.loadError}
        </p>

        <button
          type="button"
          onClick={loadPage}
          className="rounded-xl bg-[#173f38] px-6 py-3 font-bold text-white"
        >
          {text.retry}
        </button>
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
            title: "پرده سرای بزرگ رویال",
            storeName: isDari
              ? "انتخاب فروشگاه"
              : "Choose Store",
            dashboard: text.dashboard,
            managerWithdrawal: isDari
              ? "برداشت شخصی مدیر"
              : "Manager Withdrawal",
            orders: text.title,
            customers: isDari
              ? "مشتریان"
              : "Customers",
            products: isDari
              ? "محصولات"
              : "Products",
            directSales: isDari
              ? "فروش مستقیم"
              : "Direct Sale",
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
          selectedStoreCode={storeCode}
          storeOptions={storeOptions}
          isDari={isDari}
          onStoreChange={handleStoreChange}
          onManagerWithdrawal={() =>
            window.location.assign(
              `/expenses/new?store=${storeCode}&type=MANAGER_WITHDRAWAL`
            )
          }
          onDirectSale={() =>
            window.location.assign(
              `/dashboard?store=${storeCode}`
            )
          }
          onLogout={handleLogout}
        />

        <main className="min-w-0 flex-1">
          <header className="flex items-center justify-between border-b border-[#e9e1d4] bg-[#fffdf9] px-5 py-5 sm:px-8 lg:px-12">
            <div>
              <p className="mb-1 text-sm text-[#9a8063]">
                {text.title}
              </p>

              <h1 className="text-2xl font-bold text-[#173f38] sm:text-3xl">
                {ordersData.storeName}
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
          </header>

          <section className="space-y-8 px-5 py-7 sm:px-8 lg:px-12">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <h2 className="text-2xl font-bold text-[#173f38]">
                {text.orderList}
              </h2>

              <a
                href="#new-order"
                className="rounded-xl bg-[#c89232] px-5 py-3 font-bold text-white transition hover:bg-[#ae7721]"
              >
                + {text.newOrder}
              </a>
            </div>

            <OrdersTable
              orders={ordersData.orders}
              storeCode={storeCode}
              isDari={isDari}
            />

            <section id="new-order">
              <h2 className="mb-5 text-2xl font-bold text-[#173f38]">
                {text.newOrder}
              </h2>

              <OrderForm
                storeCode={storeCode}
                formData={formData}
                isDari={isDari}
                onSubmitOrder={handleSubmitOrder}
                onSaved={loadPage}
                onCancel={() =>
                  window.history.back()
                }
              />
            </section>
          </section>
        </main>
      </div>
    </div>
  );
}