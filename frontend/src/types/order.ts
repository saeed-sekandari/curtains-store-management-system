export type CustomerOption = {
  id: number;
  fullName: string;
  phone: string | null;
  address: string | null;
};

export type InventoryProductOption = {
  id: number;
  productName: string;
  color: string | null;
  availableMeterage: number;
  originalMeterage: number | null;
  productCode: string;
  location: string | null;
};

export type TailorOption = {
  id: number;
  fullName: string;
  role: string;
};

export type OrderFormData = {
  storeCode: string;
  storeName: string;
  customers: CustomerOption[];
  inventoryProducts: InventoryProductOption[];
  tailors: TailorOption[];
};

export type InventoryUsageRequest = {
  productId: number;
  estimatedMeterage: number;
};

export type FabricRequest = {
  fabricName: string;
  color: string;
  lining: boolean;
  notes: string;
};

export type OrderItemRequest = {
  productType: string;
  roomName?: string;
  width?: number;
  height?: number;
  quantity: number;
  design?: string;
  specialNotes?: string;
  tailorId?: number;
  requiredCompletionDate?: string;
  inventoryUsages: InventoryUsageRequest[];
  fabrics: FabricRequest[];
};

export type CreateOrderRequest = {
  storeCode: string;
  orderNumber: string;
  customerId?: number;
  newCustomerName?: string;
  newCustomerPhone?: string;
  newCustomerAddress?: string;
  totalAmount: number;
  initialPaymentAmount?: number;
  initialPaymentMethod?: string;
  items: OrderItemRequest[];
};

export type OrderSummary = {
  id: number;
  orderNumber: string;
  customerName: string;
  totalAmount: number;
  depositAmount: number;
  paidAmount: number;
  remainingAmount: number;
  status: string;
  createdDate: string;
  workStatus: string;
};

export type OrderItemResponse = {
  id: number;
  productType: string;
  roomName: string | null;
  width: number | null;
  height: number | null;
  quantity: number;
  design: string | null;
  specialNotes: string | null;
  workStatus: string;
  requiredCompletionDate: string | null;
  receivedAt: string | null;
  completedAt: string | null;
  assignedTailor: TailorOption | null;
  fabrics: FabricRequest[];
  inventoryUsages: {
    id: number;
    inventoryProductId: number;
    productName: string;
    productCode: string;
    color: string | null;
    estimatedMeterage: number;
    availableMeterage: number;
    inventoryDeducted: boolean;
    inventoryDeductedAt: string | null;
  }[];
};

export type OrdersResponse = {
  storeCode: string;
  storeName: string;
  orders: OrderSummary[];
};

export type OrderDetailsResponse = OrderSummary & {
  items: OrderItemResponse[];
};