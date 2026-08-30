package com.trip.routemate.product.dto;

import com.trip.routemate.product.domain.ProductOrder;
import com.trip.routemate.product.domain.ProductOrderItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 옵션 상품 예약의 상품·금액·이용일·상태 정보를 제공하는 응답이다.
 *
 * 일정 연결 후보 조회에도 같은 형식을 사용해 예약 식별자를 전달한다.
 */
@io.swagger.v3.oas.annotations.media.Schema(description = "상품 주문 번호, 구매 상품, 이용일과 주문 상태를 담는 응답 DTO")
public record ProductOrderResponse(
        Long orderId,
        String orderNo,
        Long productId,
        String productName,
        Long optionId,
        String optionName,
        String productImageUrl,
        String destinationName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        String currency,
        LocalDate useDate,
        String buyerName,
        String buyerEmail,
        String buyerPhone,
        String orderStatus,
        String paymentStatus,
        String bookingUrl,
        LocalDateTime createDt,
        List<Item> items
) {
    /** 예약 엔티티를 API 응답 형식으로 변환한다. */
    public static ProductOrderResponse from(ProductOrder order) {
        var product = order.getProduct();
        var option = order.getOption();
        var items = order.getItems().isEmpty()
                ? List.of(Item.fromLegacy(order))
                : order.getItems().stream().map(Item::from).toList();
        var optionSummaryBuilder = new StringBuilder();
        var totalQuantity = 0;
        for (Item item : items) {
            if (!optionSummaryBuilder.isEmpty()) {
                optionSummaryBuilder.append(", ");
            }
            optionSummaryBuilder.append(item.optionName()).append(" ").append(item.quantity()).append("개");
            totalQuantity += item.quantity();
        }
        var optionSummary = optionSummaryBuilder.isEmpty() ? order.getOptionName() : optionSummaryBuilder.toString();
        return new ProductOrderResponse(
                order.getOrderId(), order.getOrderNo(), product == null ? null : product.getProductId(), order.getProductName(),
                option == null ? null : option.getOptionId(), optionSummary, order.getProductImageUrl(),
                order.getDestinationName(), totalQuantity, order.getUnitPrice(), order.getTotalPrice(), order.getCurrency(),
                order.getUseDate(), order.getBuyerName(), order.getBuyerEmail(), order.getBuyerPhone(), order.getOrderStatus(),
                order.getPaymentStatus(), product == null ? null : product.getBookingUrl(), order.getCreateDt(), items
        );
    }

    /** 주문 안에 포함된 옵션별 수량과 금액이다. */
    public record Item(Long optionId, String optionName, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice, String currency) {
        static Item from(ProductOrderItem item) {
            return new Item(item.getOption() == null ? null : item.getOption().getOptionId(), item.getOptionName(), item.getQuantity(), item.getUnitPrice(), item.getTotalPrice(), item.getCurrency());
        }

        static Item fromLegacy(ProductOrder order) {
            return new Item(order.getOption() == null ? null : order.getOption().getOptionId(), order.getOptionName(), order.getQuantity(), order.getUnitPrice(), order.getTotalPrice(), order.getCurrency());
        }
    }
}
