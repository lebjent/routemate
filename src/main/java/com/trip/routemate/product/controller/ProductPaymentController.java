package com.trip.routemate.product.controller;

import com.trip.routemate.product.dto.ProductPaymentResponse;
import com.trip.routemate.product.service.ProductPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** 개발용 모의 결제 API. 실제 PG 연동 시 게이트웨이 구현으로 교체한다. */
@RestController @RequiredArgsConstructor @RequestMapping("/api/payments")
public class ProductPaymentController {
    private final ProductPaymentService service;
    @PostMapping("/orders/{orderId}/prepare") @ResponseStatus(HttpStatus.CREATED)
    public ProductPaymentResponse prepare(Authentication auth, @PathVariable Long orderId) { return service.prepare(email(auth), orderId); }
    @PostMapping("/{paymentId}/complete")
    public ProductPaymentResponse complete(Authentication auth, @PathVariable Long paymentId, @RequestParam(defaultValue = "true") boolean success) { return service.complete(email(auth), paymentId, success); }
    private String email(Authentication auth) { if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."); return auth.getName(); }
}
