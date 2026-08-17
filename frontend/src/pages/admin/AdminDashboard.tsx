import { useCallback, useEffect, useState } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';

type DashboardSummary = {
  totalUsers: number;
  activeUsers: number;
  totalPlans: number;
  publicPlans: number;
  totalDestinations: number;
  totalViews: number;
};

type DashboardPlan = {
  planId: number;
  title: string;
  userNicknm: string;
  isPublic: string;
  viewCount: number;
  createDt: string | null;
};

type DashboardData = {
  summary: DashboardSummary;
  popularPlans: DashboardPlan[];
  recentPlans: DashboardPlan[];
};

const formatNumber = (value: number) => value.toLocaleString('ko-KR');

const formatDate = (value: string | null) => {
  if (!value) return '-';
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(value));
};

export const AdminDashboard = () => {
  const navigate = useNavigate();
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get<DashboardData>('/api/admin/dashboard');
      setData(response.data);
    } catch (loadError) {
      if (axios.isAxiosError(loadError) && [401, 403].includes(loadError.response?.status ?? 0)) {
        navigate('/admin/login', { replace: true });
        return;
      }
      setError('대시보드 데이터를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
    } finally {
      setLoading(false);
    }
  }, [navigate]);

  useEffect(() => {
    void loadDashboard();
  }, [loadDashboard]);

  const metricCards = data ? [
    {
      label: '전체 회원',
      value: data.summary.totalUsers,
      detail: `활성 회원 ${formatNumber(data.summary.activeUsers)}명`,
      icon: 'fa-users',
      color: 'text-indigo-300 bg-indigo-500/15',
    },
    {
      label: '전체 여행 일정',
      value: data.summary.totalPlans,
      detail: `공개 일정 ${formatNumber(data.summary.publicPlans)}개`,
      icon: 'fa-route',
      color: 'text-cyan-300 bg-cyan-500/15',
    },
    {
      label: '추천 여행지',
      value: data.summary.totalDestinations,
      detail: '등록된 추천 명소',
      icon: 'fa-location-dot',
      color: 'text-emerald-300 bg-emerald-500/15',
    },
    {
      label: '누적 일정 조회',
      value: data.summary.totalViews,
      detail: '공개 상세 조회 합계',
      icon: 'fa-eye',
      color: 'text-amber-300 bg-amber-500/15',
    },
  ] : [];

  return (
    <>
          <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p className="text-xs font-bold tracking-[0.2em] text-indigo-300">ADMIN DASHBOARD</p>
              <h1 className="mt-2 text-3xl font-extrabold tracking-tight text-white">운영 현황</h1>
              <p className="mt-2 text-sm text-slate-500">RouteMate의 주요 서비스 지표를 확인하세요.</p>
            </div>
            <div className="flex items-center gap-3">
              <Link to="/" target="_blank" className="rounded-xl border border-white/10 bg-white/[0.035] px-4 py-2.5 text-xs font-semibold text-slate-300 transition hover:bg-white/[0.07] hover:text-white">
                <i className="fa-solid fa-arrow-up-right-from-square mr-2" />서비스 보기
              </Link>
              <button type="button" onClick={() => void loadDashboard()} disabled={loading} className="rounded-xl bg-indigo-500 px-4 py-2.5 text-xs font-bold text-white transition hover:bg-indigo-400 disabled:opacity-50">
                <i className={`fa-solid fa-rotate-right mr-2 ${loading ? 'fa-spin' : ''}`} />새로고침
              </button>
            </div>
          </header>

          {loading && !data ? (
            <div className="flex min-h-[520px] items-center justify-center text-sm text-slate-500">
              <i className="fa-solid fa-spinner fa-spin mr-2 text-indigo-400" />운영 데이터를 불러오고 있습니다...
            </div>
          ) : error && !data ? (
            <section className="mt-10 rounded-[24px] border border-rose-400/15 bg-rose-400/5 p-10 text-center">
              <i className="fa-solid fa-triangle-exclamation text-2xl text-rose-300" />
              <p className="mt-4 text-sm text-rose-200">{error}</p>
              <button type="button" onClick={() => void loadDashboard()} className="mt-5 rounded-xl bg-white/10 px-4 py-2.5 text-sm font-semibold text-white">다시 시도</button>
            </section>
          ) : data ? (
            <>
              {error ? <p className="mt-5 rounded-xl border border-rose-400/15 bg-rose-400/5 px-4 py-3 text-sm text-rose-200">{error}</p> : null}

              <section className="mt-8 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                {metricCards.map((card) => (
                  <article key={card.label} className="rounded-[22px] border border-white/10 bg-white/[0.035] p-5 shadow-lg shadow-black/10">
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <p className="text-xs font-semibold text-slate-500">{card.label}</p>
                        <p className="mt-3 text-3xl font-extrabold tracking-tight text-white">{formatNumber(card.value)}</p>
                        <p className="mt-2 text-xs text-slate-600">{card.detail}</p>
                      </div>
                      <span className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl ${card.color}`}>
                        <i className={`fa-solid ${card.icon}`} />
                      </span>
                    </div>
                  </article>
                ))}
              </section>

              <section className="mt-6 grid gap-6 xl:grid-cols-[1.55fr_0.85fr]">
                <article className="overflow-hidden rounded-[24px] border border-white/10 bg-white/[0.03]">
                  <div className="flex items-center justify-between border-b border-white/10 px-5 py-5 sm:px-6">
                    <div>
                      <h2 className="font-bold text-white">최근 등록 일정</h2>
                      <p className="mt-1 text-xs text-slate-600">가장 최근 생성된 여행 일정 5개</p>
                    </div>
                    <span className="rounded-full bg-white/5 px-3 py-1 text-xs text-slate-500">총 {formatNumber(data.summary.totalPlans)}개</span>
                  </div>
                  <div className="overflow-x-auto">
                    <table className="w-full min-w-[640px] text-left">
                      <thead className="border-b border-white/5 bg-black/10 text-[11px] uppercase tracking-wider text-slate-600">
                        <tr><th className="px-6 py-3 font-semibold">일정</th><th className="px-4 py-3 font-semibold">작성자</th><th className="px-4 py-3 font-semibold">상태</th><th className="px-4 py-3 font-semibold">조회</th><th className="px-6 py-3 font-semibold">등록일</th></tr>
                      </thead>
                      <tbody className="divide-y divide-white/5">
                        {data.recentPlans.map((plan) => (
                          <tr key={plan.planId} className="transition hover:bg-white/[0.025]">
                            <td className="max-w-[280px] truncate px-6 py-4 text-sm font-semibold text-slate-200">{plan.title}</td>
                            <td className="px-4 py-4 text-xs text-slate-500">{plan.userNicknm}</td>
                            <td className="px-4 py-4"><span className={`rounded-full px-2.5 py-1 text-[11px] font-bold ${plan.isPublic === 'Y' ? 'bg-emerald-500/10 text-emerald-300' : 'bg-slate-500/10 text-slate-400'}`}>{plan.isPublic === 'Y' ? '공개' : '비공개'}</span></td>
                            <td className="px-4 py-4 text-xs text-slate-500">{formatNumber(plan.viewCount)}</td>
                            <td className="px-6 py-4 text-xs text-slate-600">{formatDate(plan.createDt)}</td>
                          </tr>
                        ))}
                        {data.recentPlans.length === 0 ? <tr><td colSpan={5} className="px-6 py-12 text-center text-sm text-slate-600">등록된 일정이 없습니다.</td></tr> : null}
                      </tbody>
                    </table>
                  </div>
                </article>

                <article className="rounded-[24px] border border-white/10 bg-white/[0.03] p-5 sm:p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <h2 className="font-bold text-white">인기 일정 TOP 5</h2>
                      <p className="mt-1 text-xs text-slate-600">공개 일정 조회수 기준</p>
                    </div>
                    <i className="fa-solid fa-ranking-star text-amber-300" />
                  </div>
                  <ol className="mt-5 space-y-3">
                    {data.popularPlans.map((plan, index) => (
                      <li key={plan.planId} className="flex items-center gap-3 rounded-2xl border border-white/5 bg-black/10 p-3.5">
                        <span className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-xl text-sm font-extrabold ${index === 0 ? 'bg-amber-400/15 text-amber-300' : 'bg-white/5 text-slate-500'}`}>{index + 1}</span>
                        <div className="min-w-0 flex-1">
                          <p className="truncate text-sm font-semibold text-slate-200">{plan.title}</p>
                          <p className="mt-1 truncate text-xs text-slate-600">{plan.userNicknm}</p>
                        </div>
                        <span className="shrink-0 text-xs font-semibold text-slate-500"><i className="fa-regular fa-eye mr-1.5 text-cyan-400" />{formatNumber(plan.viewCount)}</span>
                      </li>
                    ))}
                    {data.popularPlans.length === 0 ? <li className="py-12 text-center text-sm text-slate-600">공개 일정이 없습니다.</li> : null}
                  </ol>
                </article>
              </section>

              <section className="mt-6 grid gap-4 md:grid-cols-2">
                <article className="rounded-[22px] border border-white/10 bg-gradient-to-br from-indigo-500/10 to-transparent p-5">
                  <div className="flex items-center justify-between text-sm"><span className="font-semibold text-slate-300">활성 회원 비율</span><span className="font-bold text-indigo-300">{data.summary.totalUsers === 0 ? 0 : Math.round((data.summary.activeUsers / data.summary.totalUsers) * 100)}%</span></div>
                  <div className="mt-4 h-2 overflow-hidden rounded-full bg-white/5"><div className="h-full rounded-full bg-indigo-400" style={{ width: `${data.summary.totalUsers === 0 ? 0 : (data.summary.activeUsers / data.summary.totalUsers) * 100}%` }} /></div>
                </article>
                <article className="rounded-[22px] border border-white/10 bg-gradient-to-br from-cyan-500/10 to-transparent p-5">
                  <div className="flex items-center justify-between text-sm"><span className="font-semibold text-slate-300">공개 일정 비율</span><span className="font-bold text-cyan-300">{data.summary.totalPlans === 0 ? 0 : Math.round((data.summary.publicPlans / data.summary.totalPlans) * 100)}%</span></div>
                  <div className="mt-4 h-2 overflow-hidden rounded-full bg-white/5"><div className="h-full rounded-full bg-cyan-400" style={{ width: `${data.summary.totalPlans === 0 ? 0 : (data.summary.publicPlans / data.summary.totalPlans) * 100}%` }} /></div>
                </article>
              </section>
            </>
          ) : null}
    </>
  );
};
