import { useEffect } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { hasMenu, hasPermission, isStaffUser, type AdminPermission } from '../../features/admin/permissions';
import { useAuth } from '../../hooks/useAuth';

type NavigationItem = {
  to?: string;
  label: string;
  icon: string;
  permission: AdminPermission;
  end?: boolean;
  ready?: boolean;
  menuCode: string;
};

const navigation: NavigationItem[] = [
  { to: '/admin', label: '대시보드', icon: 'fa-chart-pie', permission: 'DASHBOARD_VIEW', menuCode: 'DASHBOARD', end: true, ready: true },
  { to: '/admin/users', label: '회원 관리', icon: 'fa-user-gear', permission: 'MEMBER_VIEW', menuCode: 'MEMBERS', ready: true },
  { to: '/admin/staff', label: '직원 관리', icon: 'fa-users-gear', permission: 'STAFF_VIEW', menuCode: 'STAFF', ready: true },
  { label: '여행 일정 관리', icon: 'fa-map', permission: 'PLAN_MANAGE', menuCode: 'PLANS' },
  { to: '/admin/partners', label: '파트너사 관리', icon: 'fa-handshake', permission: 'PARTNER_MANAGE', menuCode: 'PARTNERS', ready: true },
  { to: '/admin/product-approvals', label: '상품 승인 관리', icon: 'fa-clipboard-check', permission: 'PARTNER_MANAGE', menuCode: 'PRODUCT_APPROVALS', ready: true },
  { to: '/admin/destinations', label: '국가·지역 관리', icon: 'fa-globe-asia', permission: 'DESTINATION_MANAGE', menuCode: 'DESTINATIONS', ready: true },
  { to: '/admin/recommendations', label: '추천 여행지 관리', icon: 'fa-star', permission: 'DESTINATION_MANAGE', menuCode: 'RECOMMENDATIONS', ready: true },
];

const roleColors: Record<string, string> = {
  ADMIN: 'bg-rose-500/15 text-rose-200',
  MASTER: 'bg-amber-500/15 text-amber-200',
  SENIOR: 'bg-indigo-500/15 text-indigo-200',
  JUNIOR: 'bg-slate-500/15 text-slate-300',
};

/**
 * 관리자 전용 화면의 사이드바, 사용자 정보, 하위 라우트 영역을 구성한다.
 *
 * 메뉴 노출은 로그인 사용자의 메뉴 권한을 기준으로 제한한다.
 */
export const AdminLayout = () => {
  const { user, loading, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!loading && !isStaffUser(user)) {
      navigate('/admin/login', { replace: true });
    }
  }, [loading, navigate, user]);

  const handleLogout = async () => {
    await logout();
    navigate('/admin/login', { replace: true });
  };

  if (loading || !isStaffUser(user) || !user) {
    return <main className="flex min-h-screen items-center justify-center text-sm text-slate-500">관리자 권한을 확인하고 있습니다...</main>;
  }

  const visibleNavigation = navigation.filter((item) => hasPermission(user, item.permission) && hasMenu(user, item.menuCode));

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 lg:flex">
      <aside className="border-b border-white/10 bg-slate-950/95 px-5 py-5 lg:sticky lg:top-0 lg:flex lg:h-screen lg:w-72 lg:shrink-0 lg:flex-col lg:border-b-0 lg:border-r lg:px-6 lg:py-7">
        <div className="flex items-center justify-between lg:block">
          <Link to="/admin" className="flex items-center gap-3 text-lg font-extrabold text-white">
            <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-indigo-500 text-white shadow-lg shadow-indigo-500/25">
              <i className="fa-solid fa-map-location-dot" />
            </span>
            <span>RouteMate <span className="text-indigo-300">Admin</span></span>
          </Link>
          <button type="button" onClick={() => void handleLogout()} className="text-xs font-semibold text-slate-500 transition hover:text-white lg:hidden">로그아웃</button>
        </div>

        <nav className="mt-5 flex gap-2 overflow-x-auto lg:mt-10 lg:flex-col lg:overflow-visible">
          {visibleNavigation.map((item) => item.ready && item.to ? (
            <NavLink
              key={item.label}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `flex shrink-0 items-center gap-3 rounded-2xl px-4 py-3 text-sm font-semibold transition ${isActive ? 'bg-indigo-500/15 font-bold text-indigo-200' : 'text-slate-500 hover:bg-white/[0.035] hover:text-slate-200'}`}
            >
              <i className={`fa-solid ${item.icon} w-5 text-center`} />{item.label}
            </NavLink>
          ) : (
            <button key={item.label} type="button" disabled className="flex shrink-0 cursor-not-allowed items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm font-semibold text-slate-700">
              <i className={`fa-solid ${item.icon} w-5 text-center`} />{item.label}
              <span className="ml-auto hidden rounded-full bg-white/5 px-2 py-0.5 text-[10px] lg:inline">준비 중</span>
            </button>
          ))}
        </nav>

        <div className="mt-auto hidden border-t border-white/10 pt-5 lg:block">
          <div className="rounded-2xl bg-white/[0.035] p-3">
            <div className="flex items-center gap-3">
              <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-500/15 text-sm font-bold text-indigo-200">
                {user.userNicknm.slice(0, 1)}
              </span>
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2">
                  <p className="truncate text-sm font-bold text-white">{user.userNicknm}</p>
                  <span className={`rounded-full px-2 py-0.5 text-[9px] font-extrabold ${roleColors[user.userRole] ?? roleColors.JUNIOR}`}>{user.userRole}</span>
                </div>
                <p className="mt-1 truncate text-xs text-slate-600">{user.userEmail}</p>
              </div>
              <button type="button" onClick={() => void handleLogout()} aria-label="관리자 로그아웃" className="text-slate-600 transition hover:text-white">
                <i className="fa-solid fa-arrow-right-from-bracket" />
              </button>
            </div>
          </div>
        </div>
      </aside>

      <main className="relative min-w-0 flex-1 overflow-x-hidden lg:h-screen lg:overflow-y-auto">
        <div className="pointer-events-none absolute -right-36 -top-44 h-[520px] w-[520px] rounded-full bg-indigo-600/10 blur-[150px]" />
        <div className="relative mx-auto max-w-[1500px] px-5 py-7 sm:px-8 lg:px-10 lg:py-9">
          <Outlet />
        </div>
      </main>
    </div>
  );
};
