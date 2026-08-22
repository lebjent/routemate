package com.trip.routemate.partner.dto;

import com.trip.routemate.partner.domain.PartnerUser;

import java.time.LocalDateTime;
import java.util.List;

public record PartnerStaffResponse(Long partnerId, String partnerName, List<Item> staff) {
    public record Item(Long partnerUserId, Long userId, String loginId, String name, String partnerRole,
                       String status, LocalDateTime joinedAt) {
        public static Item from(PartnerUser partnerUser) {
            var user = partnerUser.getUser();
            return new Item(partnerUser.getPartnerUserId(), user.getUserId(), user.getUserEmail(), user.getUserNicknm(),
                    partnerUser.getPartnerRole(), user.getUserStatCd(), user.getJoinDt());
        }
    }
}
