export type ExpenseReportRecord = {
  id: number;
  dateTime: string;
  storeName: string;
  category: string;
  recipientName: string;
  amount: number;
  currency: "AFN" | "USD";
};

type ExpenseTableProps = {
  records: ExpenseReportRecord[];
  isDari?: boolean;
};

function formatAmount(amount: number): string {
  return amount.toLocaleString("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function formatDateTime(
  dateTime: string,
  isDari: boolean
): string {
  const date = new Date(dateTime);

  if (Number.isNaN(date.getTime())) {
    return dateTime;
  }

  return date.toLocaleString(
    isDari ? "en-US" : "en-US",
    {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }
  );
}

export default function ExpenseTable({
  records,
  isDari = true,
}: ExpenseTableProps) {
  return (
    <section
      dir={isDari ? "rtl" : "ltr"}
      className="rounded-2xl border border-[#eadfce] bg-white p-4 shadow-sm"
    >
      <div className="mb-4 flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-[#173f38]">
            {isDari
              ? "مصارف این دوره"
              : "Expenses in This Period"}
          </h2>

          <p className="mt-1 text-sm text-[#84928b]">
            {isDari
              ? "جزئیات مصارف ثبت‌شده"
              : "Recorded expense details"}
          </p>
        </div>

        <span className="rounded-full bg-[#f6ead0] px-3 py-1 text-sm font-semibold text-[#8b681e]">
          {records.length.toLocaleString("en-US")}{" "}
          {isDari ? "مورد" : "records"}
        </span>
      </div>

      {records.length === 0 ? (
        <div className="rounded-xl bg-[#fffaf0] px-5 py-8 text-center text-[#84928b]">
          {isDari
            ? "در این دوره مصرفی ثبت نشده است."
            : "No expenses were recorded in this period."}
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[760px] border-collapse text-right">
            <thead>
              <tr className="border-b border-[#eadfce] text-sm text-[#71817a]">
                <th className="px-3 py-3 font-semibold">
                  {isDari
                    ? "تاریخ و زمان"
                    : "Date and Time"}
                </th>

                <th className="px-3 py-3 font-semibold">
                  {isDari ? "فروشگاه" : "Store"}
                </th>

                <th className="px-3 py-3 font-semibold">
                  {isDari
                    ? "نوع مصرف"
                    : "Expense Type"}
                </th>

                <th className="px-3 py-3 font-semibold">
                  {isDari
                    ? "دریافت‌کننده"
                    : "Recipient"}
                </th>

                <th className="px-3 py-3 font-semibold">
                  {isDari ? "مبلغ" : "Amount"}
                </th>
              </tr>
            </thead>

            <tbody>
              {records.map((record) => (
                <tr
                  key={record.id}
                  className="border-b border-[#f0e9df] text-sm last:border-b-0 hover:bg-[#fffaf0]"
                >
                  <td className="px-3 py-3 text-[#53665e]">
                    {formatDateTime(
                      record.dateTime,
                      isDari
                    )}
                  </td>

                  <td className="px-3 py-3 font-medium text-[#173f38]">
                    {record.storeName}
                  </td>

                  <td className="px-3 py-3 text-[#53665e]">
                    {record.category}
                  </td>

                  <td className="px-3 py-3 text-[#53665e]">
                    {record.recipientName || "—"}
                  </td>

                  <td className="px-3 py-3 font-bold text-[#173f38]">
                    {formatAmount(record.amount)}{" "}
                    {record.currency === "AFN"
                      ? isDari
                        ? "افغانی"
                        : "AFN"
                      : "USD"}
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