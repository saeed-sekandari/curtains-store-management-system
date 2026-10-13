package com.royalcurtains.storemanagement.web.dto;

import java.time.LocalDate;

public class OrderUpdateRequest {

    private String storeCode;
    private LocalDate completionDate;
    private Long tailorId;
    private String details;

    public OrderUpdateRequest() {
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public Long getTailorId() {
        return tailorId;
    }

    public void setTailorId(Long tailorId) {
        this.tailorId = tailorId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}