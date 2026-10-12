export type ReportPeriod =
  | "today"
  | "week"
  | "month"
  | "year";

type StoreOption = {
  code: string;
  name: string;
};

type ExpenseReportFiltersProps = {
  selectedStoreCode: string;
  date: string;
  period: ReportPeriod;
  storeOptions: StoreOption[];
  isDari?: boolean;
  onStoreChange: (storeCode: string) => void;
  onDateChange: (date: string) => void;
  onPeriodChange: (period: ReportPeriod) => void;
};

const periods: {
  value: ReportPeriod;
  dari: string;
  english: string;
}[] = [
  {
    value: "today",
    dari: "امروز",
    english: "Today",
  },
  {
    value: "week",
    dari: "هفته",
    english: "Week",
  },
  {
    value: "month",
    dari: "ماه",
    english: "Month",
  },
  {
    value: "year",
    dari: "سال",
    english: "Year",
  },
];

export default function ExpenseReportFilters({
  selectedStoreCode,
  date,
  period,
  storeOptions,
  isDari = true,
  onStoreChange,
  onDateChange,
  onPeriodChange,
}: ExpenseReportFiltersProps) {
  return (
    <section className="mb-6 rounded-2xl border border-[#eadfce] bg-white p-4 shadow-sm">
      <div
        dir={isDari ? "rtl" : "ltr"}
        className="grid gap-4 xl:grid-cols-[1fr_auto_auto]"
      >
        <div>
          <label
            htmlFor="expense-report-store"
            className="mb-2 block text-sm font-bold text-[#173f38]"
          >
            {isDari ? "فروشگاه" : "Store"}
          </label>

          <select
            id="expense-report-store"
            value={selectedStoreCode}
            onChange={(event) =>
              onStoreChange(event.target.value)
            }
            className="w-full rounded-xl border border-[#d9cdbd] bg-[#fffdf9] px-4 py-2.5 text-right text-[#173f38] outline-none transition focus:border-[#d7ad62] focus:ring-2 focus:ring-[#d7ad62]/20"
          >
            {storeOptions.map((store) => (
              <option
                key={store.code}
                value={store.code}
              >
                {store.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label
            htmlFor="expense-report-date"
            className="mb-2 block text-sm font-bold text-[#173f38]"
          >
            {isDari ? "تاریخ" : "Date"}
          </label>

          <input
            id="expense-report-date"
            type="date"
            value={date}
            onChange={(event) =>
              onDateChange(event.target.value)
            }
            className="rounded-xl border border-[#d9cdbd] bg-[#fffdf9] px-4 py-2.5 text-[#173f38] outline-none transition focus:border-[#d7ad62] focus:ring-2 focus:ring-[#d7ad62]/20"
          />
        </div>

        <div>
          <span className="mb-2 block text-sm font-bold text-[#173f38]">
            {isDari ? "دوره گزارش" : "Report Period"}
          </span>

          <div className="flex flex-wrap gap-2">
            {periods.map((item) => (
              <button
                key={item.value}
                type="button"
                onClick={() =>
                  onPeriodChange(item.value)
                }
                className={`rounded-xl px-4 py-2.5 text-sm font-semibold transition ${
                  period === item.value
                    ? "bg-[#173f38] text-white"
                    : "border border-[#d9cdbd] bg-[#fffdf9] text-[#587068] hover:border-[#d7ad62]"
                }`}
              >
                {isDari
                  ? item.dari
                  : item.english}
              </button>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}