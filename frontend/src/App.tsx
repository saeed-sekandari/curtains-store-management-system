import ManagerDashboard from "./pages/ManagerDashboard";
import ExpensePage from "./pages/ExpensePage";
import InventoryPage from "./pages/InventoryPage";
import InventoryProductForm from "./pages/InventoryProductForm";
import OrdersPage from "./pages/OrdersPage";
import OrderDetailsPage from "./pages/OrderDetailsPage";

function App() {
  const currentPath = window.location.pathname;

  const searchParams = new URLSearchParams(
    window.location.search
  );

  const storeCode =
    searchParams.get("store") || "royal";

  const inventoryEditMatch = currentPath.match(
    /^\/inventory\/(\d+)\/edit\/?$/
  );

  const orderEditMatch = currentPath.match(
    /^\/orders\/(\d+)\/edit\/?$/
  );

  const orderDetailsMatch = currentPath.match(
    /^\/orders\/(\d+)\/?$/
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

  if (orderEditMatch) {
    return (
      <OrderDetailsPage
        orderId={Number(orderEditMatch[1])}
        storeCode={storeCode}
        mode="edit"
      />
    );
  }

  if (orderDetailsMatch) {
    return (
      <OrderDetailsPage
        orderId={Number(orderDetailsMatch[1])}
        storeCode={storeCode}
        mode="view"
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

  if (inventoryEditMatch) {
    return (
      <InventoryProductForm
        storeCode={storeCode}
        productId={Number(inventoryEditMatch[1])}
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