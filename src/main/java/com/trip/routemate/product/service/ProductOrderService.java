package com.trip.routemate.product.service;

import com.trip.routemate.product.domain.ProductOrder;
import com.trip.routemate.product.domain.ProductOrderItem;
import com.trip.routemate.product.domain.TravelProduct;
import com.trip.routemate.product.dto.ProductOrderRequest;
import com.trip.routemate.product.dto.ProductOrderResponse;
import com.trip.routemate.product.repository.ProductOrderRepository;
import com.trip.routemate.product.repository.TravelProductOptionRepository;
import com.trip.routemate.product.repository.TravelProductRepository;
import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.repository.UserMstrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * 옵션 상품 예약의 생성과 사용자별 조회를 처리한다.
 *
 * 예약은 활성 사용자와 판매 가능한 옵션을 확인한 뒤 저장한다. 일정 연결 후보를 조회할 때는
 * 예약 상태, 이용일, 국가·지역이 일정 조건과 모두 일치해야 한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOrderService {
    private final ProductOrderRepository orderRepository;
    private final TravelProductRepository productRepository;
    private final TravelProductOptionRepository optionRepository;
    private final UserMstrRepository userRepository;

    /**
     * 판매 가능한 옵션에 대해 로그인 사용자 명의의 예약을 생성한다.
     *
     * @param userEmail 예약 소유자의 로그인 이메일
     * @param request 옵션, 이용일, 예약자, 수량 정보
     * @return 생성된 예약 정보
     */
    @Transactional
    public ProductOrderResponse createOrder(String userEmail, ProductOrderRequest request) {
        var user = getActiveUser(userEmail);
        var product = productRepository.findWithDestinationByProductId(request.productId())
                .filter(found -> "Y".equals(found.getUseYn()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "판매 중인 옵션상품을 찾을 수 없습니다."));
        var selections = resolveSelections(product, request.items());
        var primarySelection = selections.getFirst();
        var totalPrice = BigDecimal.ZERO;
        var totalQuantity = 0;
        for (var selection : selections) {
            totalPrice = totalPrice.add(selection.totalPrice());
            totalQuantity += selection.quantity();
        }
        var destination = product.getDestination();
        var destinationName = String.join(" · ", destination.getCountry().getCountryName(),
                destination.getRegion().getRegionName(), destination.getDestName());

        var order = Objects.requireNonNull(ProductOrder.builder()
                .orderNo("RM" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT))
                .user(user)
                .product(product)
                .option(primarySelection.option())
                .productName(product.getProductName())
                .optionName(primarySelection.option().getOptionName())
                .productImageUrl(product.getImageUrl())
                .destinationName(destinationName)
                .quantity(totalQuantity)
                .unitPrice(primarySelection.option().getPrice())
                .totalPrice(totalPrice)
                .currency(primarySelection.option().getCurrency())
                .useDate(request.useDate())
                .buyerName(request.buyerName().trim())
                .buyerEmail(request.buyerEmail().trim().toLowerCase(Locale.ROOT))
                .buyerPhone(normalizeNullable(request.buyerPhone()))
                .build());
        for (var selection : selections) {
            order.addItem(ProductOrderItem.builder()
                    .order(order)
                    .option(selection.option())
                    .optionName(selection.option().getOptionName())
                    .quantity(selection.quantity())
                    .unitPrice(selection.option().getPrice())
                    .totalPrice(selection.totalPrice())
                    .currency(selection.option().getCurrency())
                    .build());
        }
        var savedOrder = orderRepository.save(order);
        return ProductOrderResponse.from(savedOrder);
    }

    /** 현재 사용자의 예약 내역을 최신순으로 조회한다. */
    public List<ProductOrderResponse> getMyOrders(String userEmail) {
        var user = getActiveUser(userEmail);
        return orderRepository.findAllByUserOrderByCreateDtDescOrderIdDesc(user).stream()
                .map(ProductOrderResponse::from)
                .toList();
    }

    /**
     * 특정 여행 일차에 연결할 수 있는 사용자의 유효 예약을 조회한다.
     *
     * @param userEmail 예약 소유자의 로그인 이메일
     * @param countryCode 일정의 국가 코드
     * @param regionCode 일정의 지역 코드
     * @param useDate 일정 날짜
     * @return 여행지와 이용일이 일치하는 예약 목록
     */
    public List<ProductOrderResponse> getMyScheduleCandidates(String userEmail, String countryCode, String regionCode, LocalDate useDate) {
        var user = getActiveUser(userEmail);
        if (countryCode == null || countryCode.isBlank() || regionCode == null || regionCode.isBlank() || useDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "일정 날짜와 여행지 정보를 확인해 주세요.");
        }
        return orderRepository.findScheduleCandidatesByUserAndDestination(user, countryCode.trim(), regionCode.trim(), useDate).stream()
                .map(ProductOrderResponse::from)
                .toList();
    }

    /** 로그인 이메일로 활성·미탈퇴 사용자를 조회한다. */
    private UserMstr getActiveUser(String userEmail) {
        return userRepository.findByUserEmail(userEmail)
                .filter(user -> "N".equals(user.getDelYn()) && "ACTIVE".equals(user.getUserStatCd()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "활성 회원 로그인이 필요합니다."));
    }

    /** 공백 문자열을 null로 정규화해 선택 입력값을 저장한다. */
    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    /** 요청의 같은 옵션 수량을 합산하고 판매 가능한 옵션·통화를 검증한다. */
    private List<Selection> resolveSelections(TravelProduct product, List<ProductOrderRequest.Item> items) {
        var quantities = new LinkedHashMap<Long, Integer>();
        for (var item : items) {
            var optionId = item.optionId();
            var previousQuantity = quantities.get(optionId);
            var mergedQuantity = (previousQuantity == null ? 0 : previousQuantity) + item.quantity();
            if (mergedQuantity > 10) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "옵션별 구매 수량은 10개까지 가능합니다.");
            }
            quantities.put(optionId, mergedQuantity);
        }
        var selections = new ArrayList<Selection>();
        for (var entry : quantities.entrySet()) {
            var option = optionRepository.findByOptionIdAndProductAndUseYn(entry.getKey(), product, "Y")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 상품에서 판매 중인 옵션이 아닙니다."));
            selections.add(new Selection(option, entry.getValue(), option.getPrice().multiply(BigDecimal.valueOf(entry.getValue()))));
        }
        var currency = selections.getFirst().option().getCurrency();
        for (var selection : selections) {
            if (!currency.equals(selection.option().getCurrency())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "서로 다른 통화의 옵션은 함께 주문할 수 없습니다.");
            }
        }
        return selections;
    }

    /** 서버에서 검증한 옵션과 수량·주문 시점 총액을 묶는다. */
    private record Selection(com.trip.routemate.product.domain.TravelProductOption option, int quantity, BigDecimal totalPrice) {
    }

}
