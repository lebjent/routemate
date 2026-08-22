package com.trip.routemate.product.controller;

import com.trip.routemate.product.dto.ProductOrderRequest;
import com.trip.routemate.product.dto.ProductOrderResponse;
import com.trip.routemate.product.service.ProductOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-orders")
public class ProductOrderController {
    private final ProductOrderService productOrderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductOrderResponse createOrder(Authentication authentication, @Valid @RequestBody ProductOrderRequest request) {
        return productOrderService.createOrder(resolveUserEmail(authentication), request);
    }

    @GetMapping("/my")
    public List<ProductOrderResponse> getMyOrders(Authentication authentication) {
        return productOrderService.getMyOrders(resolveUserEmail(authentication));
    }

    private String resolveUserEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return authentication.getName();
    }
}
