export type ExpenseReportRecord = {
  id: number;
  dateTime: string;
  storeName: string;
  category: string;
  recipientName: string;
  amount: number;
  currency: string;
};

type ExpenseTableProps = {
  records: ExpenseReportRecord[];
  isDari?: boolean;
};

const dariCategories: Record<string, string> = {
  RENT: "کرایه دکان",
  UTILITIES: "برق و آب",
  TAX: "مالیات",
  TRANSPORTATION: "ترانسپورت",
  EMPLOYEE_PAYMENT: "پرداخت کارمند",
  TAILOR_PAYMENT: "پرداخت خیاط",
  MANAGER_WITHDRAWAL: "برداشت شخصی مدیر",
  OTHER: "مصرف دیگر",
};

const englishCategories: Record<string, string> = {
  RENT: "Store Rent",
  UTILITIES: "Utilities",
  TAX: "Tax",
  TRANSPORTATION: "Transportation",
  EMPLOYEE_PAYMENT: "Employee Payment",
  TAILOR_PAYMENT: "Tailor Payment",
  MANAGER_WITHDRAWAL: "Manager Withdrawal",
  OTHER: "Other Expense",
};

function getCategoryName(
  category: string,
  isDari: boolean
): string {
  const categories = isDari
    ? dariCategories
    : englishCategories;

  return categories[category] || category;
}

function formatDate(
  value: string,
  isDari: boolean
): string {
  const date = new Date(value);

  return new Intl.DateTimeFormat(
    isDari ? "en-US" : "en-US",
    {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "numeric",
      minute: "2-digit",
    }
  ).format(date);
}

function getCurrencyName(
  currency: string,
  isDari: boolean
): string {
  if (currency === "AFN") {
    return isDari ? "افغانی" : "AFN";
  }

  return isDari ? "دالر آمریکا" : "USD";
}

export default function ExpenseTable({
  records,
  isDari = true,
}: ExpenseTableProps) {
  return (
    <section className="mt-8">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <h3 className="text-xl font-bold text-[#173f38]">
            {isDari
              ? "مصارف این دوره"
              : "Expenses in This Period"}
          </h3>

          <p className="mt-1 text-sm text-[#7d887f]">
            {isDari
              ? "جزئیات مصارف ثبت‌شده"
              : "Details of recorded expenses"}
          </p>
        </div>

        <span className="rounded-full bg-[#f5efe4] px-3 py-1 text-sm font-semibold text-[#8b6a35]">
          {records.length}{" "}
          {isDari ? "مورد" : "records"}
        </span>
      </div>

      {records.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-[#d8cbb9] bg-[#fffdf9] px-5 py-8 text-center text-[#7d887f]">
          {isDari
            ? "در این دوره مصرفی ثبت نشده است."
            : "No expenses were recorded in this period."}
        </div>
      ) : (
        <div className="overflow-x-auto rounded-2xl border border-[#e5ddd0]">
          <table className="min-w-full border-collapse bg-white text-sm">
            <thead className="bg-[#f4faf4] text-[#173f38]">
              <tr>
                <th className="whitespace-nowrap px-4 py-3 text-right font-bold">
                  {isDari
                    ? "تاریخ و زمان"
                    : "Date and Time"}
                </th>

                <th className="whitespace-nowrap px-4 py-3 text-right font-bold">
                  {isDari ? "فروشگاه" : "Store"}
                </th>

                <th className="whitespace-nowrap px-4 py-3 text-right font-bold">
                  {isDari
                    ? "نوع مصرف"
                    : "Expense Type"}
                </th>

                <th className="whitespace-nowrap px-4 py-3 text-right font-bold">
                  {isDari
                    ? "دریافت‌کننده"
                    : "Recipient"}
                </th>

                <th className="whitespace-nowrap px-4 py-3 text-right font-bold">
                  {isDari ? "مبلغ" : "Amount"}
                </th>
              </tr>
            </thead>

            <tbody>
              {records.map((record) => (
                <tr
                  key={record.id}
                  className="border-t border-[#eee6da] transition hover:bg-[#fffaf1]"
                >
                  <td className="whitespace-nowrap px-4 py-4 text-[#56665e]">
                    {formatDate(
                      record.dateTime,
                      isDari
                    )}
                  </td>

                  <td className="whitespace-nowrap px-4 py-4 font-semibold text-[#173f38]">
                    {record.storeName}
                  </td>

                  <td className="whitespace-nowrap px-4 py-4 font-semibold text-[#56665e]">
                    {getCategoryName(
                      record.category,
                      isDari
                    )}
                  </td>

                  <td className="whitespace-nowrap px-4 py-4 text-[#56665e]">
                    {record.recipientName || "—"}
                  </td>

                  <td className="whitespace-nowrap px-4 py-4 font-bold text-[#173f38]">
                    {record.amount.toLocaleString("en-US", {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    })}{" "}
                    {getCurrencyName(
                      record.currency,
                      isDari
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}