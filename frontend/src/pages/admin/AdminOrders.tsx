import { useEffect, useState } from 'react';
import { api } from '../../lib/http';

type Order = { orderId: number; orderNo: string; productName: string; buyerName: string; totalPrice: number; currency: string; orderStatus: string; paymentStatus: string; useDate: string };

/** 관리자 주문 현황과 주문 상태를 관리한다. */
export const AdminOrders = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [error, setError] = useState('');
  const load = async () => { try { setOrders((await api.get<Order[]>('/api/admin/orders')).data); } catch { setError('주문을 불러오지 못했습니다.'); } };
  useEffect(() => { void load(); }, []);
  const change = async (orderId: number, status: string) => { try { await api.patch(`/api/admin/orders/${orderId}/status`, null, { params: { status } }); await load(); } catch (e) { setError('허용되지 않은 상태 변경입니다.'); } };
  return <section><header><p className="text-xs font-bold tracking-[0.2em] text-indigo-300">ORDER MANAGEMENT</p><h1 className="mt-2 text-3xl font-extrabold text-white">주문 관리</h1></header>{error && <p role="alert" className="mt-4 text-sm text-rose-300">{error}</p>}<div className="mt-8 overflow-x-auto rounded-2xl border border-white/10"><table className="w-full min-w-[850px] text-left text-sm"><thead className="border-b border-white/10 text-xs text-slate-500"><tr><th className="p-4">주문번호</th><th className="p-4">상품</th><th className="p-4">구매자</th><th className="p-4">이용일</th><th className="p-4">금액</th><th className="p-4">상태</th></tr></thead><tbody className="divide-y divide-white/5">{orders.map((order) => <tr key={order.orderId}><td className="p-4 text-xs text-slate-400">{order.orderNo}</td><td className="p-4 font-semibold text-white">{order.productName}</td><td className="p-4 text-slate-300">{order.buyerName}</td><td className="p-4 text-slate-400">{order.useDate}</td><td className="p-4 text-slate-200">{order.totalPrice.toLocaleString()} {order.currency}</td><td className="p-4"><select value={order.orderStatus} onChange={(event) => void change(order.orderId, event.target.value)} className="rounded-lg border border-white/10 bg-slate-900 px-2 py-2 text-xs text-white"><option value="ORDERED">접수</option><option value="CONFIRMED">확정</option><option value="COMPLETED">완료</option><option value="CANCELLED">취소</option></select></td></tr>)}</tbody></table></div></section>;
};
