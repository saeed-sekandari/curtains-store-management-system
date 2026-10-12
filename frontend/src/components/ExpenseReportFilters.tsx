export type ReportPeriod =
  | "today"
  | "week"
  | "month"
  | "year";

type StoreOption = {
  code: string;
  dari: string;
  english: string;
};

type ExpenseReportFiltersProps = {
  isDari?: boolean;
  storeOptions: StoreOption[];
  selectedStoreCode: string;
  selectedDate: string;
  period: ReportPeriod;
  onStoreChange: (value: string) => void;
  onDateChange: (value: string) => void;
  onPeriodChange: (value: ReportPeriod) => void;
};

export default function ExpenseReportFilters({
  isDari = true,
  storeOptions,
  selectedStoreCode,
  selectedDate,
  period,
  onStoreChange,
  onDateChange,
  onPeriodChange,
}: ExpenseReportFiltersProps) {
  const periods: Array<{
    value: ReportPeriod;
    dari: string;
    english: string;
  }> = [
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

  return (
    <div className="grid gap-5 rounded-2xl border border-[#e5ddd0] bg-[#fffdf9] p-5 md:grid-cols-3">
      <div>
        <label
          htmlFor="report-store"
          className="mb-2 block text-sm font-bold text-[#173f38]"
        >
          {isDari ? "فروشگاه" : "Store"}
        </label>

        <select
          id="report-store"
          value={selectedStoreCode}
          onChange={(event) =>
            onStoreChange(event.target.value)
          }
          className="w-full appearance-none rounded-xl border border-[#d8cbb9] bg-white px-4 py-3 text-right font-semibold text-[#173f38] outline-none focus:border-[#c89232] focus:ring-2 focus:ring-[#c89232]/30"
          style={{
            color: "#173f38",
            backgroundColor: "#ffffff",
            colorScheme: "light",
          }}
        >
          {storeOptions.map((store) => (
            <option
              key={store.code}
              value={store.code}
              style={{
                color: "#173f38",
                backgroundColor: "#ffffff",
              }}
            >
              {isDari ? store.dari : store.english}
            </option>
          ))}
        </select>
      </div>

      <div>
        <label
          htmlFor="report-date"
          className="mb-2 block text-sm font-bold text-[#173f38]"
        >
          {isDari ? "تاریخ" : "Date"}
        </label>

        <input
          id="report-date"
          type="date"
          value={selectedDate}
          onChange={(event) =>
            onDateChange(event.target.value)
          }
          className="w-full rounded-xl border border-[#d8cbb9] bg-white px-4 py-3 font-semibold text-[#173f38] outline-none focus:border-[#c89232] focus:ring-2 focus:ring-[#c89232]/30"
          style={{
            color: "#173f38",
            backgroundColor: "#ffffff",
            colorScheme: "light",
          }}
        />
      </div>

      <div>
        <p className="mb-2 text-sm font-bold text-[#173f38]">
          {isDari ? "دوره گزارش" : "Report Period"}
        </p>

        <div className="grid grid-cols-4 gap-2">
          {periods.map((item) => {
            const active = period === item.value;

            return (
              <button
                key={item.value}
                type="button"
                onClick={() =>
                  onPeriodChange(item.value)
                }
                className={`rounded-xl border px-3 py-3 text-sm font-bold transition ${
                  active
                    ? "border-[#164d43] bg-[#164d43] text-white"
                    : "border-[#d8cbb9] bg-white text-[#64756f] hover:border-[#c89232] hover:text-[#173f38]"
                }`}
              >
                {isDari ? item.dari : item.english}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}