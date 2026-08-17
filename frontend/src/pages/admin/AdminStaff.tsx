import { useCallback, useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { hasPermission } from '../../features/admin/permissions';
import { useAuth } from '../../hooks/useAuth';

type StaffRole = 'ADMIN' | 'MASTER' | 'SENIOR' | 'JUNIOR';
type ManageableStaffRole = Exclude<StaffRole, 'ADMIN'>;
type StaffStatus = 'ACTIVE' | 'SUSPENDED';

type Staff = {
  userId: number;
  userEmail: string;
  userNicknm: string;
  userRole: StaffRole;
  userStatCd: StaffStatus;
  joinDt: string | null;
};

type StaffListData = {
  summary: {
    totalStaff: number;
    activeStaff: number;
    suspendedStaff: number;
    adminCount: number;
    masterCount: number;
    seniorCount: number;
    juniorCount: number;
  };
  staff: Staff[];
};

const roleStyle: Record<StaffRole, string> = {
  ADMIN: 'bg-rose-500/10 text-rose-300 border-rose-400/15',
  MASTER: 'bg-amber-500/10 text-amber-300 border-amber-400/15',
  SENIOR: 'bg-indigo-500/10 text-indigo-300 border-indigo-400/15',
  JUNIOR: 'bg-slate-500/10 text-slate-400 border-slate-400/15',
};

const formatDate = (value: string | null) => value
  ? new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(value))
  : '-';

