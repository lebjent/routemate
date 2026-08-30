import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../hooks/useAuth';
import {
  formatProductPrice,
  hasExternalBookingUrl,
  paymentStatusLabel,
  productTypeLabel,
  type ProductDetail as ProductDetailData,
  type ProductOrder,
} from '../features/products/model';

const today = () => {
  const date = new Date();
  const offset = date.getTimezoneOffset() * 60_000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 10);
};

const DetailSection = ({ title, icon, text }: { title: string; icon: string; text: string | null }) => {
  if (!text) return null;
  return <section className="border-t border-white/10 py-7"><h2 className="mb-4 flex items-center gap-2 text-xl font-bold text-white"><i className={`${icon} text-indigo-300`} />{title}</h2><p className="whitespace-pre-line text-sm leading-7 text-slate-300">{text}</p></section>;
};

/** 상품 상세와 판매 옵션을 보여 주고, 이용일과 수량을 선택해 예약을 시작하는 화면이다. */
export const ProductDetail = () => {
  const { productId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const [product, setProduct] = useState<ProductDetailData | null>(null);
  const [optionQuantities, setOptionQuantities] = useState<Record<number, number>>({});
  const [useDate, setUseDate] = useState(today());
  const [buyerName, setBuyerName] = useState(user?.userNicknm ?? '');
  const [buyerEmail, setBuyerEmail] = useState(user?.userEmail ?? '');
  const [buyerPhone, setBuyerPhone] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [order, setOrder] = useState<ProductOrder | null>(null);

  useEffect(() => {
    setBuyerName((value) => value || user?.userNicknm || '');
    setBuyerEmail((value) => value || user?.userEmail || '');
  }, [user]);

  useEffect(() => {
    setLoading(true);
    axios.get<ProductDetailData>(`/api/public/products/${productId}`)
      .then((response) => {
        setProduct(response.data);
        setOptionQuantities({});
      })
      .catch(() => setError('판매 중인 상품을 찾을 수 없습니다.'))
      .finally(() => setLoading(false));
  }, [productId]);

  const selectedOptions = useMemo(() => product?.options
    .map((option) => ({ option, quantity: optionQuantities[option.optionId] ?? 0 }))
    .filter((selection) => selection.quantity > 0) ?? [], [product, optionQuantities]);
  const totalPrice = selectedOptions.reduce((sum, selection) => sum + selection.option.price * selection.quantity, 0);
  const orderCurrency = selectedOptions[0]?.option.currency ?? product?.options[0]?.currency ?? 'KRW';

  /** 옵션별 수량을 0~10 범위에서 변경한다. */
  const changeOptionQuantity = (optionId: number, change: number) => {
    setOptionQuantities((current) => ({
      ...current,
      [optionId]: Math.max(0, Math.min(10, (current[optionId] ?? 0) + change)),
    }));
  };

  const submitOrder = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!user) {
      navigate('/login', { state: { from: location.pathname } });
      return;
    }
    if (!product || selectedOptions.length === 0) {
      setError('구매할 옵션과 수량을 하나 이상 선택해 주세요.');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const response = await axios.post<ProductOrder>('/api/product-orders', {
        productId: product.productId,
        items: selectedOptions.map((selection) => ({ optionId: selection.option.optionId, quantity: selection.quantity })),
        useDate,
        buyerName,
        buyerEmail,
        buyerPhone,
      });
      setOrder(response.data);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (requestError) {
      if (axios.isAxiosError(requestError)) {
        setError(requestError.response?.data?.detail ?? '주문을 접수하지 못했습니다. 입력 내용을 확인해 주세요.');
      } else {
        setError('주문을 접수하지 못했습니다. 잠시 후 다시 시도해 주세요.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <main className="flex flex-grow items-center justify-center py-24 text-slate-400"><i className="fa-solid fa-spinner fa-spin mr-3 text-2xl text-indigo-400" />상품을 불러오고 있습니다...</main>;
  if (!product) return <main className="mx-auto w-full max-w-3xl flex-grow px-6 py-24 text-center"><p className="mb-6 text-red-200">{error}</p><Link to="/products" className="theme-btn-primary inline-block px-6 py-3">상품 목록으로</Link></main>;

  if (order) return (
    <main className="relative z-10 mx-auto w-full max-w-3xl flex-grow px-6 py-16">
      <section className="rounded-3xl border border-emerald-400/20 bg-emerald-500/[0.07] p-7 text-center md:p-10">
        <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-full bg-emerald-400/15 text-3xl text-emerald-300"><i className="fa-solid fa-check" /></div>
        <p className="mb-2 text-sm font-semibold text-emerald-300">ORDER RECEIVED</p>
        <h1 className="mb-3 text-3xl font-extrabold text-white">주문이 접수되었습니다</h1>
        <p className="text-sm leading-6 text-slate-300">주문번호 <strong className="text-white">{order.orderNo}</strong><br />현재 {paymentStatusLabel(order.paymentStatus)} 상태이며 결제가 확인되면 예약이 확정됩니다.</p>
        <div className="my-7 rounded-2xl border border-white/10 bg-slate-950/50 p-5 text-left">
          <p className="font-bold text-white">{order.productName}</p>
          <div className="mt-2 space-y-1 text-sm text-slate-400">{order.items.map((item) => <p key={`${item.optionId}-${item.optionName}`}>{item.optionName} · {item.quantity}개</p>)}</div>
          <p className="mt-4 text-right text-xl font-bold text-indigo-200">{formatProductPrice(order.totalPrice, order.currency)}</p>
        </div>
        <div className="flex flex-col justify-center gap-3 sm:flex-row">
          <Link to="/my-product-orders" className="theme-btn-primary px-6 py-3">내 예약내역 보기</Link>
          {hasExternalBookingUrl(order.bookingUrl) ? <a href={order.bookingUrl!} target="_blank" rel="noreferrer" className="rounded-xl border border-white/15 bg-white/5 px-6 py-3 font-semibold text-white hover:bg-white/10">판매처 결제 페이지로 이동</a> : null}
          <Link to="/products" className="rounded-xl border border-white/15 bg-white/5 px-6 py-3 font-semibold text-white hover:bg-white/10">상품 더 보기</Link>
        </div>
      </section>
    </main>
  );

  return (
    <main className="relative z-10 mx-auto w-full max-w-7xl flex-grow px-6 pb-16 pt-8">
      <div className="mb-5 text-sm text-slate-500"><Link to="/products" className="hover:text-indigo-300">옵션상품</Link><span className="mx-2">/</span><span>{product.countryName} · {product.regionName}</span></div>
      <section className="relative mb-9 overflow-hidden rounded-3xl border border-white/10 bg-slate-900">
        <div className="h-[330px] md:h-[460px]">{product.imageUrl ? <img src={product.imageUrl} alt={product.productName} className="h-full w-full object-cover" /> : <div className="flex h-full items-center justify-center text-6xl text-slate-700"><i className="fa-solid fa-ticket" /></div>}</div>
        <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/30 to-transparent" />
        <div className="absolute inset-x-0 bottom-0 p-6 md:p-10">
          <div className="mb-3 flex flex-wrap gap-2"><span className="rounded-full bg-indigo-500 px-3 py-1 text-xs font-bold text-white">{productTypeLabel(product.productType)}</span><span className="rounded-full bg-black/50 px-3 py-1 text-xs text-white backdrop-blur"><i className="fa-solid fa-location-dot mr-1.5 text-red-300" />{product.countryName} · {product.regionName} · {product.destinationName}</span></div>
          <h1 className="max-w-4xl text-3xl font-extrabold leading-tight text-white md:text-4xl">{product.productName}</h1>
          {product.productSummary ? <p className="mt-3 max-w-3xl text-sm leading-6 text-slate-200 md:text-base">{product.productSummary}</p> : null}
        </div>
      </section>

      <div className="grid gap-8 lg:grid-cols-[minmax(0,1fr)_390px]">
        <div className="min-w-0 rounded-3xl border border-white/10 bg-slate-900/60 px-6 md:px-8">
          <section className="py-7"><div className="grid gap-4 sm:grid-cols-3"><div className="rounded-2xl bg-white/[0.04] p-4"><p className="text-xs text-slate-500">운영사</p><p className="mt-1 font-semibold text-white">{product.providerName ?? 'RouteMate Partner'}</p></div><div className="rounded-2xl bg-white/[0.04] p-4"><p className="text-xs text-slate-500">미팅 시간</p><p className="mt-1 font-semibold text-white">{product.meetingTime ?? '바우처 개별 안내'}</p></div><div className="rounded-2xl bg-white/[0.04] p-4"><p className="text-xs text-slate-500">미팅 장소</p><p className="mt-1 line-clamp-2 font-semibold text-white">{product.meetingPlace ?? '바우처 개별 안내'}</p></div></div></section>
          <DetailSection title="상품 소개" icon="fa-solid fa-circle-info" text={product.productDesc} />
          {product.detailImageUrl ? <img src={product.detailImageUrl} alt={`${product.productName} 상세`} className="mb-7 max-h-[560px] w-full rounded-2xl object-cover" /> : null}
          <DetailSection title="이용 코스" icon="fa-solid fa-route" text={product.courseText} />
          <DetailSection title="포함 사항" icon="fa-solid fa-circle-check" text={product.includedText} />
          <DetailSection title="불포함 사항" icon="fa-solid fa-circle-xmark" text={product.excludedText} />
          <DetailSection title="이용 방법" icon="fa-solid fa-mobile-screen-button" text={product.usageGuideText} />
          <DetailSection title="예약 전 확인" icon="fa-solid fa-triangle-exclamation" text={product.noticeText} />
          <DetailSection title="취소 및 환불" icon="fa-solid fa-rotate-left" text={product.cancellationPolicyText} />
          <DetailSection title="자주 묻는 질문" icon="fa-regular fa-circle-question" text={product.faqText} />
        </div>

        <aside className="h-fit rounded-3xl border border-indigo-400/20 bg-slate-900/95 p-6 shadow-2xl shadow-black/30 lg:sticky lg:top-6">
          <h2 className="mb-1 text-xl font-bold text-white">옵션 선택</h2><p className="mb-5 text-xs text-slate-500">판매 중인 옵션 가격은 서버에서 다시 확인됩니다.</p>
          {product.options.length === 0 ? <p className="rounded-xl bg-white/5 p-5 text-center text-sm text-slate-400">현재 구매 가능한 옵션이 없습니다.</p> : (
            <form onSubmit={submitOrder} className="space-y-5">
              <div className="option-scrollbar max-h-72 space-y-2 overflow-y-auto pr-1">
                {product.options.map((option) => (
                  <div key={option.optionId} className={`rounded-2xl border p-4 transition ${(optionQuantities[option.optionId] ?? 0) > 0 ? 'border-indigo-400 bg-indigo-500/10' : 'border-white/10 bg-white/[0.025]'}`}>
                    <div className="flex items-start justify-between gap-3"><div><p className="text-sm font-bold text-white">{option.optionName}</p>{option.optionDesc ? <p className="mt-1 text-xs leading-5 text-slate-400">{option.optionDesc}</p> : null}</div><p className="shrink-0 font-bold text-indigo-200">{formatProductPrice(option.price, option.currency)}</p></div>
                    <div className="mt-3 flex items-center justify-between border-t border-white/10 pt-3"><span className="text-xs text-slate-500">수량</span><div className="flex items-center gap-3"><button type="button" aria-label={`${option.optionName} 수량 감소`} onClick={() => changeOptionQuantity(option.optionId, -1)} disabled={(optionQuantities[option.optionId] ?? 0) === 0} className="flex h-8 w-8 items-center justify-center rounded-lg bg-white/5 text-slate-300 disabled:opacity-30"><i className="fa-solid fa-minus" /></button><strong className="w-5 text-center text-sm text-white">{optionQuantities[option.optionId] ?? 0}</strong><button type="button" aria-label={`${option.optionName} 수량 증가`} onClick={() => changeOptionQuantity(option.optionId, 1)} disabled={(optionQuantities[option.optionId] ?? 0) === 10} className="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-500 text-white disabled:opacity-30"><i className="fa-solid fa-plus" /></button></div></div>
                  </div>
                ))}
              </div>
              <label className="text-xs font-semibold text-slate-400">이용일<input type="date" min={today()} required value={useDate} onChange={(event) => setUseDate(event.target.value)} className="mt-2 w-full rounded-xl border border-white/10 bg-slate-950 px-3 py-3 text-sm text-white outline-none focus:border-indigo-400" /></label>
              <label className="block text-xs font-semibold text-slate-400">구매자명<input required maxLength={50} value={buyerName} onChange={(event) => setBuyerName(event.target.value)} className="mt-2 w-full rounded-xl border border-white/10 bg-slate-950 px-3 py-3 text-sm text-white outline-none focus:border-indigo-400" /></label>
              <label className="block text-xs font-semibold text-slate-400">안내 이메일<input type="email" required maxLength={100} value={buyerEmail} onChange={(event) => setBuyerEmail(event.target.value)} className="mt-2 w-full rounded-xl border border-white/10 bg-slate-950 px-3 py-3 text-sm text-white outline-none focus:border-indigo-400" /></label>
              <label className="block text-xs font-semibold text-slate-400">연락처 <span className="font-normal text-slate-600">(선택)</span><input maxLength={20} value={buyerPhone} onChange={(event) => setBuyerPhone(event.target.value)} placeholder="010-0000-0000" className="mt-2 w-full rounded-xl border border-white/10 bg-slate-950 px-3 py-3 text-sm text-white outline-none placeholder:text-slate-700 focus:border-indigo-400" /></label>
              {selectedOptions.length > 0 ? <div className="border-t border-white/10 pt-4"><div className="flex items-end justify-between"><span className="text-sm text-slate-400">총 주문금액 · {selectedOptions.length}종</span><strong className="text-2xl text-white">{formatProductPrice(totalPrice, orderCurrency)}</strong></div><div className="mt-3 space-y-1 text-xs text-slate-500">{selectedOptions.map((selection) => <p key={selection.option.optionId}>{selection.option.optionName} {selection.quantity}개 · {formatProductPrice(selection.option.price * selection.quantity, selection.option.currency)}</p>)}</div></div> : null}
              {error ? <p role="alert" className="rounded-xl bg-red-500/10 px-3 py-2 text-xs leading-5 text-red-200">{error}</p> : null}
              <button type="submit" disabled={submitting || selectedOptions.length === 0} className="theme-btn-primary w-full py-4 text-base disabled:cursor-not-allowed disabled:opacity-50">{submitting ? '주문 접수 중...' : user ? '구매 신청하기' : '로그인하고 구매하기'}</button>
              <p className="text-center text-[11px] leading-5 text-slate-600">주문 접수 후 결제 상태는 예약내역에서 확인할 수 있습니다.</p>
            </form>
          )}
        </aside>
      </div>
    </main>
  );
};
