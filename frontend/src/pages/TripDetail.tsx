import { useEffect, useState } from 'react';
import axios from 'axios';
import { Link, useParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

type Schedule = {
  time: string | null;
  title: string | null;
  location: string | null;
  memo: string | null;
  transportType: 'TRAIN' | 'CAR' | 'FLIGHT' | 'CRUISE' | 'OTHER' | null;
  transportName: string | null;
  departureTime: string | null;
  arrivalTime: string | null;
  transportMemo: string | null;
};

type Region = {
  countryCode: string;
  countryName: string;
  regionCode: string;
  regionName: string;
  note: string | null;
  schedules: Schedule[];
};

type Day = {
  dayNumber: number;
  planDate: string;
  regions: Region[];
};

type TripDetail = {
  planId: number;
  title: string;
  description: string | null;
  imageUrl: string | null;
  userNicknm: string;
  spotCount: number;
  isPublic: string;
  travelStartDate: string | null;
  travelEndDate: string | null;
  days: Day[];
  packingItems: { item: string; required: boolean }[];
};

const formatDate = (value: string) => {
  const date = new Date(`${value}T00:00:00`);
  const weekday = ['일', '월', '화', '수', '목', '금', '토'][date.getDay()];
  return `${date.getMonth() + 1}월 ${date.getDate()}일 (${weekday})`;
};

const transportLabels = {
  TRAIN: { label: '기차', icon: 'fa-train-subway' },
  CAR: { label: '자가용', icon: 'fa-car-side' },
  FLIGHT: { label: '항공', icon: 'fa-plane-departure' },
  CRUISE: { label: '크루즈', icon: 'fa-ship' },
  OTHER: { label: '기타 이동', icon: 'fa-route' },
};

export const TripDetail = () => {
  const { planId } = useParams();
  const { user, loading: authLoading } = useAuth();
  const [trip, setTrip] = useState<TripDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (authLoading) return;
    if (!user || !planId) {
      setLoading(false);
      setError('로그인 후 여행 일정을 확인할 수 있어요.');
      return;
    }

    const loadTrip = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await axios.get<TripDetail>(`/api/my-travel-plans/${planId}`);
        setTrip(response.data);
      } catch (loadError) {
        console.error('Failed to load trip detail', loadError);
        setError('여행 일정을 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    void loadTrip();
  }, [authLoading, planId, user]);

  if (loading) {
    return <main className="mx-auto flex w-full max-w-6xl flex-grow items-center justify-center px-6 py-24 text-slate-400">여행 일정을 불러오고 있어요.</main>;
  }

  if (error || !trip) {
    return (
      <main className="mx-auto flex w-full max-w-6xl flex-grow items-center justify-center px-6 py-24">
        <div className="theme-glass-card max-w-md text-center">
          <i className="fa-solid fa-triangle-exclamation mb-4 text-2xl text-rose-300" />
          <p className="text-slate-300">{error || '여행 일정을 찾을 수 없어요.'}</p>
          <Link to="/my-trips" className="theme-btn-primary mt-6 px-5 py-3">내 여행으로 돌아가기</Link>
        </div>
      </main>
    );
  }

  return (
    <main className="relative z-10 w-full flex-grow">
      <div className="pointer-events-none absolute -left-36 top-0 h-[460px] w-[460px] rounded-full bg-indigo-500/10 blur-[140px]" />
      <div className="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6 md:py-12">
        <Link to="/my-trips" className="inline-flex items-center gap-2 text-sm font-semibold text-slate-400 transition hover:text-white"><i className="fa-solid fa-arrow-left" />내 여행</Link>
        <section className="mt-5 overflow-hidden rounded-[30px] border border-white/10 bg-slate-950/55 shadow-[0_28px_90px_rgba(0,0,0,0.25)]">
          <div className="relative h-56 md:h-72">
            {trip.imageUrl ? <img src={trip.imageUrl} alt={trip.title} className="h-full w-full object-cover" /> : <div className="h-full bg-gradient-to-br from-indigo-950 to-slate-950" />}
            <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/35 to-transparent" />
            <div className="absolute bottom-0 left-0 right-0 p-5 md:p-8">
              <div className="mb-3 flex flex-wrap gap-2"><span className={`rounded-full px-3 py-1 text-xs font-bold ${trip.isPublic === 'Y' ? 'bg-emerald-500/20 text-emerald-100' : 'bg-white/15 text-slate-100'}`}>{trip.isPublic === 'Y' ? '공개 여행' : '비공개 여행'}</span>{trip.travelStartDate && trip.travelEndDate ? <span className="rounded-full bg-black/30 px-3 py-1 text-xs font-bold text-white">{trip.travelStartDate.replaceAll('-', '.')} - {trip.travelEndDate.replaceAll('-', '.')}</span> : null}</div>
              <h1 className="text-3xl font-extrabold tracking-tight text-white md:text-5xl">{trip.title}</h1>
              {trip.description ? <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-200 md:text-base">{trip.description}</p> : null}
            </div>
          </div>
          <div className="grid gap-4 border-b border-white/10 p-5 sm:grid-cols-3 md:p-6"><div><p className="text-xs text-slate-500">여행 기간</p><p className="mt-1 font-bold text-white">{trip.days.length}일</p></div><div><p className="text-xs text-slate-500">입력한 일정</p><p className="mt-1 font-bold text-white">{trip.spotCount}개</p></div><div><p className="text-xs text-slate-500">작성자</p><p className="mt-1 font-bold text-white">{trip.userNicknm}</p></div></div>
          <div className="space-y-5 p-4 sm:p-6">
            {trip.days.map((day) => (
              <article key={day.dayNumber} className="rounded-[24px] border border-white/10 bg-white/[0.025]">
                <div className="flex items-center gap-3 border-b border-white/10 px-4 py-4 sm:px-5"><span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-indigo-500 text-sm font-extrabold text-white">{day.dayNumber}</span><div><p className="text-xs font-bold text-indigo-200">{day.dayNumber}일차</p><h2 className="mt-0.5 font-bold text-white">{formatDate(day.planDate)}</h2></div></div>
                {day.regions.length === 0 ? (
                  <p className="px-5 py-6 text-sm text-slate-500">아직 등록한 여행지가 없어요.</p>
                ) : (
                  <div className="space-y-4 p-4 sm:p-5">
                    {day.regions.map((region) => (
                      <section key={`${day.dayNumber}-${region.countryCode}-${region.regionCode}`} className="rounded-2xl border border-white/10 bg-slate-950/35">
                        <div className="flex flex-wrap items-center gap-2 border-b border-white/10 px-4 py-3">
                          <i className="fa-solid fa-location-dot text-cyan-300" />
                          <h3 className="font-bold text-white">{region.regionName}</h3>
                          <span className="text-sm text-slate-500">{region.countryName}</span>
                          {region.note ? <span className="ml-auto text-xs text-slate-400">{region.note}</span> : null}
                        </div>
                        {region.schedules.length === 0 ? (
                          <p className="px-4 py-4 text-sm text-slate-500">등록한 일정이 없어요.</p>
                        ) : (
                          <div className="divide-y divide-white/5">
                            {region.schedules.map((schedule, index) => {
                              const transport = schedule.transportType ? transportLabels[schedule.transportType] : null;
                              const transportDetails = [
                                schedule.transportName,
                                schedule.departureTime || schedule.arrivalTime ? `${schedule.departureTime || '--:--'} → ${schedule.arrivalTime || '--:--'}` : null,
                                schedule.transportMemo,
                              ].filter(Boolean);

                              return (
                                <div key={`${region.regionCode}-${index}`} className="grid gap-2 px-4 py-3 sm:grid-cols-[84px_1fr]">
                                  <span className="text-sm font-bold text-indigo-200">{schedule.time || schedule.departureTime || '--:--'}</span>
                                  <div>
                                    <p className="font-semibold text-white">{schedule.title || schedule.location || transport?.label || '일정'}</p>
                                    {[schedule.location, schedule.memo].filter(Boolean).length > 0 ? <p className="mt-1 text-sm text-slate-400">{[schedule.location, schedule.memo].filter(Boolean).join(' · ')}</p> : null}
                                    {transport ? <p className="mt-1.5 text-sm text-indigo-200"><i className={`fa-solid ${transport.icon} mr-1.5`} />{transport.label}{transportDetails.length > 0 ? ` · ${transportDetails.join(' · ')}` : ''}</p> : null}
                                  </div>
                                </div>
                              );
                            })}
                          </div>
                        )}
                      </section>
                    ))}
                  </div>
                )}
              </article>
            ))}
          </div>
          {trip.packingItems.length > 0 ? <section className="border-t border-white/10 p-5 md:p-6"><h2 className="font-bold text-white">여행 준비물</h2><div className="mt-3 flex flex-wrap gap-2">{trip.packingItems.map((item) => <span key={item.item} className={`rounded-full border px-3 py-1.5 text-sm ${item.required ? 'border-indigo-400/20 bg-indigo-500/10 text-indigo-100' : 'border-white/10 bg-white/[0.03] text-slate-300'}`}>{item.required ? '필수' : '선택'} · {item.item}</span>)}</div></section> : null}
        </section>
      </div>
    </main>
  );
};
