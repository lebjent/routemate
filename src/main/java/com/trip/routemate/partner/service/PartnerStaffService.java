package com.trip.routemate.partner.service;

import com.trip.routemate.partner.domain.PartnerUser;
import com.trip.routemate.partner.dto.PartnerStaffCreateRequest;
import com.trip.routemate.partner.dto.PartnerStaffResponse;
import com.trip.routemate.partner.repository.PartnerUserRepository;
import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.repository.UserMstrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Objects;

/**
 * 파트너 대표 권한으로 직원 계정을 생성·조회·상태 변경한다.
 *
 * 모든 대상 직원은 로그인한 대표와 동일한 파트너사에 속해야 한다. 대표가 다른 회사의
 * 직원 계정에 접근할 수 없도록 소속 관계를 매번 검증한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerStaffService {
    private final PartnerUserRepository partnerUserRepository;
    private final UserMstrRepository userMstrRepository;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('PARTNER_OWNER')")
    /** 현재 대표의 파트너사 정보와 사용 중인 직원 목록을 조회한다. */
    public PartnerStaffResponse getStaff(Authentication authentication) {
        var owner = currentOwner(authentication);
        return new PartnerStaffResponse(owner.getPartner().getPartnerId(), owner.getPartner().getPartnerName(),
                partnerUserRepository.findAllByPartnerOrderByPartnerRoleAscCreateDtAsc(owner.getPartner()).stream()
                        .map(PartnerStaffResponse.Item::from).toList());
    }

    @Transactional
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    /**
     * 현재 파트너사에 새 직원 로그인 계정과 소속 관계를 만든다.
     *
     * @throws ResponseStatusException 로그인 이메일이 이미 사용 중일 때
     */
    public PartnerStaffResponse.Item createStaff(Authentication authentication, PartnerStaffCreateRequest request) {
        var owner = currentOwner(authentication);
        var loginId = required(request.loginId()).toLowerCase(Locale.ROOT);
        var name = required(request.name());
        var password = required(request.password());
        if (password.length() < 8) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상이어야 합니다.");
        if (userMstrRepository.existsByUserEmail(loginId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 직원 ID입니다.");
        if (userMstrRepository.existsByUserNicknm(name)) throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 직원 이름입니다.");
        var user = userMstrRepository.save(Objects.requireNonNull(UserMstr.builder()
                .userEmail(loginId).userPwd(passwordEncoder.encode(password)).userNicknm(name)
                .snsProvider("LOCAL").userRole("PARTNER_STAFF").userStatCd("ACTIVE").delYn("N").build()));
        var partnerUser = partnerUserRepository.save(Objects.requireNonNull(PartnerUser.builder()
                .partner(owner.getPartner()).user(user).partnerRole("STAFF").useYn("Y").build()));
        return PartnerStaffResponse.Item.from(partnerUser);
    }

    @Transactional
    @PreAuthorize("hasRole('PARTNER_OWNER')")
    /** 현재 대표와 같은 파트너사에 속한 직원의 사용 상태를 변경한다. */
    public PartnerStaffResponse.Item updateStatus(Authentication authentication, Long partnerUserId, String status) {
        var owner = currentOwner(authentication);
        var staff = partnerUserRepository.findByPartnerUserIdAndPartner(partnerUserId, owner.getPartner())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "직원 계정을 찾을 수 없습니다."));
        if (!"STAFF".equals(staff.getPartnerRole())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대표 직원 계정은 변경할 수 없습니다.");
        var normalizedStatus = required(status).toUpperCase(Locale.ROOT);
        if (!"ACTIVE".equals(normalizedStatus) && !"SUSPENDED".equals(normalizedStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 직원 상태입니다.");
        }
        staff.getUser().updateStatus(normalizedStatus);
        return PartnerStaffResponse.Item.from(staff);
    }

    /** 현재 인증 사용자가 활성 상태의 파트너 대표인지 검증한다. */
    private PartnerUser currentOwner(Authentication authentication) {
        if (authentication == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        var owner = partnerUserRepository.findByUserUserEmailAndUseYn(authentication.getName(), "Y")
                .filter(mapping -> "OWNER".equals(mapping.getPartnerRole()))
                .filter(mapping -> "ACTIVE".equals(mapping.getPartner().getPartnerStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "활성 파트너사의 대표 직원만 사용할 수 있습니다."));
        if (!"ACTIVE".equals(owner.getUser().getUserStatCd()) || !"N".equals(owner.getUser().getDelYn())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "사용할 수 없는 대표 직원 계정입니다.");
        }
        return owner;
    }

    /** 필수 문자열을 정규화하고 빈 값이면 요청 오류를 발생시킨다. */
    private String required(String value) {
        var normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "필수 항목을 입력하세요.");
        return normalized;
    }
}
