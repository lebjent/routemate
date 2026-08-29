package com.trip.routemate.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@io.swagger.v3.oas.annotations.media.Schema(description = "신규 회원가입에 필요한 계정·연락처·주소 정보 DTO")
/** 일반 회원 가입에 필요한 입력값을 전달하는 DTO다. */
public class UserJoinDto {
    private String userEmail;
    private String userPwd;
    private String userPwdCheck; // 비밀번호 확인 검증용
    private String userNicknm;
    private String userPhone;
    private String userZipcode;
    private String userAddr;
    private String userAddrDetail;
    private String userBirth;
}
