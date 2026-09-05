package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.service.AdminOrderService;
import com.trip.routemate.product.dto.ProductOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** 관리자 주문 운영 API. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final AdminOrderService service;

    @GetMapping
    public List<ProductOrderResponse> getOrders() { return service.getOrders(); }

    @PatchMapping("/{orderId}/status")
    public ProductOrderResponse changeStatus(@PathVariable Long orderId, @RequestParam String status) {
        return service.changeStatus(orderId, status);
    }
}
