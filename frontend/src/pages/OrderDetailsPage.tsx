import { useEffect, useState } from "react";

import Sidebar from "../components/Sidebar";
import {
  getOrder,
  getOrderFormData,
  updateOrder,
} from "../services/orderApi";

import type {
  OrderDetailsResponse,
  OrderFormData,
} from "../types/order";

type OrderDetailsPageProps = {
  orderId: number;
  storeCode: string;
  mode?: "view" | "edit";
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

function formatMoney(
  amount: number,
  isDari: boolean
): string {
  return `${amount.toLocaleString("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} ${isDari ? "افغانی" : "AFN"}`;
}

function formatDate(
  value: string | null,
  isDari: boolean
): string {
  if (!value) {
    return "—";
  }

  const date = new Date(`${value}T00:00:00`);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  if (isDari) {
    return new Intl.DateTimeFormat(
      "fa-AF-u-ca-persian-nu-latn",
      {
        day: "numeric",
        month: "long",
        year: "numeric",
      }
    ).format(date);
  }

  return new Intl.DateTimeFormat("en-US", {
    month: "long",
    day: "numeric",
    year: "numeric",
  }).format(date);
}

export default function OrderDetailsPage({
  orderId,
  storeCode,
  mode = "view",
}: OrderDetailsPageProps) {
  const [isDari, setIsDari] = useState(true);

  const [order, setOrder] =
    useState<OrderDetailsResponse | null>(null);

  const [formData, setFormData] =
    useState<OrderFormData | null>(null);

  const [completionDate, setCompletionDate] =
    useState("");

  const [tailorId, setTailorId] =
    useState("");

  const [details, setDetails] = useState("");

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const isEdit = mode === "edit";

  async function loadOrder() {
    try {
      setLoading(true);
      setError("");

      const [loadedOrder, loadedFormData] =
        await Promise.all([
          getOrder(orderId, storeCode),
          getOrderFormData(storeCode),
        ]);

      setOrder(loadedOrder);
      setFormData(loadedFormData);

      const firstItem = loadedOrder.items[0];

      setCompletionDate(
        loadedOrder.completionDate ||
          firstItem?.requiredCompletionDate ||
          ""
      );

      setTailorId(
        firstItem?.assignedTailor?.id
          ? String(firstItem.assignedTailor.id)
          : ""
      );

      setDetails(firstItem?.specialNotes || "");
    } catch (loadError) {
      setError(
        loadError instanceof Error
          ? loadError.message
          : isDari
            ? "اطلاعات سفارش بارگذاری نشد."
            : "Order details could not be loaded."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadOrder();
  }, [orderId, storeCode]);

  async function handleSave() {
    if (!completionDate) {
      setError(
        isDari
          ? "لطفاً تاریخ تکمیل را انتخاب کنید."
          : "Please select a completion date."
      );
      return;
    }

    try {
      setSaving(true);
      setError("");
      setSuccess("");

      await updateOrder(orderId, {
        storeCode,
        completionDate,
        tailorId: tailorId
          ? Number(tailorId)
          : undefined,
        details,
      });

      setSuccess(
        isDari
          ? "سفارش با موفقیت ذخیره شد."
          : "Order updated successfully."
      );

      await loadOrder();
    } catch (saveError) {
      setError(
        saveError instanceof Error
          ? saveError.message
          : isDari
            ? "سفارش ذخیره نشد."
            : "The order could not be saved."
      );
    } finally {
      setSaving(false);
    }
  }

  function goBack() {
    window.location.assign(
      `/orders?store=${encodeURIComponent(storeCode)}`
    );
  }

  function handleStoreChange(nextStore: string) {
    if (nextStore === "all") {
      window.location.assign("/dashboard?store=all");
      return;
    }

    window.location.assign(
      `/orders?store=${encodeURIComponent(nextStore)}`
    );
  }

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

  if (!order || !formData) {
    return (
      <div
        dir={isDari ? "rtl" : "ltr"}
        className="flex min-h-screen items-center justify-center bg-[#fbf8f1] px-6 text-center"
      >
        <div>
          <p className="mb-5 rounded-xl bg-[#fff0ed] px-6 py-4 font-semibold text-[#b42318]">
            {error ||
              (isDari
                ? "اطلاعات سفارش پیدا نشد."
                : "Order details were not found.")}
          </p>

          <button
            type="button"
            onClick={goBack}
            className="rounded-xl bg-[#173f38] px-6 py-3 font-bold text-white"
          >
            {isDari
              ? "بازگشت به سفارش‌ها"
              : "Back to Orders"}
          </button>
        </div>
      </div>
    );
  }

  const firstItem = order.items[0];

  return (
    <div
      dir={isDari ? "rtl" : "ltr"}
      className="min-h-screen bg-[#fbf8f1] text-[#173f38]"
    >
      <div className="flex min-h-screen">
        <Sidebar
          isDari={isDari}
          labels={{
            title: "پرده سرای بزرگ رویال",
            storeName: isDari
              ? "فروشگاه"
              : "Store",
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
          selectedStoreCode={storeCode}
          storeOptions={storeOptions}
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
          onLogout={() =>
            window.location.assign("/logout")
          }
        />

        <main className="min-w-0 flex-1">
          <header className="flex items-center justify-between border-b border-[#e9e1d4] bg-[#fffdf9] px-5 py-5 sm:px-8 lg:px-12">
            <div>
              <p className="mb-1 text-sm text-[#9a8063]">
                {isDari ? "سفارش‌ها" : "Orders"}
              </p>

              <h1 className="text-2xl font-bold text-[#173f38] sm:text-3xl">
                {isEdit
                  ? isDari
                    ? "ویرایش سفارش"
                    : "Edit Order"
                  : isDari
                    ? "جزئیات سفارش"
                    : "Order Details"}
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

          <section className="mx-auto max-w-5xl space-y-6 px-5 py-8 sm:px-8">
            <div className="rounded-2xl border border-[#e4dacb] bg-white p-6 shadow-sm">
              <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <p className="text-sm text-[#718077]">
                    {isDari
                      ? "شماره سفارش"
                      : "Order Number"}
                  </p>

                  <p className="mt-1 text-lg font-bold">
                    {order.orderNumber}
                  </p>
                </div>

                <div>
                  <p className="text-sm text-[#718077]">
                    {isDari ? "مشتری" : "Customer"}
                  </p>

                  <p className="mt-1 text-lg font-bold">
                    {order.customerName}
                  </p>
                </div>

                <div>
                  <p className="text-sm text-[#718077]">
                    {isDari
                      ? "مبلغ کل"
                      : "Total Amount"}
                  </p>

                  <p className="mt-1 text-lg font-bold">
                    {formatMoney(
                      order.totalAmount,
                      isDari
                    )}
                  </p>
                </div>

                <div>
                  <p className="text-sm text-[#718077]">
                    {isDari
                      ? "باقی‌مانده"
                      : "Remaining"}
                  </p>

                  <p className="mt-1 text-lg font-bold text-[#a56a00]">
                    {formatMoney(
                      order.remainingAmount,
                      isDari
                    )}
                  </p>
                </div>
              </div>
            </div>

            <div className="rounded-2xl border border-[#e4dacb] bg-white p-6 shadow-sm">
              <h2 className="mb-6 text-xl font-bold">
                {isDari
                  ? "اطلاعات انجام سفارش"
                  : "Order Work Information"}
              </h2>

              <div className="grid gap-5 md:grid-cols-2">
                <div>
                  <label
                    htmlFor="completion-date"
                    className="mb-2 block font-bold"
                  >
                    {isDari
                      ? "تاریخ تکمیل"
                      : "Completion Date"}
                  </label>

                  {isEdit ? (
                    <input
                      id="completion-date"
                      type="date"
                      value={completionDate}
                      onChange={(event) =>
                        setCompletionDate(
                          event.target.value
                        )
                      }
                      className="form-input"
                    />
                  ) : (
                    <p className="rounded-xl bg-[#f7f3eb] px-4 py-3 font-semibold">
                      {formatDate(
                        completionDate,
                        isDari
                      )}
                    </p>
                  )}
                </div>

                <div>
                  <label
                    htmlFor="tailor"
                    className="mb-2 block font-bold"
                  >
                    {isDari ? "خیاط" : "Tailor"}
                  </label>

                  {isEdit ? (
                    <select
                      id="tailor"
                      value={tailorId}
                      onChange={(event) =>
                        setTailorId(
                          event.target.value
                        )
                      }
                      className="form-input"
                    >
                      <option value="">
                        {isDari
                          ? "بدون خیاط"
                          : "No Tailor"}
                      </option>

                      {formData.tailors.map((tailor) => (
                        <option
                          key={tailor.id}
                          value={tailor.id}
                        >
                          {tailor.fullName}
                        </option>
                      ))}
                    </select>
                  ) : (
                    <p className="rounded-xl bg-[#f7f3eb] px-4 py-3 font-semibold">
                      {firstItem?.assignedTailor
                        ?.fullName ||
                        (isDari
                          ? "بدون خیاط"
                          : "No Tailor")}
                    </p>
                  )}
                </div>
              </div>

              <div className="mt-6">
                <label
                  htmlFor="order-details"
                  className="mb-2 block font-bold"
                >
                  {isDari
                    ? "اندازه‌ها و جزئیات سفارش"
                    : "Measurements and Order Details"}
                </label>

                {isEdit ? (
                  <textarea
                    id="order-details"
                    value={details}
                    onChange={(event) =>
                      setDetails(event.target.value)
                    }
                    className="form-input min-h-56"
                    placeholder={
                      isDari
                        ? "اندازه‌ها، طرح، نوع پرده، پارچه و توضیحات سفارش را وارد کنید..."
                        : "Enter measurements, design, fabric, and order details..."
                    }
                    maxLength={2000}
                  />
                ) : (
                  <div className="min-h-56 whitespace-pre-wrap rounded-xl bg-[#f7f3eb] px-4 py-4 leading-8 text-[#40564d]">
                    {details ||
                      (isDari
                        ? "جزئیاتی ثبت نشده است."
                        : "No details have been recorded.")}
                  </div>
                )}
              </div>

              {error && (
                <p className="mt-5 rounded-xl bg-[#fff0ed] px-4 py-3 font-semibold text-[#b42318]">
                  {error}
                </p>
              )}

              {success && (
                <p className="mt-5 rounded-xl bg-[#edf8f0] px-4 py-3 font-semibold text-[#276044]">
                  {success}
                </p>
              )}

              <div className="mt-7 flex flex-wrap gap-3">
                <button
                  type="button"
                  onClick={goBack}
                  className="rounded-xl border border-[#b8c2bc] bg-white px-6 py-3 font-bold text-[#4c6258]"
                >
                  {isDari ? "بازگشت" : "Back"}
                </button>

                {isEdit && (
                  <button
                    type="button"
                    onClick={handleSave}
                    disabled={saving}
                    className="rounded-xl bg-[#c89232] px-7 py-3 font-bold text-white disabled:opacity-60"
                  >
                    {saving
                      ? isDari
                        ? "در حال ذخیره..."
                        : "Saving..."
                      : isDari
                        ? "ذخیره سفارش"
                        : "Save Order"}
                  </button>
                )}
              </div>
            </div>
          </section>
        </main>
      </div>
    </div>
  );
}