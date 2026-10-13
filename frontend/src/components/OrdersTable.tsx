import type { OrderSummary } from "../types/order";

type OrdersTableProps = {
  orders: OrderSummary[];
  storeCode: string;
  isDari?: boolean;
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
    const dariDate = new Intl.DateTimeFormat(
      "fa-AF-u-ca-persian-nu-latn",
      {
        day: "numeric",
        month: "long",
        year: "numeric",
      }
    ).format(date);

    return toEnglishNumbers(dariDate);
  }

  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(date);
}

function formatMoney(
  amount: number,
  isDari: boolean
): string {
  return `${amount.toLocaleString("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} ${isDari ? "افغانی" : "AFN"}`;
}

function getOrderStatusLabel(
  status: string,
  isDari: boolean
): string {
  if (status === "CANCELLED") {
    return isDari ? "لغوشده" : "Cancelled";
  }

  return isDari ? "فعال" : "Active";
}

function getWorkStatusLabel(
  status: string,
  isDari: boolean
): string {
  const labels: Record<string, [string, string]> = {
    NOT_STARTED: ["شروع‌نشده", "Not Started"],
    RECEIVED: ["دریافت‌شده", "Received"],
    IN_PROGRESS: ["در حال انجام", "In Progress"],
    COMPLETED: ["تکمیل‌شده", "Completed"],
  };

  const label = labels[status];

  if (!label) {
    return status;
  }

  return isDari ? label[0] : label[1];
}

export default function OrdersTable({
  orders,
  storeCode,
  isDari = true,
}: OrdersTableProps) {
  return (
    <div className="overflow-hidden rounded-2xl border border-[#e4dacb] bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-[1350px] w-full border-collapse text-right">
          <thead className="bg-[#f5f0e8] text-sm text-[#52675d]">
            <tr>
              <th className="px-5 py-4 font-bold">
                {isDari ? "شماره سفارش" : "Order Number"}
              </th>

              <th className="px-5 py-4 font-bold">
                {isDari ? "مشتری" : "Customer"}
              </th>

              <th className="px-5 py-4 font-bold">
                {isDari ? "مبلغ کل" : "Total Amount"}
              </th>

              <th className="px-5 py-4 font-bold">
                {isDari ? "پرداخت‌شده" : "Paid"}
              </th>

              <th className="px-5 py-4 font-bold">
                {isDari ? "باقی‌مانده" : "Remaining"}
              </th>

              <th className="px-5 py-4 font-bold">
                {isDari ? "تاریخ تکمیل" : "Completion Date"}
              </th>

              <th className="px-5 py-4 font-bold">
                {isDari ? "وضعیت سفارش" : "Order Status"}
              </th>

              <th className="px-5 py-4 font-bold">
                {isDari ? "وضعیت کار" : "Work Status"}
              </th>

              <th className="px-5 py-4 font-bold">
                {isDari ? "جزئیات" : "Details"}
              </th>

              <th className="px-5 py-4 font-bold">
                {isDari ? "ویرایش" : "Edit"}
              </th>
            </tr>
          </thead>

          <tbody className="divide-y divide-[#eee7dc]">
            {orders.map((order) => {
              const cancelled =
                order.status === "CANCELLED";

              return (
                <tr
                  key={order.id}
                  className="transition hover:bg-[#fffaf1]"
                >
                  <td className="px-5 py-5 font-bold text-[#173f38]">
                    {order.orderNumber}
                  </td>

                  <td className="px-5 py-5 font-semibold text-[#273d37]">
                    {order.customerName || "—"}
                  </td>

                  <td className="px-5 py-5 font-bold text-[#173f38]">
                    {formatMoney(
                      order.totalAmount,
                      isDari
                    )}
                  </td>

                  <td className="px-5 py-5 text-[#276044]">
                    {formatMoney(
                      order.paidAmount,
                      isDari
                    )}
                  </td>

                  <td className="px-5 py-5 font-bold text-[#a56a00]">
                    {formatMoney(
                      order.remainingAmount,
                      isDari
                    )}
                  </td>

                  <td className="px-5 py-5 text-[#64756f]">
                    {formatDate(
                      order.completionDate,
                      isDari
                    )}
                  </td>

                  <td className="px-5 py-5">
                    <span
                      className={[
                        "inline-flex rounded-full px-4 py-2 text-sm font-bold",
                        cancelled
                          ? "bg-[#fff0ed] text-[#b42318]"
                          : "bg-[#edf8f0] text-[#276044]",
                      ].join(" ")}
                    >
                      {getOrderStatusLabel(
                        order.status,
                        isDari
                      )}
                    </span>
                  </td>

                  <td className="px-5 py-5">
                    <span className="inline-flex rounded-full bg-[#eef3f1] px-4 py-2 text-sm font-semibold text-[#64756f]">
                      {getWorkStatusLabel(
                        order.workStatus,
                        isDari
                      )}
                    </span>
                  </td>

                  <td className="px-5 py-5">
                    <a
                      href={`/orders/${order.id}?store=${encodeURIComponent(
                        storeCode
                      )}`}
                      className="inline-flex rounded-xl border-2 border-[#173f38] px-4 py-2 font-bold text-[#173f38] transition hover:bg-[#edf5f1]"
                    >
                      {isDari ? "جزئیات" : "Details"}
                    </a>
                  </td>

                  <td className="px-5 py-5">
                    {cancelled ? (
                      <span className="inline-flex rounded-xl bg-[#f1eee9] px-4 py-2 font-bold text-[#a39b90]">
                        {isDari ? "غیرفعال" : "Disabled"}
                      </span>
                    ) : (
                      <a
                        href={`/orders/${order.id}/edit?store=${encodeURIComponent(
                          storeCode
                        )}`}
                        className="inline-flex rounded-xl border-2 border-[#c89232] px-4 py-2 font-bold text-[#9a6818] transition hover:bg-[#fff4dc]"
                      >
                        {isDari ? "ویرایش" : "Edit"}
                      </a>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}