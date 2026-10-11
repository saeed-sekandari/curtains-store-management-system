import type {
  DashboardData,
  DirectSaleRequest,
  InventoryProduct,
} from "../types/dashboard";

async function request<T>(
  url: string,
  options?: RequestInit
): Promise<T> {
  const response = await fetch(url, {
    credentials: "include",
    ...options,
  });

  if (!response.ok) {
    throw new Error("Request failed");
  }

  return response.json();
}

export function getDashboard(
  store = "all"
): Promise<DashboardData> {
  return request<DashboardData>(
    `/api/dashboard?store=${encodeURIComponent(store)}`
  );
}

export function searchInventoryProducts(
  store: string,
  search = ""
): Promise<InventoryProduct[]> {
  return request<InventoryProduct[]>(
    `/api/inventory/products?store=${encodeURIComponent(
      store
    )}&search=${encodeURIComponent(search)}`
  );
}

export function createDirectSale(
  sale: DirectSaleRequest
): Promise<unknown> {
  return request<unknown>("/api/direct-sales", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(sale),
  });
}