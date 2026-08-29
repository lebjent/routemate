import { Fragment, useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import axios from 'axios';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { CustomCalendar } from '../components/CustomCalendar';
import { SearchableSelect } from '../components/SearchableSelect';
import { StyledSelect } from '../components/StyledSelect';
import { useAuth } from '../hooks/useAuth';
import { PackingModal } from '../features/trip-builder/PackingModal';
import { BookedProductModal, type BookedProduct } from '../features/trip-builder/BookedProductModal';
import { productApi } from '../features/products/api';
import { TripPreview } from '../features/trip-builder/TripPreview';
import {
  createDays,
  createPackingItem,
  formatDate,
  formatShortDate,
  formatTimeInput,
  hasScheduleContent,
  parseLocalDate,
  transportByType,
  transportOptions,
  type CountryOption,
  type PackingItem,
  type RegionOption,
} from '../features/trip-builder/model';
import { useTripPlanEditor } from '../features/trip-builder/useTripPlanEditor';

const inputClassName =
  'min-w-0 w-full rounded-xl border border-white/10 bg-slate-950/55 px-3.5 py-3 text-sm text-white outline-none transition placeholder:text-slate-500 focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400/60';
const panelClassName =
  'rounded-[28px] border border-white/10 bg-white/[0.035] shadow-[0_24px_80px_rgba(0,0,0,0.2)] backdrop-blur-sm';
const secondaryButtonClassName =
  'inline-flex shrink-0 items-center justify-center gap-2 whitespace-nowrap rounded-xl border border-white/10 bg-white/[0.045] px-3.5 py-2.5 text-sm font-semibold text-slate-200 transition hover:border-indigo-300/30 hover:bg-indigo-500/15 hover:text-white focus:outline-none focus:ring-2 focus:ring-indigo-400/40';

type ReservationTarget = {
  dayIndex: number;
  regionIndex: number;
  dayLabel: string;
  regionLabel: string;
};

/**
 * 여행 계획을 생성하거나 기존 계획 전체를 수정하는 편집 화면이다.
 *
 * 일차·지역·세부 일정·교통편·준비물을 로컬 편집 상태로 관리하고, 저장 시 백엔드 요청 DTO로
 * 변환한다. 예약 상품은 이용일과 여행지 조건이 맞는 경우에만 일정에 연결할 수 있다.
 */
export const CreateTrip = () => {
  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const { planId } = useParams();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [travelStartDate, setTravelStartDate] = useState('');
  const [travelEndDate, setTravelEndDate] = useState('');
  const [packingItems, setPackingItems] = useState<PackingItem[]>([
    createPackingItem('여권'),
    createPackingItem('충전기'),
    createPackingItem('상비약'),
  ]);
  const [countries, setCountries] = useState<CountryOption[]>([]);
  const [regionsByCountry, setRegionsByCountry] = useState<Record<string, RegionOption[]>>({});
  const [loadingCountries, setLoadingCountries] = useState(false);
  const [packingOpen, setPackingOpen] = useState(false);
  const [isPublic, setIsPublic] = useState<'Y' | 'N'>('Y');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [bookedProductModalOpen, setBookedProductModalOpen] = useState(false);
  const [bookedProducts, setBookedProducts] = useState<BookedProduct[]>([]);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [reservationTarget, setReservationTarget] = useState<ReservationTarget | null>(null);

  useEffect(() => {
    const loadCountries = async () => {
      setLoadingCountries(true);
      try {
        const response = await axios.get<CountryOption[]>('/api/destinations/countries');
        setCountries(response.data);
      } catch (loadError) {
        console.error('Failed to load countries', loadError);
        setError('국가 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
      } finally {
        setLoadingCountries(false);
      }
    };

    void loadCountries();
  }, []);

  const days = useMemo(() => createDays(travelStartDate, travelEndDate), [travelStartDate, travelEndDate]);
  const { dayPlans, updateRegion, addRegion, removeRegion, addSchedule, updateSchedule, removeSchedule, replaceDayPlans } = useTripPlanEditor(days);

  const loadRegions = async (countryCode: string) => {
    if (!countryCode || regionsByCountry[countryCode]) return;
    try {
      const response = await axios.get<RegionOption[]>(`/api/destinations/countries/${countryCode}/regions`);
      setRegionsByCountry((current) => ({ ...current, [countryCode]: response.data }));
    } catch (loadError) {
      console.error('Failed to load regions', loadError);
      setError('선택한 국가의 지역을 불러오지 못했습니다.');
    }
  };

  const updateDayRegion = (dayIndex: number, regionIndex: number, field: 'countryCode' | 'regionCode' | 'note', value: string) => {
    if (field === 'countryCode') void loadRegions(value);
    updateRegion(dayIndex, regionIndex, field, value);
  };

  useEffect(() => {
    if (!planId || authLoading || !user) return;
    const loadPlanForEdit = async () => {
      try {
        const response = await axios.get(`/api/my-travel-plans/${planId}`);
        const data = response.data;
        setTitle(data.title || '');
        setDescription(data.description || '');
        setImageUrl(data.imageUrl || '');
        setTravelStartDate(data.travelStartDate || '');
        setTravelEndDate(data.travelEndDate || '');
        setIsPublic(data.isPublic === 'N' ? 'N' : 'Y');
        setPackingItems((data.packingItems || []).map((item: { item: string; required: boolean }) => createPackingItem(item.item, item.required)));
        const loadedCountries = new Set<string>();
        replaceDayPlans((data.days || []).map((day: { dayNumber: number; planDate: string; regions: any[] }) => ({
          day: day.dayNumber,
          date: day.planDate,
          regions: (day.regions || []).map((region) => {
            loadedCountries.add(region.countryCode);
            return {
              id: crypto.randomUUID(), countryCode: region.countryCode, regionCode: region.regionCode, note: region.note || '',
              schedules: (region.schedules || []).map((schedule: any) => ({
                id: crypto.randomUUID(), time: schedule.time || '', title: schedule.title || '', location: schedule.location || '', memo: schedule.memo || '',
                transportType: schedule.transportType || '', transportName: schedule.transportName || '', departureTime: schedule.departureTime || '', arrivalTime: schedule.arrivalTime || '', transportMemo: schedule.transportMemo || '',
                productOrderId: schedule.productOrderId || null, productOrderNo: schedule.productOrderNo || null,
              })),
            };
          }),
        })));
        await Promise.all([...loadedCountries].map(loadRegions));
      } catch (loadError) {
        console.error('Failed to load travel plan for edit', loadError);
        setError('여행 일정을 불러오지 못했습니다.');
      }
    };
    void loadPlanForEdit();
  }, [authLoading, planId, user]);

  const updatePackingItem = (index: number, field: 'item' | 'required', value: string | boolean) => {
    setPackingItems((current) => current.map((item, itemIndex) => (itemIndex === index ? { ...item, [field]: value } : item)));
  };

  const removePackingItem = (index: number) => {
    setPackingItems((current) => current.filter((_, itemIndex) => itemIndex !== index));
  };

  const previewDays = dayPlans.map((plan) => {
    const regions = plan.regions
      .filter((region) => region.countryCode || region.regionCode || region.note.trim())
      .map((region) => {
        const country = countries.find((item) => item.countryCode === region.countryCode);
        const selectedRegion = regionsByCountry[region.countryCode]?.find((item) => item.regionCode === region.regionCode);
        return {
          ...region,
          label: selectedRegion?.regionName || country?.countryName || '여행지 선택 중',
          schedules: region.schedules.filter(hasScheduleContent),
        };
      });
    return { ...plan, regions };
  });

  const spotCount = previewDays.reduce(
    (total, plan) => total + plan.regions.reduce((regionTotal, region) => regionTotal + region.schedules.length, 0),
    0
  );
  const completedRegionCount = previewDays.reduce(
    (total, plan) => total + plan.regions.filter((region) => region.countryCode && region.regionCode).length,
    0
  );
  const checkedPackingCount = packingItems.filter((item) => item.item.trim() && item.required).length;
  const previewTitle = title.trim() || '새 여행 일정';
  const previewSummary = description.trim() || '날짜별 여행지를 추가하고, 그 아래에 일정을 자유롭게 쌓아보세요.';
  const previewImage = imageUrl.trim() || 'https://images.unsplash.com/photo-1467269204594-9661b134dd2b?auto=format&fit=crop&w=1200&q=80';

  const scrollToDay = (date: string) => {
    document.getElementById(`travel-day-${date}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  const linkedOrderIds = useMemo(
    () => new Set(dayPlans.flatMap((plan) => plan.regions.flatMap((region) => region.schedules.map((schedule) => schedule.productOrderId).filter((orderId): orderId is number => orderId !== null)))),
    [dayPlans]
  );

  const openBookedProducts = async (dayIndex: number, regionIndex: number, regionLabel: string) => {
    const day = dayPlans[dayIndex];
    const region = day?.regions[regionIndex];
    if (!day || !region?.countryCode || !region.regionCode) return;

    setReservationTarget({ dayIndex, regionIndex, dayLabel: formatDate(day.date), regionLabel });
    setBookedProductModalOpen(true);
    setBookingLoading(true);
    setBookedProducts([]);
    try {
      setBookedProducts(await productApi.getScheduleCandidates({ countryCode: region.countryCode, regionCode: region.regionCode, useDate: day.date }));
    } catch (loadError) {
      console.error('Failed to load booked products for schedule', loadError);
      setError('일정에 추가할 예약 상품을 불러오지 못했습니다.');
    } finally {
      setBookingLoading(false);
    }
  };

  const linkBookedProduct = (order: BookedProduct) => {
    if (!reservationTarget) return;
    addSchedule(reservationTarget.dayIndex, reservationTarget.regionIndex, {
      title: order.productName,
      location: order.destinationName,
      memo: `${order.optionName} · 예약번호 ${order.orderNo} · 이용일 ${order.useDate} · 수량 ${order.quantity}개`,
      productOrderId: order.orderId,
      productOrderNo: order.orderNo,
    });
    setBookedProductModalOpen(false);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!user) {
      setError('로그인 후 일정을 만들 수 있어요.');
      return;
    }
    if (!travelStartDate || !travelEndDate) {
      setError('여행 시작일과 종료일을 먼저 선택해 주세요.');
      return;
    }

    const normalizedDays = dayPlans.map((plan) => ({
      dayNumber: plan.day,
      planDate: plan.date,
      regions: plan.regions
        .filter((region) => region.countryCode && region.regionCode)
        .map((region) => ({
          countryCode: region.countryCode,
          regionCode: region.regionCode,
          note: region.note.trim() || null,
          schedules: region.schedules
            .filter(hasScheduleContent)
            .map((schedule) => ({
              time: schedule.time || null,
              title: schedule.title.trim() || null,
              location: schedule.location.trim() || null,
              memo: schedule.memo.trim() || null,
              transportType: schedule.transportType || null,
              transportName: schedule.transportName.trim() || null,
              departureTime: schedule.departureTime || null,
              arrivalTime: schedule.arrivalTime || null,
              transportMemo: schedule.transportMemo.trim() || null,
              productOrderId: schedule.productOrderId,
            })),
        })),
    }));
    const hasRegion = normalizedDays.some((plan) => plan.regions.length > 0);
    if (false && !hasRegion) {
      setError('일정표에서 최소 한 곳의 여행지를 선택해 주세요.');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const payload = {
        title: title.trim(),
        description: description.trim() || null,
        imageUrl: imageUrl.trim() || null,
        isPublic,
        travelStartDate,
        travelEndDate,
        days: normalizedDays,
        packingItems: packingItems
          .filter((item) => item.item.trim())
          .map((item) => ({ item: item.item.trim(), required: item.required })),
      };
      const response = planId
        ? await axios.put(`/api/my-travel-plans/${planId}`, payload)
        : await axios.post('/api/my-travel-plans', payload);
      navigate('/my-trips', { replace: true, state: { createdPlanId: response.data.planId } });
    } catch (submitError) {
      console.error('Failed to create travel plan', submitError);
      if (axios.isAxiosError(submitError)) {
        const serverMessage = submitError.response?.data?.message || submitError.response?.data?.detail;
        if (serverMessage) {
          setError(serverMessage);
          return;
        }
      }
      setError('일정을 저장하지 못했습니다. 입력 내용을 확인한 뒤 다시 시도해 주세요.');
    } finally {
      setSubmitting(false);
    }
  };

  if (authLoading) {
    return <main className="mx-auto flex w-full max-w-6xl flex-grow items-center justify-center px-6 py-24 text-slate-400">로그인 정보를 확인하고 있어요.</main>;
  }

  if (!user) {
    return (
      <main className="mx-auto flex w-full max-w-6xl flex-grow items-center justify-center px-6 py-24">
        <div className={`${panelClassName} max-w-md p-8 text-center`}>
          <i className="fa-solid fa-lock mb-4 text-2xl text-indigo-300" />
          <h1 className="text-2xl font-bold text-white">로그인이 필요해요</h1>
          <p className="mt-3 text-sm leading-6 text-slate-400">내 여행 일정을 만들려면 로그인해 주세요.</p>
          <Link to="/login" className="theme-btn-primary mt-6 px-5 py-3">로그인하러 가기</Link>
        </div>
      </main>
    );
  }

  return (
    <main className="relative z-10 w-full flex-grow">
      <div className="pointer-events-none absolute -left-40 -top-32 h-[540px] w-[540px] rounded-full bg-indigo-500/10 blur-[150px]" />
      <div className="pointer-events-none absolute -bottom-48 -right-32 h-[540px] w-[540px] rounded-full bg-cyan-400/10 blur-[150px]" />

      <div className="mx-auto w-full max-w-[1440px] px-4 py-8 sm:px-6 md:py-12">
        <header className="mb-8 max-w-3xl">
          <p className="mb-3 text-xs font-bold tracking-[0.28em] text-indigo-300">TRIP BUILDER</p>
          <h1 className="text-3xl font-extrabold tracking-tight text-white md:text-5xl">{planId ? '여행 일정 수정' : '여행 일정 만들기'}</h1>
          <p className="mt-4 text-sm leading-7 text-slate-400 md:text-base">여행 기간을 고르면 모든 일차가 준비됩니다. 각 일차에 여행지를 넣고, 그 아래 일정표를 채워보세요.</p>
        </header>

        <div className="grid items-start gap-6 xl:grid-cols-[minmax(0,1.55fr)_minmax(360px,0.75fr)]">
          <form
            onSubmit={handleSubmit}
            onKeyDown={(event) => {
              if (event.key === 'Enter' && !(event.target as HTMLElement).dataset.searchableSelect) {
                event.preventDefault();
              }
            }}
            className="space-y-5"
          >
            <section className={`${panelClassName} p-5 md:p-6`}>
              <div className="flex flex-col justify-between gap-4 md:flex-row md:items-end">
                <div>
                  <p className="text-xs font-bold tracking-[0.2em] text-indigo-300">01. TRAVEL DATES</p>
                  <h2 className="mt-2 text-xl font-bold text-white">언제 떠나시나요?</h2>
                  <p className="mt-1 text-sm text-slate-500">선택한 날짜만큼 일차별 일정표가 자동으로 만들어집니다.</p>
                </div>
                {days.length > 0 ? <span className="rounded-full border border-indigo-400/20 bg-indigo-500/10 px-3 py-1.5 text-sm font-semibold text-indigo-100">총 {days.length}일</span> : null}
              </div>
              <div className="mt-5 grid gap-4 md:grid-cols-2">
                <div>
                  <label className="mb-2 block text-sm font-semibold text-slate-200">시작일</label>
                  <CustomCalendar value={travelStartDate} onChange={setTravelStartDate} placeholder="출발 날짜 선택" maxDate={travelEndDate ? parseLocalDate(travelEndDate) : undefined} />
                </div>
                <div>
                  <label className="mb-2 block text-sm font-semibold text-slate-200">종료일</label>
                  <CustomCalendar value={travelEndDate} onChange={setTravelEndDate} placeholder="도착 날짜 선택" minDate={travelStartDate ? parseLocalDate(travelStartDate) : undefined} />
                </div>
              </div>
            </section>

            <section className={`${panelClassName} p-5 md:p-6`}>
              <div className="flex items-center gap-3">
                <span className="flex h-8 w-8 items-center justify-center rounded-xl bg-indigo-500/15 text-sm font-bold text-indigo-200">02</span>
                <div>
                  <h2 className="font-bold text-white">여행 기본 정보</h2>
                  <p className="text-sm text-slate-500">제목과 설명은 여행 카드에 표시됩니다.</p>
                </div>
              </div>
              <div className="mt-5 grid gap-4 md:grid-cols-2">
                <div>
                  <label className="mb-2 block text-sm font-semibold text-slate-200">여행 제목</label>
                  <input value={title} onChange={(event) => setTitle(event.target.value)} className={inputClassName} placeholder="예: 여름의 파리와 로마 8일" required maxLength={150} />
                </div>
                <div>
                  <label className="mb-2 block text-sm font-semibold text-slate-200">대표 이미지 URL <span className="font-normal text-slate-500">(선택)</span></label>
                  <input value={imageUrl} onChange={(event) => setImageUrl(event.target.value)} className={inputClassName} placeholder="https://..." maxLength={500} />
                </div>
              </div>
              <div className="mt-4">
                <label className="mb-2 block text-sm font-semibold text-slate-200">여행 한 줄 소개 <span className="font-normal text-slate-500">(선택)</span></label>
                <textarea value={description} onChange={(event) => setDescription(event.target.value)} className={`${inputClassName} min-h-24 resize-y`} placeholder="이번 여행에서 놓치고 싶지 않은 순간을 적어보세요." maxLength={500} />
              </div>
            </section>

            <section className={panelClassName}>
              <div className="border-b border-white/10 px-5 py-5 md:px-6">
                <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                  <div>
                    <p className="text-xs font-bold tracking-[0.2em] text-indigo-300">03. DAILY PLAN</p>
                    <h2 className="mt-2 text-xl font-bold text-white">날짜별 일정표</h2>
                    <p className="mt-1 text-sm text-slate-500">여행지 하나를 추가한 뒤, 시간 순서대로 일정을 작성하세요.</p>
                  </div>
                  <span className="text-sm text-slate-400">여행지 {completedRegionCount}곳 · 일정 {spotCount}개</span>
                </div>
              </div>

              {days.length === 0 ? (
                <div className="px-5 py-14 text-center md:px-6">
                  <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-500/10 text-indigo-200"><i className="fa-regular fa-calendar-days" /></div>
                  <p className="mt-4 font-semibold text-white">여행 기간을 먼저 선택해 주세요.</p>
                  <p className="mt-2 text-sm text-slate-500">기간을 고르면 1일차부터 마지막 날까지 일정표가 생성됩니다.</p>
                </div>
              ) : (
                <>
                  <div className="flex gap-2 overflow-x-auto border-b border-white/10 bg-slate-950/20 px-5 py-3 md:px-6">
                    {dayPlans.map((plan) => (
                      <button key={plan.date} type="button" onClick={() => scrollToDay(plan.date)} className="min-w-[68px] rounded-xl border border-white/10 bg-white/[0.035] px-3 py-2 text-left transition hover:border-indigo-300/30 hover:bg-indigo-500/10">
                        <span className="block text-[11px] font-bold tracking-wide text-indigo-200">{plan.day}일차</span>
                        <span className="mt-0.5 block text-xs text-slate-400">{formatShortDate(plan.date)}</span>
                      </button>
                    ))}
                  </div>

                  <div className="space-y-5 p-3 sm:p-5 md:p-6">
                    {dayPlans.map((plan, dayIndex) => (
                      <article id={`travel-day-${plan.date}`} key={plan.date} className="scroll-mt-6 rounded-[24px] border border-white/10 bg-slate-950/35">
                        <div className="flex flex-col gap-4 border-b border-white/10 px-4 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-5">
                          <div className="flex items-center gap-3">
                            <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-indigo-500 text-sm font-extrabold text-white shadow-lg shadow-indigo-500/25">{plan.day}</span>
                            <div>
                              <h3 className="font-bold text-white">{formatDate(plan.date)}</h3>
                              <p className="mt-0.5 text-xs text-slate-500">{plan.regions.length === 0 ? '아직 여행지가 없어요.' : `${plan.regions.length}개 여행지 · ${plan.regions.reduce((sum, region) => sum + region.schedules.filter(hasScheduleContent).length, 0)}개 일정`}</p>
                            </div>
                          </div>
                          <button type="button" onClick={() => addRegion(dayIndex)} className={secondaryButtonClassName}><i className="fa-solid fa-plus text-indigo-300" />여행지 추가</button>
                        </div>

                        {plan.regions.length === 0 ? (
                          <div className="px-5 py-8 text-center">
                            <p className="text-sm text-slate-400">이 날 머무를 여행지를 먼저 추가해 주세요.</p>
                            <button type="button" onClick={() => addRegion(dayIndex)} className="mt-3 text-sm font-semibold text-indigo-200 transition hover:text-white">+ 여행지 추가하기</button>
                          </div>
                        ) : (
                          <div className="space-y-4 p-3 sm:p-5">
                            {plan.regions.map((region, regionIndex) => {
                              const options = regionsByCountry[region.countryCode] || [];
                              const selectedRegion = options.find((item) => item.regionCode === region.regionCode);
                              const regionLabel = selectedRegion?.regionName || '여행지 선택';
                              const canAddSchedule = Boolean(region.countryCode && region.regionCode);
                              return (
                                <section key={region.id} className="overflow-hidden rounded-[20px] border border-white/10 bg-white/[0.025]">
                                  <div className="flex flex-wrap items-center justify-between gap-3 border-b border-white/10 bg-white/[0.025] px-4 py-3.5">
                                    <div className="flex min-w-0 items-center gap-2.5">
                                      <i className="fa-solid fa-location-dot text-cyan-300" />
                                      <h4 className="truncate font-bold text-white">{regionLabel}</h4>
                                      <span className="rounded-full bg-white/5 px-2.5 py-1 text-xs text-slate-400">여행지 {regionIndex + 1}</span>
                                    </div>
                                    <button type="button" onClick={() => removeRegion(dayIndex, regionIndex)} className="text-xs font-medium text-slate-500 transition hover:text-rose-300">여행지 삭제</button>
                                  </div>

                                  <div className="p-4">
                                    <div className="grid gap-3 md:grid-cols-[1fr_1fr_1.3fr]">
                                      <SearchableSelect
                                        value={region.countryCode}
                                        options={countries.map((country) => ({ value: country.countryCode, label: country.countryName, hint: country.countryCode }))}
                                        placeholder={loadingCountries ? '국가를 불러오는 중...' : '국가 검색'}
                                        disabled={loadingCountries}
                                        onChange={(value) => updateDayRegion(dayIndex, regionIndex, 'countryCode', value)}
                                      />
                                      <SearchableSelect
                                        value={region.regionCode}
                                        options={options.map((option) => ({ value: option.regionCode, label: option.regionName, hint: option.regionCode }))}
                                        placeholder={region.countryCode ? '지역 검색' : '국가를 먼저 선택'}
                                        disabled={!region.countryCode}
                                        onChange={(value) => updateDayRegion(dayIndex, regionIndex, 'regionCode', value)}
                                      />
                                      <input value={region.note} onChange={(event) => updateDayRegion(dayIndex, regionIndex, 'note', event.target.value)} className={inputClassName} placeholder="이동·숙소 메모 (선택)" maxLength={500} />
                                    </div>

                                    <div className="mt-5 overflow-x-hidden rounded-2xl border border-white/10">
                                      <div className="min-w-0">
                                        <div className="hidden grid-cols-[92px_1.1fr_1.1fr_1fr_128px_36px] gap-2 border-b border-white/10 bg-white/[0.04] px-3 py-2 text-[11px] font-bold tracking-wide text-slate-500 md:grid">
                                          <span>시간</span><span>일정</span><span>장소</span><span>비고</span><span>이동수단</span><span className="sr-only">삭제</span>
                                        </div>
                                        {region.schedules.length === 0 ? (
                                          <div className="px-3 py-5 text-center text-sm text-slate-500">{canAddSchedule ? '아직 입력된 일정이 없어요. 아래 버튼으로 추가해 주세요.' : '국가와 지역을 선택하면 이곳에 일정표를 추가할 수 있어요.'}</div>
                                        ) : region.schedules.map((schedule, scheduleIndex) => {
                                          const transport = schedule.transportType ? transportByType[schedule.transportType] : null;
                                          return (
                                            <Fragment key={schedule.id}>
                                              <div className="grid grid-cols-1 items-center gap-2 border-b border-white/5 px-3 py-2 sm:grid-cols-2 md:grid-cols-[92px_1.1fr_1.1fr_1fr_128px_36px]">
                                                <input value={schedule.time} onChange={(event) => updateSchedule(dayIndex, regionIndex, scheduleIndex, 'time', formatTimeInput(event.target.value))} className={`${inputClassName} px-2.5 py-2`} inputMode="numeric" placeholder="09:00" maxLength={5} aria-label="시간" />
                                                <input value={schedule.title} onChange={(event) => updateSchedule(dayIndex, regionIndex, scheduleIndex, 'title', event.target.value)} className={`${inputClassName} px-2.5 py-2`} placeholder="예: 미술관 관람" maxLength={150} />
                                                <input value={schedule.location} onChange={(event) => updateSchedule(dayIndex, regionIndex, scheduleIndex, 'location', event.target.value)} className={`${inputClassName} px-2.5 py-2`} placeholder="예: 루브르 박물관" maxLength={150} />
                                                <input value={schedule.memo} onChange={(event) => updateSchedule(dayIndex, regionIndex, scheduleIndex, 'memo', event.target.value)} className={`${inputClassName} px-2.5 py-2`} placeholder="예약·주의사항" maxLength={500} />
                                                <StyledSelect value={schedule.transportType} onChange={(value) => updateSchedule(dayIndex, regionIndex, scheduleIndex, 'transportType', value)} ariaLabel="이동수단" options={[{ value: '', label: '이동 없음', icon: 'fa-ban' }, ...transportOptions.map((option) => ({ value: option.value, label: option.label, icon: option.icon }))]} className={`${inputClassName} px-2.5 py-2`} />
                                                <button type="button" onClick={() => removeSchedule(dayIndex, regionIndex, scheduleIndex)} aria-label="일정 삭제" className="rounded-lg p-2 text-slate-500 transition hover:bg-rose-500/10 hover:text-rose-300"><i className="fa-solid fa-xmark" /></button>
                                              </div>
                                              {transport ? (
                                                <div className="border-b border-indigo-400/10 bg-indigo-500/[0.055] px-4 py-3">
                                                  <div className="mb-2 flex items-center gap-2 text-xs font-bold text-indigo-200"><i className={`fa-solid ${transport.icon}`} />{transport.label} 이동 정보</div>
                                                  <div className="grid grid-cols-1 gap-2 sm:grid-cols-2 md:grid-cols-[1.25fr_0.7fr_0.7fr_1.3fr]">
                                                    <input value={schedule.transportName} onChange={(event) => updateSchedule(dayIndex, regionIndex, scheduleIndex, 'transportName', event.target.value)} className={`${inputClassName} px-2.5 py-2`} placeholder={`${transport.nameLabel}: ${transport.namePlaceholder}`} maxLength={100} aria-label={transport.nameLabel} />
                                                    <input value={schedule.departureTime} onChange={(event) => updateSchedule(dayIndex, regionIndex, scheduleIndex, 'departureTime', formatTimeInput(event.target.value))} className={`${inputClassName} px-2.5 py-2`} inputMode="numeric" placeholder="출발 09:00" maxLength={5} aria-label="출발 시간" />
                                                    <input value={schedule.arrivalTime} onChange={(event) => updateSchedule(dayIndex, regionIndex, scheduleIndex, 'arrivalTime', formatTimeInput(event.target.value))} className={`${inputClassName} px-2.5 py-2`} inputMode="numeric" placeholder="도착 11:30" maxLength={5} aria-label="도착 시간" />
                                                    <input value={schedule.transportMemo} onChange={(event) => updateSchedule(dayIndex, regionIndex, scheduleIndex, 'transportMemo', event.target.value)} className={`${inputClassName} px-2.5 py-2`} placeholder={transport.memoPlaceholder} maxLength={500} aria-label="이동 메모" />
                                                  </div>
                                                </div>
                                              ) : null}
                                              {schedule.productOrderId ? (
                                                <div className="border-b border-cyan-300/10 bg-cyan-400/[0.045] px-4 py-2 text-xs text-cyan-100">
                                                  <i className="fa-solid fa-ticket mr-2 text-cyan-300" />예약 상품 연동됨{schedule.productOrderNo ? ` · ${schedule.productOrderNo}` : ''}
                                                </div>
                                              ) : null}
                                            </Fragment>
                                          );
                                        })}
                                      </div>
                                    </div>
                                    <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-2">
                                      <button type="button" onClick={() => addSchedule(dayIndex, regionIndex)} disabled={!canAddSchedule} className="inline-flex items-center gap-2 text-sm font-semibold text-indigo-200 transition hover:text-white disabled:cursor-not-allowed disabled:text-slate-600"><i className="fa-solid fa-plus" />일정 추가</button>
                                      <button type="button" onClick={() => void openBookedProducts(dayIndex, regionIndex, regionLabel)} disabled={!canAddSchedule} className="inline-flex items-center gap-2 text-sm font-semibold text-cyan-200 transition hover:text-white disabled:cursor-not-allowed disabled:text-slate-600"><i className="fa-solid fa-ticket" />예약 상품 추가</button>
                                    </div>
                                  </div>
                                </section>
                              );
                            })}
                          </div>
                        )}
                      </article>
                    ))}
                  </div>
                </>
              )}
            </section>

            <section className={`${panelClassName} p-5 md:p-6`}>
              <div className="grid gap-5 md:grid-cols-[0.9fr_1.1fr]">
                <div>
                  <p className="text-xs font-bold tracking-[0.2em] text-indigo-300">04. SETTINGS</p>
                  <h2 className="mt-2 text-lg font-bold text-white">공개 설정</h2>
                  <div className="mt-4 flex gap-2">
                    {([['Y', '공개'], ['N', '비공개']] as const).map(([value, label]) => <button key={value} type="button" onClick={() => setIsPublic(value)} className={`rounded-xl border px-4 py-2.5 text-sm font-semibold transition ${isPublic === value ? 'border-white bg-white text-slate-950' : 'border-white/10 bg-white/[0.03] text-slate-400 hover:bg-white/10 hover:text-white'}`}>{label}</button>)}
                  </div>
                </div>
                <div className="rounded-2xl border border-white/10 bg-slate-950/35 p-4">
                  <div className="flex items-center justify-between gap-3">
                    <div><h3 className="font-bold text-white">여행 준비물</h3><p className="mt-1 text-sm text-slate-500">필요한 경우에만 추가해 주세요.</p></div>
                    <button type="button" onClick={() => setPackingOpen(true)} className={secondaryButtonClassName}><i className="fa-solid fa-suitcase-rolling text-indigo-300" />준비물 관리</button>
                  </div>
                  <p className="mt-4 text-sm text-slate-400">필수 준비물 <span className="font-bold text-indigo-200">{checkedPackingCount}개</span></p>
                </div>
              </div>

              {error ? <p className="mt-5 rounded-xl border border-rose-400/20 bg-rose-500/10 px-4 py-3 text-sm text-rose-200">{error}</p> : null}
              <div className="mt-5 flex flex-col gap-3 sm:flex-row">
                <button type="submit" disabled={submitting} className="theme-btn-primary min-w-[154px] px-6 py-3.5 disabled:cursor-not-allowed disabled:opacity-60">{submitting ? '저장 중...' : planId ? '수정 저장' : '일정 만들기'}</button>
                <Link to="/my-trips" className="inline-flex items-center justify-center rounded-2xl border border-white/10 bg-white/[0.04] px-6 py-3.5 font-semibold text-slate-300 transition hover:bg-white/10 hover:text-white">취소</Link>
              </div>
            </section>
          </form>

          <TripPreview
            isPublic={isPublic}
            travelStartDate={travelStartDate}
            travelEndDate={travelEndDate}
            totalDays={days.length}
            title={previewTitle}
            summary={previewSummary}
            imageUrl={previewImage}
            regionCount={completedRegionCount}
            scheduleCount={spotCount}
            days={previewDays}
            panelClassName={panelClassName}
          />
        </div>
      </div>

      <PackingModal
        isOpen={packingOpen}
        items={packingItems}
        secondaryButtonClassName={secondaryButtonClassName}
        onClose={() => setPackingOpen(false)}
        onItemChange={updatePackingItem}
        onItemRemove={removePackingItem}
        onItemAdd={() => setPackingItems((current) => [...current, createPackingItem()])}
      />
      <BookedProductModal
        isOpen={bookedProductModalOpen}
        orders={bookedProducts}
        linkedOrderIds={linkedOrderIds}
        loading={bookingLoading}
        dayLabel={reservationTarget?.dayLabel || ''}
        regionLabel={reservationTarget?.regionLabel || ''}
        onClose={() => setBookedProductModalOpen(false)}
        onLink={linkBookedProduct}
      />
    </main>
  );
};
