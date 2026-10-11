export type Language = "dari" | "english";

export type SaleRow = {
  dateTime: string;
  type: string;
  description: string;
  amount: number;
  currency: string;
};

export type DashboardData = {
  storeName: string;
  storeCode: string;
  todayOrderCount: number;
  customerCount: number;
  todayOrderSalesAfn: number;
  todayDirectSalesAfn: number;
  todaySalesAfn: number;
  todaySalesUsd: number;
  todayExpensesAfn: number;
  todayExpensesUsd: number;
  monthlySalesAfn: number;
  sales: SaleRow[];
};

export type InventoryProduct = {
  id: number;
  productName: string;
  color: string | null;
  productCode: string;
  availableMeterage: number;
  location: string | null;
};

export type DirectSaleRequest = {
  storeCode: string;
  productId: number;
  quantity: number;
  unitPrice: number;
  notes: string;
};