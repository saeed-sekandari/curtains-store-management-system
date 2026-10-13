import { useRef } from "react";

import type {
  InventoryProductOption,
  OrderItemRequest,
  TailorOption,
} from "../types/order";

type OrderItemsListProps = {
  items: OrderItemRequest[];
  inventoryProducts: InventoryProductOption[];
  tailors: TailorOption[];
  isDari: boolean;
  onChange: (items: OrderItemRequest[]) => void;
};

function toEnglishNumbers(value: string): string {
  return value
    .replace(/[۰-۹]/g, (digit) =>
      String("۰۱۲۳۴۵۶۷۸۹".indexOf(digit))
    )
    .replace(/[٠-٩]/g, (digit) =>
      String("٠١٢٣٤٥٦٧٨٩".indexOf(digit))
    );
}

function normalizeMeterInput(value: string): string {
  return toEnglishNumbers(value)
    .replace(/٫/g, ".")
    .replace(/،/g, ".");
}

function formatDate(value: string | undefined): string {
  if (!value) {
    return "Select a date";
  }

  const date = new Date(`${value}T00:00:00`);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("en-US", {
    month: "long",
    day: "numeric",
    year: "numeric",
  }).format(date);
}

type DatePickerFieldProps = {
  value: string | undefined;
  onChange: (value: string) => void;
};

function DatePickerField({
  value,
  onChange,
}: DatePickerFieldProps) {
  const inputRef = useRef<HTMLInputElement>(null);

  function openDatePicker() {
    const input = inputRef.current;

    if (!input) {
      return;
    }

    const picker = input as HTMLInputElement & {
      showPicker?: () => void;
    };

    if (picker.showPicker) {
      picker.showPicker();
    } else {
      input.click();
    }
  }

  return (
    <div className="relative">
      <button
        type="button"
        onClick={openDatePicker}
        className="form-input w-full text-right"
      >
        {formatDate(value)}
      </button>

      <input
        ref={inputRef}
        type="date"
        value={value || ""}
        onChange={(event) =>
          onChange(event.target.value)
        }
        className="pointer-events-none absolute h-0 w-0 opacity-0"
        tabIndex={-1}
        aria-hidden="true"
      />
    </div>
  );
}

export function createEmptyOrderItem(): OrderItemRequest {
  return {
    productType: "CURTAIN",
    roomName: "",
    width: undefined,
    height: undefined,
    quantity: 1,
    design: "",
    specialNotes: "",
    tailorId: undefined,
    requiredCompletionDate: "",
    inventoryUsages: [],
    fabrics: [],
  };
}

