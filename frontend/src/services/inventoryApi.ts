import type {
  InventoryMovement,
  InventoryProduct,
  InventoryProductRequest,
} from "../types/inventory";

const API_URL = "http://localhost:8080/api/inventory";

async function readError(response: Response) {
  const text = await response.text();

  if (!text) {
    return "The request failed.";
  }

  try {
    const data = JSON.parse(text);

    return data.message || data.error || text;
  } catch {
    return text;
  }
}

export async function getInventoryProducts(
  storeCode: string,
  search = ""
): Promise<InventoryProduct[]> {
  const params = new URLSearchParams({
    store: storeCode,
  });

  if (search.trim()) {
    params.set("search", search.trim());
  }

  const response = await fetch(
    `${API_URL}?${params.toString()}`,
    {
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  return response.json();
}

export async function getInventoryProduct(
  productId: number,
  storeCode: string
): Promise<InventoryProduct> {
  const response = await fetch(
    `${API_URL}/${productId}?store=${encodeURIComponent(
      storeCode
    )}`,
    {
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  return response.json();
}

export async function getInventoryMovements(
  productId: number,
  storeCode: string
): Promise<InventoryMovement[]> {
  const response = await fetch(
    `${API_URL}/${productId}/movements?store=${encodeURIComponent(
      storeCode
    )}`,
    {
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  return response.json();
}

export async function createInventoryProduct(
  request: InventoryProductRequest
): Promise<InventoryProduct> {
  const response = await fetch(API_URL, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  return response.json();
}

export async function updateInventoryProduct(
  productId: number,
  storeCode: string,
  request: InventoryProductRequest
): Promise<InventoryProduct> {
  const response = await fetch(
    `${API_URL}/${productId}?store=${encodeURIComponent(
      storeCode
    )}`,
    {
      method: "PUT",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    }
  );

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  return response.json();
}