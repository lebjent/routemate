import { useCallback, useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { hasPermission } from '../../features/admin/permissions';
import { useAuth } from '../../hooks/useAuth';

type UserStatus = 'ACTIVE' | 'SUSPENDED';
type StatusFilter = 'ALL' | UserStatus;

type AdminUser = {
  userId: number;
  userEmail: string;
  userNicknm: string;
  userRole: string;
  userStatCd: string;
  snsProvider: string;
  joinDt: string | null;
};

type UserListData = {
  summary: { totalUsers: number; activeUsers: number; suspendedUsers: number };
  users: AdminUser[];
};

const formatNumber = (value: number) => value.toLocaleString('ko-KR');
const formatDate = (value: string | null) => value
  ? new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(value))
  : '-';

export const AdminUsers = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const canUpdate = hasPermission(user, 'MEMBER_STATUS_UPDATE');
  const [queryInput, setQueryInput] = useState('');
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState<StatusFilter>('ALL');
  const [refreshKey, setRefreshKey] = useState(0);
  const [data, setData] = useState<UserListData | null>(null);
  const [loading, setLoading] = useState(true);
  const [updatingUserId, setUpdatingUserId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (user && !hasPermission(user, 'MEMBER_VIEW')) navigate('/admin', { replace: true });
  }, [navigate, user]);

  const loadUsers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get<UserListData>('/api/admin/users', { params: { query, status } });
      setData(response.data);
    } catch (loadError) {
      if (axios.isAxiosError(loadError) && [401, 403].includes(loadError.response?.status ?? 0)) {
        navigate(loadError.response?.status === 401 ? '/admin/login' : '/admin', { replace: true });
        return;
      }
      setError('회원 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
    } finally {
      setLoading(false);
    }
  }, [navigate, query, status]);

  useEffect(() => { void loadUsers(); }, [loadUsers, refreshKey]);

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault();
    const nextQuery = queryInput.trim();
    if (nextQuery === query) setRefreshKey((current) => current + 1);
    else setQuery(nextQuery);
  };

  const handleStatusUpdate = async (member: AdminUser) => {
    if (!canUpdate) return;
    const nextStatus: UserStatus = member.userStatCd === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    if (nextStatus === 'SUSPENDED' && !window.confirm(`${member.userNicknm} 회원을 정지하시겠습니까?`)) return;
    setUpdatingUserId(member.userId);
    setError(null);
    try {
      await axios.patch(`/api/admin/users/${member.userId}/status`, { userStatCd: nextStatus });
      setRefreshKey((current) => current + 1);
    } catch (updateError) {
      setError(axios.isAxiosError(updateError) ? updateError.response?.data?.detail || '회원 상태를 변경하지 못했습니다.' : '회원 상태를 변경하지 못했습니다.');
    } finally {
      setUpdatingUserId(null);
    }
  };

  const summaryCards = data ? [
    { label: '전체 회원', value: data.summary.totalUsers, icon: 'fa-users', color: 'text-indigo-300 bg-indigo-500/15' },
    { label: '활성 회원', value: data.summary.activeUsers, icon: 'fa-user-check', color: 'text-emerald-300 bg-emerald-500/15' },
    { label: '정지 회원', value: data.summary.suspendedUsers, icon: 'fa-user-lock', color: 'text-rose-300 bg-rose-500/15' },
  ] : [];

  return (
    <>
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div><p className="text-xs font-bold tracking-[0.2em] text-indigo-300">MEMBER MANAGEMENT</p><h1 className="mt-2 text-3xl font-extrabold text-white">회원 관리</h1><p className="mt-2 text-sm text-slate-500">일반 회원을 검색하고 서비스 이용 상태를 관리합니다.</p></div>
        <button type="button" onClick={() => setRefreshKey((current) => current + 1)} disabled={loading} className="self-start rounded-xl bg-indigo-500 px-4 py-2.5 text-xs font-bold text-white hover:bg-indigo-400 disabled:opacity-50 sm:self-auto"><i className={`fa-solid fa-rotate-right mr-2 ${loading ? 'fa-spin' : ''}`} />새로고침</button>
      </header>

      {data ? <section className="mt-8 grid gap-4 sm:grid-cols-3">{summaryCards.map((card) => <article key={card.label} className="rounded-[22px] border border-white/10 bg-white/[0.035] p-5"><div className="flex items-center justify-between"><div><p className="text-xs font-semibold text-slate-500">{card.label}</p><p className="mt-2 text-3xl font-extrabold text-white">{formatNumber(card.value)}</p></div><span className={`flex h-11 w-11 items-center justify-center rounded-2xl ${card.color}`}><i className={`fa-solid ${card.icon}`} /></span></div></article>)}</section> : null}

      <section className="mt-6 rounded-[24px] border border-white/10 bg-white/[0.03] p-5 sm:p-6">
        <form onSubmit={handleSearch} className="flex flex-col gap-3 lg:flex-row">
          <div className="relative min-w-0 flex-1"><i className="fa-solid fa-magnifying-glass absolute left-4 top-1/2 -translate-y-1/2 text-sm text-slate-600" /><input value={queryInput} onChange={(event) => setQueryInput(event.target.value)} placeholder="이메일 또는 닉네임 검색" className="h-12 w-full rounded-2xl border border-white/10 bg-black/15 pl-11 pr-4 text-sm text-white outline-none placeholder:text-slate-700 focus:border-indigo-400/50" /></div>
          <div className="flex gap-2 overflow-x-auto">{([{ key: 'ALL', label: '전체' }, { key: 'ACTIVE', label: '활성' }, { key: 'SUSPENDED', label: '정지' }] as const).map((item) => <button key={item.key} type="button" onClick={() => setStatus(item.key)} className={`h-12 shrink-0 rounded-2xl border px-5 text-sm font-semibold ${status === item.key ? 'border-indigo-400/30 bg-indigo-500/15 text-indigo-200' : 'border-white/10 bg-white/[0.025] text-slate-500'}`}>{item.label}</button>)}<button type="submit" className="h-12 rounded-2xl bg-indigo-500 px-6 text-sm font-bold text-white">검색</button></div>
        </form>
        <div className="mt-4 flex justify-between text-xs text-slate-600"><span>검색 결과 {formatNumber(data?.users.length ?? 0)}명</span>{query ? <button type="button" onClick={() => { setQueryInput(''); setQuery(''); }} className="text-indigo-300">검색 초기화</button> : null}</div>
      </section>

      {error ? <p role="alert" className="mt-5 rounded-2xl border border-rose-400/15 bg-rose-400/5 px-4 py-3 text-sm text-rose-200"><i className="fa-solid fa-circle-exclamation mr-2" />{error}</p> : null}

      <section className="mt-6 overflow-hidden rounded-[24px] border border-white/10 bg-white/[0.03]">
        {loading && !data ? <div className="flex min-h-[360px] items-center justify-center text-sm text-slate-500"><i className="fa-solid fa-spinner fa-spin mr-2 text-indigo-400" />회원 정보를 불러오고 있습니다...</div> : <div className="overflow-x-auto"><table className="w-full min-w-[820px] text-left"><thead className="border-b border-white/10 bg-black/10 text-[11px] text-slate-600"><tr><th className="px-6 py-4">회원</th><th className="px-4 py-4">가입 방식</th><th className="px-4 py-4">상태</th><th className="px-4 py-4">가입일</th><th className="px-6 py-4 text-right">관리</th></tr></thead><tbody className="divide-y divide-white/5">
          {data?.users.map((member) => { const isActive = member.userStatCd === 'ACTIVE'; return <tr key={member.userId} className="hover:bg-white/[0.02]"><td className="px-6 py-4"><div className="flex items-center gap-3"><span className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-500/10 text-sm font-bold text-indigo-200">{member.userNicknm.slice(0, 1)}</span><div><p className="text-sm font-semibold text-slate-200">{member.userNicknm}</p><p className="mt-1 text-xs text-slate-600">{member.userEmail}</p></div></div></td><td className="px-4 py-4 text-xs text-slate-500">{member.snsProvider === 'LOCAL' ? '이메일' : member.snsProvider}</td><td className="px-4 py-4"><span className={`rounded-full px-2.5 py-1 text-[11px] font-bold ${isActive ? 'bg-emerald-500/10 text-emerald-300' : 'bg-rose-500/10 text-rose-300'}`}>{isActive ? '활성' : '정지'}</span></td><td className="px-4 py-4 text-xs text-slate-600">{formatDate(member.joinDt)}</td><td className="px-6 py-4 text-right">{canUpdate ? <button type="button" disabled={updatingUserId === member.userId} onClick={() => void handleStatusUpdate(member)} className={`rounded-xl border px-3.5 py-2 text-xs font-semibold disabled:opacity-50 ${isActive ? 'border-rose-400/15 bg-rose-400/5 text-rose-300' : 'border-emerald-400/15 bg-emerald-400/5 text-emerald-300'}`}>{updatingUserId === member.userId ? <i className="fa-solid fa-spinner fa-spin mr-1.5" /> : null}{isActive ? '회원 정지' : '다시 활성화'}</button> : <span className="text-xs text-slate-700">조회 전용</span>}</td></tr>; })}
          {data?.users.length === 0 ? <tr><td colSpan={5} className="px-6 py-16 text-center text-sm text-slate-600">조건에 맞는 회원이 없습니다.</td></tr> : null}
        </tbody></table></div>}
      </section>
    </>
  );
};
