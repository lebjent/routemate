export type BookedProduct = {
  orderId: number;
  orderNo: string;
  productName: string;
  optionName: string;
  destinationName: string;
  useDate: string;
  quantity: number;
  orderStatus: string;
  paymentStatus: string;
};

const formatUseDate = (value: string) => {
  if (!value) return '이용일 미정';
  const date = new Date(`${value}T00:00:00`);
  return Number.isNaN(date.getTime()) ? value : `${date.getMonth() + 1}월 ${date.getDate()}일 이용`;
};

export const BookedProductModal = ({
  isOpen,
  orders,
  linkedOrderIds,
  loading,
  dayLabel,
  regionLabel,
  onClose,
  onLink,
}: {
  isOpen: boolean;
  orders: BookedProduct[];
  linkedOrderIds: Set<number>;
  loading: boolean;
  dayLabel: string;
  regionLabel: string;
  onClose: () => void;
  onLink: (order: BookedProduct) => void;
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-slate-950/75 p-0 backdrop-blur-sm sm:items-center sm:p-5" role="dialog" aria-modal="true" aria-labelledby="booked-product-modal-title">
      <div className="max-h-[88vh] w-full max-w-2xl overflow-y-auto rounded-t-[28px] border border-white/10 bg-slate-900 shadow-2xl sm:rounded-[28px]">
        <div className="sticky top-0 z-10 flex items-start justify-between gap-4 border-b border-white/10 bg-slate-900 px-5 py-5 md:px-6">
          <div>
            <p className="text-xs font-bold tracking-[0.18em] text-indigo-300">MY RESERVATIONS</p>
            <h2 id="booked-product-modal-title" className="mt-1 text-xl font-bold text-white">예약한 옵션상품 불러오기</h2>
            <p className="mt-1 text-sm text-slate-400"><span className="font-semibold text-slate-200">{dayLabel} · {regionLabel}</span>에서 이용하는 예약 상품만 표시합니다.</p>
          </div>
          <button type="button" onClick={onClose} className="rounded-xl p-2 text-slate-400 transition hover:bg-white/10 hover:text-white" aria-label="닫기"><i className="fa-solid fa-xmark" /></button>
        </div>

        <div className="space-y-4 p-5 md:p-6">
          {loading ? <p className="py-8 text-center text-sm text-slate-400">예약 내역을 불러오는 중이에요.</p> : null}
          {!loading && orders.length === 0 ? <p className="py-8 text-center text-sm text-slate-400">연동할 예약 내역이 없습니다.</p> : null}
          {!loading && orders.map((order) => {
            const linked = linkedOrderIds.has(order.orderId);
            return (
              <article key={order.orderId} className="rounded-2xl border border-white/10 bg-white/[0.035] p-4">
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="rounded-full bg-indigo-500/15 px-2.5 py-1 text-xs font-bold text-indigo-100">{formatUseDate(order.useDate)}</span>
                      <span className="text-xs text-slate-500">예약번호 {order.orderNo}</span>
                    </div>
                    <h3 className="mt-2 truncate font-bold text-white">{order.productName}</h3>
                    <p className="mt-1 text-sm text-slate-400">{order.optionName} · {order.destinationName} · {order.quantity}개</p>
                  </div>
                  <button type="button" disabled={linked} onClick={() => onLink(order)} className="inline-flex shrink-0 items-center justify-center gap-2 rounded-xl border border-indigo-300/25 bg-indigo-500/15 px-4 py-2.5 text-sm font-bold text-indigo-100 transition hover:bg-indigo-500/25 disabled:cursor-not-allowed disabled:border-white/10 disabled:bg-white/[0.03] disabled:text-slate-500">
                    <i className={`fa-solid ${linked ? 'fa-check' : 'fa-link'}`} />{linked ? '이미 연동됨' : '일정에 연동'}
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      </div>
    </div>
  );
};
