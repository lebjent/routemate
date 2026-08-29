import { useCallback, useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { StyledSelect } from '../../components/StyledSelect';
import { hasPermission } from '../../features/admin/permissions';
import { useAuth } from '../../hooks/useAuth';

type PartnerStatus = 'ONBOARDING' | 'ACTIVE' | 'SUSPENDED' | 'TERMINATED';
type Partner = {
  partnerId: number; partnerCode: string; partnerName: string; businessNumber: string | null;
  representativeName: string | null; managerName: string | null; managerEmail: string | null;
  managerPhone: string | null; websiteUrl: string | null; commissionRate: number;
  contractStartDate: string | null; contractEndDate: string | null; partnerStatus: PartnerStatus;
  memo: string | null; totalProducts: number; activeProducts: number; pendingProducts: number;
  createDt: string; mdfyDt: string;
};
type Form = {
  partnerCode: string; partnerName: string; businessNumber: string; representativeName: string;
  managerName: string; managerEmail: string; managerPhone: string; websiteUrl: string;
  commissionRate: string; contractStartDate: string; contractEndDate: string; partnerStatus: PartnerStatus; memo: string;
};

const emptyForm: Form = { partnerCode: '', partnerName: '', businessNumber: '', representativeName: '', managerName: '', managerEmail: '', managerPhone: '', websiteUrl: '', commissionRate: '12', contractStartDate: '', contractEndDate: '', partnerStatus: 'ONBOARDING', memo: '' };
const statusOptions = [
  { value: 'ALL', label: '전체 상태', icon: 'fa-layer-group' }, { value: 'ONBOARDING', label: '온보딩', icon: 'fa-hourglass-half' },
  { value: 'ACTIVE', label: '정상 운영', icon: 'fa-circle-check' }, { value: 'SUSPENDED', label: '일시 중지', icon: 'fa-circle-pause' },
  { value: 'TERMINATED', label: '계약 종료', icon: 'fa-circle-xmark' },
];
const statusMeta: Record<PartnerStatus, { label: string; className: string }> = {
  ONBOARDING: { label: '온보딩', className: 'bg-cyan-500/10 text-cyan-300' }, ACTIVE: { label: '정상 운영', className: 'bg-emerald-500/10 text-emerald-300' },
  SUSPENDED: { label: '일시 중지', className: 'bg-amber-500/10 text-amber-300' }, TERMINATED: { label: '계약 종료', className: 'bg-slate-500/10 text-slate-400' },
};
const inputClass = 'h-11 rounded-xl border border-white/10 bg-slate-950/70 px-3 text-sm text-white outline-none transition placeholder:text-slate-700 focus:border-indigo-400/50 focus:ring-2 focus:ring-indigo-400/10';
const nullable = (value: string) => value.trim() || null;

/** 파트너사 목록을 조회하고 사업자 정보와 운영 상태를 수정하는 화면이다. */
export const AdminPartners = () => {
  const navigate = useNavigate(); const { user } = useAuth(); const canManage = hasPermission(user, 'PARTNER_MANAGE');
  const [partners, setPartners] = useState<Partner[]>([]); const [query, setQuery] = useState(''); const [status, setStatus] = useState('ALL');
  const [loading, setLoading] = useState(true); const [saving, setSaving] = useState(false); const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false); const [editingId, setEditingId] = useState<number | null>(null); const [form, setForm] = useState<Form>(emptyForm);

  useEffect(() => { if (user && !canManage) navigate('/admin', { replace: true }); }, [canManage, navigate, user]);
  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try { setPartners((await axios.get<{ partners: Partner[] }>('/api/admin/partners', { params: { query, status } })).data.partners); }
    catch (loadError) { if (axios.isAxiosError(loadError) && [401, 403].includes(loadError.response?.status ?? 0)) navigate('/admin', { replace: true }); else setError('파트너사 정보를 불러오지 못했습니다.'); }
    finally { setLoading(false); }
  }, [navigate, query, status]);
  useEffect(() => { const timer = window.setTimeout(() => void load(), 220); return () => window.clearTimeout(timer); }, [load]);

  const summary = useMemo(() => ({
    total: partners.length, active: partners.filter((item) => item.partnerStatus === 'ACTIVE').length,
    products: partners.reduce((sum, item) => sum + item.totalProducts, 0), pending: partners.reduce((sum, item) => sum + item.pendingProducts, 0),
  }), [partners]);
  const openCreate = () => { navigate('/admin/partners/new'); };
  const openEdit = (partner: Partner) => { setEditingId(partner.partnerId); setForm({ partnerCode: partner.partnerCode, partnerName: partner.partnerName, businessNumber: partner.businessNumber ?? '', representativeName: partner.representativeName ?? '', managerName: partner.managerName ?? '', managerEmail: partner.managerEmail ?? '', managerPhone: partner.managerPhone ?? '', websiteUrl: partner.websiteUrl ?? '', commissionRate: String(partner.commissionRate), contractStartDate: partner.contractStartDate ?? '', contractEndDate: partner.contractEndDate ?? '', partnerStatus: partner.partnerStatus, memo: partner.memo ?? '' }); setError(null); setShowForm(true); };
  const update = (key: keyof Form, value: string) => setForm((current) => ({ ...current, [key]: value }));
  const save = async (event: React.FormEvent) => {
    event.preventDefault(); setSaving(true); setError(null);
    const payload = { ...form, partnerCode: form.partnerCode.trim().toUpperCase(), partnerName: form.partnerName.trim(), businessNumber: nullable(form.businessNumber), representativeName: nullable(form.representativeName), managerName: nullable(form.managerName), managerEmail: nullable(form.managerEmail), managerPhone: nullable(form.managerPhone), websiteUrl: nullable(form.websiteUrl), commissionRate: Number(form.commissionRate), contractStartDate: form.contractStartDate || null, contractEndDate: form.contractEndDate || null, memo: nullable(form.memo) };
    try { if (editingId) await axios.patch(`/api/admin/partners/${editingId}`, payload); else await axios.post('/api/admin/partners', payload); setShowForm(false); await load(); }
    catch (saveError) { setError(axios.isAxiosError(saveError) ? saveError.response?.data?.detail || '파트너사를 저장하지 못했습니다.' : '파트너사를 저장하지 못했습니다.'); }
    finally { setSaving(false); }
  };

  return <>
    <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between"><div><p className="text-xs font-bold tracking-[0.2em] text-indigo-300">PARTNER OPERATIONS</p><h1 className="mt-2 text-3xl font-extrabold text-white">파트너사 관리</h1><p className="mt-2 text-sm text-slate-500">상품 공급사와 계약 조건을 관리하고 회사별 상품 운영 현황을 확인합니다.</p></div><button type="button" onClick={openCreate} className="self-start rounded-xl bg-indigo-500 px-4 py-2.5 text-xs font-bold text-white shadow-lg shadow-indigo-500/20"><i className="fa-solid fa-plus mr-2" />파트너사 등록</button></header>
    {error ? <p role="alert" className="mt-5 rounded-2xl border border-rose-400/15 bg-rose-400/5 px-4 py-3 text-sm text-rose-200">{error}</p> : null}

    <section className="mt-7 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {[['전체 파트너', summary.total, 'fa-handshake', 'text-indigo-300 bg-indigo-500/15'], ['정상 운영', summary.active, 'fa-circle-check', 'text-emerald-300 bg-emerald-500/15'], ['연결 상품', summary.products, 'fa-ticket', 'text-fuchsia-300 bg-fuchsia-500/15'], ['승인 대기', summary.pending, 'fa-clock', 'text-amber-300 bg-amber-500/15']].map(([label, value, icon, color]) => <article key={String(label)} className="rounded-[22px] border border-white/10 bg-white/[0.035] p-5"><div className="flex items-center justify-between"><div><p className="text-xs font-semibold text-slate-500">{label}</p><p className="mt-3 text-3xl font-extrabold text-white">{Number(value).toLocaleString('ko-KR')}</p></div><span className={`flex h-11 w-11 items-center justify-center rounded-2xl ${color}`}><i className={`fa-solid ${icon}`} /></span></div></article>)}
    </section>

    <section className="mt-6 rounded-[24px] border border-white/10 bg-white/[0.03] p-5"><div className="flex flex-col gap-3 md:flex-row md:items-center"><label className="relative flex-1"><i className="fa-solid fa-magnifying-glass absolute left-4 top-1/2 -translate-y-1/2 text-xs text-slate-600" /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="회사명, 파트너 코드, 사업자번호, 담당자 검색" className={`${inputClass} w-full pl-10`} /></label><StyledSelect value={status} onChange={setStatus} ariaLabel="파트너 상태" options={statusOptions} className="w-full md:w-44" /></div></section>

    <section className="mt-4 overflow-hidden rounded-[24px] border border-white/10 bg-white/[0.03]">{loading ? <div className="flex min-h-[360px] items-center justify-center text-sm text-slate-500"><i className="fa-solid fa-spinner fa-spin mr-2 text-indigo-400" />파트너사를 불러오는 중...</div> : <div className="overflow-x-auto"><table className="w-full min-w-[1050px] text-left"><thead className="border-b border-white/10 bg-black/10 text-[11px] text-slate-600"><tr><th className="px-6 py-4">파트너사</th><th className="px-4 py-4">담당자</th><th className="px-4 py-4">계약</th><th className="px-4 py-4">수수료</th><th className="px-4 py-4">상품 현황</th><th className="px-4 py-4">상태</th><th className="px-6 py-4 text-right">관리</th></tr></thead><tbody className="divide-y divide-white/5">{partners.map((partner) => <tr key={partner.partnerId} className="hover:bg-white/[0.02]"><td className="px-6 py-4"><div className="flex items-center gap-3"><span className="flex h-11 w-11 items-center justify-center rounded-xl bg-indigo-500/10 font-extrabold text-indigo-300">{partner.partnerName.slice(0, 1)}</span><div><p className="text-sm font-bold text-slate-200">{partner.partnerName}</p><p className="mt-1 text-xs text-slate-600">{partner.partnerCode}{partner.businessNumber ? ` · ${partner.businessNumber}` : ''}</p></div></div></td><td className="px-4 py-4"><p className="text-sm text-slate-300">{partner.managerName || '-'}</p><p className="mt-1 text-xs text-slate-600">{partner.managerEmail || partner.managerPhone || '담당자 미등록'}</p></td><td className="px-4 py-4 text-xs text-slate-500"><p>{partner.contractStartDate || '시작일 미정'}</p><p className="mt-1 text-slate-700">~ {partner.contractEndDate || '종료일 미정'}</p></td><td className="px-4 py-4 text-sm font-bold text-indigo-200">{partner.commissionRate}%</td><td className="px-4 py-4"><p className="text-xs text-slate-400">전체 <strong className="text-white">{partner.totalProducts}</strong> · 판매 <strong className="text-emerald-300">{partner.activeProducts}</strong></p>{partner.pendingProducts > 0 ? <p className="mt-1 text-xs font-semibold text-amber-300">승인대기 {partner.pendingProducts}건</p> : <p className="mt-1 text-xs text-slate-700">승인대기 없음</p>}</td><td className="px-4 py-4"><span className={`rounded-full px-2.5 py-1 text-[11px] font-bold ${statusMeta[partner.partnerStatus].className}`}>{statusMeta[partner.partnerStatus].label}</span></td><td className="px-6 py-4 text-right"><button type="button" onClick={() => openEdit(partner)} className="text-xs font-bold text-indigo-300 hover:text-indigo-200">상세·수정</button></td></tr>)}{partners.length === 0 ? <tr><td colSpan={7} className="px-6 py-16 text-center text-sm text-slate-600">조건에 맞는 파트너사가 없습니다.</td></tr> : null}</tbody></table></div>}</section>

    {showForm ? <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-slate-950/75 px-4 py-6 backdrop-blur-sm" onMouseDown={(event) => { if (event.target === event.currentTarget && !saving) setShowForm(false); }}><section role="dialog" aria-modal="true" className="my-auto w-full max-w-3xl rounded-[28px] border border-white/10 bg-slate-900 p-6 shadow-2xl sm:p-8"><div className="flex items-start justify-between"><div><p className="text-xs font-bold tracking-[0.18em] text-indigo-300">PARTNER PROFILE</p><h2 className="mt-2 text-xl font-extrabold text-white">{editingId ? '파트너사 정보 수정' : '새 파트너사 등록'}</h2></div><button type="button" onClick={() => setShowForm(false)} className="flex h-9 w-9 items-center justify-center rounded-xl text-slate-500 hover:bg-white/5 hover:text-white"><i className="fa-solid fa-xmark" /></button></div><form onSubmit={(event) => void save(event)} className="mt-7 grid gap-5"><div className="grid gap-4 sm:grid-cols-2"><label className="grid gap-2 text-xs font-semibold text-slate-400">파트너 코드<input required maxLength={30} value={form.partnerCode} onChange={(event) => update('partnerCode', event.target.value.toUpperCase())} placeholder="예: PARTNER-JP-001" className={`${inputClass} uppercase`} /></label><label className="grid gap-2 text-xs font-semibold text-slate-400">회사명<input required maxLength={120} value={form.partnerName} onChange={(event) => update('partnerName', event.target.value)} placeholder="법인 또는 브랜드명" className={inputClass} /></label><label className="grid gap-2 text-xs font-semibold text-slate-400">사업자번호<input value={form.businessNumber} onChange={(event) => update('businessNumber', event.target.value)} placeholder="000-00-00000" className={inputClass} /></label><label className="grid gap-2 text-xs font-semibold text-slate-400">대표자명<input value={form.representativeName} onChange={(event) => update('representativeName', event.target.value)} className={inputClass} /></label></div><div className="border-t border-white/10 pt-5"><p className="mb-4 text-xs font-bold tracking-[0.14em] text-indigo-300">운영 담당자</p><div className="grid gap-4 sm:grid-cols-3"><label className="grid gap-2 text-xs font-semibold text-slate-400">담당자명<input value={form.managerName} onChange={(event) => update('managerName', event.target.value)} className={inputClass} /></label><label className="grid gap-2 text-xs font-semibold text-slate-400">이메일<input type="email" value={form.managerEmail} onChange={(event) => update('managerEmail', event.target.value)} className={inputClass} /></label><label className="grid gap-2 text-xs font-semibold text-slate-400">연락처<input value={form.managerPhone} onChange={(event) => update('managerPhone', event.target.value)} className={inputClass} /></label></div></div><div className="border-t border-white/10 pt-5"><p className="mb-4 text-xs font-bold tracking-[0.14em] text-indigo-300">계약 및 정산</p><div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4"><label className="grid gap-2 text-xs font-semibold text-slate-400">수수료율 (%)<input required min="0" max="100" step="0.01" type="number" value={form.commissionRate} onChange={(event) => update('commissionRate', event.target.value)} className={inputClass} /></label><label className="grid gap-2 text-xs font-semibold text-slate-400">계약 시작<input type="date" value={form.contractStartDate} onChange={(event) => update('contractStartDate', event.target.value)} className={inputClass} /></label><label className="grid gap-2 text-xs font-semibold text-slate-400">계약 종료<input type="date" value={form.contractEndDate} onChange={(event) => update('contractEndDate', event.target.value)} className={inputClass} /></label><label className="grid gap-2 text-xs font-semibold text-slate-400">운영 상태<StyledSelect value={form.partnerStatus} onChange={(value) => update('partnerStatus', value)} ariaLabel="운영 상태" options={statusOptions.slice(1)} /></label></div></div><label className="grid gap-2 text-xs font-semibold text-slate-400">웹사이트<input type="url" value={form.websiteUrl} onChange={(event) => update('websiteUrl', event.target.value)} placeholder="https://partner.example.com" className={inputClass} /></label><label className="grid gap-2 text-xs font-semibold text-slate-400">관리 메모<textarea rows={3} value={form.memo} onChange={(event) => update('memo', event.target.value)} placeholder="정산 조건, 온보딩 진행 상황 등 내부 메모" className="rounded-xl border border-white/10 bg-slate-950/70 px-3 py-3 text-sm text-white outline-none focus:border-indigo-400/50" /></label><div className="flex justify-end gap-2"><button type="button" disabled={saving} onClick={() => setShowForm(false)} className="h-11 rounded-xl border border-white/10 px-5 text-sm font-semibold text-slate-400">취소</button><button type="submit" disabled={saving} className="h-11 rounded-xl bg-indigo-500 px-6 text-sm font-bold text-white disabled:opacity-50">{saving ? <><i className="fa-solid fa-spinner fa-spin mr-2" />저장 중</> : '저장'}</button></div></form></section></div> : null}
  </>;
};
