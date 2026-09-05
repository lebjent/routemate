package com.trip.routemate.partner.service;

import com.trip.routemate.common.storage.ImageStorageService;
import com.trip.routemate.common.storage.ImageCategory;
import com.trip.routemate.partner.dto.PartnerProductImageUploadResponse;
import com.trip.routemate.partner.repository.PartnerUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * 파트너 상품의 대표 이미지를 개발용 로컬 폴더의 용도·날짜별 하위 폴더에 저장한다.
 *
 * 상품 테이블에는 컴퓨터 절대 경로 대신 반환된 URL만 저장하므로, 향후 외부 파일 저장소로
 * 교체해도 상품 데이터 구조를 변경할 필요가 없다.
 */
@Service
@RequiredArgsConstructor
public class PartnerProductImageStorageService {
    private final ImageStorageService imageStorage;
    private final PartnerUserRepository partnerUserRepository;

    public PartnerProductImageUploadResponse store(Authentication authentication, MultipartFile file) {
        return store(authentication, file, false);
    }

    public PartnerProductImageUploadResponse store(Authentication authentication, MultipartFile file, boolean detail) {
        requireActivePartnerOwner(authentication);
        var category = detail ? ImageCategory.PRODUCT_DETAIL
                : ImageCategory.PRODUCT;
        return new PartnerProductImageUploadResponse(imageStorage.store(category, file).imageUrl());
    }

    /** 활성 상태인 파트너사 대표 계정만 파일을 저장할 수 있게 제한하고 계정 이메일을 반환한다. */
    private String requireActivePartnerOwner(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        var partnerUser = partnerUserRepository.findByUserUserEmailAndUseYn(authentication.getName(), "Y")
                .filter(candidate -> "OWNER".equals(candidate.getPartnerRole()))
                .filter(candidate -> "ACTIVE".equals(candidate.getPartner().getPartnerStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "파트너사 대표 계정만 이미지를 업로드할 수 있습니다."));
        if (partnerUser.getPartner() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "파트너 계정이 아닙니다.");
        }
        return authentication.getName();
    }

}
