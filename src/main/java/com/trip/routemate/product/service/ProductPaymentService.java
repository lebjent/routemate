package com.trip.routemate.product.service;

import com.trip.routemate.product.domain.ProductPayment;
import com.trip.routemate.product.dto.ProductPaymentResponse;
import com.trip.routemate.product.repository.ProductOrderRepository;
import com.trip.routemate.product.repository.ProductPaymentRepository;
import com.trip.routemate.user.repository.UserMstrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;
import java.util.UUID;

/** 결제 준비와 개발용 결제 성공·실패 처리를 담당한다. */
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ProductPaymentService {
    private final ProductOrderRepository orders;
    private final ProductPaymentRepository payments;
    private final UserMstrRepository users;

    @Transactional
    public ProductPaymentResponse prepare(String email, Long orderId) {
        var user = users.findByUserEmail(email).orElseThrow(() -> unauthorized());
        var order = orders.findById(orderId).filter(found -> found.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));
        if (!"ORDERED".equals(order.getOrderStatus()) || !"PENDING".equals(order.getPaymentStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "결제할 수 없는 주문 상태입니다.");
        var existing = payments.findByOrder(order);
        if (existing.isPresent()) return ProductPaymentResponse.from(existing.get());
        var payment = payments.save(ProductPayment.builder().order(order).paymentKey("MOCK_" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT)).amount(order.getTotalPrice()).currency(order.getCurrency()).build());
        return ProductPaymentResponse.from(payment);
    }

    @Transactional
    public ProductPaymentResponse complete(String email, Long paymentId, boolean success) {
        var payment = payments.findById(paymentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "결제 요청을 찾을 수 없습니다."));
        if (!payment.getOrder().getUser().getUserEmail().equalsIgnoreCase(email)) throw unauthorized();
        if (!"READY".equals(payment.getStatus())) return ProductPaymentResponse.from(payment);
        if (success) { payment.complete(); payment.getOrder().changeStatus("CONFIRMED", "PAID"); }
        else { payment.fail(); payment.getOrder().changeStatus("ORDERED", "FAILED"); }
        return ProductPaymentResponse.from(payment);
    }

    private ResponseStatusException unauthorized() { return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."); }
}
