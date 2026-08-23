package com.trip.routemate.partner.service;

import com.trip.routemate.admin.dto.AdminProductOptionRequest;
import com.trip.routemate.admin.dto.AdminProductRequest;
import com.trip.routemate.admin.dto.AdminProductResponse;
import com.trip.routemate.destination.domain.Destination;
import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.partner.domain.PartnerCompany;
import com.trip.routemate.partner.dto.PartnerDashboardResponse;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerPortalService {
    private final PartnerUserRepository partnerUserRepository;
    private final TravelProductRepository productRepository;
    private final ProductOrderRepository orderRepository;
    private final DestinationRepository destinationRepository;
    private final TravelProductOptionRepository optionRepository;

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

    public List<AdminProductResponse.Item> products(Authentication authentication) {
        var partner = partner(authentication);
        return productRepository.findAllByPartnerOrderByCreateDtDesc(partner).stream()
                .map(this::response)
                .toList();
    }

    public List<PlaceItem> places(Authentication authentication) {
        partner(authentication);
        return destinationRepository.findAllByOrderByDestNameAsc().stream()
                .map(destination -> new PlaceItem(destination.getDestId(), destination.getDestName(),
                        destination.getCountry().getCountryName(), destination.getRegion().getRegionName()))
                .toList();
    }

    @Transactional
    public AdminProductResponse.Item create(Authentication authentication, AdminProductRequest request) {
        var partner = partner(authentication);
        var product = TravelProduct.builder()
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
                .build();
        productRepository.save(product);
        saveOptions(product, request.options());
        return response(product);
    }

    @Transactional
    public AdminProductResponse.Item update(Authentication authentication, Long productId, AdminProductRequest request) {
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

    private void saveOptions(TravelProduct product, List<AdminProductOptionRequest> requests) {
        optionRepository.deleteAllByProduct(product);
        if (requests == null) return;
        optionRepository.saveAll(requests.stream().map(request -> TravelProductOption.builder()
                .product(product).optionName(normalize(request.optionName())).optionDesc(nullable(request.optionDesc()))
                .price(request.price()).currency(currency(request.currency()))
                .cancellationPolicy(nullable(request.cancellationPolicy())).validityText(nullable(request.validityText()))
                .confirmationType(normalize(request.confirmationType()).toUpperCase())
                .useYn(useYn(request.useYn())).sortOrder(sortOrder(request.sortOrder())).build()).toList());
    }

    private AdminProductResponse.Item response(TravelProduct product) {
        return AdminProductResponse.Item.from(product,
                optionRepository.findAllByProductOrderBySortOrderAscOptionIdAsc(product));
    }

    private Destination destination(Long destinationId) {
        return destinationRepository.findWithCountryAndRegionByDestId(destinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "여행지를 찾을 수 없습니다."));
    }

    private PartnerCompany partner(Authentication authentication) {
        if (authentication == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        return partnerUserRepository.findByUserUserEmailAndUseYn(authentication.getName(), "Y")
                .filter(partnerUser -> "ACTIVE".equals(partnerUser.getPartner().getPartnerStatus()))
                .map(partnerUser -> partnerUser.getPartner())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "파트너 계정이 아닙니다."));
    }

    private String normalize(String value) { return value == null ? "" : value.trim(); }
    private String nullable(String value) { var normalized = normalize(value); return normalized.isBlank() ? null : normalized; }
    private String currency(String value) { var normalized = normalize(value).toUpperCase(); return normalized.isBlank() ? "KRW" : normalized; }
    private String useYn(String value) { return "N".equalsIgnoreCase(value) ? "N" : "Y"; }
    private int sortOrder(Integer value) { return value == null || value < 1 ? 1 : value; }

    public record PlaceItem(Long destinationId, String destName, String countryName, String regionName) { }
}
