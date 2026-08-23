package com.trip.routemate.partner.controller;

import com.trip.routemate.admin.dto.AdminProductRequest;
import com.trip.routemate.admin.dto.AdminProductResponse;
import com.trip.routemate.partner.dto.PartnerDashboardResponse;
import com.trip.routemate.partner.service.PartnerPortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/partner")
public class PartnerPortalController {
    private final PartnerPortalService service;

    @GetMapping("/dashboard")
    public PartnerDashboardResponse dashboard(Authentication authentication) {
        return service.dashboard(authentication);
    }

    @GetMapping("/products")
    public List<AdminProductResponse.Item> products(Authentication authentication) {
        return service.products(authentication);
    }

    @GetMapping("/destinations/places")
    public List<PartnerPortalService.PlaceItem> places(Authentication authentication) {
        return service.places(authentication);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminProductResponse.Item create(Authentication authentication,
                                            @Valid @RequestBody AdminProductRequest request) {
        return service.create(authentication, request);
    }

    @PatchMapping("/products/{productId}")
    public AdminProductResponse.Item update(Authentication authentication,
                                            @PathVariable Long productId,
                                            @Valid @RequestBody AdminProductRequest request) {
        return service.update(authentication, productId, request);
    }
}
