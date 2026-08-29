package com.trip.routemate.admin.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_ADMIN_MENU")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
/** 관리자 화면에서 노출할 메뉴의 코드, 이름, 정렬 순서를 보관한다. */
public class AdminMenu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "MENU_ID")
    private Long menuId;
    @Column(name = "MENU_CODE", nullable = false, unique = true, length = 100)
    private String menuCode;
    @Column(name = "MENU_NAME", nullable = false, length = 100)
    private String menuName;
    @Column(name = "PARENT_MENU_ID")
    private Long parentMenuId;
    @Column(name = "MENU_PATH", length = 200)
    private String menuPath;
    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder;
    @Column(name = "USE_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String useYn;
}
