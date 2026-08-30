package com.trip.routemate.partner.service;

import com.trip.routemate.common.storage.LocalStorageProperties;
import com.trip.routemate.partner.dto.PartnerProductImageUploadResponse;
import com.trip.routemate.partner.repository.PartnerUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 파트너 상품의 대표 이미지를 개발용 로컬 폴더의 업로더 계정·날짜별 하위 폴더에 저장한다.
 *
 * 상품 테이블에는 컴퓨터 절대 경로 대신 반환된 URL만 저장하므로, 향후 외부 파일 저장소로
 * 교체해도 상품 데이터 구조를 변경할 필요가 없다.
 */
@Service
@RequiredArgsConstructor
public class PartnerProductImageStorageService {
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final DateTimeFormatter DATE_DIRECTORY_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final Map<String, String> EXTENSIONS_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif"
    );

    private final LocalStorageProperties storageProperties;
    private final PartnerUserRepository partnerUserRepository;

    /**
     * 로그인한 활성 파트너사 대표가 선택한 대표 이미지 파일을 저장한다.
     *
     * @param authentication 현재 로그인 인증 정보
     * @param file 브라우저에서 전송한 이미지 파일
     * @return DB의 {@code imageUrl} 컬럼에 저장할 공개 URL
     */
    public PartnerProductImageUploadResponse store(Authentication authentication, MultipartFile file) {
        var uploaderEmail = requireActivePartnerOwner(authentication);
        validate(file);

        var storageRoot = storageProperties.optionProductDirectoryPath();
        var accountDirectory = accountDirectoryName(uploaderEmail);
        var dateDirectory = LocalDate.now().format(DATE_DIRECTORY_FORMAT);
        var directory = storageRoot.resolve(accountDirectory).resolve(dateDirectory).normalize();
        var extension = EXTENSIONS_BY_CONTENT_TYPE.get(file.getContentType());
        var fileName = UUID.randomUUID() + "." + extension;
        var destination = directory.resolve(fileName).normalize();

        if (!destination.startsWith(storageRoot)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 파일 경로입니다.");
        }

        try (var inputStream = file.getInputStream()) {
            Files.createDirectories(directory);
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 파일을 저장하지 못했습니다.", exception);
        }

        return new PartnerProductImageUploadResponse(
                storageProperties.normalizedOptionProductUrlPrefix() + "/" + accountDirectory + "/" + dateDirectory + "/" + fileName
        );
    }

    /** 업로드 파일의 크기와 지원 이미지 형식을 검증한다. */
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 이미지 파일을 선택하세요.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "이미지 파일은 10MB 이하만 업로드할 수 있습니다.");
        }
        if (!EXTENSIONS_BY_CONTENT_TYPE.containsKey(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JPG, PNG, WEBP, GIF 이미지만 업로드할 수 있습니다.");
        }
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

    /** 이메일을 파일 시스템과 URL 경로에 안전하게 사용할 수 있는 계정 폴더명으로 바꾼다. */
    private String accountDirectoryName(String email) {
        var directoryName = email.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
        if (directoryName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드 계정 정보를 확인할 수 없습니다.");
        }
        return directoryName;
    }
}
