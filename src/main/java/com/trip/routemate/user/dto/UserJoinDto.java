package com.trip.routemate.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJoinDto {
    private String userEmail;
    private String userPwd;
    private String userPwdCheck; // 비밀번호 확인 검증용
    private String userNicknm;
}
