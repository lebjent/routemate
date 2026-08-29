package com.trip.routemate.partner.dto;

import com.trip.routemate.partner.domain.PartnerUser;

import java.time.LocalDateTime;
import java.util.List;

/** 파트너사 직원 목록과 소속 파트너 정보를 제공하는 응답이다. */
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
