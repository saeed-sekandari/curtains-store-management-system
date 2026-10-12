type ExpenseSummaryCardsProps = {
  totalRecords: number;
  totalAfn: number;
  totalUsd: number;
  isDari?: boolean;
};

function formatNumber(value: number): string {
  return value.toLocaleString("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

export default function ExpenseSummaryCards({
  totalRecords,
  totalAfn,
  totalUsd,
  isDari = true,
}: ExpenseSummaryCardsProps) {
  return (
    <section
      dir={isDari ? "rtl" : "ltr"}
      className="mb-6 grid gap-3 md:grid-cols-3"
    >
      <article className="rounded-2xl border border-[#eadfce] bg-white p-4 shadow-sm">
        <p className="text-sm font-semibold text-[#7b8b83]">
          {isDari
            ? "تعداد مصارف"
            : "Number of Expenses"}
        </p>

        <p className="mt-2 text-2xl font-bold text-[#173f38]">
          {totalRecords.toLocaleString("en-US")}
        </p>

        <p className="mt-1 text-sm text-[#8b9a92]">
          {isDari
            ? "ثبت‌شده در این دوره"
            : "Recorded in this period"}
        </p>
      </article>

      <article className="rounded-2xl border border-[#eadfce] bg-white p-4 shadow-sm">
        <p className="text-sm font-semibold text-[#7b8b83]">
          {isDari
            ? "مجموع افغانی"
            : "Total AFN"}
        </p>

        <p className="mt-2 text-2xl font-bold text-[#173f38]">
          {formatNumber(totalAfn)}
        </p>

        <p className="mt-1 text-sm text-[#8b9a92]">
          {isDari ? "افغانی" : "AFN"}
        </p>
      </article>

      <article className="rounded-2xl border border-[#eadfce] bg-white p-4 shadow-sm">
        <p className="text-sm font-semibold text-[#7b8b83]">
          {isDari
            ? "مجموع دالر"
            : "Total USD"}
        </p>

        <p className="mt-2 text-2xl font-bold text-[#173f38]">
          {formatNumber(totalUsd)}
        </p>

        <p className="mt-1 text-sm text-[#8b9a92]">
          {isDari ? "دالر آمریکا" : "US Dollars"}
        </p>
      </article>
    </section>
  );
}