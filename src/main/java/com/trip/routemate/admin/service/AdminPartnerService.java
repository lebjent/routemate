package com.trip.routemate.admin.service;

import com.trip.routemate.admin.dto.AdminPartnerRequest;
import com.trip.routemate.admin.dto.AdminPartnerResponse;
import com.trip.routemate.partner.domain.PartnerCompany;
import com.trip.routemate.partner.domain.PartnerUser;
import com.trip.routemate.partner.repository.PartnerCompanyRepository;
import com.trip.routemate.partner.repository.PartnerUserRepository;
import com.trip.routemate.product.repository.TravelProductRepository;
import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.repository.UserMstrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPartnerService {
    private static final java.util.Set<String> STATUSES = java.util.Set.of("ONBOARDING", "ACTIVE", "SUSPENDED", "TERMINATED");
    private final PartnerCompanyRepository partnerRepository;
    private final TravelProductRepository productRepository;
    private final PartnerUserRepository partnerUserRepository;
    private final UserMstrRepository userMstrRepository;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasAuthority('PARTNER_MANAGE')")
    public AdminPartnerResponse getPartners(String query, String status) {
        var normalizedQuery = normalize(query).toLowerCase(Locale.ROOT);
        var normalizedStatus = normalize(status).toUpperCase(Locale.ROOT);
        var counts = productRepository.findPartnerProductCounts().stream().collect(Collectors.toMap(
                TravelProductRepository.PartnerProductCountView::getPartnerId,
                item -> new AdminPartnerResponse.ProductCount(item.getTotalProducts(), item.getActiveProducts(), item.getPendingProducts())));
        Predicate<PartnerCompany> queryFilter = partner -> normalizedQuery.isBlank()
                || contains(partner.getPartnerName(), normalizedQuery) || contains(partner.getPartnerCode(), normalizedQuery)
                || contains(partner.getBusinessNumber(), normalizedQuery) || contains(partner.getManagerName(), normalizedQuery);
        var partners = partnerRepository.findAllByOrderByPartnerNameAsc().stream()
                .filter(partner -> normalizedStatus.isBlank() || "ALL".equals(normalizedStatus) || normalizedStatus.equals(partner.getPartnerStatus()))
                .filter(queryFilter)
                .map(partner -> AdminPartnerResponse.Item.from(partner, counts.getOrDefault(partner.getPartnerId(), AdminPartnerResponse.ProductCount.empty())))
                .toList();
        return new AdminPartnerResponse(partners);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public AdminPartnerResponse.Item create(AdminPartnerRequest request) {
        validate(request, null);
        var businessNumber = required(request.businessNumber());
        var representativeName = required(request.representativeName());
        var managerName = required(request.managerName());
        var managerEmail = required(request.managerEmail());
        var managerPhone = required(request.managerPhone());
        var ownerLoginId = required(request.ownerLoginId()).toLowerCase(Locale.ROOT);
        var ownerName = required(request.ownerName());
        var ownerPassword = required(request.ownerPassword());
        if (ownerPassword.length() < 8) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대표 직원 비밀번호는 8자 이상이어야 합니다.");
        if (userMstrRepository.existsByUserEmail(ownerLoginId)) conflict("이미 사용 중인 대표 직원 ID입니다.");
        if (userMstrRepository.existsByUserNicknm(ownerName)) conflict("이미 사용 중인 대표 직원 이름입니다.");
        var partner = partnerRepository.save(PartnerCompany.builder()
                .partnerCode(generatePartnerCode()).partnerName(required(request.partnerName()))
                .businessNumber(businessNumber).representativeName(representativeName)
                .managerName(managerName).managerEmail(managerEmail)
                .managerPhone(managerPhone).websiteUrl(nullable(request.websiteUrl()))
                .commissionRate(request.commissionRate()).contractStartDate(request.contractStartDate()).contractEndDate(request.contractEndDate())
                .partnerStatus(status(request.partnerStatus())).memo(nullable(request.memo())).build());
        var owner = userMstrRepository.save(UserMstr.builder()
                .userEmail(ownerLoginId).userPwd(passwordEncoder.encode(ownerPassword)).userNicknm(ownerName)
                .snsProvider("LOCAL").userRole("PARTNER_OWNER").userStatCd("ACTIVE").delYn("N").build());
        partnerUserRepository.save(PartnerUser.builder().partner(partner).user(owner).partnerRole("OWNER").useYn("Y").build());
        return AdminPartnerResponse.Item.from(partner, AdminPartnerResponse.ProductCount.empty());
    }

    @Transactional
    @PreAuthorize("hasAuthority('PARTNER_MANAGE')")
    public AdminPartnerResponse.Item update(Long partnerId, AdminPartnerRequest request) {
        var partner = partnerRepository.findById(partnerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파트너사를 찾을 수 없습니다."));
        validate(request, partnerId);
        partner.update(code(request.partnerCode()), required(request.partnerName()), nullable(request.businessNumber()), nullable(request.representativeName()),
                nullable(request.managerName()), nullable(request.managerEmail()), nullable(request.managerPhone()), nullable(request.websiteUrl()),
                request.commissionRate(), request.contractStartDate(), request.contractEndDate(), status(request.partnerStatus()), nullable(request.memo()));
        var count = productRepository.findPartnerProductCounts().stream().filter(item -> partnerId.equals(item.getPartnerId())).findFirst()
                .map(item -> new AdminPartnerResponse.ProductCount(item.getTotalProducts(), item.getActiveProducts(), item.getPendingProducts()))
                .orElse(AdminPartnerResponse.ProductCount.empty());
        return AdminPartnerResponse.Item.from(partner, count);
    }

    private void validate(AdminPartnerRequest request, Long currentId) {
        if (currentId != null) partnerRepository.findByPartnerCode(code(request.partnerCode())).filter(found -> !found.getPartnerId().equals(currentId)).ifPresent(found -> conflict("이미 사용 중인 파트너 코드입니다."));
        var businessNumber = nullable(request.businessNumber());
        if (businessNumber != null) partnerRepository.findByBusinessNumber(businessNumber).filter(found -> !found.getPartnerId().equals(currentId)).ifPresent(found -> conflict("이미 등록된 사업자번호입니다."));
        if (request.contractStartDate() != null && request.contractEndDate() != null && request.contractEndDate().isBefore(request.contractStartDate()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "계약 종료일은 시작일보다 빠를 수 없습니다.");
        status(request.partnerStatus());
    }
    private void conflict(String message) { throw new ResponseStatusException(HttpStatus.CONFLICT, message); }
    private String status(String value) { var result = code(value); if (!STATUSES.contains(result)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 파트너 상태입니다."); return result; }
    private String code(String value) { return required(value).toUpperCase(Locale.ROOT); }
    private String required(String value) { var result = normalize(value); if (result.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "필수 항목을 입력하세요."); return result; }
    private String nullable(String value) { var result = normalize(value); return result.isBlank() ? null : result; }
    private String normalize(String value) { return value == null ? "" : value.trim(); }
    private boolean contains(String value, String query) { return value != null && value.toLowerCase(Locale.ROOT).contains(query); }
    private String generatePartnerCode() { return "PARTNER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT); }
}
