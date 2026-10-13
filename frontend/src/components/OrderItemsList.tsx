import { useState } from "react";

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

export function createEmptyOrderItem(): OrderItemRequest {
  return {
    productType: "",
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
  const [searches, setSearches] = useState<
    Record<string, string>
  >({});

  const text = {
    item: isDari ? "آیتم سفارش" : "Order Item",
    addItem: isDari ? "افزودن آیتم" : "Add Item",
    remove: isDari ? "حذف" : "Remove",
    productType: isDari ? "نوع محصول" : "Product Type",
    room: isDari ? "نام اتاق" : "Room",
    width: isDari ? "عرض (سانتی‌متر)" : "Width (cm)",
    height: isDari ? "ارتفاع (سانتی‌متر)" : "Height (cm)",
    quantity: isDari ? "تعداد" : "Quantity",
    design: isDari ? "طرح" : "Design",
    tailor: isDari ? "خیاط" : "Tailor",
    noTailor: isDari ? "بدون خیاط" : "No Tailor",
    deadline: isDari
      ? "تاریخ تکمیل"
      : "Completion Date",
    notes: isDari ? "یادداشت" : "Notes",
    inventory: isDari
      ? "محصولات مصرفی از انبار"
      : "Inventory Products Used",
    search: isDari
      ? "جستجوی محصول از انبار"
      : "Search inventory product",
    meters: isDari
      ? "متر مصرفی"
      : "Estimated Meters",
    addProduct: isDari
      ? "افزودن محصول"
      : "Add Product",
    noResults: isDari
      ? "محصولی پیدا نشد"
      : "No products found",
  };

  function updateItem(
    itemIndex: number,
    changes: Partial<OrderItemRequest>
  ) {
    onChange(
      items.map((item, index) =>
        index === itemIndex
          ? { ...item, ...changes }
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
    onChange(
      items.filter((_, index) => index !== itemIndex)
    );
  }

  function addInventoryUsage(itemIndex: number) {
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

  function removeInventoryUsage(
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

  function updateUsage(
    itemIndex: number,
    usageIndex: number,
    productId: number,
    estimatedMeterage: number
  ) {
    const item = items[itemIndex];

    updateItem(itemIndex, {
      inventoryUsages:
        item.inventoryUsages.map((usage, index) =>
          index === usageIndex
            ? {
                productId,
                estimatedMeterage,
              }
            : usage
        ),
    });
  }

  return (
    <section className="space-y-6">
      {items.map((item, itemIndex) => (
        <article
          key={itemIndex}
          className="rounded-2xl border border-[#e4dacb] bg-[#fffdf9] p-5"
        >
          <div className="mb-5 flex items-center justify-between">
            <h3 className="text-lg font-bold text-[#173f38]">
              {text.item} {itemIndex + 1}
            </h3>

            <button
              type="button"
              onClick={() => removeItem(itemIndex)}
              className="rounded-lg px-3 py-2 text-sm font-bold text-red-600 hover:bg-red-50"
            >
              {text.remove}
            </button>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <label>
              <span className="mb-2 block font-semibold">
                {text.productType}
              </span>

              <input
                value={item.productType}
                onChange={(event) =>
                  updateItem(itemIndex, {
                    productType: event.target.value,
                  })
                }
                className="form-input"
                required
              />
            </label>

            <label>
              <span className="mb-2 block font-semibold">
                {text.room}
              </span>

              <input
                value={item.roomName || ""}
                onChange={(event) =>
                  updateItem(itemIndex, {
                    roomName: event.target.value,
                  })
                }
                className="form-input"
              />
            </label>

            <label>
              <span className="mb-2 block font-semibold">
                {text.width}
              </span>

              <input
                type="number"
                min="0"
                step="0.01"
                value={item.width ?? ""}
                onChange={(event) =>
                  updateItem(itemIndex, {
                    width: event.target.value
                      ? Number(event.target.value)
                      : undefined,
                  })
                }
                className="form-input"
              />
            </label>

            <label>
              <span className="mb-2 block font-semibold">
                {text.height}
              </span>

              <input
                type="number"
                min="0"
                step="0.01"
                value={item.height ?? ""}
                onChange={(event) =>
                  updateItem(itemIndex, {
                    height: event.target.value
                      ? Number(event.target.value)
                      : undefined,
                  })
                }
                className="form-input"
              />
            </label>

            <label>
              <span className="mb-2 block font-semibold">
                {text.quantity}
              </span>

              <input
                type="number"
                min="1"
                step="1"
                value={item.quantity}
                onChange={(event) =>
                  updateItem(itemIndex, {
                    quantity: Number(event.target.value),
                  })
                }
                className="form-input"
                required
              />
            </label>

            <label>
              <span className="mb-2 block font-semibold">
                {text.design}
              </span>

              <input
                value={item.design || ""}
                onChange={(event) =>
                  updateItem(itemIndex, {
                    design: event.target.value,
                  })
                }
                className="form-input"
              />
            </label>

            <label>
              <span className="mb-2 block font-semibold">
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

            <label>
              <span className="mb-2 block font-semibold">
                {text.deadline}
              </span>

              <input
                type="date"
                value={item.requiredCompletionDate || ""}
                onChange={(event) =>
                  updateItem(itemIndex, {
                    requiredCompletionDate:
                      event.target.value,
                  })
                }
                className="form-input"
              />
            </label>

            <label className="md:col-span-2">
              <span className="mb-2 block font-semibold">
                {text.notes}
              </span>

              <textarea
                value={item.specialNotes || ""}
                onChange={(event) =>
                  updateItem(itemIndex, {
                    specialNotes: event.target.value,
                  })
                }
                className="form-input min-h-24"
              />
            </label>
          </div>

          <div className="mt-6 border-t border-[#eadfce] pt-5">
            <div className="mb-4 flex items-center justify-between">
              <h4 className="font-bold text-[#173f38]">
                {text.inventory}
              </h4>

              <button
                type="button"
                onClick={() =>
                  addInventoryUsage(itemIndex)
                }
                className="rounded-lg bg-[#173f38] px-3 py-2 text-sm font-bold text-white"
              >
                {text.addProduct}
              </button>
            </div>

            <div className="space-y-3">
              {item.inventoryUsages.map(
                (usage, usageIndex) => {
                  const key =
                    `${itemIndex}-${usageIndex}`;

                  const search =
                    searches[key] || "";

                  const filteredProducts =
                    inventoryProducts.filter((product) => {
                      const value = search
                        .trim()
                        .toLowerCase();

                      if (!value) {
                        return true;
                      }

                      return [
                        product.productName,
                        product.productCode,
                        product.color || "",
                      ].some((field) =>
                        field.toLowerCase().includes(value)
                      );
                    });

                  const selectedProduct =
                    inventoryProducts.find(
                      (product) =>
                        product.id === usage.productId
                    );

                  return (
                    <div
                      key={key}
                      className="rounded-xl border border-[#e4dacb] bg-white p-4"
                    >
                      <div className="grid gap-3 md:grid-cols-[1fr_180px_auto]">
                        <div>
                          <input
                            value={
                              selectedProduct
                                ? `${selectedProduct.productName} — ${selectedProduct.productCode}`
                                : search
                            }
                            onChange={(event) => {
                              setSearches((current) => ({
                                ...current,
                                [key]: event.target.value,
                              }));

                              updateUsage(
                                itemIndex,
                                usageIndex,
                                0,
                                usage.estimatedMeterage
                              );
                            }}
                            placeholder={text.search}
                            className="form-input"
                          />

                          {!selectedProduct &&
                            filteredProducts.length > 0 && (
                              <div className="mt-2 max-h-48 overflow-y-auto rounded-xl border border-[#e4dacb] bg-white shadow">
                                {filteredProducts.map(
                                  (product) => (
                                    <button
                                      type="button"
                                      key={product.id}
                                      onClick={() => {
                                        updateUsage(
                                          itemIndex,
                                          usageIndex,
                                          product.id,
                                          usage.estimatedMeterage
                                        );

                                        setSearches(
                                          (current) => ({
                                            ...current,
                                            [key]: "",
                                          })
                                        );
                                      }}
                                      className="block w-full border-b border-[#f0e8dc] px-4 py-3 text-right hover:bg-[#fff8e8]"
                                    >
                                      <strong>
                                        {product.productName}
                                      </strong>
                                      <span className="mr-2 text-sm text-[#718077]">
                                        {product.productCode}
                                      </span>
                                    </button>
                                  )
                                )}
                              </div>
                            )}

                          {!selectedProduct &&
                            search &&
                            filteredProducts.length === 0 && (
                              <p className="mt-2 text-sm text-red-600">
                                {text.noResults}
                              </p>
                            )}

                          {selectedProduct && (
                            <p className="mt-2 text-sm text-[#718077]">
                              {selectedProduct.color || "—"} ·{" "}
                              {selectedProduct.availableMeterage} m
                            </p>
                          )}
                        </div>

                        <label>
                          <span className="mb-2 block text-sm font-semibold">
                            {text.meters}
                          </span>

                          <input
                            type="number"
                            min="0.01"
                            step="0.01"
                            value={
                              usage.estimatedMeterage || ""
                            }
                            onChange={(event) =>
                              updateUsage(
                                itemIndex,
                                usageIndex,
                                usage.productId,
                                Number(event.target.value)
                              )
                            }
                            className="form-input"
                            required
                          />
                        </label>

                        <button
                          type="button"
                          onClick={() =>
                            removeInventoryUsage(
                              itemIndex,
                              usageIndex
                            )
                          }
                          className="self-end rounded-lg px-3 py-3 font-bold text-red-600 hover:bg-red-50"
                        >
                          {text.remove}
                        </button>
                      </div>
                    </div>
                  );
                }
              )}
            </div>
          </div>
        </article>
      ))}

      <button
        type="button"
        onClick={addItem}
        className="w-full rounded-xl border-2 border-dashed border-[#c89232] px-5 py-4 font-bold text-[#9a6818] hover:bg-[#fff8e8]"
      >
        + {text.addItem}
      </button>
    </section>
  );
}