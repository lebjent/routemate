package com.trip.routemate.user.repository;

import com.trip.routemate.user.domain.UserMstr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserMstrRepository extends JpaRepository<UserMstr, Long> {

    long countByDelYn(String delYn);

    long countByUserStatCdAndDelYn(String userStatCd, String delYn);

    long countByUserRoleAndDelYn(String userRole, String delYn);

    long countByUserRoleAndUserStatCdAndDelYn(String userRole, String userStatCd, String delYn);

    long countByUserRoleInAndDelYn(Set<String> userRoles, String delYn);

    long countByUserRoleInAndUserStatCdAndDelYn(Set<String> userRoles, String userStatCd, String delYn);

    @Query("select count(user) from UserMstr user where user.delYn = :delYn and user.userRole in (select role.roleCode from AdminRole role where role.useYn = 'Y')")
    long countStaffByDelYn(@Param("delYn") String delYn);

    @Query("select count(user) from UserMstr user where user.delYn = :delYn and user.userStatCd = :status and user.userRole in (select role.roleCode from AdminRole role where role.useYn = 'Y')")
    long countStaffByStatusAndDelYn(@Param("status") String status, @Param("delYn") String delYn);

    Optional<UserMstr> findByUserIdAndDelYn(Long userId, String delYn);

    Optional<UserMstr> findByUserIdAndUserRoleAndDelYn(Long userId, String userRole, String delYn);

    Optional<UserMstr> findByUserIdAndUserRoleInAndDelYn(Long userId, Set<String> userRoles, String delYn);

    @Query("select user from UserMstr user where user.userId = :userId and user.delYn = :delYn and user.userRole in (select role.roleCode from AdminRole role where role.useYn = 'Y')")
    Optional<UserMstr> findStaffByUserIdAndDelYn(@Param("userId") Long userId, @Param("delYn") String delYn);

    @Query("""
            select user
              from UserMstr user
             where user.delYn = 'N'
               and user.userRole = 'USER'
               and (:status = 'ALL' or user.userStatCd = :status)
               and (
                    :query = ''
                    or lower(user.userEmail) like lower(concat('%', :query, '%'))
                    or lower(user.userNicknm) like lower(concat('%', :query, '%'))
               )
             order by user.joinDt desc, user.userId desc
            """)
    List<UserMstr> findAdminUsers(@Param("query") String query, @Param("status") String status);

    @Query("""
            select user
              from UserMstr user
             where user.delYn = 'N'
               and user.userRole in (select role.roleCode from AdminRole role where role.useYn = 'Y')
               and (:status = 'ALL' or user.userStatCd = :status)
               and (:role = 'ALL' or user.userRole = :role)
               and (
                    :query = ''
                    or lower(user.userEmail) like lower(concat('%', :query, '%'))
                    or lower(user.userNicknm) like lower(concat('%', :query, '%'))
               )
             order by
               case user.userRole
                 when 'ADMIN' then 1
                 when 'MASTER' then 2
                 when 'SENIOR' then 3
                 else 4
               end,
               user.joinDt desc,
               user.userId desc
            """)
    List<UserMstr> findAdminStaff(
            @Param("query") String query,
            @Param("status") String status,
            @Param("role") String role
    );

    // 회원가입 시 이메일 중복 체크를 위한 메서드
    boolean existsByUserEmail(String userEmail);

    // 닉네임 중복 체크를 위한 메서드
    boolean existsByUserNicknm(String userNicknm);

    // 로그인 시 이메일로 회원을 조회하기 위한 메서드
    Optional<UserMstr> findByUserEmail(String userEmail);
}
