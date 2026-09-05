package com.trip.routemate.common.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

/** 이미지 업로드 및 DB에 기록된 파일 조회 API. */
@RestController
@RequiredArgsConstructor
public class ImageController {
    private final ImageStorageService storage;

    @PostMapping(value = "/api/admin/images/{category}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public ImageStorageService.UploadedImage uploadAdmin(@PathVariable ImageCategory category, @RequestParam MultipartFile file) {
        return storage.store(category, file);
    }

    @PostMapping(value = "/api/images/travel-plan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ImageStorageService.UploadedImage uploadPlan(@RequestParam MultipartFile file) {
        return storage.store(ImageCategory.TRAVEL_PLAN, file);
    }

    @GetMapping("/api/public/images/{imageId}")
    public ResponseEntity<Resource> image(@PathVariable String imageId, Authentication authentication) {
        var image = storage.load(imageId, authentication);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType()))
                .contentLength(image.size()).cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(1)))
                .header("X-Content-Type-Options", "nosniff").body(image.resource());
    }
}
