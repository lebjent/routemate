import { useCallback, useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';

type DashboardSummary = {
  totalUsers: number; activeUsers: number; totalPlans: number; publicPlans: number;
  totalDestinations: number; totalViews: number; totalProducts: number; activeProducts: number;
  totalOptions: number; activeOptions: number; totalOrders: number; paidOrders: number;
  pendingPayments: number; paidRevenue: number;
};
type DashboardPlan = { planId: number; title: string; userNicknm: string; isPublic: string; viewCount: number; createDt: string | null };
type ProductTypeItem = { productType: string; totalCount: number; activeCount: number };
type DashboardOrder = {
  orderId: number; orderNo: string; productName: string; optionName: string; destinationName: string;
  quantity: number; totalPrice: number; currency: string; useDate: string; orderStatus: string;
  paymentStatus: string; createDt: string | null;
};
type DashboardData = {
  summary: DashboardSummary; productTypes: ProductTypeItem[]; recentOrders: DashboardOrder[];
  popularPlans: DashboardPlan[]; recentPlans: DashboardPlan[];
};

const formatNumber = (value: number) => new Intl.NumberFormat('ko-KR').format(value ?? 0);
const formatMoney = (value: number, currency = 'KRW') => new Intl.NumberFormat('ko-KR', {
  style: 'currency', currency, maximumFractionDigits: currency === 'KRW' ? 0 : 2,
}).format(value ?? 0);
const formatDate = (value: string | null) => value
  ? new Intl.DateTimeFormat('ko-KR', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).format(new Date(value))
  : '-';
const percentage = (value: number, total: number) => total > 0 ? Math.round((value / total) * 100) : 0;
const productTypeLabels: Record<string, string> = { TICKET: '입장권', TOUR: '투어', TRANSFER: '이동·교통', SIM: 'eSIM', ETC: '기타 체험' };
const productTypeColors: Record<string, string> = { TICKET: 'bg-indigo-400', TOUR: 'bg-cyan-400', TRANSFER: 'bg-emerald-400', SIM: 'bg-amber-400', ETC: 'bg-fuchsia-400' };
const paymentLabels: Record<string, string> = { PAID: '결제 완료', PENDING: '결제 대기', FAILED: '결제 실패', REFUNDED: '환불' };
const paymentColors: Record<string, string> = {
  PAID: 'bg-emerald-500/10 text-emerald-300', PENDING: 'bg-amber-500/10 text-amber-300',
  FAILED: 'bg-rose-500/10 text-rose-300', REFUNDED: 'bg-slate-500/10 text-slate-400',
};

/** 회원·상품·예약·일정 운영 지표와 최근 활동을 보여 주는 관리자 첫 화면이다. */
export const AdminDashboard = () => {
  const navigate = useNavigate();
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadDashboard = useCallback(async () => {
    setLoading(true); setError(null);
    try {
      setData((await axios.get<DashboardData>('/api/admin/dashboard')).data);
    } catch (loadError) {
      if (axios.isAxiosError(loadError) && [401, 403].includes(loadError.response?.status ?? 0)) {
        navigate('/admin/login', { replace: true }); return;
      }
      setError('대시보드 데이터를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
    } finally { setLoading(false); }
  }, [navigate]);

  useEffect(() => { void loadDashboard(); }, [loadDashboard]);

  const cards = useMemo(() => data ? [
    { label: '전체 회원', value: formatNumber(data.summary.totalUsers), detail: `활성 ${formatNumber(data.summary.activeUsers)}명 · ${percentage(data.summary.activeUsers, data.summary.totalUsers)}%`, icon: 'fa-users', color: 'bg-indigo-500/15 text-indigo-300' },
    { label: '여행 일정', value: formatNumber(data.summary.totalPlans), detail: `공개 ${formatNumber(data.summary.publicPlans)}개 · 조회 ${formatNumber(data.summary.totalViews)}회`, icon: 'fa-route', color: 'bg-cyan-500/15 text-cyan-300' },
    { label: '등록 여행지', value: formatNumber(data.summary.totalDestinations), detail: '국가·지역에 연결된 플레이스', icon: 'fa-location-dot', color: 'bg-emerald-500/15 text-emerald-300' },
    { label: '판매 상품', value: formatNumber(data.summary.activeProducts), detail: `전체 ${formatNumber(data.summary.totalProducts)}개 · 옵션 ${formatNumber(data.summary.activeOptions)}개`, icon: 'fa-ticket', color: 'bg-fuchsia-500/15 text-fuchsia-300' },
    { label: '전체 주문', value: formatNumber(data.summary.totalOrders), detail: `결제 완료 ${formatNumber(data.summary.paidOrders)}건 · 대기 ${formatNumber(data.summary.pendingPayments)}건`, icon: 'fa-bag-shopping', color: 'bg-amber-500/15 text-amber-300' },
    { label: '결제완료 금액', value: formatMoney(data.summary.paidRevenue), detail: '결제 상태 PAID 주문 합계', icon: 'fa-won-sign', color: 'bg-rose-500/15 text-rose-300' },
  ] : [], [data]);
  const maxProductCount = Math.max(1, ...(data?.productTypes.map((item) => item.totalCount) ?? [1]));

  return <>
    <header className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <div className="flex items-center gap-2 text-xs font-bold tracking-[0.2em] text-indigo-300"><span className="h-2 w-2 rounded-full bg-emerald-400 shadow-[0_0_12px_rgba(52,211,153,.8)]" />LIVE OPERATIONS</div>
        <h1 className="mt-3 text-3xl font-extrabold tracking-tight text-white">RouteMate 운영 대시보드</h1>
        <p className="mt-2 text-sm text-slate-500">현재 데이터베이스의 서비스·상품·주문 현황을 한눈에 확인하세요.</p>
      </div>
      <div className="flex items-center gap-3">
        <Link to="/products" target="_blank" className="rounded-xl border border-white/10 bg-white/[0.035] px-4 py-2.5 text-xs font-semibold text-slate-300 transition hover:bg-white/[0.07] hover:text-white"><i className="fa-solid fa-arrow-up-right-from-square mr-2" />상품몰 보기</Link>
        <button type="button" onClick={() => void loadDashboard()} disabled={loading} className="rounded-xl bg-indigo-500 px-4 py-2.5 text-xs font-bold text-white transition hover:bg-indigo-400 disabled:opacity-50"><i className={`fa-solid fa-rotate-right mr-2 ${loading ? 'fa-spin' : ''}`} />새로고침</button>
      </div>
    </header>

    {loading && !data ? <div className="flex min-h-[560px] items-center justify-center text-sm text-slate-500"><i className="fa-solid fa-spinner fa-spin mr-2 text-indigo-400" />운영 데이터를 집계하고 있습니다...</div>
      : error && !data ? <section className="mt-10 rounded-[24px] border border-rose-400/15 bg-rose-400/5 p-10 text-center"><i className="fa-solid fa-triangle-exclamation text-2xl text-rose-300" /><p className="mt-4 text-sm text-rose-200">{error}</p><button type="button" onClick={() => void loadDashboard()} className="mt-5 rounded-xl bg-white/10 px-4 py-2.5 text-sm font-semibold text-white">다시 시도</button></section>
      : data ? <>
        {error ? <p className="mt-5 rounded-xl border border-rose-400/15 bg-rose-400/5 px-4 py-3 text-sm text-rose-200">{error}</p> : null}

        <section className="mt-8 grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">
          {cards.map((card) => <article key={card.label} className="group rounded-[22px] border border-white/10 bg-white/[0.035] p-5 shadow-lg shadow-black/10 transition hover:-translate-y-0.5 hover:border-white/15 hover:bg-white/[0.05]">
            <div className="flex items-start justify-between gap-3"><div className="min-w-0"><p className="text-xs font-semibold text-slate-500">{card.label}</p><p className="mt-3 truncate text-2xl font-extrabold tracking-tight text-white">{card.value}</p></div><span className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl ${card.color}`}><i className={`fa-solid ${card.icon}`} /></span></div>
            <p className="mt-3 truncate text-[11px] text-slate-600">{card.detail}</p>
          </article>)}
        </section>

        <section className="mt-6 grid gap-6 xl:grid-cols-[0.8fr_1.45fr]">
          <article className="rounded-[24px] border border-white/10 bg-white/[0.03] p-5 sm:p-6">
            <div className="flex items-center justify-between"><div><h2 className="font-bold text-white">상품 포트폴리오</h2><p className="mt-1 text-xs text-slate-600">상품 유형별 등록·판매 현황</p></div><Link to="/admin/partners" className="text-xs font-semibold text-indigo-300 hover:text-indigo-200">파트너사 관리 <i className="fa-solid fa-chevron-right ml-1" /></Link></div>
            <div className="mt-6 space-y-5">
              {data.productTypes.map((item) => <div key={item.productType}>
                <div className="mb-2 flex items-center justify-between gap-4 text-xs"><span className="flex items-center gap-2 font-semibold text-slate-300"><span className={`h-2 w-2 rounded-full ${productTypeColors[item.productType] ?? 'bg-slate-400'}`} />{productTypeLabels[item.productType] ?? item.productType}</span><span className="text-slate-600"><strong className="text-slate-300">{formatNumber(item.activeCount)}</strong> 판매 / {formatNumber(item.totalCount)}</span></div>
                <div className="h-2 overflow-hidden rounded-full bg-white/5"><div className={`h-full rounded-full ${productTypeColors[item.productType] ?? 'bg-slate-400'}`} style={{ width: `${Math.max(4, (item.totalCount / maxProductCount) * 100)}%` }} /></div>
              </div>)}
              {data.productTypes.length === 0 ? <p className="py-12 text-center text-sm text-slate-600">등록된 상품이 없습니다.</p> : null}
            </div>
            <div className="mt-6 grid grid-cols-2 gap-3 border-t border-white/5 pt-5">
              <div className="rounded-2xl bg-black/10 p-4"><p className="text-[11px] text-slate-600">상품 판매율</p><p className="mt-2 text-xl font-extrabold text-fuchsia-300">{percentage(data.summary.activeProducts, data.summary.totalProducts)}%</p></div>
              <div className="rounded-2xl bg-black/10 p-4"><p className="text-[11px] text-slate-600">옵션 활성률</p><p className="mt-2 text-xl font-extrabold text-cyan-300">{percentage(data.summary.activeOptions, data.summary.totalOptions)}%</p></div>
            </div>
          </article>

          <article className="overflow-hidden rounded-[24px] border border-white/10 bg-white/[0.03]">
            <div className="flex items-center justify-between border-b border-white/10 px-5 py-5 sm:px-6"><div><h2 className="font-bold text-white">최근 상품 주문</h2><p className="mt-1 text-xs text-slate-600">가장 최근 접수된 주문 5건</p></div><span className="rounded-full bg-amber-500/10 px-3 py-1 text-xs font-semibold text-amber-300">결제 대기 {formatNumber(data.summary.pendingPayments)}</span></div>
            <div className="overflow-x-auto"><table className="w-full min-w-[780px] text-left">
              <thead className="border-b border-white/5 bg-black/10 text-[10px] uppercase tracking-wider text-slate-600"><tr><th className="px-6 py-3 font-semibold">주문 / 상품</th><th className="px-4 py-3 font-semibold">여행지</th><th className="px-4 py-3 font-semibold">이용일</th><th className="px-4 py-3 font-semibold">결제금액</th><th className="px-4 py-3 font-semibold">결제</th><th className="px-6 py-3 font-semibold">접수</th></tr></thead>
              <tbody className="divide-y divide-white/5">{data.recentOrders.map((order) => <tr key={order.orderId} className="transition hover:bg-white/[0.025]">
                <td className="px-6 py-4"><p className="max-w-[240px] truncate text-sm font-semibold text-slate-200">{order.productName}</p><p className="mt-1 max-w-[240px] truncate text-[11px] text-slate-600">{order.orderNo} · {order.optionName} × {order.quantity}</p></td>
                <td className="max-w-[150px] truncate px-4 py-4 text-xs text-slate-500">{order.destinationName}</td><td className="px-4 py-4 text-xs text-slate-500">{order.useDate}</td><td className="px-4 py-4 text-xs font-bold text-white">{formatMoney(order.totalPrice, order.currency)}</td>
                <td className="px-4 py-4"><span className={`rounded-full px-2.5 py-1 text-[10px] font-bold ${paymentColors[order.paymentStatus] ?? paymentColors.PENDING}`}>{paymentLabels[order.paymentStatus] ?? order.paymentStatus}</span></td><td className="px-6 py-4 text-xs text-slate-600">{formatDate(order.createDt)}</td>
              </tr>)}{data.recentOrders.length === 0 ? <tr><td colSpan={6} className="px-6 py-16 text-center text-sm text-slate-600"><i className="fa-solid fa-bag-shopping mb-3 block text-xl text-slate-700" />아직 접수된 상품 주문이 없습니다.</td></tr> : null}</tbody>
            </table></div>
          </article>
        </section>

        <section className="mt-6 grid gap-6 xl:grid-cols-[1.45fr_0.8fr]">
          <article className="overflow-hidden rounded-[24px] border border-white/10 bg-white/[0.03]">
            <div className="flex items-center justify-between border-b border-white/10 px-5 py-5 sm:px-6"><div><h2 className="font-bold text-white">최근 등록 일정</h2><p className="mt-1 text-xs text-slate-600">최근 생성된 여행 일정 5개</p></div><span className="rounded-full bg-white/5 px-3 py-1 text-xs text-slate-500">전체 {formatNumber(data.summary.totalPlans)}</span></div>
            <div className="overflow-x-auto"><table className="w-full min-w-[640px] text-left"><thead className="border-b border-white/5 bg-black/10 text-[10px] uppercase tracking-wider text-slate-600"><tr><th className="px-6 py-3 font-semibold">일정</th><th className="px-4 py-3 font-semibold">작성자</th><th className="px-4 py-3 font-semibold">공개</th><th className="px-4 py-3 font-semibold">조회</th><th className="px-6 py-3 font-semibold">등록</th></tr></thead>
              <tbody className="divide-y divide-white/5">{data.recentPlans.map((plan) => <tr key={plan.planId} className="transition hover:bg-white/[0.025]"><td className="max-w-[280px] truncate px-6 py-4 text-sm font-semibold text-slate-200">{plan.title}</td><td className="px-4 py-4 text-xs text-slate-500">{plan.userNicknm}</td><td className="px-4 py-4"><span className={`rounded-full px-2.5 py-1 text-[10px] font-bold ${plan.isPublic === 'Y' ? 'bg-emerald-500/10 text-emerald-300' : 'bg-slate-500/10 text-slate-400'}`}>{plan.isPublic === 'Y' ? '공개' : '비공개'}</span></td><td className="px-4 py-4 text-xs text-slate-500">{formatNumber(plan.viewCount)}</td><td className="px-6 py-4 text-xs text-slate-600">{formatDate(plan.createDt)}</td></tr>)}{data.recentPlans.length === 0 ? <tr><td colSpan={5} className="px-6 py-14 text-center text-sm text-slate-600">등록된 일정이 없습니다.</td></tr> : null}</tbody>
            </table></div>
          </article>
          <article className="rounded-[24px] border border-white/10 bg-white/[0.03] p-5 sm:p-6">
            <div className="flex items-center justify-between"><div><h2 className="font-bold text-white">인기 일정 TOP 5</h2><p className="mt-1 text-xs text-slate-600">공개 일정 조회수 기준</p></div><i className="fa-solid fa-ranking-star text-amber-300" /></div>
            <ol className="mt-5 space-y-3">{data.popularPlans.map((plan, index) => <li key={plan.planId} className="flex items-center gap-3 rounded-2xl border border-white/5 bg-black/10 p-3.5"><span className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-xl text-sm font-extrabold ${index === 0 ? 'bg-amber-400/15 text-amber-300' : 'bg-white/5 text-slate-500'}`}>{index + 1}</span><div className="min-w-0 flex-1"><p className="truncate text-sm font-semibold text-slate-200">{plan.title}</p><p className="mt-1 truncate text-xs text-slate-600">{plan.userNicknm}</p></div><span className="shrink-0 text-xs font-semibold text-slate-500"><i className="fa-regular fa-eye mr-1.5 text-cyan-400" />{formatNumber(plan.viewCount)}</span></li>)}{data.popularPlans.length === 0 ? <li className="py-12 text-center text-sm text-slate-600">공개 일정이 없습니다.</li> : null}</ol>
          </article>
        </section>
      </> : null}
  </>;
};
