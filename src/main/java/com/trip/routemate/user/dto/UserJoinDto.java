package com.trip.routemate.user.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@io.swagger.v3.oas.annotations.media.Schema(description = "신규 회원가입에 필요한 계정·연락처·주소 정보 DTO")
/** 일반 회원 가입에 필요한 입력값을 전달하는 DTO다. */
public class UserJoinDto {
    @NotBlank @Email private String userEmail;
    @NotBlank @Size(min = 8, max = 100) private String userPwd;
    @NotBlank @Size(min = 8, max = 100) private String userPwdCheck; // 비밀번호 확인 검증용
    @NotBlank @Size(max = 50) private String userNicknm;
    private String userPhone;
    private String userZipcode;
    private String userAddr;
    private String userAddrDetail;
    private String userBirth;
}
