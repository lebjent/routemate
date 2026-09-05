package com.trip.routemate.user.service;

import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.dto.UserJoinDto;
import com.trip.routemate.user.repository.UserMstrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 일반 회원의 중복 확인과 가입 처리 규칙을 담당한다.
 *
 * 중복 여부는 가입 시점에 다시 확인하며, 비밀번호는 저장 전에 BCrypt로 암호화한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserMstrRepository userMstrRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /** 닉네임이 이미 가입된 회원에게 사용 중인지 확인한다. */
    public boolean checkNicknameDuplicate(String nicknm) {
        return userMstrRepository.existsByUserNicknm(nicknm);
    }

    /** 이메일이 이미 가입된 회원에게 사용 중인지 확인한다. */
    public boolean checkEmailDuplicate(String email) {
        return userMstrRepository.existsByUserEmail(email);
    }

    /**
     * 이메일과 닉네임 중복을 검증한 뒤 일반 회원 계정을 생성한다.
     *
     * @param dto 가입에 필요한 회원 정보
     * @return 생성된 회원 식별자
     * @throws IllegalStateException 이메일·닉네임이 중복되거나 비밀번호 확인값이 다를 때
     */
    @Transactional
    public Long join(UserJoinDto dto) {

        var email = dto.getUserEmail().trim().toLowerCase(java.util.Locale.ROOT);
        var nickname = dto.getUserNicknm().trim();
        if (nickname.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "닉네임을 입력해 주세요.");

        if (userMstrRepository.existsByUserEmail(email)) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        if (userMstrRepository.existsByUserNicknm(nickname)) {
            throw new IllegalStateException("이미 존재하는 닉네임입니다.");
        }

        if (!dto.getUserPwd().equals(dto.getUserPwdCheck())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        UserMstr user = UserMstr.builder()
                .userEmail(email)
                .userPwd(passwordEncoder.encode(dto.getUserPwd()))
                .userNicknm(nickname)
                .userPhone(dto.getUserPhone())
                .userZipcode(dto.getUserZipcode())
                .userAddr(dto.getUserAddr())
                .userAddrDetail(dto.getUserAddrDetail())
                .userBirth(dto.getUserBirth())
                .build();

        UserMstr savedUser = userMstrRepository.save(user);

        return savedUser.getUserId();
    }
}
