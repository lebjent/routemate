package com.trip.routemate.partner.controller;

import com.trip.routemate.partner.dto.PartnerDashboardResponse;
import com.trip.routemate.partner.dto.PartnerProductRequest;
import com.trip.routemate.partner.dto.PartnerProductResponse;
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
    public List<PartnerProductResponse> products(Authentication authentication) {
        return service.products(authentication);
    }

    @GetMapping("/destinations/places")
    public List<PartnerPortalService.PlaceItem> places(Authentication authentication) {
        return service.places(authentication);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerProductResponse create(Authentication authentication,
                                         @Valid @RequestBody PartnerProductRequest request) {
        return service.create(authentication, request);
    }

    @PatchMapping("/products/{productId}")
    public PartnerProductResponse update(Authentication authentication,
                                         @PathVariable Long productId,
                                         @Valid @RequestBody PartnerProductRequest request) {
        return service.update(authentication, productId, request);
    }
}
