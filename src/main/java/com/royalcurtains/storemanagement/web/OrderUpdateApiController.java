package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.Order;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import com.royalcurtains.storemanagement.service.OrderUpdateService;
import com.royalcurtains.storemanagement.web.dto.OrderUpdateRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderUpdateApiController {

    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;
    private final OrderUpdateService orderUpdateService;

    public OrderUpdateApiController(
            StoreRepository storeRepository,
            StoreAccessService storeAccessService,
            OrderUpdateService orderUpdateService) {

        this.storeRepository = storeRepository;
        this.storeAccessService = storeAccessService;
        this.orderUpdateService = orderUpdateService;
    }

    @PutMapping("/{orderId}")
    public Map<String, Object> updateOrder(
            @PathVariable Long orderId,
            @RequestBody OrderUpdateRequest request,
            Principal principal) {

        if (request.getStoreCode() == null
                || request.getStoreCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Store code is required"
            );
        }

        Store store = storeRepository
                .findByCode(
                        request.getStoreCode()
                                .trim()
                                .toLowerCase()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Store not found"
                        )
                );

        storeAccessService.checkStoreAccess(
                principal,
                store.getCode()
        );

        Order updatedOrder =
                orderUpdateService.updateOrder(
                        orderId,
                        request,
                        store
                );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("success", true);
        response.put("orderId", updatedOrder.getId());
        response.put(
                "message",
                "Order updated successfully"
        );

        return response;
    }
}