export type InventoryProduct = {
  id: number;
  productName: string;
  color: string;
  originalMeterage: number;
  availableMeterage: number;
  location: string;
  productCode: string;
  storeCode: string;
  storeName: string;
  addedBy: string;
  addedAt: string;
  lastEditedBy: string;
  lastEditedAt: string;
  active: boolean;
};

export type InventoryMovement = {
  id: number;
  movementType: string;
  quantity: number;
  reason: string;
  createdAt: string;
  recordedBy: string;
};

export type InventoryProductRequest = {
  storeCode: string;
  productName: string;
  color: string;
  meterage: number;
  location: string;
  productCode: string;
};