package com.trip.routemate.admin.repository;

import com.trip.routemate.admin.domain.AdminUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminUserRoleRepository extends JpaRepository<AdminUserRole, Long> {

    @Modifying
    @Query("delete from AdminUserRole mapping where mapping.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("""
            select role.roleCode
              from AdminUserRole mapping
              join AdminRole role on role.roleId = mapping.roleId
             where mapping.userId = :userId
               and mapping.primaryYn = 'Y'
               and role.useYn = 'Y'
            """)
    List<String> findPrimaryRoleCodesByUserId(@Param("userId") Long userId);
}
