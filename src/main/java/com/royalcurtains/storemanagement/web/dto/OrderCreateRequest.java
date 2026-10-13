package com.royalcurtains.storemanagement.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderCreateRequest {

    private String storeCode;
    private String orderNumber;

    private Long customerId;
    private String newCustomerName;
    private String newCustomerPhone;
    private String newCustomerAddress;

    private BigDecimal totalAmount;
    private BigDecimal initialPaymentAmount;
    private String initialPaymentMethod;

    private List<ItemRequest> items = new ArrayList<>();

    public OrderCreateRequest() {
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getNewCustomerName() {
        return newCustomerName;
    }

    public void setNewCustomerName(String newCustomerName) {
        this.newCustomerName = newCustomerName;
    }

    public String getNewCustomerPhone() {
        return newCustomerPhone;
    }

    public void setNewCustomerPhone(String newCustomerPhone) {
        this.newCustomerPhone = newCustomerPhone;
    }

    public String getNewCustomerAddress() {
        return newCustomerAddress;
    }

    public void setNewCustomerAddress(String newCustomerAddress) {
        this.newCustomerAddress = newCustomerAddress;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getInitialPaymentAmount() {
        return initialPaymentAmount;
    }

    public void setInitialPaymentAmount(
            BigDecimal initialPaymentAmount) {
        this.initialPaymentAmount = initialPaymentAmount;
    }

    public String getInitialPaymentMethod() {
        return initialPaymentMethod;
    }

    public void setInitialPaymentMethod(
            String initialPaymentMethod) {
        this.initialPaymentMethod = initialPaymentMethod;
    }

    public List<ItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemRequest> items) {
        this.items = items;
    }

    public static class ItemRequest {

        private String productType;
        private String roomName;
        private BigDecimal width;
        private BigDecimal height;
        private Integer quantity;
        private String design;
        private String specialNotes;
        private Long tailorId;
        private LocalDate requiredCompletionDate;

        private List<InventoryUsageRequest> inventoryUsages =
                new ArrayList<>();

        private List<FabricRequest> fabrics =
                new ArrayList<>();

        public ItemRequest() {
        }

        public String getProductType() {
            return productType;
        }

        public void setProductType(String productType) {
            this.productType = productType;
        }

        public String getRoomName() {
            return roomName;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public BigDecimal getWidth() {
            return width;
        }

        public void setWidth(BigDecimal width) {
            this.width = width;
        }

        public BigDecimal getHeight() {
            return height;
        }

        public void setHeight(BigDecimal height) {
            this.height = height;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getDesign() {
            return design;
        }

        public void setDesign(String design) {
            this.design = design;
        }

        public String getSpecialNotes() {
            return specialNotes;
        }

        public void setSpecialNotes(String specialNotes) {
            this.specialNotes = specialNotes;
        }

        public Long getTailorId() {
            return tailorId;
        }

        public void setTailorId(Long tailorId) {
            this.tailorId = tailorId;
        }

        public LocalDate getRequiredCompletionDate() {
            return requiredCompletionDate;
        }

        public void setRequiredCompletionDate(
                LocalDate requiredCompletionDate) {
            this.requiredCompletionDate =
                    requiredCompletionDate;
        }

        public List<InventoryUsageRequest>
        getInventoryUsages() {
            return inventoryUsages;
        }

        public void setInventoryUsages(
                List<InventoryUsageRequest> inventoryUsages) {
            this.inventoryUsages = inventoryUsages;
        }

        public List<FabricRequest> getFabrics() {
            return fabrics;
        }

        public void setFabrics(
                List<FabricRequest> fabrics) {
            this.fabrics = fabrics;
        }
    }

    public static class InventoryUsageRequest {

        private Long productId;
        private BigDecimal estimatedMeterage;

        public InventoryUsageRequest() {
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public BigDecimal getEstimatedMeterage() {
            return estimatedMeterage;
        }

        public void setEstimatedMeterage(
                BigDecimal estimatedMeterage) {
            this.estimatedMeterage =
                    estimatedMeterage;
        }
    }

    public static class FabricRequest {

        private String fabricName;
        private String color;
        private boolean lining;
        private String notes;

        public FabricRequest() {
        }

        public String getFabricName() {
            return fabricName;
        }

        public void setFabricName(String fabricName) {
            this.fabricName = fabricName;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public boolean isLining() {
            return lining;
        }

        public void setLining(boolean lining) {
            this.lining = lining;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}