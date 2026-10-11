import { useEffect, useState } from "react";
import type { FormEvent, ReactNode } from "react";

import {
  createDirectSale,
  searchInventoryProducts,
} from "../services/api";

import type { InventoryProduct } from "../types/dashboard";

type DirectSaleLabels = {
  title: string;
  store: string;
  searchProduct: string;
  productCode: string;
  available: string;
  quantityMeters: string;
  pricePerMeter: string;
  totalPrice: string;
  notes: string;
  royalCurtains: string;
  kabulDubai: string;
  noProducts: string;
  changeProduct: string;
  cancel: string;
  saveSale: string;
  saving: string;
};

type DirectSaleModalProps = {
  isDari: boolean;
  labels: DirectSaleLabels;
  onClose: () => void;
  onSaved: () => void;
};

export default function DirectSaleModal({
  isDari,
  labels,
  onClose,
  onSaved,
}: DirectSaleModalProps) {
  const [storeCode, setStoreCode] = useState("royal");
  const [productSearch, setProductSearch] = useState("");

  const [products, setProducts] = useState<
    InventoryProduct[]
  >([]);

  const [selectedProduct, setSelectedProduct] =
    useState<InventoryProduct | null>(null);

  const [quantity, setQuantity] = useState("");
  const [unitPrice, setUnitPrice] = useState("");
  const [notes, setNotes] = useState("");

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const currency = isDari ? "افغانی" : "AFN";

  const totalPrice =
    Number(quantity || 0) * Number(unitPrice || 0);

  useEffect(() => {
    if (productSearch.trim().length === 0) {
      setProducts([]);
      return;
    }

    const timer = setTimeout(async () => {
      try {
        const result = await searchInventoryProducts(
          storeCode,
          productSearch.trim()
        );

        setProducts(result);
      } catch {
        setProducts([]);
      }
    }, 250);

    return () => clearTimeout(timer);
  }, [storeCode, productSearch]);

  const formatMoney = (amount: number) => {
    return `${amount.toLocaleString("en-US")} ${currency}`;
  };

  const resetProduct = () => {
    setSelectedProduct(null);
    setProductSearch("");
    setQuantity("");
    setUnitPrice("");
  };

  const submitSale = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();
    setError("");

    if (!selectedProduct) {
      setError(
        isDari
          ? "لطفاً یک محصول از انبار انتخاب کنید."
          : "Select an inventory product."
      );
      return;
    }

    if (Number(quantity) <= 0) {
      setError(
        isDari
          ? "مقدار باید بیشتر از صفر باشد."
          : "Amount must be greater than zero."
      );
      return;
    }

    if (
      Number(quantity) >
      selectedProduct.availableMeterage
    ) {
      setError(
        isDari
          ? "مقدار واردشده از موجودی انبار بیشتر است."
          : "The amount is greater than available inventory."
      );
      return;
    }

    if (Number(unitPrice) <= 0) {
      setError(
        isDari
          ? "قیمت هر متر باید بیشتر از صفر باشد."
          : "Price per meter must be greater than zero."
      );
      return;
    }

    setSaving(true);

    try {
      await createDirectSale({
        storeCode,
        productId: selectedProduct.id,
        quantity: Number(quantity),
        unitPrice: Number(unitPrice),
        notes: notes.trim(),
      });

      onSaved();
    } catch {
      setError(
        isDari
          ? "ثبت فروش انجام نشد. دوباره تلاش کنید."
          : "The sale could not be saved."
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-[#173f38]/50 px-4 py-8">
      <div className="w-full max-w-xl rounded-3xl bg-white p-6 shadow-2xl sm:p-8">
        <div className="mb-6 flex items-center justify-between">
          <h2 className="text-2xl font-bold text-[#173f38]">
            {labels.title}
          </h2>

          <button
            onClick={onClose}
            className="text-3xl text-[#766c60] hover:text-[#173f38]"
          >
            ×
          </button>
        </div>

        <form
          onSubmit={submitSale}
          className="space-y-5"
        >
          <FormField label={labels.store}>
            <select
              value={storeCode}
              onChange={(event) => {
                setStoreCode(event.target.value);
                resetProduct();
              }}
              className="form-input"
            >
              <option value="royal">
                {labels.royalCurtains}
              </option>

              <option value="kabul">
                {labels.kabulDubai}
              </option>
            </select>
          </FormField>

          <FormField label={labels.searchProduct}>
            <input
              value={productSearch}
              onChange={(event) => {
                setProductSearch(event.target.value);
                setSelectedProduct(null);
              }}
              className="form-input"
              type="search"
              placeholder={
                isDari
                  ? "نام، رنگ یا کد محصول را جستجو کنید"
                  : "Search by name, color, or code"
              }
            />
          </FormField>

          {!selectedProduct &&
            productSearch.trim().length > 0 && (
              <div className="max-h-44 space-y-2 overflow-y-auto rounded-xl border-2 border-[#c9bca9] bg-[#fffdf9] p-2">
                {products.length === 0 ? (
                  <p className="p-3 text-center text-sm text-[#766c60]">
                    {labels.noProducts}
                  </p>
                ) : (
                  products.map((product) => (
                    <button
                      type="button"
                      key={product.id}
                      onClick={() => {
                        setSelectedProduct(product);
                        setProductSearch(
                          `${product.productName} ${
                            product.color ?? ""
                          }`
                        );
                      }}
                      className="block w-full rounded-xl border-2 border-[#eee6db] bg-white p-2 text-right transition hover:border-[#d7ad62] hover:bg-[#fff8e9]"
                    >
                      <div className="font-bold text-[#173f38]">
                        {product.productName}

                        {product.color
                          ? ` — ${product.color}`
                          : ""}
                      </div>

                      <div className="mt-1 text-xs text-[#766c60]">
                        {labels.productCode}:{" "}
                        {product.productCode}
                      </div>

                      <div className="text-xs text-[#766c60]">
                        {labels.available}:{" "}
                        {product.availableMeterage} متر
                      </div>
                    </button>
                  ))
                )}
              </div>
            )}

          {selectedProduct && (
            <div className="rounded-xl border-2 border-[#d7ad62] bg-[#fff8e9] p-4">
              <div className="font-bold text-[#173f38]">
                {selectedProduct.productName}

                {selectedProduct.color
                  ? ` — ${selectedProduct.color}`
                  : ""}
              </div>

              <div className="mt-2 text-sm text-[#766c60]">
                {labels.productCode}:{" "}
                {selectedProduct.productCode}
              </div>

              <div className="text-sm text-[#766c60]">
                {labels.available}:{" "}
                {selectedProduct.availableMeterage} متر
              </div>

              <button
                type="button"
                onClick={resetProduct}
                className="mt-3 text-sm font-semibold text-[#a47739] underline"
              >
                {labels.changeProduct}
              </button>
            </div>
          )}

          <FormField label={labels.quantityMeters}>
            <input
              value={quantity}
              onChange={(event) =>
                setQuantity(event.target.value)
              }
              className="form-input"
              type="number"
              min="0.01"
              step="0.01"
              max={selectedProduct?.availableMeterage}
              required
            />
          </FormField>

          <FormField label={labels.pricePerMeter}>
            <input
              value={unitPrice}
              onChange={(event) =>
                setUnitPrice(event.target.value)
              }
              className="form-input"
              type="number"
              min="0.01"
              step="0.01"
              required
            />
          </FormField>

          <FormField label={labels.totalPrice}>
            <input
              value={formatMoney(totalPrice)}
              className="form-input bg-[#f7f1e7] font-bold"
              type="text"
              readOnly
            />
          </FormField>

          <FormField label={labels.notes}>
            <textarea
              value={notes}
              onChange={(event) =>
                setNotes(event.target.value)
              }
              className="form-input min-h-24 resize-y"
            />
          </FormField>

          {error && (
            <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">
              {error}
            </p>
          )}

          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 rounded-xl border-2 border-[#d8cbb9] px-4 py-3 text-[#6d6256] transition hover:bg-[#fbf8f1]"
            >
              {labels.cancel}
            </button>

            <button
              type="submit"
              disabled={saving || !selectedProduct}
              className="flex-1 rounded-xl bg-[#d7ad62] px-4 py-3 font-semibold text-white transition hover:bg-[#bd914a] disabled:cursor-not-allowed disabled:opacity-60"
            >
              {saving ? labels.saving : labels.saveSale}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function FormField({
  label,
  children,
}: {
  label: string;
  children: ReactNode;
}) {
  return (
    <label className="block">
      <span className="mb-2 block text-sm font-bold text-[#405a53]">
        {label}
      </span>

      {children}
    </label>
  );
}