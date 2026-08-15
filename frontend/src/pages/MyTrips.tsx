import { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { formatTravelDuration, getTravelDurationDays } from '../features/trip-builder/model';

interface MyTravelPlan {
  planId: number;
  title: string;
  description: string;
  imageUrl: string;
  userNicknm: string;
  likeCount: number;
  isPublic: string;
  travelStartDate: string | null;
  travelEndDate: string | null;
  createDt: string;
  mdfyDt: string;
}

const filters = [
  { key: 'ALL', label: '전체' },
  { key: 'Y', label: '공개' },
  { key: 'N', label: '비공개' },
] as const;

export const MyTrips = () => {
  const { user, loading: authLoading } = useAuth();
  const [plans, setPlans] = useState<MyTravelPlan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'Y' | 'N'>('ALL');
  const location = useLocation();
  const createdPlanId = location.state && typeof location.state === 'object' ? (location.state as { createdPlanId?: number }).createdPlanId : undefined;

  useEffect(() => {
    const fetchPlans = async () => {
      if (authLoading) return;

      if (!user) {
        setPlans([]);
        setLoading(false);
        setError('로그인 후 내 여행을 확인할 수 있어요.');
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const response = await axios.get<MyTravelPlan[]>('/api/my-travel-plans');
        setPlans(response.data || []);
      } catch (err) {
        console.error('Failed to load my travel plans', err);
        setError('내 여행 일정을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
      } finally {
        setLoading(false);
      }
    };

    void fetchPlans();
  }, [authLoading, user]);

  const filteredPlans = useMemo(() => {
    const keyword = query.trim().toLowerCase();

    return plans.filter((plan) => {
      const matchesQuery =
        keyword.length === 0 ||
        plan.title.toLowerCase().includes(keyword) ||
        (plan.description || '').toLowerCase().includes(keyword) ||
        plan.userNicknm.toLowerCase().includes(keyword);

      const matchesStatus = statusFilter === 'ALL' ? true : plan.isPublic === statusFilter;

      return matchesQuery && matchesStatus;
    });
  }, [plans, query, statusFilter]);

  const totalTravelDays = plans.reduce(
    (sum, plan) => sum + getTravelDurationDays(plan.travelStartDate, plan.travelEndDate),
    0
  );
  const publicCount = plans.filter((plan) => plan.isPublic === 'Y').length;
  const privateCount = plans.filter((plan) => plan.isPublic === 'N').length;

  return (
    <main className="max-w-7xl mx-auto px-6 py-10 flex-grow w-full relative z-10">
      <div className="absolute top-[-8%] left-[-8%] w-[520px] h-[520px] bg-indigo-600/10 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[520px] h-[520px] bg-purple-600/10 rounded-full blur-[120px] pointer-events-none" />

      <section className="flex flex-col gap-6 mb-8">
          <div className="max-w-2xl">
            <p className="text-sm tracking-[0.24em] text-indigo-300/90 font-semibold mb-3">MY TRIPS</p>
            <h1 className="text-3xl md:text-4xl lg:text-5xl font-extrabold tracking-tight text-white">
              내 여행 둘러보기
            </h1>
            <p className="text-gray-400 mt-4 text-base leading-relaxed">
              내가 만든 여행 일정을 모아서 보고, 검색하고, 관리할 수 있는 공간입니다.
            </p>
            {createdPlanId ? (
              <p className="mt-4 inline-flex items-center gap-2 rounded-2xl border border-emerald-500/20 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-200">
                <i className="fa-solid fa-circle-check" />
                새 일정이 저장되었습니다.
              </p>
            ) : null}
          </div>

        <div className="flex flex-wrap gap-3">
          <Link to="/my-trips/new" className="theme-btn-primary px-5 py-3 inline-flex items-center gap-2">
            <i className="fa-solid fa-plus" />
            새 일정 만들기
          </Link>
          {[
            { label: '전체 일정', value: plans.length },
            { label: '공개 일정', value: publicCount },
            { label: '총 여행일', value: `${totalTravelDays}일` },
          ].map((item) => (
            <div
              key={item.label}
              className="theme-glass-card !py-4 !px-5 min-w-[150px] flex-1 sm:flex-none sm:w-[170px]"
            >
              <p className="text-gray-400 text-xs font-medium mb-1.5">{item.label}</p>
              <p className="text-white text-2xl font-bold leading-none">{item.value}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="theme-glass-card mb-6 !p-5 md:!p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center">
          <div className="flex flex-1 min-w-0 items-center gap-3 rounded-2xl border border-white/10 bg-white/5 px-4 h-14">
            <i className="fa-solid fa-magnifying-glass shrink-0 text-gray-500 text-base leading-none" />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="제목, 설명, 닉네임으로 검색"
              className="w-full bg-transparent border-0 p-0 text-sm text-white placeholder:text-gray-500 focus:outline-none focus:ring-0"
            />
          </div>

          <div className="flex flex-wrap gap-2">
            {filters.map((item) => (
              <button
                key={item.key}
                type="button"
                onClick={() => setStatusFilter(item.key)}
                className={`min-w-[72px] px-4 h-12 rounded-2xl text-sm font-semibold border transition ${
                  statusFilter === item.key
                    ? 'bg-white text-slate-950 border-white shadow-lg shadow-black/20'
                    : 'bg-white/5 text-gray-300 border-white/10 hover:bg-white/10 hover:text-white'
                }`}
              >
                {item.label}
              </button>
            ))}
          </div>
        </div>

        <div className="mt-3 flex flex-wrap items-center gap-2.5 text-xs text-gray-500">
          <span className="inline-flex items-center gap-1.5">
            <i className="fa-solid fa-circle-info text-indigo-300" />
            최근 수정 순으로 정렬됩니다.
          </span>
          <span className="hidden sm:inline text-gray-600">·</span>
          <span>공개 {publicCount}개</span>
          <span className="hidden sm:inline text-gray-600">·</span>
          <span>비공개 {privateCount}개</span>
        </div>
      </section>

      {loading ? (
        <div className="theme-glass-card text-center py-20">
          <i className="fa-solid fa-spinner fa-spin text-3xl mb-3 text-indigo-500" />
          <p className="text-gray-400">내 여행 일정을 불러오는 중입니다...</p>
        </div>
      ) : error ? (
        <div className="theme-glass-card text-center py-16">
          <div className="mx-auto mb-4 w-14 h-14 rounded-full bg-red-500/10 flex items-center justify-center text-red-300">
            <i className="fa-solid fa-triangle-exclamation text-xl" />
          </div>
          <p className="text-red-300 font-medium mb-4">{error}</p>
          {!user ? (
            <Link to="/login" className="theme-btn-primary px-6 py-3 inline-flex">
              로그인하러 가기
            </Link>
          ) : null}
        </div>
      ) : filteredPlans.length === 0 ? (
        <div className="theme-glass-card text-center py-20">
          <div className="mx-auto mb-5 w-16 h-16 rounded-full bg-indigo-500/10 flex items-center justify-center text-indigo-300">
            <i className="fa-solid fa-route text-2xl" />
          </div>
          <h2 className="text-xl font-bold text-white mb-2">조건에 맞는 일정이 없어요</h2>
          <p className="text-gray-400 mb-6 max-w-md mx-auto leading-relaxed">
            검색어를 바꿔보거나 상태 필터를 전체로 돌려보세요. 아직 일정이 없다면 새 여행을 만들어도 좋아요.
          </p>
          <Link to="/" className="theme-btn-primary px-6 py-3 inline-flex">
            홈으로 돌아가기
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 xl:grid-cols-2 gap-5">
          {filteredPlans.map((plan) => {
            const isPublic = plan.isPublic === 'Y';

            return (
              <article
                key={plan.planId}
                className="group overflow-hidden rounded-[28px] border border-white/8 bg-white/[0.03] backdrop-blur-xl shadow-[0_18px_60px_rgba(0,0,0,0.18)] transition duration-300 hover:-translate-y-0.5 hover:border-white/12"
              >
                <div className="grid grid-cols-1 sm:grid-cols-[200px_1fr]">
                  <div className="relative h-56 sm:h-full min-h-[220px] bg-slate-900">
                    {plan.imageUrl ? (
                      <img
                        src={plan.imageUrl}
                        alt={plan.title}
                        className="h-full w-full object-cover transition duration-500 group-hover:scale-[1.03]"
                      />
                    ) : (
                      <div className="h-full w-full flex items-center justify-center text-gray-600">
                        <i className="fa-solid fa-image text-3xl" />
                      </div>
                    )}

                    <div className="absolute inset-0 bg-gradient-to-t from-black/55 via-black/10 to-transparent" />
                    <div className="absolute left-4 top-4 flex flex-wrap gap-2">
                      <span
                        className={`inline-flex items-center rounded-full px-2.5 py-1 text-[11px] font-semibold backdrop-blur-md ${
                          isPublic ? 'bg-emerald-500/15 text-emerald-200' : 'bg-slate-900/70 text-slate-200'
                        }`}
                      >
                        {isPublic ? '공개' : '비공개'}
                      </span>
                    </div>
                  </div>

                  <div className="p-6 flex flex-col justify-between gap-6">
                    <div>
                      <div className="flex flex-wrap items-center gap-x-3 gap-y-2 text-xs text-gray-500 mb-3">
                        <span className="inline-flex items-center gap-1.5">
                          <i className="fa-solid fa-user text-indigo-300" />
                          {plan.userNicknm}
                        </span>
                        <span>수정 {new Date(plan.mdfyDt).toLocaleDateString('ko-KR')}</span>
                        {plan.travelStartDate && plan.travelEndDate ? (
                          <span className="inline-flex items-center gap-1.5 text-indigo-200">
                            <i className="fa-regular fa-calendar-days" />
                            {plan.travelStartDate.replaceAll('-', '.')} - {plan.travelEndDate.replaceAll('-', '.')}
                          </span>
                        ) : null}
                      </div>

                      <h2 className="text-2xl font-bold text-white leading-snug">{plan.title}</h2>
                      <p className="mt-3 text-sm text-gray-400 leading-relaxed line-clamp-3">
                        {plan.description}
                      </p>
                    </div>

                    <div className="flex items-center justify-between gap-4 border-t border-white/8 pt-4">
                      <div className="flex flex-wrap items-center gap-4 text-sm text-gray-300">
                        <span className="inline-flex items-center gap-1.5">
                          <i className="fa-regular fa-calendar-days text-indigo-400" />
                          {formatTravelDuration(plan.travelStartDate, plan.travelEndDate)}
                        </span>
                        <span className="inline-flex items-center gap-1.5">
                          <i className="fa-solid fa-heart text-pink-500" />
                          {plan.likeCount?.toLocaleString()}
                        </span>
                      </div>

                      <Link
                        to={`/my-trips/${plan.planId}`}
                        className="inline-flex items-center gap-1.5 text-sm font-semibold text-indigo-300 hover:text-indigo-200 transition"
                      >
                        상세 보기
                        <i className="fa-solid fa-arrow-right text-xs" />
                      </Link>
                    </div>
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </main>
  );
};
