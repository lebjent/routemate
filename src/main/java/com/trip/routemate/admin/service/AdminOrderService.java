package com.trip.routemate.admin.service;

import com.trip.routemate.product.dto.ProductOrderResponse;
import com.trip.routemate.product.repository.ProductOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

/** 관리자 주문 조회와 안전한 주문 상태 전이를 담당한다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {
    private static final java.util.Map<String, java.util.Set<String>> ALLOWED = java.util.Map.of(
            "ORDERED", java.util.Set.of("CONFIRMED", "CANCELLED"),
            "CONFIRMED", java.util.Set.of("COMPLETED", "CANCELLED"),
            "COMPLETED", java.util.Set.of(), "CANCELLED", java.util.Set.of());
    private final ProductOrderRepository repository;

    @PreAuthorize("hasAuthority('PARTNER_MANAGE')")
    public List<ProductOrderResponse> getOrders() {
        return repository.findAllByOrderByCreateDtDescOrderIdDesc().stream().map(ProductOrderResponse::from).toList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('PARTNER_MANAGE')")
    public ProductOrderResponse changeStatus(Long orderId, String nextStatus) {
        var order = repository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));
        var next = nextStatus == null ? "" : nextStatus.trim().toUpperCase();
        if (!ALLOWED.getOrDefault(order.getOrderStatus(), java.util.Set.of()).contains(next)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "허용되지 않은 주문 상태 변경입니다.");
        }
        var payment = "CANCELLED".equals(next) ? ("PAID".equals(order.getPaymentStatus()) ? "REFUNDED" : "FAILED") : order.getPaymentStatus();
        order.changeStatus(next, payment);
        return ProductOrderResponse.from(order);
    }
}
