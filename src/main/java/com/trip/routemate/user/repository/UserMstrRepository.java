package com.trip.routemate.user.repository;

import com.trip.routemate.user.domain.UserMstr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMstrRepository extends JpaRepository<UserMstr, Long> {

    // 회원가입 시 이메일 중복 체크를 위한 메서드
    boolean existsByUserEmail(String userEmail);

    // 로그인 시 이메일로 회원을 조회하기 위한 메서드
    Optional<UserMstr> findByUserEmail(String userEmail);
}
