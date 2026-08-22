import { useCallback, useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { hasPermission } from '../../features/admin/permissions';
import { useAuth } from '../../hooks/useAuth';
import { StyledSelect } from '../../components/StyledSelect';

type Status = 'ACTIVE' | 'INACTIVE';
type Country = { countryId: number; countryName: string; countryCode: string; countryStatCd: Status; regionCount: number };
type Region = { regionId: number; regionName: string; regionCode: string; sortOrder: number; regionStatCd: Status };
type CountryData = { summary: { totalCountries: number; activeCountries: number; inactiveCountries: number; totalRegions: number }; countries: Country[] };
type Place = { destinationId: number; destName: string; destDesc: string | null; countryId: number; countryName: string; regionId: number; regionName: string; category: string; categoryLabel: string; imageUrl: string | null; mapLat: number; mapLng: number; likeCount: number };
type PlaceCategoryOption = { code: string; label: string };

const emptyCountry = { countryName: '', countryCode: '', countryStatCd: 'ACTIVE' as Status };
const emptyRegion = { regionName: '', regionCode: '', sortOrder: 0, regionStatCd: 'ACTIVE' as Status };
const defaultPlaceCategories: PlaceCategoryOption[] = [
  { code: 'FOOD', label: '맛집' },
  { code: 'SIGHTSEEING', label: '관광지' },
  { code: 'SHOPPING', label: '쇼핑' },
  { code: 'ACCOMMODATION', label: '숙박' },
  { code: 'CAFE', label: '카페' },
  { code: 'NATURE', label: '자연·공원' },
  { code: 'CULTURE', label: '문화·전시' },
  { code: 'ACTIVITY', label: '체험·액티비티' },
];
const placeCategoryIcons: Record<string, string> = { FOOD: 'fa-utensils', SIGHTSEEING: 'fa-camera', SHOPPING: 'fa-bag-shopping', ACCOMMODATION: 'fa-bed', CAFE: 'fa-mug-hot', NATURE: 'fa-tree', CULTURE: 'fa-landmark', ACTIVITY: 'fa-person-hiking' };
const emptyPlace = { destName: '', destDesc: '', countryId: '', regionId: '', category: 'FOOD', imageUrl: '', mapLat: '', mapLng: '' };

export const AdminDestinations = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const canManage = hasPermission(user, 'DESTINATION_MANAGE');
  const [data, setData] = useState<CountryData | null>(null);
  const [regions, setRegions] = useState<Region[]>([]);
  const [places, setPlaces] = useState<Place[]>([]);
  const [placeCategories, setPlaceCategories] = useState<PlaceCategoryOption[]>(defaultPlaceCategories);
  const [selectedCountryId, setSelectedCountryId] = useState<number | null>(null);
  const [countryForm, setCountryForm] = useState(emptyCountry);
  const [regionForm, setRegionForm] = useState(emptyRegion);
  const [editingCountryId, setEditingCountryId] = useState<number | null>(null);
  const [editingRegionId, setEditingRegionId] = useState<number | null>(null);
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [placeForm, setPlaceForm] = useState(emptyPlace);
  const [editingPlaceId, setEditingPlaceId] = useState<number | null>(null);

  useEffect(() => {
    if (user && !canManage) navigate('/admin', { replace: true });
  }, [canManage, navigate, user]);

  const loadCountries = useCallback(async () => {
    setLoading(true);
    try {
      const response = await axios.get<CountryData>('/api/admin/destinations/countries', { params: { query, status } });
      setData(response.data);
      setSelectedCountryId((current) => current && response.data.countries.some((country) => country.countryId === current) ? current : response.data.countries[0]?.countryId ?? null);
    } catch (loadError) {
      if (axios.isAxiosError(loadError) && [401, 403].includes(loadError.response?.status ?? 0)) navigate(loadError.response?.status === 401 ? '/admin/login' : '/admin', { replace: true });
      else setError('국가 정보를 불러오지 못했습니다.');
    } finally { setLoading(false); }
  }, [navigate, query, status]);

  const loadRegions = useCallback(async () => {
    if (!selectedCountryId) { setRegions([]); return; }
    try {
      const response = await axios.get<Region[]>(`/api/admin/destinations/countries/${selectedCountryId}/regions`);
      setRegions(response.data);
    } catch { setError('지역 정보를 불러오지 못했습니다.'); }
  }, [selectedCountryId]);

  useEffect(() => { void loadCountries(); }, [loadCountries]);
  useEffect(() => { void loadRegions(); }, [loadRegions]);

  const loadPlaces = useCallback(async () => {
    if (!selectedCountryId) { setPlaces([]); return; }
    try {
      const response = await axios.get<{ places: Place[] }>('/api/admin/destinations/places', { params: { countryId: selectedCountryId } });
      setPlaces(response.data.places);
    } catch { setError('여행지 정보를 불러오지 못했습니다.'); }
  }, [selectedCountryId]);

  useEffect(() => { void loadPlaces(); }, [loadPlaces]);

  useEffect(() => {
    void axios.get<{ categories: PlaceCategoryOption[] }>('/api/admin/destinations/place-categories')
      .then((response) => setPlaceCategories(response.data.categories))
      .catch(() => setPlaceCategories(defaultPlaceCategories));
  }, []);

  const selectedCountry = data?.countries.find((country) => country.countryId === selectedCountryId) ?? null;
  const selectCountry = (country: Country) => { setSelectedCountryId(country.countryId); setEditingCountryId(null); setCountryForm({ countryName: country.countryName, countryCode: country.countryCode, countryStatCd: country.countryStatCd }); setEditingRegionId(null); setRegionForm(emptyRegion); setEditingPlaceId(null); setPlaceForm({ ...emptyPlace, countryId: String(country.countryId) }); };

  const saveCountry = async (event: React.FormEvent) => {
    event.preventDefault(); setSaving(true); setError(null);
    try {
      if (editingCountryId) await axios.patch(`/api/admin/destinations/countries/${editingCountryId}`, countryForm);
      else await axios.post('/api/admin/destinations/countries', countryForm);
      setCountryForm(emptyCountry); setEditingCountryId(null); await loadCountries();
    } catch (saveError) { setError(axios.isAxiosError(saveError) ? saveError.response?.data?.detail || '국가를 저장하지 못했습니다.' : '국가를 저장하지 못했습니다.'); } finally { setSaving(false); }
  };

  const saveRegion = async (event: React.FormEvent) => {
    event.preventDefault(); if (!selectedCountryId) return;
    setSaving(true); setError(null);
    try {
      if (editingRegionId) await axios.patch(`/api/admin/destinations/countries/${selectedCountryId}/regions/${editingRegionId}`, regionForm);
      else await axios.post(`/api/admin/destinations/countries/${selectedCountryId}/regions`, regionForm);
      setRegionForm(emptyRegion); setEditingRegionId(null); await loadRegions(); await loadCountries();
    } catch (saveError) { setError(axios.isAxiosError(saveError) ? saveError.response?.data?.detail || '지역을 저장하지 못했습니다.' : '지역을 저장하지 못했습니다.'); } finally { setSaving(false); }
  };

  const savePlace = async (event: React.FormEvent) => {
    event.preventDefault();
    setSaving(true); setError(null);
    const payload = { ...placeForm, countryId: Number(placeForm.countryId), regionId: Number(placeForm.regionId), mapLat: Number(placeForm.mapLat), mapLng: Number(placeForm.mapLng) };
    try {
      if (editingPlaceId) await axios.patch(`/api/admin/destinations/places/${editingPlaceId}`, payload);
      else await axios.post('/api/admin/destinations/places', payload);
      setPlaceForm({ ...emptyPlace, countryId: String(selectedCountryId ?? '') }); setEditingPlaceId(null); await loadPlaces(); await loadCountries();
    } catch (saveError) { setError(axios.isAxiosError(saveError) ? saveError.response?.data?.detail || '플레이스를 저장하지 못했습니다.' : '플레이스를 저장하지 못했습니다.'); } finally { setSaving(false); }
  };

  const editPlace = (place: Place) => { setEditingPlaceId(place.destinationId); setPlaceForm({ destName: place.destName, destDesc: place.destDesc ?? '', countryId: String(place.countryId), regionId: String(place.regionId), category: place.category, imageUrl: place.imageUrl ?? '', mapLat: String(place.mapLat), mapLng: String(place.mapLng) }); };

  return <>
    <header className="flex flex-col gap-6 sm:flex-row sm:items-start sm:justify-between"><div><p className="text-xs font-bold tracking-[0.2em] text-indigo-300">DESTINATION MASTER</p><h1 className="mt-2 text-3xl font-extrabold text-white">국가·지역 관리</h1><p className="mt-2 text-sm text-slate-500">여행 일정에서 사용하는 국가와 지역 기준정보를 관리합니다.</p></div>{data ? <div className="grid w-full max-w-[220px] grid-cols-2 gap-3 self-start sm:w-auto"><div className="rounded-2xl border border-white/10 bg-white/[0.04] px-4 py-3"><p className="text-[11px] font-semibold text-slate-500">등록 국가</p><p className="mt-1 text-xl font-extrabold text-white">{data.summary.totalCountries}<span className="ml-1 text-xs font-medium text-slate-500">개</span></p></div><div className="rounded-2xl border border-white/10 bg-white/[0.04] px-4 py-3"><p className="text-[11px] font-semibold text-slate-500">등록 지역</p><p className="mt-1 text-xl font-extrabold text-white">{data.summary.totalRegions}<span className="ml-1 text-xs font-medium text-slate-500">개</span></p></div></div> : null}</header>
    {error ? <p role="alert" className="mt-5 rounded-2xl border border-rose-400/15 bg-rose-400/5 px-4 py-3 text-sm text-rose-200">{error}</p> : null}
    <section className="mt-7 grid gap-5 2xl:grid-cols-[360px_minmax(0,1fr)]">
      <div className="rounded-[24px] border border-white/10 bg-white/[0.03] p-5">
        <div className="flex items-center justify-between"><h2 className="text-sm font-bold text-white">국가 목록</h2><button type="button" onClick={() => { setEditingCountryId(null); setCountryForm(emptyCountry); }} className="rounded-xl bg-indigo-500/15 px-3 py-2 text-xs font-bold text-indigo-200"><i className="fa-solid fa-plus mr-1.5" />국가 추가</button></div>
        <div className="mt-4 grid gap-2 sm:grid-cols-[1fr_110px]"><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="국가 검색" className="h-10 rounded-xl border border-white/10 bg-slate-950/60 px-3 text-xs text-white outline-none" /><StyledSelect value={status} onChange={setStatus} ariaLabel="국가 상태" options={[{ value: 'ALL', label: '전체', icon: 'fa-layer-group' }, { value: 'ACTIVE', label: '활성', icon: 'fa-circle-check' }, { value: 'INACTIVE', label: '비활성', icon: 'fa-circle-pause' }]} className="h-10 rounded-xl px-2 text-xs" /></div>
        <div className="mt-4 space-y-2">{loading ? <p className="py-10 text-center text-xs text-slate-600">불러오는 중...</p> : data?.countries.map((country) => <button type="button" key={country.countryId} onClick={() => selectCountry(country)} className={`flex w-full items-center justify-between rounded-2xl border px-4 py-3 text-left transition ${selectedCountryId === country.countryId ? 'border-indigo-400/30 bg-indigo-500/10' : 'border-white/5 bg-white/[0.02] hover:bg-white/[0.05]'}`}><span><span className="block text-sm font-semibold text-slate-200">{country.countryName}</span><span className="mt-1 block text-[11px] text-slate-600">{country.countryCode} · 지역 {country.regionCount}개</span></span><span className={`h-2 w-2 rounded-full ${country.countryStatCd === 'ACTIVE' ? 'bg-emerald-400' : 'bg-slate-600'}`} /></button>)}{!loading && data?.countries.length === 0 ? <p className="py-10 text-center text-xs text-slate-600">등록된 국가가 없습니다.</p> : null}</div>
      </div>
      <div className="space-y-5">
        <form onSubmit={(event) => void saveCountry(event)} className="rounded-[24px] border border-white/10 bg-white/[0.03] p-5"><div className="flex items-center justify-between"><div><p className="text-xs font-bold tracking-[0.16em] text-indigo-300">COUNTRY</p><h2 className="mt-1 text-lg font-bold text-white">{editingCountryId ? '국가 수정' : '국가 등록'}</h2></div>{selectedCountry && !editingCountryId ? <button type="button" onClick={() => { setEditingCountryId(selectedCountry.countryId); setCountryForm({ countryName: selectedCountry.countryName, countryCode: selectedCountry.countryCode, countryStatCd: selectedCountry.countryStatCd }); }} className="rounded-xl border border-white/10 px-3 py-2 text-xs font-semibold text-slate-400">선택 국가 수정</button> : null}</div><div className="mt-5 grid gap-3 md:grid-cols-2 xl:grid-cols-[minmax(0,1fr)_150px_130px_auto]"><input required value={countryForm.countryName} onChange={(event) => setCountryForm((value) => ({ ...value, countryName: event.target.value }))} placeholder="국가명" className="h-11 rounded-xl border border-white/10 bg-slate-950/60 px-3 text-sm text-white outline-none" /><input required value={countryForm.countryCode} onChange={(event) => setCountryForm((value) => ({ ...value, countryCode: event.target.value }))} placeholder="코드 (KR)" className="h-11 rounded-xl border border-white/10 bg-slate-950/60 px-3 text-sm uppercase text-white outline-none" /><StyledSelect value={countryForm.countryStatCd} onChange={(value) => setCountryForm((current) => ({ ...current, countryStatCd: value as Status }))} ariaLabel="국가 상태" options={[{ value: 'ACTIVE', label: '활성', icon: 'fa-circle-check' }, { value: 'INACTIVE', label: '비활성', icon: 'fa-circle-pause' }]} /><button type="submit" disabled={saving} className="h-11 whitespace-nowrap rounded-xl bg-indigo-500 px-4 text-xs font-bold text-white disabled:opacity-50 md:col-span-2 xl:col-span-1">{saving ? '저장 중' : editingCountryId ? '수정 저장' : '등록'}</button></div></form>
        <form onSubmit={(event) => void saveRegion(event)} className="rounded-[24px] border border-white/10 bg-white/[0.03] p-5"><div className="flex items-center justify-between"><div><p className="text-xs font-bold tracking-[0.16em] text-indigo-300">REGION</p><h2 className="mt-1 text-lg font-bold text-white">{selectedCountry ? `${selectedCountry.countryName} 지역` : '지역 관리'}</h2></div>{editingRegionId ? <button type="button" onClick={() => { setEditingRegionId(null); setRegionForm(emptyRegion); }} className="text-xs text-slate-500">수정 취소</button> : null}</div>{selectedCountry ? <><div className="mt-5 grid gap-3 md:grid-cols-2 xl:grid-cols-[minmax(0,1fr)_150px_100px_130px_auto]"><input required value={regionForm.regionName} onChange={(event) => setRegionForm((value) => ({ ...value, regionName: event.target.value }))} placeholder="지역명" className="h-11 rounded-xl border border-white/10 bg-slate-950/60 px-3 text-sm text-white outline-none" /><input required value={regionForm.regionCode} onChange={(event) => setRegionForm((value) => ({ ...value, regionCode: event.target.value }))} placeholder="지역 코드" className="h-11 rounded-xl border border-white/10 bg-slate-950/60 px-3 text-sm uppercase text-white outline-none" /><input type="number" value={regionForm.sortOrder} onChange={(event) => setRegionForm((value) => ({ ...value, sortOrder: Number(event.target.value) }))} className="h-11 rounded-xl border border-white/10 bg-slate-950/60 px-3 text-sm text-white outline-none" /><StyledSelect value={regionForm.regionStatCd} onChange={(value) => setRegionForm((current) => ({ ...current, regionStatCd: value as Status }))} ariaLabel="지역 상태" options={[{ value: 'ACTIVE', label: '활성', icon: 'fa-circle-check' }, { value: 'INACTIVE', label: '비활성', icon: 'fa-circle-pause' }]} /><button type="submit" disabled={saving} className="h-11 min-w-[88px] whitespace-nowrap rounded-xl bg-indigo-500 px-4 text-xs font-bold text-white disabled:opacity-50 md:col-span-2 xl:col-span-1">{editingRegionId ? '수정 저장' : '지역 등록'}</button></div><div className="mt-5 overflow-x-auto"><table className="w-full min-w-[560px] text-left"><thead className="border-b border-white/10 text-[11px] text-slate-600"><tr><th className="px-3 py-3">지역명</th><th className="px-3 py-3">코드</th><th className="px-3 py-3">순서</th><th className="px-3 py-3">상태</th><th className="px-3 py-3 text-right">관리</th></tr></thead><tbody className="divide-y divide-white/5">{regions.map((region) => <tr key={region.regionId}><td className="px-3 py-3 text-sm text-slate-200">{region.regionName}</td><td className="px-3 py-3 text-xs text-slate-500">{region.regionCode}</td><td className="px-3 py-3 text-xs text-slate-500">{region.sortOrder}</td><td className="px-3 py-3 text-xs"><span className={region.regionStatCd === 'ACTIVE' ? 'text-emerald-300' : 'text-slate-600'}>{region.regionStatCd === 'ACTIVE' ? '활성' : '비활성'}</span></td><td className="px-3 py-3 text-right"><button type="button" onClick={() => { setEditingRegionId(region.regionId); setRegionForm({ regionName: region.regionName, regionCode: region.regionCode, sortOrder: region.sortOrder, regionStatCd: region.regionStatCd }); }} className="text-xs font-semibold text-indigo-300">수정</button></td></tr>)}{regions.length === 0 ? <tr><td colSpan={5} className="px-3 py-10 text-center text-xs text-slate-600">등록된 지역이 없습니다.</td></tr> : null}</tbody></table></div></> : <p className="mt-8 py-12 text-center text-sm text-slate-600">왼쪽에서 국가를 선택하세요.</p>}</form>
      </div>
    </section>
    <section className="mt-6 rounded-[24px] border border-white/10 bg-white/[0.03] p-5 sm:p-6">
      <div className="flex items-center justify-between"><div><p className="text-xs font-bold tracking-[0.16em] text-indigo-300">PLACE MANAGEMENT</p><h2 className="mt-1 text-lg font-bold text-white">플레이스 관리</h2><p className="mt-1 text-xs text-slate-500">국가와 지역에 플레이스를 연결하고 구분별로 관리합니다.</p></div>{editingPlaceId ? <button type="button" onClick={() => { setEditingPlaceId(null); setPlaceForm({ ...emptyPlace, countryId: String(selectedCountryId ?? '') }); }} className="text-xs text-slate-500">수정 취소</button> : null}</div>
      <form onSubmit={(event) => void savePlace(event)} className="mt-5 grid gap-3 lg:grid-cols-4">
        <StyledSelect value={placeForm.countryId} onChange={(value) => { setSelectedCountryId(Number(value) || null); setPlaceForm((current) => ({ ...current, countryId: value, regionId: '' })); }} ariaLabel="국가 선택" placeholder="국가 선택" options={[{ value: '', label: '국가 선택', icon: 'fa-earth-asia' }, ...(data?.countries.map((country) => ({ value: String(country.countryId), label: country.countryName, description: `${country.countryCode} · 지역 ${country.regionCount}개`, icon: 'fa-location-dot' })) ?? [])]} />
        <StyledSelect value={placeForm.regionId} onChange={(value) => setPlaceForm((current) => ({ ...current, regionId: value }))} ariaLabel="지역 선택" placeholder="지역 선택" options={[{ value: '', label: '지역 선택', icon: 'fa-map' }, ...regions.map((region) => ({ value: String(region.regionId), label: region.regionName, icon: 'fa-location-dot' }))]} />
        <input required value={placeForm.destName} onChange={(event) => setPlaceForm((current) => ({ ...current, destName: event.target.value }))} placeholder="플레이스명" className="h-11 rounded-xl border border-white/10 bg-slate-950/60 px-3 text-sm text-white outline-none" />
        <StyledSelect value={placeForm.category} onChange={(value) => setPlaceForm((current) => ({ ...current, category: value }))} ariaLabel="플레이스 구분" placeholder="구분 선택" options={placeCategories.map((category) => ({ value: category.code, label: category.label, icon: placeCategoryIcons[category.code] ?? 'fa-location-dot' }))} />
        <div className="lg:col-span-2"><input value={placeForm.imageUrl} onChange={(event) => setPlaceForm((current) => ({ ...current, imageUrl: event.target.value }))} placeholder="대표 이미지 URL" className="h-11 w-full rounded-xl border border-white/10 bg-slate-950/60 px-3 text-sm text-white outline-none" />{placeForm.imageUrl ? <img src={placeForm.imageUrl} alt="대표 이미지 미리보기" className="mt-2 h-24 w-full rounded-xl object-cover" /> : <p className="mt-2 text-[11px] text-slate-600">이미지 URL을 입력하면 미리보기가 표시됩니다.</p>}</div>
        <textarea value={placeForm.destDesc} onChange={(event) => setPlaceForm((current) => ({ ...current, destDesc: event.target.value }))} placeholder="플레이스 설명" className="h-24 rounded-xl border border-white/10 bg-slate-950/60 px-3 py-3 text-sm text-white outline-none lg:col-span-2" />
        <input required type="number" step="any" value={placeForm.mapLat} onChange={(event) => setPlaceForm((current) => ({ ...current, mapLat: event.target.value }))} placeholder="위도" className="h-11 rounded-xl border border-white/10 bg-slate-950/60 px-3 text-sm text-white outline-none" />
        <input required type="number" step="any" value={placeForm.mapLng} onChange={(event) => setPlaceForm((current) => ({ ...current, mapLng: event.target.value }))} placeholder="경도" className="h-11 rounded-xl border border-white/10 bg-slate-950/60 px-3 text-sm text-white outline-none" />
        <button type="submit" disabled={saving || !placeForm.countryId || !placeForm.regionId || !placeForm.category} className="h-11 rounded-xl bg-indigo-500 px-4 text-xs font-bold text-white disabled:opacity-50 lg:col-span-4">{saving ? '저장 중...' : editingPlaceId ? '플레이스 수정' : '플레이스 등록'}</button>
      </form>
      <div className="mt-6 overflow-x-auto"><table className="w-full min-w-[760px] text-left"><thead className="border-b border-white/10 text-[11px] text-slate-600"><tr><th className="px-3 py-3">대표 이미지</th><th className="px-3 py-3">플레이스</th><th className="px-3 py-3">국가·지역</th><th className="px-3 py-3">구분</th><th className="px-3 py-3 text-right">관리</th></tr></thead><tbody className="divide-y divide-white/5">{places.map((place) => <tr key={place.destinationId}><td className="px-3 py-3">{place.imageUrl ? <img src={place.imageUrl} alt="" className="h-12 w-20 rounded-lg object-cover" /> : <div className="flex h-12 w-20 items-center justify-center rounded-lg bg-white/5 text-[10px] text-slate-600">이미지 없음</div>}</td><td className="px-3 py-3 text-sm font-semibold text-slate-200">{place.destName}</td><td className="px-3 py-3 text-xs text-slate-500">{place.countryName} · {place.regionName}</td><td className="px-3 py-3 text-xs text-slate-500">{place.categoryLabel}</td><td className="px-3 py-3 text-right"><button type="button" onClick={() => editPlace(place)} className="text-xs font-semibold text-indigo-300">수정</button></td></tr>)}{places.length === 0 ? <tr><td colSpan={5} className="px-3 py-10 text-center text-xs text-slate-600">등록된 플레이스가 없습니다.</td></tr> : null}</tbody></table></div>
    </section>
  </>;
};
