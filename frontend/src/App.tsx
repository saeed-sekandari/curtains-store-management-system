import ManagerDashboard from "./pages/ManagerDashboard";
import ExpensePage from "./pages/ExpensePage";

function App() {
  const currentPath = window.location.pathname;

  const searchParams = new URLSearchParams(
    window.location.search
  );

  const storeCode =
    searchParams.get("store") || "royal";

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

  return <ManagerDashboard />;
}

export default App;