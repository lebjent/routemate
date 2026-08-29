package com.trip.routemate.admin.service;

import com.trip.routemate.admin.dto.AdminProductRequest;
import com.trip.routemate.admin.dto.AdminProductResponse;
import com.trip.routemate.admin.dto.AdminProductApprovalRequest;
import com.trip.routemate.admin.dto.AdminProductApprovalHistoryResponse;
import com.trip.routemate.destination.domain.Destination;
import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.product.domain.TravelProduct;
import com.trip.routemate.partner.domain.PartnerCompany;
import com.trip.routemate.partner.repository.PartnerCompanyRepository;
import com.trip.routemate.product.repository.TravelProductRepository;
import com.trip.routemate.product.repository.TravelProductOptionRepository;
import com.trip.routemate.product.repository.ProductApprovalHistoryRepository;
import com.trip.routemate.product.domain.TravelProductOption;
import com.trip.routemate.product.domain.ProductApprovalHistory;
import com.trip.routemate.user.repository.UserMstrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/** 관리자 상품 목록과 상품 승인 이력을 관리한다. */
public class AdminProductService {
    private final TravelProductRepository productRepository;
    private final DestinationRepository destinationRepository;
    private final TravelProductOptionRepository optionRepository;
    private final PartnerCompanyRepository partnerRepository;
    private final ProductApprovalHistoryRepository approvalHistoryRepository;
    private final UserMstrRepository userRepository;

    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    /** 여행지와 판매 상태 조건으로 전체 상품 목록을 조회한다. */
    public AdminProductResponse getProducts(Long destinationId, String useYn) {
        var status = normalizeStatus(useYn);
        var products = productRepository.findAllByOrderBySortOrderAscCreateDtDesc().stream()
                .filter(product -> destinationId == null || product.getDestination().getDestId().equals(destinationId))
                .filter(product -> "ALL".equals(status) || status.equals(product.getUseYn()))
                .map(product -> AdminProductResponse.Item.from(product, optionRepository.findAllByProductOrderBySortOrderAscOptionIdAsc(product)))
                .toList();
        return new AdminProductResponse(products);
    }

    @Transactional
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    /** 관리자 등록 상품을 생성하고 옵션 정보를 함께 저장한다. */
    public AdminProductResponse.Item create(AdminProductRequest request) {
        var destination = getDestination(request.destinationId());
        var partner = getPartner(request.partnerId());
        var product = productRepository.save(Objects.requireNonNull(TravelProduct.builder()
                .destination(destination).partner(partner)
                .productName(normalize(request.productName()))
                .productSummary(normalizeNullable(request.productSummary()))
                .productType(normalize(request.productType()).toUpperCase())
                .providerName(partner == null ? normalizeNullable(request.providerName()) : partner.getPartnerName())
                .registrationSource("ADMIN").approvalStatus("APPROVED").approveDt(java.time.LocalDateTime.now())
                .productDesc(normalizeNullable(request.productDesc()))
                .imageUrl(normalizeNullable(request.imageUrl()))
                .detailImageUrl(normalizeNullable(request.detailImageUrl()))
                .courseText(normalizeNullable(request.courseText()))
                .includedText(normalizeNullable(request.includedText()))
                .excludedText(normalizeNullable(request.excludedText()))
                .usageGuideText(normalizeNullable(request.usageGuideText()))
                .noticeText(normalizeNullable(request.noticeText()))
                .cancellationPolicyText(normalizeNullable(request.cancellationPolicyText()))
                .faqText(normalizeNullable(request.faqText()))
                .meetingTime(normalizeNullable(request.meetingTime()))
                .meetingPlace(normalizeNullable(request.meetingPlace()))
                .bookingUrl(normalizeNullable(request.bookingUrl()))
                .price(request.price())
                .currency(normalizeCurrency(request.currency()))
                .useYn(normalizeUseYn(request.useYn()))
                .sortOrder(normalizeSortOrder(request.sortOrder()))
                .build()));
        saveOptions(product, request.options());
        return AdminProductResponse.Item.from(product, optionRepository.findAllByProductOrderBySortOrderAscOptionIdAsc(product));
    }

