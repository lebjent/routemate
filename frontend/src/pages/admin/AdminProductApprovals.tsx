import { useCallback, useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { hasPermission } from '../../features/admin/permissions';
import { useAuth } from '../../hooks/useAuth';
import { StyledSelect } from '../../components/StyledSelect';

type Product = {
  productId: number; productName: string; productSummary: string | null; productType: string;
  partnerName: string | null; destinationName: string; price: number; currency: string;
  approvalStatus: 'PENDING' | 'APPROVED' | 'REJECTED' | 'HOLD'; approvalMemo: string | null;
  createDt: string | null;
};

const statusLabels: Record<string, string> = { PENDING: '승인 대기', APPROVED: '승인 완료', REJECTED: '거절', HOLD: '보류' };
const statusColors: Record<string, string> = {
  PENDING: 'bg-amber-500/10 text-amber-300', APPROVED: 'bg-emerald-500/10 text-emerald-300',
  REJECTED: 'bg-rose-500/10 text-rose-300', HOLD: 'bg-indigo-500/10 text-indigo-300',
};

/**
 * 파트너사가 제출한 상품을 승인, 거절, 보류로 심사하는 화면이다.
 *
 * 거절·보류 사유는 상품 승인 이력에 남으므로 운영자가 사용자에게 안내할 문구를 입력한다.
 */
export const AdminProductApprovals = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [status, setStatus] = useState('PENDING');
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<Product | null>(null);
  const [decision, setDecision] = useState('APPROVED');
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try { setProducts((await axios.get<{ products: Product[] }>('/api/admin/products/approvals', { params: { status } })).data.products); }
    catch (loadError) {
      if (axios.isAxiosError(loadError) && [401, 403].includes(loadError.response?.status ?? 0)) navigate('/admin', { replace: true });
      else setError('승인 대상 상품을 불러오지 못했습니다.');
    } finally { setLoading(false); }
  }, [navigate, status]);

  useEffect(() => { if (user && !hasPermission(user, 'PARTNER_MANAGE')) navigate('/admin', { replace: true }); }, [navigate, user]);
  useEffect(() => { void load(); }, [load]);

  const openDecision = (product: Product, nextDecision: string) => {
    setSelected(product); setDecision(nextDecision); setReason(nextDecision === 'APPROVED' ? '' : product.approvalMemo ?? '');
  };
  const submit = async () => {
    if (!selected) return;
    if (decision !== 'APPROVED' && !reason.trim()) { setError('거절 또는 보류 사유를 입력하세요.'); return; }
    setSaving(true); setError(null);
    try {
      await axios.patch(`/api/admin/products/${selected.productId}/approval`, { decisionStatus: decision, reason: reason.trim() || null });
      setSelected(null); await load();
    } catch (saveError) { setError(axios.isAxiosError(saveError) ? saveError.response?.data?.detail ?? '승인 처리에 실패했습니다.' : '승인 처리에 실패했습니다.'); }
    finally { setSaving(false); }
  };

  return <>
    <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between"><div><p className="text-xs font-bold tracking-[0.2em] text-indigo-300">PRODUCT APPROVAL WORKFLOW</p><h1 className="mt-2 text-3xl font-extrabold text-white">상품 승인 관리</h1><p className="mt-2 text-sm text-slate-500">파트너사가 등록한 옵션상품을 검토하고 처리 사유와 이력을 남깁니다.</p></div><button type="button" onClick={() => void load()} className="self-start rounded-xl border border-white/10 px-4 py-2.5 text-xs font-bold text-slate-300"><i className="fa-solid fa-rotate-right mr-2" />새로고침</button></header>
    {error ? <p role="alert" className="mt-5 rounded-2xl border border-rose-400/15 bg-rose-400/5 px-4 py-3 text-sm text-rose-200">{error}</p> : null}
    <section className="mt-7 rounded-[24px] border border-white/10 bg-white/[0.03] p-5"><div className="flex items-center justify-between gap-4"><div><p className="text-xs font-bold tracking-[0.16em] text-indigo-300">REVIEW QUEUE</p><p className="mt-1 text-sm text-slate-500">처리 상태별 파트너사 등록 상품</p></div><StyledSelect value={status} onChange={setStatus} ariaLabel="승인 상태" className="w-40" options={[{ value: 'PENDING', label: '승인 대기', icon: 'fa-hourglass-half' }, { value: 'HOLD', label: '보류', icon: 'fa-pause' }, { value: 'REJECTED', label: '거절', icon: 'fa-ban' }, { value: 'APPROVED', label: '승인 완료', icon: 'fa-circle-check' }, { value: 'ALL', label: '전체', icon: 'fa-layer-group' }]} /></div></section>
    <section className="mt-4 overflow-hidden rounded-[24px] border border-white/10 bg-white/[0.03]">{loading ? <div className="flex min-h-[280px] items-center justify-center text-sm text-slate-500"><i className="fa-solid fa-spinner fa-spin mr-2" />승인 대상을 불러오는 중...</div> : <div className="overflow-x-auto"><table className="w-full min-w-[980px] text-left"><thead className="border-b border-white/10 bg-black/10 text-[11px] text-slate-600"><tr><th className="px-6 py-4">상품</th><th className="px-4 py-4">파트너사</th><th className="px-4 py-4">여행지·가격</th><th className="px-4 py-4">처리 상태</th><th className="px-6 py-4 text-right">결재 처리</th></tr></thead><tbody className="divide-y divide-white/5">{products.map((product) => <tr key={product.productId} className="hover:bg-white/[0.02]"><td className="px-6 py-4"><p className="text-sm font-bold text-slate-200">{product.productName}</p><p className="mt-1 max-w-[280px] truncate text-xs text-slate-600">{product.productSummary || '상품 요약 없음'}</p></td><td className="px-4 py-4 text-sm text-slate-300">{product.partnerName}</td><td className="px-4 py-4"><p className="text-sm text-slate-300">{product.destinationName}</p><p className="mt-1 text-xs font-bold text-indigo-200">{product.price.toLocaleString()} {product.currency}</p></td><td className="px-4 py-4"><span className={`rounded-full px-2.5 py-1 text-[11px] font-bold ${statusColors[product.approvalStatus]}`}>{statusLabels[product.approvalStatus]}</span>{product.approvalMemo ? <p className="mt-2 max-w-[200px] text-xs text-slate-500">{product.approvalMemo}</p> : null}</td><td className="px-6 py-4 text-right"><div className="flex justify-end gap-2"><button type="button" onClick={() => openDecision(product, 'APPROVED')} className="rounded-lg bg-emerald-500/10 px-2.5 py-1.5 text-xs font-bold text-emerald-300">승인</button><button type="button" onClick={() => openDecision(product, 'HOLD')} className="rounded-lg bg-indigo-500/10 px-2.5 py-1.5 text-xs font-bold text-indigo-300">보류</button><button type="button" onClick={() => openDecision(product, 'REJECTED')} className="rounded-lg bg-rose-500/10 px-2.5 py-1.5 text-xs font-bold text-rose-300">거절</button></div></td></tr>)}{products.length === 0 ? <tr><td colSpan={5} className="px-6 py-16 text-center text-sm text-slate-600">해당 상태의 파트너사 등록 상품이 없습니다.</td></tr> : null}</tbody></table></div>}</section>
    {selected ? <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/75 px-4 backdrop-blur-sm"><section role="dialog" aria-modal="true" className="w-full max-w-lg rounded-[26px] border border-white/10 bg-slate-900 p-6 shadow-2xl"><p className="text-xs font-bold tracking-[0.16em] text-indigo-300">APPROVAL DECISION</p><h2 className="mt-2 text-xl font-extrabold">{selected.productName}</h2><p className="mt-2 text-sm text-slate-500">처리 결과와 사유는 승인 이력으로 저장됩니다.</p><div className="mt-5"><StyledSelect value={decision} onChange={setDecision} ariaLabel="결재 결과" options={[{ value: 'APPROVED', label: '승인', icon: 'fa-circle-check' }, { value: 'HOLD', label: '보류', icon: 'fa-pause' }, { value: 'REJECTED', label: '거절', icon: 'fa-ban' }]} /></div><label className="mt-4 grid gap-2 text-xs font-semibold text-slate-400">{decision === 'APPROVED' ? '승인 메모 (선택)' : `${statusLabels[decision]} 사유`}<textarea required={decision !== 'APPROVED'} value={reason} onChange={(event) => setReason(event.target.value)} rows={5} maxLength={500} placeholder={decision === 'APPROVED' ? '승인 관련 메모를 남길 수 있습니다.' : '파트너사에 전달할 처리 사유를 입력하세요.'} className="rounded-xl border border-white/10 bg-slate-950/70 px-3 py-3 text-sm leading-6 text-white outline-none" /></label><div className="mt-6 flex justify-end gap-2"><button type="button" disabled={saving} onClick={() => setSelected(null)} className="rounded-xl border border-white/10 px-4 py-2.5 text-sm font-semibold text-slate-300">취소</button><button type="button" disabled={saving} onClick={() => void submit()} className="rounded-xl bg-indigo-500 px-4 py-2.5 text-sm font-bold text-white disabled:opacity-50">{saving ? '저장 중...' : `${statusLabels[decision]} 저장`}</button></div></section></div> : null}
  </>;
};
