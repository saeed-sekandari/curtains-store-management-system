type SidebarLabels = {
  title: string;
  storeName: string;
  dashboard: string;
  managerWithdrawal: string;
  orders: string;
  customers: string;
  products: string;
  directSales: string;
  wholesaleAccounts: string;
  expenses: string;
  users: string;
  logout: string;
};

type StoreOption = {
  code: string;
  name: string;
};

type SidebarProps = {
  labels: SidebarLabels;
  selectedStoreCode?: string;
  storeOptions?: StoreOption[];
  isDari?: boolean;
  onStoreChange?: (storeCode: string) => void;
  onManagerWithdrawal?: () => void;
  onDirectSale: () => void;
  onLogout?: () => void;
};

export default function Sidebar({
  labels,
  selectedStoreCode = "royal",
  storeOptions = [],
  isDari = true,
  onStoreChange,
  onManagerWithdrawal,
  onDirectSale,
  onLogout,
}: SidebarProps) {
  const openExpensePage = () => {
    window.location.assign(
      `/expenses/new?store=${selectedStoreCode}`
    );
  };

  const openManagerWithdrawal = () => {
    onManagerWithdrawal?.();

    window.location.assign(
      `/expenses/new?store=${selectedStoreCode}&type=MANAGER_WITHDRAWAL`
    );
  };

  const textAlignment = isDari
    ? "text-right"
    : "text-left";

  return (
    <aside
      dir={isDari ? "rtl" : "ltr"}
      className="hidden w-72 shrink-0 flex-col bg-[#123b35] px-5 py-6 text-white lg:flex"
    >
      <div className="mb-7 border-b border-white/10 pb-6">
        <div className="mb-4 text-2xl font-bold leading-relaxed">
          {labels.title}
        </div>

        <label
          htmlFor="sidebar-store-selector"
          className={`mb-2 block text-sm text-emerald-100/70 ${textAlignment}`}
        >
          {labels.storeName}
        </label>

        <select
          id="sidebar-store-selector"
          value={selectedStoreCode}
          onChange={(event) =>
            onStoreChange?.(event.target.value)
          }
          className={`w-full rounded-xl border border-white/20 bg-white px-3 py-3 text-sm font-semibold text-[#123b35] outline-none transition focus:border-[#d7ad62] focus:ring-2 focus:ring-[#d7ad62]/40 ${textAlignment}`}
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

      <nav className="space-y-2">
        <MenuItem
          label={labels.dashboard}
          icon="⌂"
          active
          isDari={isDari}
          onClick={() => {
            window.location.assign(
              `/dashboard?store=${selectedStoreCode}`
            );
          }}
        />

        <MenuItem
          label={labels.managerWithdrawal}
          icon="▣"
          isDari={isDari}
          onClick={openManagerWithdrawal}
        />

        <MenuItem
          label={labels.orders}
          icon="▤"
          isDari={isDari}
          onClick={() => {
            window.location.assign(
              `/orders?store=${selectedStoreCode}`
            );
          }}
        />

        <MenuItem
          label={labels.directSales}
          icon="↗"
          isDari={isDari}
          onClick={onDirectSale}
        />

        <MenuItem
          label={labels.products}
          icon="▦"
          isDari={isDari}
          onClick={() => {
            window.location.assign(
              `/inventory?store=${selectedStoreCode}`
            );
          }}
        />

        <MenuItem
          label={labels.customers}
          icon="♙"
          isDari={isDari}
          onClick={() => {
            window.location.assign(
              `/customers?store=${selectedStoreCode}`
            );
          }}
        />

        <MenuItem
          label={labels.users}
          icon="◎"
          isDari={isDari}
          onClick={() => {
            window.location.assign("/users");
          }}
        />

        <MenuItem
          label={labels.wholesaleAccounts}
          icon="◈"
          isDari={isDari}
          onClick={() => {
            window.location.assign(
              `/suppliers?store=${selectedStoreCode}`
            );
          }}
        />

        <MenuItem
          label={labels.expenses}
          icon="−"
          isDari={isDari}
          onClick={openExpensePage}
        />
      </nav>

      <div className="mt-auto pt-8">
        <button
          type="button"
          onClick={onLogout}
          className={`flex w-full items-center gap-3 rounded-xl px-4 py-3 text-emerald-100 transition hover:bg-white/10 ${textAlignment}`}
        >
          <span className="w-6 text-center text-lg">
            ↪
          </span>

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
  isDari,
  onClick,
}: {
  label: string;
  icon: string;
  active?: boolean;
  isDari: boolean;
  onClick?: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      dir={isDari ? "rtl" : "ltr"}
      className={`flex w-full items-center gap-3 rounded-xl px-4 py-3 transition ${
        isDari ? "text-right" : "text-left"
      } ${
        active
          ? "bg-[#d7ad62] font-semibold text-white shadow-sm"
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