export default function OrderItemsList({
  items,
  inventoryProducts,
  tailors,
  isDari,
  onChange,
}: OrderItemsListProps) {
  const text = {
    item: isDari ? "آیتم سفارش" : "Order Item",
    remove: isDari ? "حذف" : "Remove",
    tailor: isDari ? "خیاط" : "Tailor",
    noTailor: isDari ? "بدون خیاط" : "No Tailor",
    completionDate: isDari
      ? "تاریخ تکمیل"
      : "Completion Date",
    details: isDari
      ? "اندازه‌ها و جزئیات سفارش"
      : "Measurements and Order Details",
    detailsPlaceholder: isDari
      ? "اندازه‌ها، نام اتاق، نوع پرده، طرح، پارچه، تعداد و تمام جزئیات سفارش را اینجا وارد کنید..."
      : "Enter measurements, room name, curtain type, design, fabric, quantity, and all order details here...",
    inventoryTitle: isDari
      ? "محصولات مصرفی از انبار"
      : "Products Used from Inventory",
    addProduct: isDari
      ? "افزودن محصول"
      : "Add Product",
    selectProduct: isDari
      ? "انتخاب محصول از انبار"
      : "Select Inventory Product",
    meters: isDari
      ? "متر مصرفی"
      : "Used Meters",
    available: isDari
      ? "موجودی"
      : "Available",
    addItem: isDari
      ? "افزودن آیتم"
      : "Add Item",
  };

  function updateItem(
    itemIndex: number,
    changes: Partial<OrderItemRequest>
  ) {
    onChange(
      items.map((item, index) =>
        index === itemIndex
          ? {
              ...item,
              ...changes,
              productType: "CURTAIN",
              quantity: 1,
            }
          : item
      )
    );
  }

  function addItem() {
    onChange([
      ...items,
      createEmptyOrderItem(),
    ]);
  }

  function removeItem(itemIndex: number) {
    if (items.length === 1) {
      return;
    }

    onChange(
      items.filter((_, index) => index !== itemIndex)
    );
  }

  function addInventoryProduct(itemIndex: number) {
    const item = items[itemIndex];

    updateItem(itemIndex, {
      inventoryUsages: [
        ...item.inventoryUsages,
        {
          productId: 0,
          estimatedMeterage: 0,
        },
      ],
    });
  }

  function removeInventoryProduct(
    itemIndex: number,
    usageIndex: number
  ) {
    const item = items[itemIndex];

    updateItem(itemIndex, {
      inventoryUsages:
        item.inventoryUsages.filter(
          (_, index) => index !== usageIndex
        ),
    });
  }

  function updateInventoryProduct(
    itemIndex: number,
    usageIndex: number,
    changes: {
      productId?: number;
      estimatedMeterage?: number;
    }
  ) {
    const item = items[itemIndex];

    updateItem(itemIndex, {
      inventoryUsages:
        item.inventoryUsages.map(
          (usage, index) =>
            index === usageIndex
              ? {
                  ...usage,
                  ...changes,
                }
              : usage
        ),
    });
  }

  return (
    <section
      dir={isDari ? "rtl" : "ltr"}
      className="space-y-6"
    >
      {items.map((item, itemIndex) => (
        <article
          key={itemIndex}
          className="rounded-2xl border border-[#e4dacb] bg-[#fffdf9] p-5 sm:p-6"
        >
          <div className="mb-6 flex items-center justify-between">
            <h3 className="text-xl font-bold text-[#173f38]">
              {text.item} {itemIndex + 1}
            </h3>

            {items.length > 1 && (
              <button
                type="button"
                onClick={() =>
                  removeItem(itemIndex)
                }
                className="rounded-lg px-3 py-2 font-bold text-red-600 hover:bg-red-50"
              >
                {text.remove}
              </button>
            )}
          </div>

          <div className="grid gap-5 md:grid-cols-2">
            <label>
              <span className="mb-2 block font-bold text-[#173f38]">
                {text.completionDate}
              </span>

              <DatePickerField
                value={item.requiredCompletionDate}
                onChange={(value) =>
                  updateItem(itemIndex, {
                    requiredCompletionDate: value,
                  })
                }
              />
            </label>

            <label>
              <span className="mb-2 block font-bold text-[#173f38]">
                {text.tailor}
              </span>

              <select
                value={item.tailorId ?? ""}
                onChange={(event) =>
                  updateItem(itemIndex, {
                    tailorId: event.target.value
                      ? Number(event.target.value)
                      : undefined,
                  })
                }
                className="form-input"
              >
                <option value="">
                  {text.noTailor}
                </option>

                {tailors.map((tailor) => (
                  <option
                    key={tailor.id}
                    value={tailor.id}
                  >
                    {tailor.fullName}
                  </option>
                ))}
              </select>
            </label>

            <label className="md:col-span-2">
              <span className="mb-2 block font-bold text-[#173f38]">
                {text.details}
              </span>

              <textarea
                value={item.specialNotes || ""}
                onChange={(event) =>
                  updateItem(itemIndex, {
                    specialNotes:
                      event.target.value,
                  })
                }
                className="form-input min-h-[260px] resize-y"
                placeholder={text.detailsPlaceholder}
                maxLength={2000}
              />

              <div className="mt-2 text-left text-xs text-[#89948c]">
                {(item.specialNotes || "").length}/2000
              </div>
            </label>
          </div>

          <section className="mt-7 rounded-2xl border border-[#e4dacb] bg-white p-5">
            <div className="mb-5 flex items-center justify-between gap-4">
              <h4 className="text-lg font-bold text-[#173f38]">
                {text.inventoryTitle}
              </h4>

              <button
                type="button"
                onClick={() =>
                  addInventoryProduct(itemIndex)
                }
                className="rounded-xl bg-[#173f38] px-4 py-3 font-bold text-white transition hover:bg-[#0d2d29]"
              >
                + {text.addProduct}
              </button>
            </div>

            <div className="space-y-4">
              {item.inventoryUsages.map(
                (usage, usageIndex) => {
                  const selectedProduct =
                    inventoryProducts.find(
                      (product) =>
                        product.id === usage.productId
                    );

                  return (
                    <div
                      key={`${itemIndex}-${usageIndex}`}
                      className="rounded-xl border border-[#eadfce] bg-[#fffdf9] p-4"
                    >
                      <div className="grid gap-4 md:grid-cols-[1fr_190px_auto]">
                        <label>
                          <span className="mb-2 block text-sm font-bold text-[#52675d]">
                            {text.selectProduct}
                          </span>

                          <select
                            value={
                              usage.productId || ""
                            }
                            onChange={(event) =>
                              updateInventoryProduct(
                                itemIndex,
                                usageIndex,
                                {
                                  productId: Number(
                                    event.target.value
                                  ),
                                }
                              )
                            }
                            className="form-input"
                            required
                          >
                            <option value="">
                              {text.selectProduct}
                            </option>

                            {inventoryProducts.map(
                              (product) => (
                                <option
                                  key={product.id}
                                  value={product.id}
                                >
                                  {product.productName} —{" "}
                                  {product.productCode}
                                </option>
                              )
                            )}
                          </select>

                          {selectedProduct && (
                            <p className="mt-2 text-sm text-[#718077]">
                              {selectedProduct.color || "—"} ·{" "}
                              {text.available}:{" "}
                              {selectedProduct.availableMeterage.toLocaleString(
                                "en-US"
                              )}{" "}
                              {isDari ? "متر" : "m"}
                            </p>
                          )}
                        </label>

                        <label>
                          <span className="mb-2 block text-sm font-bold text-[#52675d]">
                            {text.meters}
                          </span>

                          <input
                            type="text"
                            inputMode="decimal"
                            value={
                              usage.estimatedMeterage || ""
                            }
                            onChange={(event) => {
                              const normalized =
                                normalizeMeterInput(
                                  event.target.value
                                );

                              if (
                                !/^\d*\.?\d*$/.test(
                                  normalized
                                )
                              ) {
                                return;
                              }

                              updateInventoryProduct(
                                itemIndex,
                                usageIndex,
                                {
                                  estimatedMeterage:
                                    normalized === ""
                                      ? 0
                                      : Number(normalized),
                                }
                              );
                            }}
                            className="form-input"
                            placeholder="0"
                            required
                          />
                        </label>

                        <button
                          type="button"
                          onClick={() =>
                            removeInventoryProduct(
                              itemIndex,
                              usageIndex
                            )
                          }
                          className="self-end rounded-xl px-4 py-3 font-bold text-red-600 hover:bg-red-50"
                        >
                          {text.remove}
                        </button>
                      </div>
                    </div>
                  );
                }
              )}
            </div>
          </section>
        </article>
      ))}

      <button
        type="button"
        onClick={addItem}
        className="w-full rounded-2xl border-2 border-dashed border-[#c89232] px-5 py-6 text-lg font-bold text-[#9a6818] transition hover:bg-[#fff8e8]"
      >
        + {text.addItem}
      </button>
    </section>
  );
}