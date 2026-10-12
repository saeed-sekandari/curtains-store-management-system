import type {
  ExpenseFormData,
} from "../types/expense";

async function request<T>(
  url: string,
  options?: RequestInit
): Promise<T> {
  const response = await fetch(url, {
    credentials: "include",
    ...options,
  });

  if (!response.ok) {
    throw new Error("Expense request failed");
  }

  const contentType =
    response.headers.get("content-type");

  if (contentType?.includes("application/json")) {
    return response.json();
  }

  return undefined as T;
}

export function createExpense(
  data: ExpenseFormData
): Promise<void> {
  return request<void>("/api/expenses", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      storeCode: data.storeCode,
      category: data.category,
      recipientName: data.recipientName,
      amount: data.amount,
      currency: data.currency,
      expenseDateTime: data.expenseDate,
      description: data.description,
    }),
  });
}