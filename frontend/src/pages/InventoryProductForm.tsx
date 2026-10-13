import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import {
  createInventoryProduct,
  getInventoryMovements,
  getInventoryProduct,
  updateInventoryProduct,
} from "../services/inventoryApi";

import type {
  InventoryMovement,
  InventoryProductRequest,
} from "../types/inventory";

type InventoryProductFormProps = {
  storeCode?: string;
  productId?: number;
};

const stores = [
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

function normalizeMeterage(value: string): string {
  return value
    .replace(/[۰-۹]/g, (digit) =>
      String("۰۱۲۳۴۵۶۷۸۹".indexOf(digit))
    )
    .replace(/[٠-٩]/g, (digit) =>
      String("٠١٢٣٤٥٦٧٨٩".indexOf(digit))
    )
    .replace(/[٫،]/g, ".")
    .replace(/[^0-9.]/g, "");
}

function translateMovementReason(
  reason: string,
  isDari: boolean
): string {
  if (!isDari) {
    return reason;
  }

  return reason
    .replace(
      /Available meterage changed from (.*?) meters to (.*?) meters/gi,
      "متر موجودی از $1 متر به $2 متر تغییر کرد"
    )
    .replace(
      /Store changed from '(.*?)' to '(.*?)'/gi,
      "فروشگاه از $1 به $2 تغییر کرد"
    )
    .replace(
      /Product name changed from '(.*?)' to '(.*?)'/gi,
      "نام محصول از $1 به $2 تغییر کرد"
    )
    .replace(
      /Color changed from '(.*?)' to '(.*?)'/gi,
      "رنگ از $1 به $2 تغییر کرد"
    )
    .replace(
      /Location changed from '(.*?)' to '(.*?)'/gi,
      "محل نگهداری از $1 به $2 تغییر کرد"
    )
    .replace(
      /Product code changed from '(.*?)' to '(.*?)'/gi,
      "کد محصول از $1 به $2 تغییر کرد"
    )
    .replace(
      /Supplier purchase from (.*)/gi,
      "خرید از عمده‌فروش $1"
    )
    .replace(/; /g, "؛ ");
}

export default function InventoryProductForm({
  storeCode = "royal",
  productId,
}: InventoryProductFormProps) {
  const [isDari, setIsDari] = useState(true);

  const [selectedStoreCode, setSelectedStoreCode] = useState(
    storeCode === "all" ? "royal" : storeCode
  );

  const [productName, setProductName] = useState("");
  const [color, setColor] = useState("");
  const [meterage, setMeterage] = useState("");
  const [location, setLocation] = useState("");
  const [productCode, setProductCode] = useState("");

  const [movements, setMovements] = useState<
    InventoryMovement[]
  >([]);

  const [loading, setLoading] = useState(
    productId !== undefined
  );
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const isEdit = productId !== undefined;

  useEffect(() => {
    if (productId === undefined) {
      setLoading(false);
      return;
    }

    const currentProductId = productId;

    async function loadProduct() {
      try {
        setLoading(true);
        setError("");

        const [product, history] = await Promise.all([
          getInventoryProduct(currentProductId, storeCode),
          getInventoryMovements(currentProductId, storeCode),
        ]);

        setProductName(product.productName || "");
        setColor(product.color || "");

        setMeterage(
          normalizeMeterage(
            String(product.availableMeterage ?? 0)
          )
        );

        setLocation(product.location || "");
        setProductCode(product.productCode || "");

        setSelectedStoreCode(
          product.storeCode ||
            (storeCode === "all" ? "royal" : storeCode)
        );

        setMovements(history);
      } catch {
        setError(
          isDari
            ? "اطلاعات محصول بارگذاری نشد."
            : "Product information could not be loaded."
        );
      } finally {
        setLoading(false);
      }
    }

    loadProduct();
  }, [productId, storeCode, isDari]);

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    setMessage("");
    setError("");

    const normalizedMeterage = normalizeMeterage(meterage);
    const numericMeterage = Number(normalizedMeterage);

    if (!productName.trim()) {
      setError(
        isDari
          ? "نام محصول الزامی است."
          : "Product name is required."
      );
      return;
    }

    if (!productCode.trim()) {
      setError(
        isDari
          ? "کد محصول الزامی است."
          : "Product code is required."
      );
      return;
    }

    if (
      !normalizedMeterage ||
      !Number.isFinite(numericMeterage) ||
      numericMeterage < 0
    ) {
      setError(
        isDari
          ? "متر موجودی معتبر وارد کنید."
          : "Enter a valid meterage."
      );
      return;
    }

    if (
      !selectedStoreCode ||
      selectedStoreCode === "all"
    ) {
      setError(
        isDari
          ? "لطفاً یک فروشگاه مشخص انتخاب کنید."
          : "Please choose a specific store."
      );
      return;
    }

    const request: InventoryProductRequest = {
      storeCode: selectedStoreCode,
      productName: productName.trim(),
      color: color.trim(),
      meterage: numericMeterage,
      location: location.trim(),
      productCode: productCode.trim(),
    };

    try {
      setSaving(true);

      if (isEdit && productId !== undefined) {
        await updateInventoryProduct(
          productId,
          storeCode,
          request
        );

        setMessage(
          isDari
            ? "محصول با موفقیت ذخیره شد."
            : "Product saved successfully."
        );

        const history = await getInventoryMovements(
          productId,
          selectedStoreCode
        );

        setMovements(history);
      } else {
        await createInventoryProduct(request);

        setMessage(
          isDari
            ? "محصول با موفقیت اضافه شد."
            : "Product added successfully."
        );

        setProductName("");
        setColor("");
        setMeterage("");
        setLocation("");
        setProductCode("");
      }
    } catch (submitError) {
      setError(
        submitError instanceof Error
          ? submitError.message
          : isDari
            ? "محصول ذخیره نشد."
            : "The product could not be saved."
      );
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[#fbf8f1] text-lg text-[#173f38]">
        {isDari ? "در حال بارگذاری..." : "Loading..."}
      </div>
    );
  }

  const currentStore = stores.find(
    (store) => store.code === selectedStoreCode
  );

  return (
    <div
      dir={isDari ? "rtl" : "ltr"}
      className="min-h-screen bg-[#fbf8f1] px-5 py-8 text-[#173f38] sm:px-8"
    >
      <main className="mx-auto max-w-4xl">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-3xl font-bold">
            {isEdit
              ? isDari
                ? "ویرایش محصول"
                : "Edit Product"
              : isDari
                ? "محصول جدید"
                : "New Product"}
          </h1>

          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setIsDari(true)}
              className={`rounded-lg px-3 py-2 text-sm font-bold ${
                isDari
                  ? "bg-[#d7ad62] text-white"
                  : "bg-white text-[#52675d]"
              }`}
            >
              دری
            </button>

            <button
              type="button"
              onClick={() => setIsDari(false)}
              className={`rounded-lg px-3 py-2 text-sm font-bold ${
                !isDari
                  ? "bg-[#d7ad62] text-white"
                  : "bg-white text-[#52675d]"
              }`}
            >
              EN
            </button>
          </div>
        </div>

        <form
          onSubmit={handleSubmit}
          className="rounded-3xl border border-[#e4dacb] bg-white p-6 shadow-sm sm:p-8"
        >
          <div className="mb-7 rounded-2xl bg-[#f4faf4] p-5">
            <label
              htmlFor="product-store"
              className="mb-2 block text-sm text-[#718077]"
            >
              {isDari ? "فروشگاه" : "Store"}
            </label>

            <select
              id="product-store"
              value={selectedStoreCode}
              onChange={(event) =>
                setSelectedStoreCode(event.target.value)
              }
              className="w-full rounded-xl border border-[#cfc4b5] bg-white px-4 py-3 font-bold text-[#173f38] outline-none focus:border-[#c89232]"
            >
              {stores.map((store) => (
                <option
                  key={store.code}
                  value={store.code}
                >
                  {isDari ? store.dari : store.english}
                </option>
              ))}
            </select>

            <p className="mt-2 text-sm text-[#718077]">
              {currentStore
                ? isDari
                  ? currentStore.dari
                  : currentStore.english
                : ""}
            </p>
          </div>

          <div className="grid gap-5 md:grid-cols-2">
            <label className="block">
              <span className="mb-2 block font-bold">
                {isDari ? "نام محصول" : "Product Name"} *
              </span>

              <input
                value={productName}
                onChange={(event) =>
                  setProductName(event.target.value)
                }
                className="w-full rounded-xl border border-[#cfc4b5] px-4 py-3 outline-none focus:border-[#c89232]"
                required
              />
            </label>

            <label className="block">
              <span className="mb-2 block font-bold">
                {isDari ? "رنگ" : "Color"}
              </span>

              <input
                value={color}
                onChange={(event) =>
                  setColor(event.target.value)
                }
                className="w-full rounded-xl border border-[#cfc4b5] px-4 py-3 outline-none focus:border-[#c89232]"
              />
            </label>

            <label className="block">
              <span className="mb-2 block font-bold">
                {isDari
                  ? "متر موجود فعلی"
                  : "Current Available Meterage"}{" "}
                *
              </span>

              <input
                type="text"
                inputMode="decimal"
                value={meterage}
                onChange={(event) =>
                  setMeterage(
                    normalizeMeterage(event.target.value)
                  )
                }
                className="w-full rounded-xl border border-[#cfc4b5] px-4 py-3 outline-none focus:border-[#c89232]"
                required
              />

              <span className="mt-2 block text-sm text-[#7d887f]">
                {isDari
                  ? "برای اصلاح اشتباهات، مقدار موجودی را تغییر دهید."
                  : "Change this value to correct a meterage mistake."}
              </span>
            </label>

            <label className="block">
              <span className="mb-2 block font-bold">
                {isDari ? "محل نگهداری" : "Storage Location"}
              </span>

              <input
                value={location}
                onChange={(event) =>
                  setLocation(event.target.value)
                }
                className="w-full rounded-xl border border-[#cfc4b5] px-4 py-3 outline-none focus:border-[#c89232]"
              />
            </label>

            <label className="block md:col-span-2">
              <span className="mb-2 block font-bold">
                {isDari ? "کد محصول" : "Product Code"} *
              </span>

              <input
                value={productCode}
                onChange={(event) =>
                  setProductCode(event.target.value)
                }
                className="w-full rounded-xl border border-[#cfc4b5] px-4 py-3 outline-none focus:border-[#c89232]"
                required
              />
            </label>
          </div>

          {error && (
            <div className="mt-6 rounded-xl bg-[#fff0ed] px-4 py-3 font-semibold text-[#b42318]">
              {error}
            </div>
          )}

          {message && (
            <div className="mt-6 rounded-xl bg-[#edf8f0] px-4 py-3 font-semibold text-[#276044]">
              {message}
            </div>
          )}

          <div className="mt-8 flex gap-3">
            <a
              href={`/inventory?store=${storeCode}`}
              className="rounded-xl border border-[#cfc4b5] px-6 py-3 font-bold text-[#52675d]"
            >
              {isDari
                ? "بازگشت به محصولات"
                : "Back to Products"}
            </a>

            <button
              type="submit"
              disabled={saving}
              className="rounded-xl bg-[#c89232] px-7 py-3 font-bold text-white disabled:opacity-60"
            >
              {saving
                ? isDari
                  ? "در حال ذخیره..."
                  : "Saving..."
                : isDari
                  ? "ذخیره محصول"
                  : "Save Product"}
            </button>
          </div>
        </form>

        {isEdit && (
          <section className="mt-6 rounded-3xl border border-[#e4dacb] bg-white p-6 shadow-sm">
            <h2 className="mb-5 text-xl font-bold">
              {isDari ? "تاریخچه تغییرات" : "Change History"}
            </h2>

            {movements.length === 0 ? (
              <p className="text-[#718077]">
                {isDari
                  ? "هنوز تغییری ثبت نشده است."
                  : "No changes have been recorded yet."}
              </p>
            ) : (
              <div className="space-y-4">
                {movements.map((movement) => (
                  <div
                    key={movement.id}
                    className="rounded-2xl bg-[#f8f5ef] p-4"
                  >
                    <p className="font-semibold">
                      {translateMovementReason(
                        movement.reason,
                        isDari
                      )}
                    </p>

                    <p className="mt-2 text-sm text-[#718077]">
                      {isDari ? "تغییر توسط" : "Changed by"}{" "}
                      <strong>{movement.recordedBy}</strong>
                      {" — "}
                      {new Date(
                        movement.createdAt
                      ).toLocaleString("en-US")}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
}