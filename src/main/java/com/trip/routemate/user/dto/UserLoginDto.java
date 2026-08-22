package com.trip.routemate.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserLoginDto {

    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(description = "가입된 사용자 이메일", example = "user@example.com")
    private String userEmail;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Schema(description = "사용자 비밀번호", example = "Password1!")
    private String userPwd;
}