    @Transactional
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    /** 상품 정보와 옵션 구성을 수정한다. */
    public AdminProductResponse.Item update(Long productId, AdminProductRequest request) {
        var product = productRepository.findWithDestinationByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "옵션상품을 찾을 수 없습니다."));
        var destination = getDestination(request.destinationId());
        var partner = getPartner(request.partnerId());
        product.update(destination, partner, normalize(request.productName()), normalizeNullable(request.productSummary()), normalize(request.productType()).toUpperCase(),
                partner == null ? normalizeNullable(request.providerName()) : partner.getPartnerName(), normalizeNullable(request.productDesc()), normalizeNullable(request.imageUrl()),
                normalizeNullable(request.detailImageUrl()), normalizeNullable(request.courseText()), normalizeNullable(request.includedText()),
                normalizeNullable(request.excludedText()), normalizeNullable(request.usageGuideText()), normalizeNullable(request.noticeText()),
                normalizeNullable(request.cancellationPolicyText()), normalizeNullable(request.faqText()), normalizeNullable(request.meetingTime()),
                normalizeNullable(request.meetingPlace()), normalizeNullable(request.bookingUrl()), request.price(), normalizeCurrency(request.currency()),
                normalizeUseYn(request.useYn()), normalizeSortOrder(request.sortOrder()));
        saveOptions(product, request.options());
        return AdminProductResponse.Item.from(product, optionRepository.findAllByProductOrderBySortOrderAscOptionIdAsc(product));
    }

    @PreAuthorize("hasAuthority('PARTNER_MANAGE')")
    /** 심사 상태별로 파트너 등록 상품을 조회한다. */
    public AdminProductResponse getApprovalProducts(String status) {
        var approvalStatus = normalizeApprovalStatus(status);
        var products = productRepository.findAllByOrderBySortOrderAscCreateDtDesc().stream()
                .filter(product -> "ALL".equals(approvalStatus) || approvalStatus.equals(product.getApprovalStatus()))
                .filter(product -> product.getPartner() != null)
                .map(product -> AdminProductResponse.Item.from(product, optionRepository.findAllByProductOrderBySortOrderAscOptionIdAsc(product)))
                .toList();
        return new AdminProductResponse(products);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PARTNER_MANAGE')")
    /** 승인·거절·보류 결과와 사유를 상품 및 승인 이력에 함께 기록한다. */
    public AdminProductResponse.Item review(Long productId, AdminProductApprovalRequest request, String approverEmail) {
        var product = productRepository.findWithDestinationByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "옵션상품을 찾을 수 없습니다."));
        if (product.getPartner() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파트너사 등록 상품만 승인 처리할 수 있습니다.");
        var status = normalizeDecisionStatus(request.decisionStatus());
        var reason = normalizeNullable(request.reason());
        if (!"APPROVED".equals(status) && reason == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "거절 또는 보류 사유를 입력하세요.");
        }
        var approver = userRepository.findByUserEmail(approverEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "승인 담당자 정보를 찾을 수 없습니다."));
        product.review(status, reason, "APPROVED".equals(status) ? java.time.LocalDateTime.now() : null);
        approvalHistoryRepository.save(Objects.requireNonNull(ProductApprovalHistory.builder()
                .product(product).decisionStatus(status).decisionReason(reason).approver(approver).build()));
        return AdminProductResponse.Item.from(product, optionRepository.findAllByProductOrderBySortOrderAscOptionIdAsc(product));
    }

    @PreAuthorize("hasAuthority('PARTNER_MANAGE')")
    /** 상품의 심사 이력을 최신 결정 순으로 조회한다. */
    public java.util.List<AdminProductApprovalHistoryResponse> approvalHistory(Long productId) {
        var product = productRepository.findById(Objects.requireNonNull(productId, "상품 ID가 필요합니다."))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "옵션상품을 찾을 수 없습니다."));
        return approvalHistoryRepository.findAllByProductOrderByDecisionDtDesc(product).stream()
                .map(AdminProductApprovalHistoryResponse::from).toList();
    }

    private void saveOptions(TravelProduct product, java.util.List<com.trip.routemate.admin.dto.AdminProductOptionRequest> requests) {
        optionRepository.deleteAllByProduct(product);
        if (requests == null) return;
        var options = requests.stream().map(request -> TravelProductOption.builder()
                .product(product).optionName(normalize(request.optionName())).optionDesc(normalizeNullable(request.optionDesc()))
                .price(request.price()).currency(normalizeCurrency(request.currency())).cancellationPolicy(normalizeNullable(request.cancellationPolicy()))
                .validityText(normalizeNullable(request.validityText())).confirmationType(normalize(request.confirmationType()).toUpperCase())
                .useYn(normalizeUseYn(request.useYn())).sortOrder(normalizeSortOrder(request.sortOrder())).build()).toList();
        optionRepository.saveAll(Objects.requireNonNull(options));
    }

    private Destination getDestination(Long destinationId) {
        return destinationRepository.findWithCountryAndRegionByDestId(destinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "여행지를 찾을 수 없습니다."));
    }

    private PartnerCompany getPartner(Long partnerId) {
        if (partnerId == null) return null;
        var partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파트너사를 찾을 수 없습니다."));
        if (!"ACTIVE".equals(partner.getPartnerStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "활성 상태인 파트너사에만 옵션상품을 연결할 수 있습니다.");
        }
        return partner;
    }

    private String normalize(String value) { return value == null ? "" : value.trim(); }
    private String normalizeNullable(String value) { var normalized = normalize(value); return normalized.isBlank() ? null : normalized; }
    private String normalizeCurrency(String value) { var normalized = normalize(value).toUpperCase(); return normalized.isBlank() ? "KRW" : normalized; }
    private String normalizeUseYn(String value) { return "N".equalsIgnoreCase(value) ? "N" : "Y"; }
    private int normalizeSortOrder(Integer value) { return value == null || value < 1 ? 1 : value; }
    private String normalizeStatus(String value) { var normalized = normalize(value).toUpperCase(); if ("ALL".equals(normalized) || "Y".equals(normalized) || "N".equals(normalized)) return normalized; throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 판매 상태입니다."); }
    private String normalizeApprovalStatus(String value) { var normalized = normalize(value).toUpperCase(); if (java.util.Set.of("ALL", "PENDING", "APPROVED", "REJECTED", "HOLD").contains(normalized)) return normalized; throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 승인 상태입니다."); }
    private String normalizeDecisionStatus(String value) { var normalized = normalize(value).toUpperCase(); if (java.util.Set.of("APPROVED", "REJECTED", "HOLD").contains(normalized)) return normalized; throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "승인, 거절, 보류 중 하나를 선택하세요."); }
}
