package com.trip.routemate.admin.repository;

import com.trip.routemate.admin.domain.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {
    Optional<AdminRole> findByRoleCodeAndUseYn(String roleCode, String useYn);

    @Query("select role.roleCode from AdminRole role where role.useYn = 'Y'")
    List<String> findActiveRoleCodes();
}
