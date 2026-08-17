package com.trip.routemate.admin.dto;

import com.trip.routemate.user.domain.UserMstr;

import java.time.LocalDateTime;
import java.util.List;

public record AdminStaffListResponse(Summary summary, List<StaffItem> staff) {

    public record Summary(
            long totalStaff,
            long activeStaff,
            long suspendedStaff,
            long adminCount,
            long masterCount,
            long seniorCount,
            long juniorCount
    ) {
    }

    public record StaffItem(
            Long userId,
            String userEmail,
            String userNicknm,
            String userRole,
            String userStatCd,
            LocalDateTime joinDt,
            LocalDateTime mdfyDt
    ) {
        public static StaffItem from(UserMstr user) {
            return new StaffItem(
                    user.getUserId(),
                    user.getUserEmail(),
                    user.getUserNicknm(),
                    user.getUserRole(),
                    user.getUserStatCd(),
                    user.getJoinDt(),
                    user.getMdfyDt()
            );
        }
    }
}
