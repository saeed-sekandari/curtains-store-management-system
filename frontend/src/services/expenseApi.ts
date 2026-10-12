import type { ExpenseFormData } from "../types/expense";

const API_BASE_URL = "http://localhost:8080/api";

export async function createExpense(
  data: ExpenseFormData
): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/expenses`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify({
      storeCode: data.storeCode,
      category: data.category,
      recipientName: data.recipientName,
      amount: data.amount,
      currency: data.currency,
      expenseDate: data.expenseDate,
      description: data.description,
    }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(
      errorText || `Expense save failed: ${response.status}`
    );
  }
}