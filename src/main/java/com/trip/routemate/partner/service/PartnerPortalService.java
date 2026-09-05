package com.trip.routemate.partner.service;

import com.trip.routemate.destination.domain.Destination;
import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.partner.domain.PartnerCompany;
import com.trip.routemate.partner.dto.PartnerDashboardResponse;
import com.trip.routemate.partner.dto.PartnerProductOptionRequest;
import com.trip.routemate.partner.dto.PartnerProductRequest;
import com.trip.routemate.partner.dto.PartnerProductResponse;
import com.trip.routemate.partner.repository.PartnerUserRepository;
import com.trip.routemate.product.domain.TravelProduct;
import com.trip.routemate.product.domain.TravelProductOption;
import com.trip.routemate.product.repository.ProductOrderRepository;
import com.trip.routemate.product.repository.TravelProductOptionRepository;
import com.trip.routemate.product.repository.TravelProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 로그인한 파트너사 범위에서 대시보드와 옵션 상품 관리를 처리한다.
 *
 * 파트너사 식별자는 인증 정보로만 찾고, 상품 식별자만으로는 수정하지 않는다. 이 규칙은
 * 파트너 간 상품 접근을 방지하는 핵심 경계다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerPortalService {
    private final PartnerUserRepository partnerUserRepository;
    private final TravelProductRepository productRepository;
    private final ProductOrderRepository orderRepository;
    private final DestinationRepository destinationRepository;
    private final TravelProductOptionRepository optionRepository;

    /**
     * 현재 파트너사의 판매·상품 상태를 대시보드 형식으로 집계한다.
     *
     * @param authentication 현재 파트너 사용자 인증 정보
     * @return 상품 건수, 주문 건수, 최근 상품을 포함한 대시보드 정보
     */
    public PartnerDashboardResponse dashboard(Authentication authentication) {
        var partner = partner(authentication);
        var products = productRepository.findAllByPartnerOrderByCreateDtDesc(partner);
        return new PartnerDashboardResponse(partner.getPartnerName(), products.size(),
                products.stream().filter(product -> "Y".equals(product.getUseYn())).count(),
                orderRepository.countByProductPartner(partner), orderRepository.getPaidRevenueByPartner(partner),
                products.stream().limit(5)
                        .map(product -> new PartnerDashboardResponse.ProductItem(product.getProductId(), product.getProductName()))
                        .toList());
    }

    /** 현재 파트너사가 소유한 상품과 옵션을 조회한다. */
    public List<PartnerProductResponse> products(Authentication authentication) {
        var partner = partner(authentication);
        var products = productRepository.findAllByPartnerOrderByCreateDtDesc(partner);
        var options = optionRepository.findAllByProductInOrderByProductProductIdAscSortOrderAscOptionIdAsc(products)
                .stream().collect(java.util.stream.Collectors.groupingBy(option -> option.getProduct().getProductId()));
        return products.stream()
                .map(product -> PartnerProductResponse.from(product, options.getOrDefault(product.getProductId(), List.of())))
                .toList();
    }

    /** 상품 등록 화면에서 사용할 여행지 선택 목록을 조회한다. */
    public List<PlaceItem> places(Authentication authentication) {
        partner(authentication);
        return destinationRepository.findAllByOrderByDestNameAsc().stream()
                .map(destination -> new PlaceItem(destination.getDestId(), destination.getDestName(),
                        destination.getCountry().getCountryName(), destination.getRegion().getRegionName()))
                .toList();
    }

    @Transactional
    /**
     * 파트너사 명의의 상품과 옵션을 등록하고 심사 대기 상태로 저장한다.
     *
     * @param authentication 현재 파트너 사용자 인증 정보
     * @param request 상품 기본 정보와 판매 옵션
     * @return 생성된 상품 정보
     */
    public PartnerProductResponse create(Authentication authentication, PartnerProductRequest request) {
        var partner = partner(authentication);
        var product = Objects.requireNonNull(TravelProduct.builder()
                .destination(destination(request.destinationId()))
                .partner(partner)
                .productName(normalize(request.productName()))
                .productSummary(nullable(request.productSummary()))
                .productType(normalize(request.productType()).toUpperCase())
                .providerName(partner.getPartnerName())
                .registrationSource("PARTNER")
                .approvalStatus("PENDING")
                .submitDt(LocalDateTime.now())
                .productDesc(nullable(request.productDesc()))
                .imageUrl(nullable(request.imageUrl()))
                .detailImageUrl(nullable(request.detailImageUrl()))
                .courseText(nullable(request.courseText()))
                .includedText(nullable(request.includedText()))
                .excludedText(nullable(request.excludedText()))
                .usageGuideText(nullable(request.usageGuideText()))
                .noticeText(nullable(request.noticeText()))
                .cancellationPolicyText(nullable(request.cancellationPolicyText()))
                .faqText(nullable(request.faqText()))
                .meetingTime(nullable(request.meetingTime()))
                .meetingPlace(nullable(request.meetingPlace()))
                .bookingUrl(nullable(request.bookingUrl()))
                .price(request.price())
                .currency(currency(request.currency()))
                .useYn(useYn(request.useYn()))
                .sortOrder(sortOrder(request.sortOrder()))
                .build());
        productRepository.save(product);
        saveOptions(product, request.options());
        return response(product);
    }

    @Transactional
    /**
     * 현재 파트너사가 소유한 상품의 정보와 옵션 구성을 수정한다.
     *
     * @throws ResponseStatusException 상품이 없거나 다른 파트너사 상품일 때
     */
    public PartnerProductResponse update(Authentication authentication, Long productId, PartnerProductRequest request) {
        var partner = partner(authentication);
        var product = productRepository.findWithDestinationByProductId(productId)
                .filter(candidate -> candidate.getPartner() != null
                        && candidate.getPartner().getPartnerId().equals(partner.getPartnerId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "관리할 수 있는 옵션상품을 찾을 수 없습니다."));
        product.update(destination(request.destinationId()), partner, normalize(request.productName()), nullable(request.productSummary()),
                normalize(request.productType()).toUpperCase(), partner.getPartnerName(), nullable(request.productDesc()),
                nullable(request.imageUrl()), nullable(request.detailImageUrl()), nullable(request.courseText()),
                nullable(request.includedText()), nullable(request.excludedText()), nullable(request.usageGuideText()),
                nullable(request.noticeText()), nullable(request.cancellationPolicyText()), nullable(request.faqText()),
                nullable(request.meetingTime()), nullable(request.meetingPlace()), nullable(request.bookingUrl()), request.price(),
                currency(request.currency()), useYn(request.useYn()), sortOrder(request.sortOrder()));
        saveOptions(product, request.options());
        return response(product);
    }

    /** 기존 옵션을 교체하고 요청 순서대로 새 옵션을 저장한다. */
    private void saveOptions(TravelProduct product, List<PartnerProductOptionRequest> requests) {
        if (requests == null) return;
        var existing = optionRepository.findAllByProductOrderBySortOrderAscOptionIdAsc(product).stream()
                .collect(java.util.stream.Collectors.toMap(TravelProductOption::getOptionId, java.util.function.Function.identity()));
        var retained = new java.util.HashSet<Long>();
        for (var request : requests) {
            var option = request.optionId() == null ? null : existing.get(request.optionId());
            if (request.optionId() != null && option == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상품에 속하지 않은 옵션입니다.");
            }
            if (option == null) option = TravelProductOption.builder().product(product).build();
            option.update(normalize(request.optionName()), nullable(request.optionDesc()), request.price(), currency(request.currency()),
                    nullable(request.cancellationPolicy()), nullable(request.validityText()), normalize(request.confirmationType()).toUpperCase(),
                    useYn(request.useYn()), sortOrder(request.sortOrder()));
            if (option.getOptionId() == null) optionRepository.save(option); else retained.add(option.getOptionId());
        }
        existing.values().stream().filter(option -> !retained.contains(option.getOptionId())).forEach(TravelProductOption::deactivate);
    }

    /** 상품 엔티티와 옵션 목록을 포털 응답으로 변환한다. */
    private PartnerProductResponse response(TravelProduct product) {
        return PartnerProductResponse.from(product,
                optionRepository.findAllByProductOrderBySortOrderAscOptionIdAsc(product));
    }

    /** 등록 요청의 여행지 식별자가 실제 존재하는지 확인한다. */
    private Destination destination(Long destinationId) {
        return destinationRepository.findWithCountryAndRegionByDestId(destinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "여행지를 찾을 수 없습니다."));
    }

    /** 현재 인증 사용자의 활성 파트너사 소속을 조회한다. */
    private PartnerCompany partner(Authentication authentication) {
        if (authentication == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        return partnerUserRepository.findByUserUserEmailAndUseYn(authentication.getName(), "Y")
                .filter(partnerUser -> "ACTIVE".equals(partnerUser.getPartner().getPartnerStatus()))
                .map(partnerUser -> partnerUser.getPartner())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "파트너 계정이 아닙니다."));
    }

    /** null 문자열을 빈 문자열로 바꾸고 앞뒤 공백을 제거한다. */
    private String normalize(String value) { return value == null ? "" : value.trim(); }
    /** 공백 문자열을 데이터베이스 null로 저장할 수 있도록 정규화한다. */
    private String nullable(String value) { var normalized = normalize(value); return normalized.isBlank() ? null : normalized; }
    /** 통화 코드가 없으면 기본 통화인 KRW를 사용한다. */
    private String currency(String value) { var normalized = normalize(value).toUpperCase(); return normalized.isBlank() ? "KRW" : normalized; }
    /** 명시적 미사용 값만 N으로 저장하고 나머지는 Y로 저장한다. */
    private String useYn(String value) { return "N".equalsIgnoreCase(value) ? "N" : "Y"; }
    /** 정렬 순서가 없거나 유효하지 않으면 1로 보정한다. */
    private int sortOrder(Integer value) { return value == null || value < 1 ? 1 : value; }

    /** 상품 등록 화면의 여행지 선택 항목이다.
     * @param destinationId 여행지 식별자
     * @param destName 여행지명
     * @param countryName 국가명
     * @param regionName 지역명
     */
    public record PlaceItem(Long destinationId, String destName, String countryName, String regionName) { }
}
