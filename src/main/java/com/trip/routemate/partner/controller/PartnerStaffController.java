package com.trip.routemate.partner.controller;

import com.trip.routemate.partner.dto.PartnerStaffCreateRequest;
import com.trip.routemate.partner.dto.PartnerStaffResponse;
import com.trip.routemate.partner.service.PartnerStaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/partner/staff")
public class PartnerStaffController {
    private final PartnerStaffService partnerStaffService;

    @GetMapping
    public PartnerStaffResponse getStaff(Authentication authentication) {
        return partnerStaffService.getStaff(authentication);
    }

    @PostMapping
    public ResponseEntity<PartnerStaffResponse.Item> createStaff(Authentication authentication,
                                                                   @Valid @RequestBody PartnerStaffCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerStaffService.createStaff(authentication, request));
    }

    @PatchMapping("/{partnerUserId}/status")
    public PartnerStaffResponse.Item updateStatus(Authentication authentication, @PathVariable Long partnerUserId,
                                                  @RequestBody java.util.Map<String, String> request) {
        return partnerStaffService.updateStatus(authentication, partnerUserId, request.get("status"));
    }
}
