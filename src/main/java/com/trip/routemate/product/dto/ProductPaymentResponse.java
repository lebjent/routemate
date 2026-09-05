package com.trip.routemate.product.dto;

import com.trip.routemate.product.domain.ProductPayment;
import java.math.BigDecimal;

public record ProductPaymentResponse(Long paymentId, String paymentKey, Long orderId, BigDecimal amount, String currency, String status) {
    public static ProductPaymentResponse from(ProductPayment payment) {
        return new ProductPaymentResponse(payment.getPaymentId(), payment.getPaymentKey(), payment.getOrder().getOrderId(), payment.getAmount(), payment.getCurrency(), payment.getStatus());
    }
}
