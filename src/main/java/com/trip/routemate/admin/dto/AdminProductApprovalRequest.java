package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminProductApprovalRequest(
        @NotBlank String decisionStatus,
        @Size(max = 500) String reason
) { }
