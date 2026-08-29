import { useCallback, useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

type DashboardData = {
  partnerName: string;
  totalProducts: number;
  activeProducts: number;
  totalOrders: number;
  paidRevenue: number;
  popularProducts: Array<{ productId: number; productName: string }>;
};

const formatNumber = (value: number) => new Intl.NumberFormat('ko-KR').format(value ?? 0);
const formatMoney = (value: number) => new Intl.NumberFormat('ko-KR', {
  style: 'currency', currency: 'KRW', maximumFractionDigits: 0,
}).format(value ?? 0);

/** 로그인한 파트너사의 판매량, 상품 상태, 최근 등록 상품을 보여 주는 첫 화면이다. */
export const PartnerDashboard = () => {
  const { user, loading: authLoading, logout } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setData((await axios.get<DashboardData>('/api/partner/dashboard')).data);
    } catch (loadError) {
      setError(axios.isAxiosError(loadError)
        ? loadError.response?.data?.detail ?? '운영 데이터를 불러오지 못했습니다.'
        : '운영 데이터를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!authLoading && user?.userRole !== 'PARTNER_OWNER') navigate('/partner/login', { replace: true });
  }, [authLoading, navigate, user]);

  useEffect(() => {
    if (user?.userRole === 'PARTNER_OWNER') void load();
  }, [load, user]);

  const cards = useMemo(() => data ? [
    { label: '등록 상품', value: formatNumber(data.totalProducts), detail: '검토 대기 상품을 포함한 전체', icon: 'fa-ticket', color: 'bg-indigo-500/15 text-indigo-300' },
    { label: '판매중 상품', value: formatNumber(data.activeProducts), detail: `상품 활성화율 ${data.totalProducts ? Math.round((data.activeProducts / data.totalProducts) * 100) : 0}%`, icon: 'fa-circle-check', color: 'bg-emerald-500/15 text-emerald-300' },
    { label: '접수 주문', value: formatNumber(data.totalOrders), detail: '내 옵션상품 기준 주문 건수', icon: 'fa-bag-shopping', color: 'bg-amber-500/15 text-amber-300' },
    { label: '결제 완료 금액', value: formatMoney(data.paidRevenue), detail: '결제 상태 PAID 주문 합계', icon: 'fa-won-sign', color: 'bg-fuchsia-500/15 text-fuchsia-300' },
  ] : [], [data]);

  return (
    <main className="min-h-screen flex-grow bg-slate-950 px-4 py-8 text-white sm:px-6">
      <div className="mx-auto max-w-7xl">
        <header className="flex flex-col gap-5 border-b border-white/10 pb-6 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <Link to="/" className="text-sm font-bold text-indigo-300">RouteMate Partner</Link>
            <div className="mt-5 flex items-center gap-2 text-xs font-bold tracking-[0.2em] text-indigo-300"><span className="h-2 w-2 rounded-full bg-emerald-400" />PARTNER OPERATIONS</div>
            <h1 className="mt-3 text-3xl font-extrabold tracking-tight">{data?.partnerName ?? '파트너사'} 운영 대시보드</h1>
            <p className="mt-2 text-sm text-slate-500">내 상품 등록 현황과 판매 성과를 한눈에 확인하세요.</p>
            <nav className="mt-4 flex gap-4 text-sm font-semibold text-slate-400">
              <Link to="/partner" className="text-indigo-300">대시보드</Link>
              <Link to="/partner/products" className="hover:text-indigo-200">옵션상품관리</Link>
              <Link to="/partner/staff" className="hover:text-indigo-200">직원 관리</Link>
            </nav>
          </div>
          <div className="flex gap-2">
            <button type="button" onClick={() => void logout().then(() => navigate('/partner/login'))} className="rounded-xl border border-white/10 bg-white/[0.035] px-3 py-2.5 text-xs font-semibold text-slate-300">로그아웃</button>
            <button type="button" onClick={() => void load()} disabled={loading} className="rounded-xl bg-indigo-500 px-4 py-2.5 text-xs font-bold text-white disabled:opacity-50"><i className={`fa-solid fa-rotate-right mr-2 ${loading ? 'fa-spin' : ''}`} />새로고침</button>
          </div>
        </header>

        {loading && !data ? <div className="flex min-h-[440px] items-center justify-center text-sm text-slate-500"><i className="fa-solid fa-spinner fa-spin mr-2 text-indigo-400" />운영 데이터를 집계하고 있습니다...</div> : null}
        {error ? <section className="mt-8 rounded-[24px] border border-rose-400/15 bg-rose-400/5 p-8 text-center"><p className="text-sm text-rose-200">{error}</p><button type="button" onClick={() => void load()} className="mt-4 rounded-xl bg-white/10 px-4 py-2 text-sm font-semibold">다시 시도</button></section> : null}

        {data ? <>
          <section className="mt-8 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {cards.map((card) => <article key={card.label} className="rounded-[22px] border border-white/10 bg-white/[0.035] p-5 shadow-lg shadow-black/10"><div className="flex items-start justify-between gap-3"><div><p className="text-xs font-semibold text-slate-500">{card.label}</p><p className="mt-3 text-2xl font-extrabold tracking-tight">{card.value}</p></div><span className={`flex h-10 w-10 items-center justify-center rounded-2xl ${card.color}`}><i className={`fa-solid ${card.icon}`} /></span></div><p className="mt-3 text-[11px] text-slate-600">{card.detail}</p></article>)}
          </section>
          <section className="mt-6 grid gap-6 xl:grid-cols-[1.1fr_.9fr]">
            <article className="rounded-[24px] border border-white/10 bg-white/[0.03] p-5 sm:p-6"><h2 className="font-bold">상품 운영 안내</h2><p className="mt-1 text-xs text-slate-600">등록부터 고객 노출까지의 진행 상태</p><div className="mt-6 grid gap-3 sm:grid-cols-3"><div className="rounded-2xl bg-black/10 p-4"><span className="text-xs font-bold text-indigo-300">01</span><p className="mt-2 text-sm font-bold">상품 등록</p><p className="mt-1 text-xs leading-5 text-slate-600">상세 설명과 패키지 옵션을 구성합니다.</p></div><div className="rounded-2xl bg-black/10 p-4"><span className="text-xs font-bold text-amber-300">02</span><p className="mt-2 text-sm font-bold">관리자 검토</p><p className="mt-1 text-xs leading-5 text-slate-600">등록 상품은 승인 전까지 검토 대기입니다.</p></div><div className="rounded-2xl bg-black/10 p-4"><span className="text-xs font-bold text-emerald-300">03</span><p className="mt-2 text-sm font-bold">고객 판매</p><p className="mt-1 text-xs leading-5 text-slate-600">승인 완료 상품만 상품몰에 노출됩니다.</p></div></div><Link to="/partner/products" className="mt-6 inline-flex items-center rounded-xl bg-indigo-500 px-4 py-2.5 text-xs font-bold text-white">옵션상품관리 <i className="fa-solid fa-arrow-right ml-2" /></Link></article>
            <article className="rounded-[24px] border border-white/10 bg-white/[0.03] p-5 sm:p-6"><h2 className="font-bold">최근 등록 상품</h2><p className="mt-1 text-xs text-slate-600">항목을 클릭하면 상품 판매 정보를 수정할 수 있습니다.</p><ol className="mt-5 space-y-3">{data.popularProducts.map((product, index) => <li key={product.productId}><Link to={`/partner/products?edit=${product.productId}`} className="flex items-center gap-3 rounded-2xl border border-white/5 bg-black/10 p-3.5 transition hover:border-indigo-400/30 hover:bg-indigo-500/10"><span className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-xl text-sm font-extrabold ${index === 0 ? 'bg-amber-400/15 text-amber-300' : 'bg-white/5 text-slate-500'}`}>{index + 1}</span><p className="min-w-0 flex-1 truncate text-sm font-semibold text-slate-200">{product.productName}</p><i className="fa-solid fa-pen text-xs text-indigo-300" /></Link></li>)}{data.popularProducts.length === 0 ? <li className="py-10 text-center text-sm text-slate-600">등록된 상품이 없습니다.</li> : null}</ol></article>
          </section>
        </> : null}
      </div>
    </main>
  );
};
