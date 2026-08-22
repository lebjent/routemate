import type { AuthUser } from '../../contexts/authContextValue';

export type AdminPermission =
  | 'DASHBOARD_VIEW'
  | 'MEMBER_VIEW'
  | 'MEMBER_STATUS_UPDATE'
  | 'STAFF_VIEW'
  | 'STAFF_MANAGE'
  | 'PLAN_MANAGE'
  | 'DESTINATION_MANAGE'
  | 'PARTNER_MANAGE';

export const staffRoles = ['ADMIN', 'MASTER', 'SENIOR', 'JUNIOR'] as const;

export const isStaffUser = (user: AuthUser | null) =>
  Boolean(user && staffRoles.includes(user.userRole as (typeof staffRoles)[number]));

export const hasPermission = (user: AuthUser | null, permission: AdminPermission) =>
  Boolean(user?.permissions?.includes(permission));

export const hasMenu = (user: AuthUser | null, menuCode: string) =>
  Boolean(user?.menuCodes?.includes(menuCode));
