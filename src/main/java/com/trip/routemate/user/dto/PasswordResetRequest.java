package com.trip.routemate.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 개발용 임시 비밀번호 재설정에 사용하는 요청이다. */
public record PasswordResetRequest(
        @NotBlank @Email String userEmail,
        @NotBlank @Size(min = 4, max = 100) String newPassword
) {
}
