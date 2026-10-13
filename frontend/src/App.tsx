import ManagerDashboard from "./pages/ManagerDashboard";
import ExpensePage from "./pages/ExpensePage";
import InventoryPage from "./pages/InventoryPage";
import InventoryProductForm from "./pages/InventoryProductForm";
import OrdersPage from "./pages/OrdersPage";

function App() {
  const currentPath = window.location.pathname;

  const searchParams = new URLSearchParams(
    window.location.search
  );

  const storeCode =
    searchParams.get("store") || "royal";

  const editMatch = currentPath.match(
    /^\/inventory\/(\d+)\/edit\/?$/
  );

  if (
    currentPath === "/expenses" ||
    currentPath === "/expenses/" ||
    currentPath === "/expenses/new"
  ) {
    return (
      <ExpensePage
        storeCode={storeCode}
        managerName="مدیر سیستم"
      />
    );
  }

  if (
    currentPath === "/orders" ||
    currentPath === "/orders/" ||
    currentPath === "/orders/new"
  ) {
    return (
      <OrdersPage
        storeCode={storeCode}
      />
    );
  }

  if (currentPath === "/inventory/new") {
    return (
      <InventoryProductForm
        storeCode={storeCode}
      />
    );
  }

  if (editMatch) {
    return (
      <InventoryProductForm
        storeCode={storeCode}
        productId={Number(editMatch[1])}
      />
    );
  }

  if (
    currentPath === "/inventory" ||
    currentPath === "/inventory/"
  ) {
    return (
      <InventoryPage
        storeCode={storeCode}
      />
    );
  }

  return <ManagerDashboard />;
}

export default App;