package com.trip.routemate.common.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * 로그인 응답과 SecurityContext를 구성할 때 사용하는 권한 조회 계약입니다.
 * user·partner 모듈은 관리자 역할 저장소 구현을 직접 알 필요가 없습니다.
 */
public interface AuthorizationService {

    boolean isStaffRole(String roleCode);

    List<String> permissionNamesFor(Long userId, String legacyRole);

    List<GrantedAuthority> authoritiesFor(Long userId, String legacyRole);

    List<String> menuCodesFor(Long userId, String legacyRole);
}
