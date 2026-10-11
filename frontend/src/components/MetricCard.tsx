type MetricCardProps = {
  label: string;
  value: string;
  detail: string;
  icon: string;
  highlighted?: boolean;
};

export default function MetricCard({
  label,
  value,
  detail,
  icon,
  highlighted = false,
}: MetricCardProps) {
  return (
    <article
      className={`rounded-3xl border p-5 shadow-[0_10px_30px_rgba(61,49,35,0.05)] ${
        highlighted
          ? "border-[#d7ad62] bg-[#fff8e9]"
          : "border-[#e9e1d4] bg-white"
      }`}
    >
      <div className="mb-5 flex items-center justify-between">
        <span className="text-sm font-medium text-[#776d61]">
          {label}
        </span>

        <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#f4ecdf] text-lg text-[#a47739]">
          {icon}
        </span>
      </div>

      <div className="text-2xl font-bold text-[#173f38]">
        {value}
      </div>

      <p className="mt-2 text-xs text-[#958b7d]">
        {detail}
      </p>
    </article>
  );
}