import { useEffect, useMemo, useState } from "react";

import Sidebar from "../components/Sidebar";
import InventoryTable from "../components/InventoryTable";
import { getInventoryProducts } from "../services/inventoryApi";

import type { InventoryProduct } from "../types/inventory";

type InventoryPageProps = {
  storeCode?: string;
};

type FilterType = "ALL" | "LOW" | "OUT";

const stores = [
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

function isOut(product: InventoryProduct) {
  return product.availableMeterage <= 0;
}

function isLow(product: InventoryProduct) {
  return (
    product.availableMeterage > 0 &&
    product.availableMeterage <= 10
  );
}

function SummaryIcon({
  type,
}: {
  type: "products" | "available" | "low" | "out";
}) {
  if (type === "low") {
    return (
      <svg
        width="42"
        height="42"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <path d="M10.3 3.5 2.9 17a2 2 0 0 0 1.8 3h14.6a2 2 0 0 0 1.8-3L13.7 3.5a2 2 0 0 0-3.4 0Z" />
        <path d="M12 8v4" />
        <path d="M12 16h.01" />
      </svg>
    );
  }

  if (type === "available") {
    return (
      <svg
        width="42"
        height="42"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <path d="m12 3 8 4-8 4-8-4 8-4Z" />
        <path d="m4 12 8 4 8-4" />
        <path d="m4 17 8 4 8-4" />
      </svg>
    );
  }

  return (
    <svg
      width="42"
      height="42"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="m12 3 8 4v10l-8 4-8-4V7l8-4Z" />
      <path d="m4 7 8 4 8-4" />
      <path d="M12 11v10" />
    </svg>
  );
}

function SummaryCard({
  type,
  title,
  value,
  unit,
  active,
  onClick,
}: {
  type: "products" | "available" | "low" | "out";
  title: string;
  value: string | number;
  unit?: string;
  active?: boolean;
  onClick?: () => void;
}) {
  const styles = {
    products: {
      card: "border-[#d9ecfb] bg-[#eef8ff]",
      icon: "bg-[#d9efff] text-[#0759b5]",
      value: "text-[#0759b5]",
    },
    available: {
      card: "border-[#d9f0df] bg-[#f0fbf3]",
      icon: "bg-[#d9f4df] text-[#08753a]",
      value: "text-[#08753a]",
    },
    low: {
      card: "border-[#f5e7c7] bg-[#fffaf0]",
      icon: "bg-[#ffeac0] text-[#a76600]",
      value: "text-[#a76600]",
    },
    out: {
      card: "border-[#f5d8d8] bg-[#fff3f3]",
      icon: "bg-[#ffdfe0] text-[#c52424]",
      value: "text-[#c52424]",
    },
  };

  const style = styles[type];

  return (
    <button
      type="button"
      onClick={onClick}
      className={`flex min-h-[150px] items-center gap-5 rounded-2xl border p-6 text-right transition hover:shadow-md ${
        style.card
      } ${
        active
          ? "ring-2 ring-[#c89232]/50"
          : ""
      }`}
    >
      <span
        className={`flex h-20 w-20 shrink-0 items-center justify-center rounded-full ${style.icon}`}
      >
        <SummaryIcon type={type} />
      </span>

      <span className="flex flex-1 flex-col">
        <span className="text-lg font-bold text-[#173f38]">
          {title}
        </span>

        <span
          className={`mt-2 text-4xl font-bold ${style.value}`}
        >
          {value}
        </span>

        {unit && (
          <span className="mt-1 text-base font-semibold text-[#52675d]">
            {unit}
          </span>
        )}
      </span>
    </button>
  );
}

export default function InventoryPage({
  storeCode = "all",
}: InventoryPageProps) {
  const [isDari, setIsDari] = useState(true);
  const [selectedStore, setSelectedStore] = useState(storeCode);
  const [products, setProducts] = useState<InventoryProduct[]>([]);
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState<FilterType>("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    setSelectedStore(storeCode);
  }, [storeCode]);

  useEffect(() => {
    async function loadProducts() {
      try {
        setLoading(true);
        setError("");

        const data = await getInventoryProducts(
          selectedStore
        );

        setProducts(data);
      } catch {
        setError(
          isDari
            ? "اطلاعات محصولات بارگذاری نشد."
            : "Inventory products could not be loaded."
        );
      } finally {
        setLoading(false);
      }
    }

    loadProducts();
  }, [selectedStore, isDari]);

  const filteredProducts = useMemo(() => {
    const searchText = search.trim().toLowerCase();

    return products.filter((product) => {
      const matchesSearch =
        !searchText ||
        product.productName
          .toLowerCase()
          .includes(searchText) ||
        product.color
          .toLowerCase()
          .includes(searchText) ||
        product.productCode
          .toLowerCase()
          .includes(searchText) ||
        product.location
          .toLowerCase()
          .includes(searchText);

      const matchesFilter =
        filter === "ALL" ||
        (filter === "LOW" && isLow(product)) ||
        (filter === "OUT" && isOut(product));

      return matchesSearch && matchesFilter;
    });
  }, [products, search, filter]);

  const outCount = products.filter(isOut).length;
  const lowCount = products.filter(isLow).length;

  const availableMeters = products.reduce(
    (total, product) =>
      total + product.availableMeterage,
    0
  );

  function changeStore(nextStore: string) {
    setSelectedStore(nextStore);
    setSearch("");
    setFilter("ALL");

    window.history.replaceState(
      {},
      "",
      `/inventory?store=${nextStore}`
    );
  }

  return (
    <div
      dir={isDari ? "rtl" : "ltr"}
      className="min-h-screen bg-[#f7f7f5] text-[#173f38]"
    >
      <div className="flex min-h-screen">
        <Sidebar
          isDari={isDari}
          labels={{
            title: "پرده سرای بزرگ رویال",
            storeName: isDari ? "فروشگاه" : "Store",
            dashboard: isDari ? "داشبورد" : "Dashboard",
            managerWithdrawal: isDari
              ? "برداشت شخصی مدیر"
              : "Manager Withdrawal",
            orders: isDari ? "سفارش‌ها" : "Orders",
            customers: isDari ? "مشتریان" : "Customers",
            products: isDari ? "محصولات" : "Products",
            directSales: isDari
              ? "فروش مستقیم"
              : "Direct Sales",
            wholesaleAccounts: isDari
              ? "عمده‌فروشان"
              : "Wholesale Accounts",
            expenses: isDari ? "مصارف" : "Expenses",
            users: isDari ? "کاربران" : "Users",
            logout: isDari ? "خروج" : "Logout",
          }}
          selectedStoreCode={selectedStore}
          storeOptions={stores.map((store) => ({
            code: store.code,
            name: isDari
              ? store.dari
              : store.english,
          }))}
          onStoreChange={changeStore}
          onManagerWithdrawal={() => {
            if (selectedStore !== "all") {
              window.location.assign(
                `/expenses/new?store=${selectedStore}&type=MANAGER_WITHDRAWAL`
              );
            }
          }}
          onDirectSale={() => {
            if (selectedStore !== "all") {
              window.location.assign(
                `/dashboard?store=${selectedStore}`
              );
            }
          }}
          onLogout={() => {
            window.location.assign("/logout");
          }}
        />

        <main className="min-w-0 flex-1 bg-white">
          <header className="border-b border-[#e8e8e8] bg-white px-8 py-7">
            <div className="flex items-center justify-between">
              <h1 className="text-4xl font-bold text-[#111]">
                {isDari ? "محصولات" : "Products"}
              </h1>

              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setIsDari(false)}
                  className="rounded-xl bg-[#eeeae1] px-5 py-3 font-bold text-[#45645c]"
                >
                  EN
                </button>

                <button
                  type="button"
                  onClick={() => setIsDari(true)}
                  className={`rounded-xl px-5 py-3 font-bold ${
                    isDari
                      ? "bg-[#d9ab58] text-white"
                      : "bg-[#eeeae1] text-[#45645c]"
                  }`}
                >
                  دری
                </button>
              </div>
            </div>
          </header>

          <section className="p-8">
            <div className="mb-6 flex items-center gap-5">
              <select
                value={selectedStore}
                onChange={(event) =>
                  changeStore(event.target.value)
                }
                className="w-[270px] rounded-xl border border-[#d8d0c5] bg-white px-5 py-4 text-[#222] outline-none"
              >
                {stores.map((store) => (
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

              <input
                type="search"
                value={search}
                onChange={(event) =>
                  setSearch(event.target.value)
                }
                placeholder={
                  isDari
                    ? "جستجوی محصول، کد یا رنگ..."
                    : "Search product, code, or color..."
                }
                className="min-w-0 flex-1 rounded-xl border border-[#d8d0c5] px-5 py-4 outline-none focus:border-[#d09b32]"
              />

              <a
                href={`/inventory/new?store=${selectedStore}`}
                className="w-[250px] rounded-xl bg-[#d09b32] px-6 py-4 text-center font-bold text-white transition hover:bg-[#b78224]"
              >
                {isDari
                  ? "محصول جدید  +"
                  : "New Product  +"}
              </a>
            </div>

            <div className="mb-6 grid grid-cols-4 gap-5">
              <SummaryCard
                type="out"
                title={
                  isDari
                    ? "محصولات تمام‌شده"
                    : "Out of Stock"
                }
                value={outCount}
                active={filter === "OUT"}
                onClick={() => setFilter("OUT")}
              />

              <SummaryCard
                type="low"
                title={
                  isDari
                    ? "موجودی کم"
                    : "Low Stock"
                }
                value={lowCount}
                active={filter === "LOW"}
                onClick={() => setFilter("LOW")}
              />

              <SummaryCard
                type="available"
                title={isDari ? "موجود" : "Available"}
                value={availableMeters.toLocaleString(
                  "en-US",
                  {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  }
                )}
                unit={isDari ? "متر" : "meters"}
                active={filter === "ALL"}
                onClick={() => setFilter("ALL")}
              />

              <SummaryCard
                type="products"
                title={
                  isDari
                    ? "تعداد محصولات"
                    : "Number of Products"
                }
                value={products.length}
                onClick={() => setFilter("ALL")}
              />
            </div>

            {loading ? (
              <div className="p-12 text-center">
                {isDari
                  ? "در حال بارگذاری..."
                  : "Loading..."}
              </div>
            ) : error ? (
              <div className="p-12 text-center text-red-600">
                {error}
              </div>
            ) : (
              <InventoryTable
                products={filteredProducts}
                storeCode={selectedStore}
                isDari={isDari}
              />
            )}
          </section>
        </main>
      </div>
    </div>
  );
}