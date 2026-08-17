package com.trip.routemate.admin.repository;

import com.trip.routemate.admin.domain.AdminRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminRolePermissionRepository extends JpaRepository<AdminRolePermission, Long> {
    @Query("""
            select permission.permissionCode
              from AdminRolePermission mapping
              join AdminRole role on role.roleId = mapping.roleId
              join AdminPermissionEntity permission on permission.permissionId = mapping.permissionId
             where role.roleCode in :roleCodes
               and role.useYn = 'Y'
               and mapping.allowYn = 'Y'
               and permission.useYn = 'Y'
            """)
    List<String> findPermissionCodesByRoleCodes(@Param("roleCodes") List<String> roleCodes);
}
