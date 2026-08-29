package com.trip.routemate.admin.dto;

import com.trip.routemate.user.domain.UserMstr;

import java.time.LocalDateTime;
import java.util.List;

@io.swagger.v3.oas.annotations.media.Schema(description = "관리자 회원 목록과 상태별 집계 응답 DTO")
/** 일반 회원 목록과 상태별 집계를 제공하는 관리자 응답이다. */
public record AdminUserListResponse(
        Summary summary,
        List<UserItem> users
) {
    public record Summary(
            long totalUsers,
            long activeUsers,
            long suspendedUsers
    ) {
    }

    public record UserItem(
            Long userId,
            String userEmail,
            String userNicknm,
            String userRole,
            String userStatCd,
            String snsProvider,
            LocalDateTime joinDt,
            LocalDateTime mdfyDt
    ) {
        public static UserItem from(UserMstr user) {
            return new UserItem(
                    user.getUserId(),
                    user.getUserEmail(),
                    user.getUserNicknm(),
                    user.getUserRole(),
                    user.getUserStatCd(),
                    user.getSnsProvider(),
                    user.getJoinDt(),
                    user.getMdfyDt()
            );
        }
    }
}
