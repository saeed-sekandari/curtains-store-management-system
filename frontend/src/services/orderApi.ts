import type {
  CreateOrderRequest,
  OrderDetailsResponse,
  OrderFormData,
  OrdersResponse,
} from "../types/order";

const API_URL = "http://localhost:8080/api/orders";

async function readError(
  response: Response
): Promise<string> {
  const contentType =
    response.headers.get("content-type") || "";

  const text = await response.text();

  if (
    contentType.includes("text/html") ||
    text.trim().startsWith("<!")
  ) {
    return "Your session has expired. Please log in again.";
  }

  if (!text) {
    return "The request failed.";
  }

  try {
    const data = JSON.parse(text);

    return (
      data.message ||
      data.error ||
      "The request failed."
    );
  } catch {
    return text;
  }
}

async function requestJson<T>(
  url: string,
  options?: RequestInit
): Promise<T> {
  const response = await fetch(url, {
    credentials: "include",
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options?.headers || {}),
    },
  });

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  return response.json() as Promise<T>;
}

export async function getOrderFormData(
  storeCode: string
): Promise<OrderFormData> {
  return requestJson<OrderFormData>(
    `${API_URL}/form-data?store=${encodeURIComponent(
      storeCode
    )}`
  );
}

export async function getOrders(
  storeCode: string
): Promise<OrdersResponse> {
  return requestJson<OrdersResponse>(
    `${API_URL}?store=${encodeURIComponent(storeCode)}`
  );
}

export async function getOrder(
  orderId: number,
  storeCode: string
): Promise<OrderDetailsResponse> {
  return requestJson<OrderDetailsResponse>(
    `${API_URL}/${orderId}?store=${encodeURIComponent(
      storeCode
    )}`
  );
}

export async function createOrder(
  request: CreateOrderRequest
): Promise<OrderDetailsResponse> {
  return requestJson<OrderDetailsResponse>(API_URL, {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export type UpdateOrderRequest = {
  storeCode: string;
  completionDate: string;
  tailorId?: number;
  details: string;
};

export async function updateOrder(
  orderId: number,
  request: UpdateOrderRequest
): Promise<{
  success: boolean;
  orderId: number;
  message: string;
}> {
  return requestJson<{
    success: boolean;
    orderId: number;
    message: string;
  }>(`${API_URL}/${orderId}`, {
    method: "PUT",
    body: JSON.stringify(request),
  });
}