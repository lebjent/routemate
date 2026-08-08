package com.trip.routemate.user.service;

import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.dto.UserJoinDto;
import com.trip.routemate.user.repository.UserMstrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 트랜잭션을 잡아서 성능을 최적화합니다.
public class UserService {

    private final UserMstrRepository userMstrRepository;
    private final BCryptPasswordEncoder passwordEncoder; // SecurityConfig에서 등록한 빈이 주입됩니다.

    /**
     * 닉네임 중복 확인 로직
     */
    public boolean checkNicknameDuplicate(String nicknm) {
        return userMstrRepository.existsByUserNicknm(nicknm);
    }

    /**
     * 이메일 중복 확인 로직
     */
    public boolean checkEmailDuplicate(String email) {
        return userMstrRepository.existsByUserEmail(email);
    }

    /**
     * 회원가입 비즈니스 로직
     */
    @Transactional // 데이터 변경이 일어나므로 가입 메서드에는 별도로 @Transactional을 붙여줍니다.
    public Long join(UserJoinDto dto) {

        // 1. 이메일 중복 검증 (실무에선 필수 중의 필수!)
        if (userMstrRepository.existsByUserEmail(dto.getUserEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        // 2. 비밀번호와 비밀번호 확인 일치 검증
        if (!dto.getUserPwd().equals(dto.getUserPwdCheck())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 빌더 패턴을 이용해 엔티티로 변환 및 비밀번호 암호화(Encoding)
        UserMstr user = UserMstr.builder()
                .userEmail(dto.getUserEmail())
                .userPwd(passwordEncoder.encode(dto.getUserPwd())) // 🔥 평문 비번을 시큐리티로 암호화!
                .userNicknm(dto.getUserNicknm())
                .userPhone(dto.getUserPhone())
                .userZipcode(dto.getUserZipcode())
                .userAddr(dto.getUserAddr())
                .userAddrDetail(dto.getUserAddrDetail())
                .userBirth(dto.getUserBirth())
                .build();

        // 4. 오라클 DB에 최종 저장(Insert)
        UserMstr savedUser = userMstrRepository.save(user);

        // 5. 생성된 회원 일련번호(PK) 반환
        return savedUser.getUserId();
    }
}
