package com.trip.routemate.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
        @NotBlank @Email String userEmail,
        @NotBlank @Size(min = 4, max = 100) String newPassword
) {
}
