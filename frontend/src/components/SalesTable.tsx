import type { SaleRow } from "../types/dashboard";

type SalesTableLabels = {
  title: string;
  description: string;
  noSales: string;
  time: string;
  saleType: string;
  amount: string;
  today: string;
};

type SalesTableProps = {
  sales: SaleRow[];
  labels: SalesTableLabels;
  isDari: boolean;
  currency: string;
};

export default function SalesTable({
  sales,
  labels,
  isDari,
  currency,
}: SalesTableProps) {
  const formatMoney = (amount: number) => {
    return `${amount.toLocaleString("en-US")} ${currency}`;
  };

  const getSaleType = (type: string) => {
    if (type === "CUSTOMER_ORDER") {
      return isDari ? "سفارش مشتری" : "Customer Order";
    }

    return isDari ? "فروش مستقیم" : "Direct Sale";
  };

  const getSaleTime = (dateTime: string) => {
    if (dateTime.includes("T")) {
      return dateTime.substring(11, 16);
    }

    return dateTime.substring(0, 5);
  };

  return (
    <section className="rounded-3xl border border-[#e9e1d4] bg-white p-5 shadow-[0_12px_35px_rgba(61,49,35,0.06)] sm:p-7">
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2 className="text-xl font-bold text-[#173f38]">
            {labels.title}
          </h2>

          <p className="mt-1 text-sm text-[#8b8175]">
            {labels.description}
          </p>
        </div>

        <div className="rounded-full bg-[#f7f1e7] px-4 py-2 text-sm text-[#987348]">
          {labels.today}
        </div>
      </div>

      {sales.length === 0 ? (
        <p className="rounded-2xl bg-[#fbf8f1] p-6 text-center text-[#8b8175]">
          {labels.noSales}
        </p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[650px] border-collapse text-right">
            <thead>
              <tr className="border-b border-[#eee6db] text-sm text-[#8b8175]">
                <th className="px-4 py-4 font-medium">
                  {labels.time}
                </th>

                <th className="px-4 py-4 font-medium">
                  {labels.saleType}
                </th>

                <th className="px-4 py-4 font-medium">
                  {labels.description}
                </th>

                <th className="px-4 py-4 font-medium">
                  {labels.amount}
                </th>
              </tr>
            </thead>

            <tbody>
              {sales.map((sale) => (
                <tr
                  key={`${sale.dateTime}-${sale.description}`}
                  className="border-b border-[#f2ece3] last:border-0"
                >
                  <td className="px-4 py-5 text-sm text-[#766c60]">
                    {getSaleTime(sale.dateTime)}
                  </td>

                  <td className="px-4 py-5">
                    <span className="rounded-full bg-[#eef6f1] px-3 py-1.5 text-sm text-[#267052]">
                      {getSaleType(sale.type)}
                    </span>
                  </td>

                  <td className="px-4 py-5 text-sm text-[#4f5f59]">
                    {sale.description}
                  </td>

                  <td className="px-4 py-5 font-semibold text-[#173f38]">
                    {formatMoney(sale.amount)}
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