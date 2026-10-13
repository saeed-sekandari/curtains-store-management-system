import { useState } from "react";
import type { FormEvent } from "react";

import OrderItemsList, {
  createEmptyOrderItem,
} from "./OrderItemsList";

import type {
  CreateOrderRequest,
  OrderFormData,
  OrderItemRequest,
} from "../types/order";

type OrderFormProps = {
  storeCode: string;
  formData: OrderFormData;
  isDari: boolean;
  onSaved: () => void;
  onCancel: () => void;
  onSubmitOrder: (
    request: CreateOrderRequest
  ) => Promise<void>;
};

export default function OrderForm({
  storeCode,
  formData,
  isDari,
  onSaved,
  onCancel,
  onSubmitOrder,
}: OrderFormProps) {
  const [useNewCustomer, setUseNewCustomer] =
    useState(false);

  const [customerId, setCustomerId] =
    useState<number | undefined>(undefined);

  const [newCustomerName, setNewCustomerName] =
    useState("");

  const [newCustomerPhone, setNewCustomerPhone] =
    useState("");

  const [newCustomerAddress, setNewCustomerAddress] =
    useState("");

  const [orderNumber, setOrderNumber] =
    useState("");

  const [totalAmount, setTotalAmount] =
    useState("");

  const [initialPaymentAmount, setInitialPaymentAmount] =
    useState("");

  const [items, setItems] = useState<
    OrderItemRequest[]
  >([createEmptyOrderItem()]);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const text = {
    customer: isDari ? "مشتری" : "Customer",
    selectCustomer: isDari
      ? "انتخاب مشتری"
      : "Select customer",
    newCustomer: isDari
      ? "مشتری جدید"
      : "New customer",
    existingCustomer: isDari
      ? "مشتری موجود"
      : "Existing customer",
    name: isDari ? "نام مشتری" : "Customer Name",
    phone: isDari ? "شماره تماس" : "Phone",
    address: isDari ? "آدرس" : "Address",
    orderNumber: isDari
      ? "شماره سفارش"
      : "Order Number",
    total: isDari ? "مبلغ کل" : "Total Amount",
    payment: isDari
      ? "پرداخت ابتدایی"
      : "Initial Payment",
    currency: isDari ? "افغانی" : "AFN",
    save: isDari ? "ثبت سفارش" : "Save Order",
    cancel: isDari ? "لغو" : "Cancel",
    required: isDari ? "الزامی" : "Required",
    saving: isDari
      ? "در حال ثبت..."
      : "Saving...",
    customerRequired: isDari
      ? "لطفاً مشتری را انتخاب یا ثبت کنید."
      : "Please select or create a customer.",
    totalRequired: isDari
      ? "مبلغ کل باید بیشتر از صفر باشد."
      : "Total amount must be greater than zero.",
    paymentInvalid: isDari
      ? "پرداخت ابتدایی نمی‌تواند بیشتر از مبلغ کل باشد."
      : "Initial payment cannot exceed the total amount.",
  };

  function validate(): boolean {
    setError("");

    if (
      !useNewCustomer &&
      !customerId
    ) {
      setError(text.customerRequired);
      return false;
    }

    if (
      useNewCustomer &&
      !newCustomerName.trim()
    ) {
      setError(text.customerRequired);
      return false;
    }

    if (!orderNumber.trim()) {
      setError(
        isDari
          ? "شماره سفارش الزامی است."
          : "Order number is required."
      );
      return false;
    }

    const total = Number(totalAmount);
    const payment = initialPaymentAmount
      ? Number(initialPaymentAmount)
      : 0;

    if (!Number.isFinite(total) || total <= 0) {
      setError(text.totalRequired);
      return false;
    }

    if (
      !Number.isFinite(payment) ||
      payment < 0 ||
      payment > total
    ) {
      setError(text.paymentInvalid);
      return false;
    }

    return true;
  }

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    if (!validate()) {
      return;
    }

    const request: CreateOrderRequest = {
      storeCode,
      orderNumber: orderNumber.trim(),
      totalAmount: Number(totalAmount),
      initialPaymentAmount: initialPaymentAmount
        ? Number(initialPaymentAmount)
        : 0,
      initialPaymentMethod: "CASH",
      items,
    };

    if (useNewCustomer) {
      request.newCustomerName =
        newCustomerName.trim();
      request.newCustomerPhone =
        newCustomerPhone.trim();
      request.newCustomerAddress =
        newCustomerAddress.trim();
    } else {
      request.customerId = customerId;
    }

    try {
      setSaving(true);
      setError("");

      await onSubmitOrder(request);
      onSaved();
    } catch (submitError) {
      setError(
        submitError instanceof Error
          ? submitError.message
          : isDari
            ? "سفارش ثبت نشد."
            : "The order could not be saved."
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      dir={isDari ? "rtl" : "ltr"}
      className="space-y-6 rounded-3xl border border-[#e4dacb] bg-white p-6 shadow-sm sm:p-8"
    >
      <section>
        <h2 className="mb-5 text-xl font-bold text-[#173f38]">
          {text.customer}
        </h2>

        <div className="mb-5 flex gap-2">
          <button
            type="button"
            onClick={() => {
              setUseNewCustomer(false);
              setNewCustomerName("");
              setNewCustomerPhone("");
              setNewCustomerAddress("");
            }}
            className={`rounded-xl px-4 py-3 font-bold ${
              !useNewCustomer
                ? "bg-[#173f38] text-white"
                : "bg-[#f4f0e8] text-[#52675d]"
            }`}
          >
            {text.existingCustomer}
          </button>

          <button
            type="button"
            onClick={() => {
              setUseNewCustomer(true);
              setCustomerId(undefined);
            }}
            className={`rounded-xl px-4 py-3 font-bold ${
              useNewCustomer
                ? "bg-[#173f38] text-white"
                : "bg-[#f4f0e8] text-[#52675d]"
            }`}
          >
            {text.newCustomer}
          </button>
        </div>

        {!useNewCustomer ? (
          <label className="block">
            <span className="mb-2 block font-bold">
              {text.selectCustomer}
            </span>

            <select
              value={customerId ?? ""}
              onChange={(event) =>
                setCustomerId(
                  event.target.value
                    ? Number(event.target.value)
                    : undefined
                )
              }
              className="form-input"
              required
            >
              <option value="">
                {text.selectCustomer}
              </option>

              {formData.customers.map((customer) => (
                <option
                  key={customer.id}
                  value={customer.id}
                >
                  {customer.fullName}
                  {customer.phone
                    ? ` — ${customer.phone}`
                    : ""}
                </option>
              ))}
            </select>
          </label>
        ) : (
          <div className="grid gap-4 md:grid-cols-2">
            <label>
              <span className="mb-2 block font-bold">
                {text.name}
              </span>

              <input
                value={newCustomerName}
                onChange={(event) =>
                  setNewCustomerName(event.target.value)
                }
                className="form-input"
                required
              />
            </label>

            <label>
              <span className="mb-2 block font-bold">
                {text.phone}
              </span>

              <input
                value={newCustomerPhone}
                onChange={(event) =>
                  setNewCustomerPhone(event.target.value)
                }
                className="form-input"
              />
            </label>

            <label className="md:col-span-2">
              <span className="mb-2 block font-bold">
                {text.address}
              </span>

              <input
                value={newCustomerAddress}
                onChange={(event) =>
                  setNewCustomerAddress(
                    event.target.value
                  )
                }
                className="form-input"
              />
            </label>
          </div>
        )}
      </section>

      <section className="grid gap-4 border-y border-[#eee6da] py-6 md:grid-cols-3">
        <label>
          <span className="mb-2 block font-bold">
            {text.orderNumber}
          </span>

          <input
            value={orderNumber}
            onChange={(event) =>
              setOrderNumber(event.target.value)
            }
            className="form-input"
            required
          />
        </label>

        <label>
          <span className="mb-2 block font-bold">
            {text.total} ({text.currency})
          </span>

          <input
            type="number"
            min="0.01"
            step="0.01"
            value={totalAmount}
            onChange={(event) =>
              setTotalAmount(event.target.value)
            }
            className="form-input"
            required
          />
        </label>

        <label>
          <span className="mb-2 block font-bold">
            {text.payment} ({text.currency})
          </span>

          <input
            type="number"
            min="0"
            step="0.01"
            value={initialPaymentAmount}
            onChange={(event) =>
              setInitialPaymentAmount(
                event.target.value
              )
            }
            className="form-input"
          />
        </label>
      </section>

      <OrderItemsList
        items={items}
        inventoryProducts={formData.inventoryProducts}
        tailors={formData.tailors}
        isDari={isDari}
        onChange={setItems}
      />

      {error && (
        <div className="rounded-xl bg-[#fff0ed] px-4 py-3 font-semibold text-[#b42318]">
          {error}
        </div>
      )}

      <div className="flex gap-3">
        <button
          type="button"
          onClick={onCancel}
          className="rounded-xl border border-[#cfc4b5] px-6 py-3 font-bold text-[#52675d]"
        >
          {text.cancel}
        </button>

        <button
          type="submit"
          disabled={saving}
          className="rounded-xl bg-[#c89232] px-7 py-3 font-bold text-white disabled:opacity-60"
        >
          {saving ? text.saving : text.save}
        </button>
      </div>
    </form>
  );
}