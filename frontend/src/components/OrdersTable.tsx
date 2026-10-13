import type { OrderSummary } from "../types/order";

type OrdersTableProps = {
  orders: OrderSummary[];
  storeCode: string;
  isDari: boolean;
};

function formatAmount(value: number): string {
  return value.toLocaleString("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function getStatusLabel(
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
  switch (status) {
    case "COMPLETED":
      return isDari ? "تکمیل‌شده" : "Completed";

    case "IN_PROGRESS":
      return isDari ? "در حال کار" : "In Progress";

    case "RECEIVED":
      return isDari ? "دریافت‌شده" : "Received";

    default:
      return isDari ? "شروع‌نشده" : "Not Started";
  }
}

export default function OrdersTable({
  orders,
  storeCode,
  isDari,
}: OrdersTableProps) {
  if (orders.length === 0) {
    return (
      <div className="rounded-2xl border border-[#e4dacb] bg-white p-10 text-center text-[#718077]">
        {isDari
          ? "هنوز سفارشی ثبت نشده است."
          : "No orders have been recorded yet."}
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-2xl border border-[#e4dacb] bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-[1050px] w-full border-collapse">
          <thead className="bg-[#f5f0e8] text-sm text-[#52675d]">
            <tr>
              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "شماره سفارش" : "Order Number"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "مشتری" : "Customer"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "تاریخ" : "Date"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "مبلغ کل" : "Total"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "پرداخت‌شده" : "Paid"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "باقی‌مانده" : "Remaining"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "وضعیت سفارش" : "Order Status"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "وضعیت کار" : "Work Status"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "عملیات" : "Actions"}
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
                  <td className="px-5 py-4 font-bold text-[#173f38]">
                    {order.orderNumber}
                  </td>

                  <td className="px-5 py-4 font-semibold text-[#333]">
                    {order.customerName || "—"}
                  </td>

                  <td className="px-5 py-4 text-[#5f7068]">
                    {order.createdDate}
                  </td>

                  <td className="px-5 py-4 font-semibold text-[#173f38]">
                    {formatAmount(order.totalAmount)}{" "}
                    <span className="text-sm">
                      {isDari ? "افغانی" : "AFN"}
                    </span>
                  </td>

                  <td className="px-5 py-4 text-[#276044]">
                    {formatAmount(order.paidAmount)}{" "}
                    <span className="text-sm">
                      {isDari ? "افغانی" : "AFN"}
                    </span>
                  </td>

                  <td className="px-5 py-4 font-bold text-[#9a6818]">
                    {formatAmount(order.remainingAmount)}{" "}
                    <span className="text-sm">
                      {isDari ? "افغانی" : "AFN"}
                    </span>
                  </td>

                  <td className="px-5 py-4">
                    <span
                      className={`inline-flex rounded-full px-3 py-1 text-xs font-bold ${
                        cancelled
                          ? "bg-[#fff0ed] text-[#b42318]"
                          : "bg-[#edf8f0] text-[#276044]"
                      }`}
                    >
                      {getStatusLabel(
                        order.status,
                        isDari
                      )}
                    </span>
                  </td>

                  <td className="px-5 py-4">
                    <span className="inline-flex rounded-full bg-[#eef3f1] px-3 py-1 text-xs font-bold text-[#52675d]">
                      {getWorkStatusLabel(
                        order.workStatus,
                        isDari
                      )}
                    </span>
                  </td>

                  <td className="px-5 py-4">
                    <a
                      href={`/orders/${order.id}/items?store=${storeCode}`}
                      className="inline-flex rounded-lg border border-[#c89232] px-3 py-2 text-sm font-bold text-[#9a6818] transition hover:bg-[#fff4dc]"
                    >
                      {isDari
                        ? "مشاهده"
                        : "View"}
                    </a>
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