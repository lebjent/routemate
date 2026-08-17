package com.trip.routemate.user.dto;

import java.util.List;

public record UserLoginResponse(
        Long userId,
        String userEmail,
        String userNicknm,
        String userRole,
        List<String> permissions,
        List<String> menuCodes
) {
}
