import type {
  ExpenseCategory,
  ExpenseCurrency,
} from "../types/expense";

type ExpenseFieldsProps = {
  storeCode: string;
  category: ExpenseCategory;
  recipientName: string;
  amount: string;
  currency: ExpenseCurrency;
  expenseDate: string;
  description: string;
  managerName: string;
  isDari?: boolean;
  onRecipientChange: (value: string) => void;
  onAmountChange: (value: string) => void;
  onCurrencyChange: (value: ExpenseCurrency) => void;
  onDateChange: (value: string) => void;
  onDescriptionChange: (value: string) => void;
};

type Worker = {
  name: string;
  role: "EMPLOYEE" | "TAILOR";
  storeCode: "royal" | "kabul";
};

const workers: Worker[] = [
  {
    name: "Ahmad Rahimi",
    role: "EMPLOYEE",
    storeCode: "royal",
  },
  {
    name: "Fatima Ahmadi",
    role: "EMPLOYEE",
    storeCode: "kabul",
  },
  {
    name: "Mohammad",
    role: "TAILOR",
    storeCode: "kabul",
  },
  {
    name: "Khalifa Zia",
    role: "TAILOR",
    storeCode: "royal",
  },
];

export default function ExpenseFields({
  storeCode,
  category,
  recipientName,
  amount,
  currency,
  expenseDate,
  description,
  managerName,
  isDari = true,
  onRecipientChange,
  onAmountChange,
  onCurrencyChange,
  onDateChange,
  onDescriptionChange,
}: ExpenseFieldsProps) {
  const requiresWorker =
    category === "EMPLOYEE_PAYMENT" ||
    category === "TAILOR_PAYMENT";

  const isManagerWithdrawal =
    category === "MANAGER_WITHDRAWAL";

  const recipientValue = isManagerWithdrawal
    ? managerName
    : recipientName;

  const availableWorkers = workers.filter(
    (worker) =>
      worker.storeCode === storeCode &&
      (category === "EMPLOYEE_PAYMENT"
        ? worker.role === "EMPLOYEE"
        : worker.role === "TAILOR")
  );

  return (
    <>
      <section className="grid gap-4 border-b border-[#eee6da] py-4 md:grid-cols-2">
        <div>
          <label
            htmlFor="recipient-name"
            className="mb-2 block text-base font-bold text-[#173f38]"
          >
            {isDari ? "دریافت‌کننده" : "Recipient"}

            {requiresWorker && (
              <span className="mr-1 text-[#b42318]">
                *
              </span>
            )}
          </label>

          {requiresWorker ? (
            <select
              id="recipient-name"
              value={recipientName}
              onChange={(event) =>
                onRecipientChange(event.target.value)
              }
              className="form-input"
              required
            >
              <option value="">
                {isDari ? "انتخاب کنید" : "Select"}
              </option>

              {availableWorkers.map((worker) => (
                <option
                  key={worker.name}
                  value={worker.name}
                >
                  {worker.name}
                </option>
              ))}
            </select>
          ) : (
            <input
              id="recipient-name"
              type="text"
              value={recipientValue}
              onChange={(event) =>
                onRecipientChange(event.target.value)
              }
              className="form-input"
              placeholder={
                isDari
                  ? "نام دریافت‌کننده"
                  : "Recipient name"
              }
              readOnly={isManagerWithdrawal}
            />
          )}
        </div>

        <div>
          <label
            htmlFor="expense-amount"
            className="mb-2 block text-base font-bold text-[#173f38]"
          >
            {isDari ? "مبلغ" : "Amount"}

            <span className="mr-1 text-[#b42318]">
              *
            </span>
          </label>

          <input
            id="expense-amount"
            type="number"
            min="0.01"
            step="0.01"
            value={amount}
            onChange={(event) =>
              onAmountChange(event.target.value)
            }
            className="form-input"
            placeholder="0"
            required
          />
        </div>
      </section>

      <section className="grid gap-4 border-b border-[#eee6da] py-4 md:grid-cols-2">
        <div>
          <label
            htmlFor="expense-currency"
            className="mb-2 block text-base font-bold text-[#173f38]"
          >
            {isDari ? "واحد پول" : "Currency"}

            <span className="mr-1 text-[#b42318]">
              *
            </span>
          </label>

          <select
            id="expense-currency"
            value={currency}
            onChange={(event) =>
              onCurrencyChange(
                event.target.value as ExpenseCurrency
              )
            }
            className="form-input"
            required
          >
            <option value="AFN">
              {isDari ? "افغانی" : "AFN"}
            </option>

            <option value="USD">
              USD
            </option>
          </select>
        </div>

        <div>
          <label
            htmlFor="expense-date"
            className="mb-2 block text-base font-bold text-[#173f38]"
          >
            {isDari
              ? "تاریخ و زمان"
              : "Date and Time"}

            <span className="mr-1 text-[#b42318]">
              *
            </span>
          </label>

          <input
            id="expense-date"
            type="datetime-local"
            value={expenseDate}
            onChange={(event) =>
              onDateChange(event.target.value)
            }
            className="form-input"
            required
          />
        </div>
      </section>

      <section className="py-4">
        <label
          htmlFor="expense-description"
          className="mb-2 block text-base font-bold text-[#173f38]"
        >
          {isDari ? "توضیحات" : "Notes"}
        </label>

        <textarea
          id="expense-description"
          value={description}
          onChange={(event) =>
            onDescriptionChange(event.target.value)
          }
          className="form-input min-h-20"
          placeholder={
            isDari
              ? "توضیحات مربوط به مصرف"
              : "Notes about this expense"
          }
          maxLength={500}
        />

        <div className="mt-1 text-left text-xs text-[#89948c]">
          {description.length}/500
        </div>
      </section>
    </>
  );
}