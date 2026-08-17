import { useCallback, useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { hasPermission } from '../../features/admin/permissions';
import { useAuth } from '../../hooks/useAuth';

type Status = 'Y' | 'N';
type Recommendation = { recommendId: number; destinationId: number; destinationName: string; countryName: string; regionName: string; imageUrl: string | null; likeCount: number; displayStartDt: string; displayEndDt: string; sortOrder: number; useYn: Status };
type DestinationOption = { destinationId: number; destinationName: string; countryName: string; regionName: string; imageUrl: string | null };
type Data = { recommendations: Recommendation[]; destinations: DestinationOption[] };
type Form = { destinationId: string; displayStartDt: string; displayEndDt: string; sortOrder: number; useYn: Status };

const emptyForm: Form = { destinationId: '', displayStartDt: '', displayEndDt: '', sortOrder: 1, useYn: 'Y' };
const formatDate = (value: string) => new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));

export const AdminRecommendations = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const canManage = hasPermission(user, 'DESTINATION_MANAGE');
  const [data, setData] = useState<Data | null>(null);
  const [form, setForm] = useState<Form>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => { if (user && !canManage) navigate('/admin', { replace: true }); }, [canManage, navigate, user]);

  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try { const response = await axios.get<Data>('/api/admin/recommendations'); setData(response.data); }
    catch (loadError) { if (axios.isAxiosError(loadError) && [401, 403].includes(loadError.response?.status ?? 0)) navigate(loadError.response?.status === 401 ? '/admin/login' : '/admin', { replace: true }); else setError('추천 여행지 정보를 불러오지 못했습니다.'); }
    finally { setLoading(false); }
  }, [navigate]);
  useEffect(() => { void load(); }, [load]);

  const save = async (event: React.FormEvent) => {
    event.preventDefault(); setSaving(true); setError(null);
    try {
      const payload = { ...form, destinationId: Number(form.destinationId) };
      if (editingId) await axios.patch(`/api/admin/recommendations/${editingId}`, payload); else await axios.post('/api/admin/recommendations', payload);
      setForm(emptyForm); setEditingId(null); await load();
    } catch (saveError) { setError(axios.isAxiosError(saveError) ? saveError.response?.data?.detail || '추천 여행지를 저장하지 못했습니다.' : '추천 여행지를 저장하지 못했습니다.'); }
    finally { setSaving(false); }
  };

  const edit = (item: Recommendation) => setForm({ destinationId: String(item.destinationId), displayStartDt: item.displayStartDt.slice(0, 16), displayEndDt: item.displayEndDt.slice(0, 16), sortOrder: item.sortOrder, useYn: item.useYn });

  return <>
    <header className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between"><div><p className="text-xs font-bold tracking-[0.2em] text-indigo-300">RECOMMENDATION MANAGEMENT</p><h1 className="mt-2 text-3xl font-extrabold text-white">추천 여행지 관리</h1><p className="mt-2 text-sm text-slate-500">노출 기간과 순위를 설정하면 홈페이지에 활성 추천 여행지 TOP5로 노출됩니다.</p></div><div className="rounded-2xl border border-indigo-400/15 bg-indigo-500/10 px-4 py-3 text-xs text-indigo-200"><i className="fa-solid fa-star mr-2" />현재 추천 {data?.recommendations.filter((item) => item.useYn === 'Y').length ?? 0}개</div></header>
    {error ? <p role="alert" className="mt-5 rounded-2xl border border-rose-400/15 bg-rose-400/5 px-4 py-3 text-sm text-rose-200">{error}</p> : null}
    <section className="mt-7 rounded-[24px] border border-white/10 bg-white/[0.03] p-5 sm:p-6"><div className="flex items-center justify-between"><div><p className="text-xs font-bold tracking-[0.16em] text-indigo-300">{editingId ? 'EDIT RECOMMENDATION' : 'NEW RECOMMENDATION'}</p><h2 className="mt-1 text-lg font-bold text-white">추천 노출 설정</h2></div>{editingId ? <button type="button" onClick={() => { setEditingId(null); setForm(emptyForm); }} className="text-xs text-slate-500">수정 취소</button> : null}</div><form onSubmit={(event) => void save(event)} className="mt-5 grid gap-3 lg:grid-cols-[1.4fr_1fr_1fr_100px_110px_auto]"><select required value={form.destinationId} onChange={(event) => setForm((value) => ({ ...value, destinationId: event.target.value }))} className="h-12 rounded-2xl border border-white/10 bg-slate-900 px-4 text-sm text-slate-300"><option value="">여행지 선택</option>{data?.destinations.map((destination) => <option key={destination.destinationId} value={destination.destinationId}>{destination.destinationName} · {destination.countryName} {destination.regionName}</option>)}</select><label className="grid gap-1 text-[10px] font-semibold text-slate-500">노출 시작<input required type="datetime-local" value={form.displayStartDt} onChange={(event) => setForm((value) => ({ ...value, displayStartDt: event.target.value }))} className="h-12 rounded-2xl border border-white/10 bg-slate-950 px-3 text-xs text-slate-300" /></label><label className="grid gap-1 text-[10px] font-semibold text-slate-500">노출 종료<input required type="datetime-local" value={form.displayEndDt} onChange={(event) => setForm((value) => ({ ...value, displayEndDt: event.target.value }))} className="h-12 rounded-2xl border border-white/10 bg-slate-950 px-3 text-xs text-slate-300" /></label><label className="grid gap-1 text-[10px] font-semibold text-slate-500">순위<input required min={1} type="number" value={form.sortOrder} onChange={(event) => setForm((value) => ({ ...value, sortOrder: Number(event.target.value) }))} className="h-12 rounded-2xl border border-white/10 bg-slate-950 px-3 text-sm text-white" /></label><label className="grid gap-1 text-[10px] font-semibold text-slate-500">상태<select value={form.useYn} onChange={(event) => setForm((value) => ({ ...value, useYn: event.target.value as Status }))} className="h-12 rounded-2xl border border-white/10 bg-slate-900 px-3 text-sm text-slate-300"><option value="Y">활성</option><option value="N">비활성</option></select></label><button type="submit" disabled={saving || loading} className="h-12 self-end rounded-2xl bg-indigo-500 px-5 text-sm font-bold text-white disabled:opacity-50">{saving ? '저장 중' : editingId ? '수정 저장' : '추천 등록'}</button></form></section>
    <section className="mt-6 overflow-hidden rounded-[24px] border border-white/10 bg-white/[0.03]">{loading ? <div className="flex min-h-[300px] items-center justify-center text-sm text-slate-500"><i className="fa-solid fa-spinner fa-spin mr-2" />추천 정보를 불러오는 중...</div> : <div className="overflow-x-auto"><table className="w-full min-w-[1080px] text-left"><thead className="border-b border-white/10 bg-black/10 text-[11px] text-slate-600"><tr><th className="px-6 py-4">여행지</th><th className="px-4 py-4">노출 기간</th><th className="px-4 py-4">순위</th><th className="px-4 py-4">좋아요</th><th className="px-4 py-4">상태</th><th className="px-6 py-4 text-right">관리</th></tr></thead><tbody className="divide-y divide-white/5">{data?.recommendations.map((item) => <tr key={item.recommendId} className="hover:bg-white/[0.02]"><td className="px-6 py-4"><p className="text-sm font-semibold text-slate-200">{item.destinationName}</p><p className="mt-1 text-xs text-slate-600">{item.countryName} · {item.regionName}</p></td><td className="px-4 py-4 text-xs text-slate-500"><p>{formatDate(item.displayStartDt)}</p><p className="mt-1">~ {formatDate(item.displayEndDt)}</p></td><td className="px-4 py-4"><span className="rounded-full bg-indigo-500/10 px-2.5 py-1 text-xs font-bold text-indigo-300">TOP {item.sortOrder}</span></td><td className="px-4 py-4 text-xs text-slate-500">{item.likeCount?.toLocaleString('ko-KR')}</td><td className="px-4 py-4"><span className={`rounded-full px-2.5 py-1 text-[11px] font-bold ${item.useYn === 'Y' ? 'bg-emerald-500/10 text-emerald-300' : 'bg-slate-500/10 text-slate-500'}`}>{item.useYn === 'Y' ? '활성' : '비활성'}</span></td><td className="px-6 py-4 text-right"><button type="button" onClick={() => { setEditingId(item.recommendId); edit(item); }} className="rounded-xl border border-white/10 px-3 py-2 text-xs font-semibold text-indigo-300">수정</button></td></tr>)}{data?.recommendations.length === 0 ? <tr><td colSpan={6} className="px-6 py-16 text-center text-sm text-slate-600">등록된 추천 여행지가 없습니다.</td></tr> : null}</tbody></table></div>}</section>
  </>;
};
