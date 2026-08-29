package com.trip.routemate.admin.repository;

import com.trip.routemate.admin.domain.AdminRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** 역할별 관리자 메뉴 매핑을 조회하고 저장한다. */
public interface AdminRoleMenuRepository extends JpaRepository<AdminRoleMenu, Long> {
    @Query("""
            select menu.menuCode
              from AdminRoleMenu mapping
              join AdminRole role on role.roleId = mapping.roleId
              join AdminMenu menu on menu.menuId = mapping.menuId
             where role.roleCode in :roleCodes
               and role.useYn = 'Y'
               and mapping.allowYn = 'Y'
               and menu.useYn = 'Y'
            order by menu.sortOrder
            """)
    List<String> findMenuCodesByRoleCodes(@Param("roleCodes") List<String> roleCodes);
}
