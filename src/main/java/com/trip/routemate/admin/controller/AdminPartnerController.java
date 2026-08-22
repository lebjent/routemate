package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.dto.AdminPartnerRequest;
import com.trip.routemate.admin.dto.AdminPartnerResponse;
import com.trip.routemate.admin.service.AdminPartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/partners")
public class AdminPartnerController {
    private final AdminPartnerService partnerService;

    @GetMapping
    public AdminPartnerResponse getPartners(@RequestParam(required = false) String query, @RequestParam(required = false) String status) {
        return partnerService.getPartners(query, status);
    }

    @PostMapping
    public ResponseEntity<AdminPartnerResponse.Item> create(@Valid @RequestBody AdminPartnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerService.create(request));
    }

    @PatchMapping("/{partnerId}")
    public AdminPartnerResponse.Item update(@PathVariable Long partnerId, @Valid @RequestBody AdminPartnerRequest request) {
        return partnerService.update(partnerId, request);
    }
}