export const AdminStaff = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const canManage = hasPermission(user, 'STAFF_MANAGE');
  const [queryInput, setQueryInput] = useState('');
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('ALL');
  const [role, setRole] = useState('ALL');
  const [data, setData] = useState<StaffListData | null>(null);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [creating, setCreating] = useState(false);
  const [newStaff, setNewStaff] = useState({ userEmail: '', userPwd: '', userNicknm: '', userRole: 'JUNIOR' as ManageableStaffRole });

  useEffect(() => {
    if (user && !hasPermission(user, 'STAFF_VIEW')) navigate('/admin', { replace: true });
  }, [navigate, user]);

  const loadStaff = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get<StaffListData>('/api/admin/staff', { params: { query, status, role } });
      setData(response.data);
    } catch (loadError) {
      if (axios.isAxiosError(loadError) && [401, 403].includes(loadError.response?.status ?? 0)) {
        navigate(loadError.response?.status === 401 ? '/admin/login' : '/admin', { replace: true });
        return;
      }
      setError('직원 정보를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [navigate, query, role, status]);

  useEffect(() => { void loadStaff(); }, [loadStaff, refreshKey]);

  const updateRole = async (staff: Staff, nextRole: StaffRole) => {
    if (!canManage || staff.userRole === nextRole) return;
    setUpdatingId(staff.userId);
    setError(null);
    try {
      await axios.patch(`/api/admin/staff/${staff.userId}/role`, { userRole: nextRole });
      setRefreshKey((value) => value + 1);
    } catch (updateError) {
      setError(axios.isAxiosError(updateError) ? updateError.response?.data?.detail || '직원 권한을 변경하지 못했습니다.' : '직원 권한을 변경하지 못했습니다.');
    } finally {
      setUpdatingId(null);
    }
  };

  const createStaff = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!canManage) return;
    setCreating(true);
    setError(null);
    try {
      await axios.post('/api/admin/staff', newStaff);
      setNewStaff({ userEmail: '', userPwd: '', userNicknm: '', userRole: 'JUNIOR' });
      setShowCreateForm(false);
      setRefreshKey((value) => value + 1);
    } catch (createError) {
      setError(axios.isAxiosError(createError) ? createError.response?.data?.detail || '직원 계정을 등록하지 못했습니다.' : '직원 계정을 등록하지 못했습니다.');
    } finally {
      setCreating(false);
    }
  };

  const updateStatus = async (staff: Staff) => {
    if (!canManage) return;
    const nextStatus: StaffStatus = staff.userStatCd === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    if (nextStatus === 'SUSPENDED' && !window.confirm(`${staff.userNicknm} 직원 계정을 정지하시겠습니까?`)) return;
    setUpdatingId(staff.userId);
    setError(null);
    try {
      await axios.patch(`/api/admin/staff/${staff.userId}/status`, { userStatCd: nextStatus });
      setRefreshKey((value) => value + 1);
    } catch (updateError) {
      setError(axios.isAxiosError(updateError) ? updateError.response?.data?.detail || '직원 상태를 변경하지 못했습니다.' : '직원 상태를 변경하지 못했습니다.');
    } finally {
      setUpdatingId(null);
    }
  };

  const roleCounts = data ? [
    ['ADMIN', data.summary.adminCount], ['MASTER', data.summary.masterCount],
    ['SENIOR', data.summary.seniorCount], ['JUNIOR', data.summary.juniorCount],
  ] as const : [];

  return (
    <>
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div><p className="text-xs font-bold tracking-[0.2em] text-indigo-300">STAFF MANAGEMENT</p><h1 className="mt-2 text-3xl font-extrabold text-white">직원 관리</h1><p className="mt-2 text-sm text-slate-500">운영 직원의 권한과 계정 상태를 회원과 별도로 관리합니다.</p></div>
        <div className="flex gap-2 self-start sm:self-auto">{canManage ? <button type="button" onClick={() => setShowCreateForm((value) => !value)} className="rounded-xl border border-indigo-400/20 bg-indigo-500/10 px-4 py-2.5 text-xs font-bold text-indigo-200"><i className="fa-solid fa-user-plus mr-2" />직원 등록</button> : null}<button type="button" onClick={() => setRefreshKey((value) => value + 1)} disabled={loading} className="rounded-xl bg-indigo-500 px-4 py-2.5 text-xs font-bold text-white disabled:opacity-50"><i className={`fa-solid fa-rotate-right mr-2 ${loading ? 'fa-spin' : ''}`} />새로고침</button></div>
      </header>

      {showCreateForm && canManage ? <section className="mt-6 rounded-[24px] border border-indigo-400/15 bg-indigo-500/[0.045] p-5"><div className="mb-4"><h2 className="text-sm font-bold text-white">새 직원 계정 등록</h2><p className="mt-1 text-xs text-slate-500">최초 비밀번호는 8자 이상이며, ADMIN 외 운영 권한을 지정할 수 있습니다.</p></div><form onSubmit={(event) => void createStaff(event)} className="grid gap-3 lg:grid-cols-[1fr_1fr_1fr_160px_auto]"><input type="email" required value={newStaff.userEmail} onChange={(event) => setNewStaff((value) => ({ ...value, userEmail: event.target.value }))} placeholder="직원 이메일" className="h-12 rounded-2xl border border-white/10 bg-slate-950/70 px-4 text-sm text-white outline-none" /><input required maxLength={50} value={newStaff.userNicknm} onChange={(event) => setNewStaff((value) => ({ ...value, userNicknm: event.target.value }))} placeholder="직원 이름/닉네임" className="h-12 rounded-2xl border border-white/10 bg-slate-950/70 px-4 text-sm text-white outline-none" /><input type="password" required minLength={8} value={newStaff.userPwd} onChange={(event) => setNewStaff((value) => ({ ...value, userPwd: event.target.value }))} placeholder="최초 비밀번호" className="h-12 rounded-2xl border border-white/10 bg-slate-950/70 px-4 text-sm text-white outline-none" /><select value={newStaff.userRole} onChange={(event) => setNewStaff((value) => ({ ...value, userRole: event.target.value as ManageableStaffRole }))} className="h-12 rounded-2xl border border-white/10 bg-slate-900 px-4 text-sm text-slate-300"><option value="MASTER">MASTER</option><option value="SENIOR">SENIOR</option><option value="JUNIOR">JUNIOR</option></select><button type="submit" disabled={creating} className="h-12 rounded-2xl bg-indigo-500 px-5 text-sm font-bold text-white disabled:opacity-50">{creating ? <i className="fa-solid fa-spinner fa-spin" /> : '등록'}</button></form></section> : null}

      {data ? <section className="mt-8 grid gap-4 sm:grid-cols-3"><article className="rounded-[22px] border border-white/10 bg-white/[0.035] p-5"><p className="text-xs text-slate-500">전체 직원</p><p className="mt-2 text-3xl font-extrabold text-white">{data.summary.totalStaff.toLocaleString('ko-KR')}</p></article><article className="rounded-[22px] border border-white/10 bg-white/[0.035] p-5"><p className="text-xs text-slate-500">활성 직원</p><p className="mt-2 text-3xl font-extrabold text-emerald-300">{data.summary.activeStaff.toLocaleString('ko-KR')}</p></article><article className="rounded-[22px] border border-white/10 bg-white/[0.035] p-5"><p className="text-xs text-slate-500">정지 직원</p><p className="mt-2 text-3xl font-extrabold text-rose-300">{data.summary.suspendedStaff.toLocaleString('ko-KR')}</p></article></section> : null}

      {data ? <section className="mt-4 flex flex-wrap gap-2">{roleCounts.map(([name, count]) => <span key={name} className={`rounded-full border px-3 py-1.5 text-xs font-bold ${roleStyle[name]}`}>{name} <strong className="ml-1 text-white">{count}</strong></span>)}</section> : null}

      <section className="mt-6 rounded-[24px] border border-white/10 bg-white/[0.03] p-5">
        <form onSubmit={(event) => { event.preventDefault(); setQuery(queryInput.trim()); }} className="grid gap-3 lg:grid-cols-[1fr_180px_180px_auto]">
          <div className="relative"><i className="fa-solid fa-magnifying-glass absolute left-4 top-1/2 -translate-y-1/2 text-sm text-slate-600" /><input value={queryInput} onChange={(event) => setQueryInput(event.target.value)} placeholder="이메일 또는 닉네임 검색" className="h-12 w-full rounded-2xl border border-white/10 bg-black/15 pl-11 pr-4 text-sm text-white outline-none" /></div>
          <select value={role} onChange={(event) => setRole(event.target.value)} className="h-12 rounded-2xl border border-white/10 bg-slate-900 px-4 text-sm text-slate-300"><option value="ALL">전체 권한</option><option value="ADMIN">ADMIN</option><option value="MASTER">MASTER</option><option value="SENIOR">SENIOR</option><option value="JUNIOR">JUNIOR</option></select>
          <select value={status} onChange={(event) => setStatus(event.target.value)} className="h-12 rounded-2xl border border-white/10 bg-slate-900 px-4 text-sm text-slate-300"><option value="ALL">전체 상태</option><option value="ACTIVE">활성</option><option value="SUSPENDED">정지</option></select>
          <button type="submit" className="h-12 rounded-2xl bg-indigo-500 px-6 text-sm font-bold text-white">검색</button>
        </form>
      </section>

      {!canManage ? <p className="mt-4 rounded-2xl border border-indigo-400/10 bg-indigo-400/5 px-4 py-3 text-xs text-indigo-200"><i className="fa-solid fa-circle-info mr-2" />MASTER 권한은 직원 목록만 조회할 수 있으며, 권한과 상태 변경은 ADMIN만 가능합니다.</p> : null}
      {error ? <p role="alert" className="mt-4 rounded-2xl border border-rose-400/15 bg-rose-400/5 px-4 py-3 text-sm text-rose-200">{error}</p> : null}

      <section className="mt-6 overflow-hidden rounded-[24px] border border-white/10 bg-white/[0.03]">
        {loading && !data ? <div className="flex min-h-[360px] items-center justify-center text-sm text-slate-500"><i className="fa-solid fa-spinner fa-spin mr-2" />직원 정보를 불러오고 있습니다...</div> : <div className="overflow-x-auto"><table className="w-full min-w-[920px] text-left"><thead className="border-b border-white/10 bg-black/10 text-[11px] text-slate-600"><tr><th className="px-6 py-4">직원</th><th className="px-4 py-4">권한</th><th className="px-4 py-4">상태</th><th className="px-4 py-4">등록일</th><th className="px-6 py-4 text-right">관리</th></tr></thead><tbody className="divide-y divide-white/5">
          {data?.staff.map((staff) => { const protectedAccount = staff.userRole === 'ADMIN' || staff.userId === user?.userId; const isActive = staff.userStatCd === 'ACTIVE'; return <tr key={staff.userId} className="hover:bg-white/[0.02]"><td className="px-6 py-4"><p className="text-sm font-semibold text-slate-200">{staff.userNicknm}</p><p className="mt-1 text-xs text-slate-600">{staff.userEmail}</p></td><td className="px-4 py-4">{canManage && !protectedAccount ? <select value={staff.userRole} disabled={updatingId === staff.userId} onChange={(event) => void updateRole(staff, event.target.value as StaffRole)} className="rounded-xl border border-white/10 bg-slate-900 px-3 py-2 text-xs font-bold text-slate-300"><option value="MASTER">MASTER</option><option value="SENIOR">SENIOR</option><option value="JUNIOR">JUNIOR</option></select> : <span className={`rounded-full border px-2.5 py-1 text-[11px] font-bold ${roleStyle[staff.userRole]}`}>{staff.userRole}</span>}</td><td className="px-4 py-4"><span className={`rounded-full px-2.5 py-1 text-[11px] font-bold ${isActive ? 'bg-emerald-500/10 text-emerald-300' : 'bg-rose-500/10 text-rose-300'}`}>{isActive ? '활성' : '정지'}</span></td><td className="px-4 py-4 text-xs text-slate-600">{formatDate(staff.joinDt)}</td><td className="px-6 py-4 text-right">{canManage && !protectedAccount ? <button type="button" disabled={updatingId === staff.userId} onClick={() => void updateStatus(staff)} className={`rounded-xl border px-3 py-2 text-xs font-semibold disabled:opacity-50 ${isActive ? 'border-rose-400/15 text-rose-300' : 'border-emerald-400/15 text-emerald-300'}`}>{isActive ? '계정 정지' : '다시 활성화'}</button> : <span className="text-xs text-slate-700">{protectedAccount ? '보호 계정' : '조회 전용'}</span>}</td></tr>; })}
          {data?.staff.length === 0 ? <tr><td colSpan={5} className="px-6 py-16 text-center text-sm text-slate-600">조건에 맞는 직원이 없습니다.</td></tr> : null}
        </tbody></table></div>}
      </section>
    </>
  );
};
