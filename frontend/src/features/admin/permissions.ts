import type { AuthUser } from '../../contexts/authContextValue';

/** 관리자 화면에서 확인하는 서버 권한 코드의 유니온 타입이다. */
export type AdminPermission =
  | 'DASHBOARD_VIEW'
  | 'MEMBER_VIEW'
  | 'MEMBER_STATUS_UPDATE'
  | 'STAFF_VIEW'
  | 'STAFF_MANAGE'
  | 'PLAN_MANAGE'
  | 'DESTINATION_MANAGE'
  | 'PARTNER_MANAGE';

/** 관리자 직원으로 취급할 기존 역할 코드 목록이다. */
export const staffRoles = ['ADMIN', 'MASTER', 'SENIOR', 'JUNIOR'] as const;

/** 현재 사용자가 관리자 직원 역할인지 확인한다. */
export const isStaffUser = (user: AuthUser | null) =>
  Boolean(user && staffRoles.includes(user.userRole as (typeof staffRoles)[number]));

/** 현재 사용자가 특정 관리자 기능 권한을 가지고 있는지 확인한다. */
export const hasPermission = (user: AuthUser | null, permission: AdminPermission) =>
  Boolean(user?.permissions?.includes(permission));

/** 현재 사용자가 사이드바의 특정 메뉴를 볼 수 있는지 확인한다. */
export const hasMenu = (user: AuthUser | null, menuCode: string) =>
  Boolean(user?.menuCodes?.includes(menuCode));
