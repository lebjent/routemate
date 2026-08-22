package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.dto.AdminProductRequest;
import com.trip.routemate.admin.dto.AdminProductResponse;
import com.trip.routemate.admin.service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
public class AdminProductController {
    private final AdminProductService productService;

    @GetMapping
    public ResponseEntity<AdminProductResponse> getProducts(@RequestParam(required = false) Long destinationId,
                                                            @RequestParam(defaultValue = "ALL") String useYn) {
        return ResponseEntity.ok(productService.getProducts(destinationId, useYn));
    }

    @PostMapping
    public ResponseEntity<AdminProductResponse.Item> create(@Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.status(201).body(productService.create(request));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<AdminProductResponse.Item> update(@PathVariable Long productId,
                                                             @Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.ok(productService.update(productId, request));
    }
}
