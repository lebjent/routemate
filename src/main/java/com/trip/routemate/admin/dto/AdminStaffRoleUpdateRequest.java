package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminStaffRoleUpdateRequest(@NotBlank String userRole) {
}
