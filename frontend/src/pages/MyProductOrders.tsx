import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { api } from '../lib/http';
import {
  formatProductPrice,
  hasExternalBookingUrl,
  orderStatusLabel,
  paymentStatusLabel,
  type ProductOrder,
} from '../features/products/model';

/** 현재 로그인 사용자의 옵션 상품 예약 내역을 표시하는 마이페이지 화면이다. */
export const MyProductOrders = () => {
  const [orders, setOrders] = useState<ProductOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [payingId, setPayingId] = useState<number | null>(null);

  useEffect(() => {
    axios.get<ProductOrder[]>('/api/product-orders/my')
      .then((response) => setOrders(response.data))
      .catch(() => setError('예약내역을 불러오지 못했습니다. 다시 로그인해 주세요.'))
      .finally(() => setLoading(false));
  }, []);

  const pay = async (orderId: number) => {
    setPayingId(orderId); setError(null);
    try { const prepared = (await api.post<{ paymentId: number }>(`/api/payments/orders/${orderId}/prepare`)).data; await api.post(`/api/payments/${prepared.paymentId}/complete`); setOrders((current) => current.map((order) => order.orderId === orderId ? { ...order, paymentStatus: 'PAID', orderStatus: 'CONFIRMED' } : order)); }
    catch { setError('결제 처리에 실패했습니다.'); }
    finally { setPayingId(null); }
  };

  return (
    <main className="relative z-10 mx-auto w-full max-w-5xl flex-grow px-6 py-12">
      <div className="mb-8 flex flex-col justify-between gap-4 sm:flex-row sm:items-end"><div><p className="mb-2 text-xs font-bold tracking-widest text-indigo-300">MY ORDERS</p><h1 className="text-3xl font-extrabold text-white">옵션상품 예약내역</h1><p className="mt-2 text-sm text-slate-400">접수한 주문과 결제 상태를 확인하세요.</p></div><Link to="/products" className="theme-btn-primary self-start px-5 py-3 text-sm">상품 둘러보기</Link></div>
      {loading ? <div className="py-24 text-center text-slate-400"><i className="fa-solid fa-spinner fa-spin mb-3 text-3xl text-indigo-400" /><p>예약내역을 불러오고 있습니다...</p></div> : error ? <div role="alert" className="rounded-2xl border border-red-400/20 bg-red-500/10 p-8 text-center text-red-200">{error}</div> : orders.length === 0 ? <div className="rounded-3xl border border-white/10 bg-white/[0.03] py-20 text-center"><i className="fa-solid fa-bag-shopping mb-4 text-4xl text-slate-600" /><p className="font-semibold text-white">아직 예약한 옵션상품이 없습니다.</p><p className="mt-2 text-sm text-slate-500">여행에 필요한 상품을 둘러보고 첫 주문을 시작해 보세요.</p></div> : <div className="space-y-4">{orders.map((order) => (
        <article key={order.orderId} className="overflow-hidden rounded-2xl border border-white/10 bg-slate-900/70 p-5 md:flex md:items-center md:gap-6">
          <div className="h-36 w-full shrink-0 overflow-hidden rounded-xl bg-slate-950 md:h-28 md:w-40">{order.productImageUrl ? <img src={order.productImageUrl} alt={order.productName} className="h-full w-full object-cover" /> : <div className="flex h-full items-center justify-center text-3xl text-slate-700"><i className="fa-solid fa-ticket" /></div>}</div>
          <div className="mt-4 min-w-0 flex-grow md:mt-0"><div className="mb-2 flex flex-wrap items-center gap-2"><span className="rounded-full bg-indigo-500/15 px-2.5 py-1 text-xs font-semibold text-indigo-200">{orderStatusLabel(order.orderStatus)}</span><span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${order.paymentStatus === 'PAID' ? 'bg-emerald-500/15 text-emerald-200' : 'bg-amber-500/15 text-amber-200'}`}>{paymentStatusLabel(order.paymentStatus)}</span></div><h2 className="truncate text-lg font-bold text-white">{order.productName}</h2><div className="mt-1 space-y-1 text-sm text-slate-400">{order.items.map((item) => <p key={`${item.optionId}-${item.optionName}`}>{item.optionName} · {item.quantity}개</p>)}</div><p className="mt-2 truncate text-xs text-slate-500"><i className="fa-solid fa-location-dot mr-1" />{order.destinationName}</p><p className="mt-1 text-xs text-slate-500">이용일 {order.useDate} · 주문번호 {order.orderNo}</p></div>
          <div className="mt-5 shrink-0 border-t border-white/10 pt-4 text-right md:mt-0 md:border-l md:border-t-0 md:pl-6 md:pt-0"><p className="text-xs text-slate-500">주문금액</p><p className="mt-1 text-xl font-bold text-white">{formatProductPrice(order.totalPrice, order.currency)}</p><div className="mt-3 flex justify-end gap-2">{order.paymentStatus === 'PENDING' ? <button type="button" onClick={() => void pay(order.orderId)} disabled={payingId === order.orderId} className="rounded-lg bg-emerald-500 px-3 py-2 text-xs font-semibold text-white disabled:opacity-50">{payingId === order.orderId ? '결제 중...' : '모의 결제'}</button> : null}{order.productId ? <Link to={`/products/${order.productId}`} className="rounded-lg bg-white/5 px-3 py-2 text-xs text-slate-300 hover:bg-white/10">상품 보기</Link> : null}{hasExternalBookingUrl(order.bookingUrl) ? <a href={order.bookingUrl!} target="_blank" rel="noreferrer" className="rounded-lg bg-indigo-500 px-3 py-2 text-xs font-semibold text-white hover:bg-indigo-400">결제 페이지</a> : null}</div></div>
        </article>
      ))}</div>}
    </main>
  );
};
