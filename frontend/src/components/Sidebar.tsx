type SidebarLabels = {
  title: string;
  storeName: string;
  dashboard: string;
  orders: string;
  customers: string;
  products: string;
  directSales: string;
  wholesaleAccounts: string;
  expenses: string;
  users: string;
  logout: string;
};

type SidebarProps = {
  labels: SidebarLabels;
  onDirectSale: () => void;
};

export default function Sidebar({
  labels,
  onDirectSale,
}: SidebarProps) {
  return (
    <aside className="hidden w-72 shrink-0 flex-col bg-[#123b35] px-5 py-6 text-white lg:flex">
      <div className="mb-10 border-b border-white/10 pb-6">
        <div className="mb-2 text-2xl font-bold">
          {labels.title}
        </div>

        <div className="text-sm text-emerald-100/70">
          {labels.storeName}
        </div>
      </div>

      <nav className="space-y-2">
        <MenuItem
          label={labels.dashboard}
          icon="⌂"
          active
        />

        <MenuItem label={labels.orders} icon="▤" />
        <MenuItem label={labels.customers} icon="♙" />
        <MenuItem label={labels.products} icon="▦" />

        <button
          onClick={onDirectSale}
          className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-right text-emerald-50 transition hover:bg-white/10"
        >
          <span className="w-6 text-center text-lg">
            ↗
          </span>

          <span>{labels.directSales}</span>
        </button>

        <MenuItem
          label={labels.wholesaleAccounts}
          icon="◈"
        />

        <MenuItem label={labels.expenses} icon="−" />
        <MenuItem label={labels.users} icon="◎" />
      </nav>

      <div className="mt-auto">
        <button className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-right text-emerald-100 transition hover:bg-white/10">
          <span className="text-lg">↪</span>
          <span>{labels.logout}</span>
        </button>
      </div>
    </aside>
  );
}

function MenuItem({
  label,
  icon,
  active = false,
}: {
  label: string;
  icon: string;
  active?: boolean;
}) {
  return (
    <button
      className={`flex w-full items-center gap-3 rounded-xl px-4 py-3 text-right transition ${
        active
          ? "bg-[#d7ad62] font-semibold text-white"
          : "text-emerald-50 hover:bg-white/10"
      }`}
    >
      <span className="w-6 text-center text-lg">
        {icon}
      </span>

      <span>{label}</span>
    </button>
  );
}