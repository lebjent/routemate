import { useEffect, useRef, useState } from 'react';
import axios from 'axios';
import { Document, Font, Page, PDFDownloadLink, StyleSheet, Text, View } from '@react-pdf/renderer';
import { Link, useParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { formatTravelDuration } from '../features/trip-builder/model';

Font.register({ family: 'NotoSansKR', src: '/fonts/NotoSansKR-Regular.ttf' });
Font.register({ family: 'NotoSansKR', src: '/fonts/NotoSansKR-Regular.ttf', fontWeight: 700 });
Font.register({ family: 'GowunDodum', src: '/fonts/GowunDodum-Regular.ttf' });
Font.register({ family: 'Jua', src: '/fonts/Jua-Regular.ttf' });
Font.register({ family: 'Gaegu', src: '/fonts/Gaegu-Regular.ttf' });
Font.register({ family: 'DoHyeon', src: '/fonts/DoHyeon-Regular.ttf' });

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
  viewCount: number;
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

type TripDetailProps = {
  publicView?: boolean;
};

const pdfStyles = StyleSheet.create({
  page: { padding: 42, fontFamily: 'GowunDodum', color: '#172033', fontSize: 10, backgroundColor: '#fffdfb' },
  logoBar: { flexDirection: 'row', alignItems: 'center', borderBottom: '2px solid #f08aa5', paddingBottom: 14, marginBottom: 24 },
  logoMark: { width: 30, height: 30, borderRadius: 15, backgroundColor: '#f08aa5', color: '#ffffff', textAlign: 'center', paddingTop: 6, fontSize: 14 },
  logo: { color: '#d45b7c', fontSize: 20, marginLeft: 9 },
  title: { fontFamily: 'GowunDodum', fontSize: 25, marginBottom: 10, color: '#26324a' }, period: { color: '#d45b7c', fontSize: 11, marginBottom: 10 }, description: { color: '#657086', marginBottom: 18, lineHeight: 1.5 },
  day: { marginTop: 16, padding: 14, borderRadius: 12, backgroundColor: '#fff4f5', border: '1px solid #f8d7df' }, dayTitle: { color: '#c94f72', fontSize: 15, marginBottom: 8 }, region: { color: '#37445d', fontSize: 11, marginTop: 6, marginBottom: 4 },
  schedule: { flexDirection: 'row', marginTop: 5, paddingBottom: 3 }, time: { width: 55, color: '#d45b7c' }, scheduleText: { flex: 1, color: '#4b5568' }, packing: { marginTop: 20, padding: 14, borderRadius: 12, backgroundColor: '#f5f8ff', border: '1px solid #dce6ff' },
});

const pdfThemes = [
  { id: 'lovely', name: '러블리 핑크', description: '따뜻하고 귀여운 여행 다이어리', fontFamily: 'GowunDodum', accent: '#d45b7c', background: '#fffdfb', card: '#fff4f5', layout: 'rounded' },
  { id: 'mint', name: '민트 트래블', description: '산뜻한 바다 여행 느낌', fontFamily: 'Jua', accent: '#0f9f9a', background: '#f7fffd', card: '#e8fbf7', layout: 'timeline' },
  { id: 'diary', name: '감성 다이어리', description: '손글씨 감성의 기록장', fontFamily: 'Gaegu', accent: '#9a6748', background: '#fffaf0', card: '#fff1d6', layout: 'notebook' },
  { id: 'lavender', name: '모던 라벤더', description: '깔끔하고 세련된 일정표', fontFamily: 'NotoSansKR', accent: '#4f46e5', background: '#ffffff', card: '#f5f7ff', layout: 'minimal' },
  { id: 'sunset', name: '선셋 바캉스', description: '활기찬 휴양지 포스터 스타일', fontFamily: 'DoHyeon', accent: '#e76f2f', background: '#fffaf5', card: '#fff0df', layout: 'poster' },
] as const;

const pdfThemePresentation = {
  lovely: { icon: 'fa-heart', badge: 'SOFT & LOVELY', pattern: 'radial-gradient(circle at 12px 12px, rgba(212,91,124,.16) 2px, transparent 2px)' },
  mint: { icon: 'fa-water', badge: 'FRESH TIMELINE', pattern: 'linear-gradient(135deg, rgba(15,159,154,.14), transparent 55%)' },
  diary: { icon: 'fa-book-open', badge: 'HANDWRITTEN', pattern: 'repeating-linear-gradient(0deg, transparent 0 18px, rgba(154,103,72,.12) 18px 19px)' },
  lavender: { icon: 'fa-layer-group', badge: 'CLEAN & MODERN', pattern: 'linear-gradient(160deg, rgba(79,70,229,.12), transparent 48%)' },
  sunset: { icon: 'fa-sun', badge: 'BOLD POSTER', pattern: 'linear-gradient(145deg, rgba(231,111,47,.24), rgba(255,240,223,.7) 48%, transparent 49%)' },
} as const;

const PdfFooter = ({ accent }: { accent: string }) => <View fixed style={{ position: 'absolute', bottom: 20, left: 42, right: 42, flexDirection: 'row', justifyContent: 'space-between', borderTopWidth: 1, borderTopColor: `${accent}44`, paddingTop: 7 }}><Text style={{ color: accent, fontSize: 8 }}>RouteMate · MY TRAVEL PLAN</Text><Text render={({ pageNumber, totalPages }) => `${pageNumber} / ${totalPages}`} style={{ color: '#94a3b8', fontSize: 8 }} /></View>;

const PdfPacking = ({ trip, accent }: { trip: TripDetail; accent: string }) => trip.packingItems.length > 0 ? <View style={{ marginTop: 18, padding: 12, borderRadius: 10, backgroundColor: '#ffffff', borderWidth: 1, borderColor: `${accent}33` }}><Text style={{ color: accent, fontSize: 11, marginBottom: 6 }}>PACKING LIST</Text><Text style={{ color: '#475569', fontSize: 9, lineHeight: 1.6 }}>{trip.packingItems.map((item) => `${item.required ? '●' : '○'} ${item.item}`).join('    ')}</Text></View> : null;

const PdfScheduleRows = ({ day, accent, mode }: { day: Day; accent: string; mode: 'cards' | 'timeline' | 'notebook' | 'table' | 'poster' }) => <View>{day.regions.map((region) => <View key={`${day.dayNumber}-${region.regionCode}`} style={{ marginTop: 7 }}><Text style={{ color: mode === 'poster' ? '#ffffff' : '#334155', fontSize: 10, marginBottom: 5 }}>{region.countryName} · {region.regionName}</Text>{region.schedules.length === 0 ? <Text style={{ color: '#94a3b8', fontSize: 9 }}>등록된 일정이 없습니다.</Text> : region.schedules.map((schedule, index) => <View key={`${region.regionCode}-${index}`} style={{ flexDirection: 'row', paddingVertical: mode === 'table' ? 7 : 4, paddingHorizontal: mode === 'cards' ? 7 : 0, marginBottom: mode === 'cards' ? 4 : 0, borderRadius: mode === 'cards' ? 7 : 0, backgroundColor: mode === 'cards' ? '#ffffffaa' : 'transparent', borderBottomWidth: mode === 'table' || mode === 'notebook' ? 1 : 0, borderBottomColor: mode === 'notebook' ? '#d8c5ad' : '#e2e8f0' }}><Text style={{ width: 52, color: mode === 'poster' ? '#fff7ed' : accent, fontSize: 9 }}>{schedule.time || '--:--'}</Text><Text style={{ flex: 1, color: mode === 'poster' ? '#ffffff' : '#475569', fontSize: 9 }}>{schedule.title || schedule.location || '일정'}{schedule.location ? ` · ${schedule.location}` : ''}{schedule.memo ? ` — ${schedule.memo}` : ''}</Text></View>)}</View>)}</View>;

const TripPdfDocument = ({ trip, theme }: { trip: TripDetail; theme: typeof pdfThemes[number] }) => {
  const commonPage = { ...pdfStyles.page, paddingBottom: 48, fontFamily: theme.fontFamily, backgroundColor: theme.background };

  if (theme.id === 'mint') return <Document><Page size="A4" style={{ ...commonPage, backgroundColor: '#f3fffc' }}><View style={{ overflow: 'hidden', borderRadius: 18, marginBottom: 20, backgroundColor: '#087f7a' }}><View style={{ padding: 22, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' }}><View style={{ width: '72%' }}><Text style={{ color: '#bff8ed', fontSize: 8, letterSpacing: 2 }}>ROUTEMATE BOARDING PLAN</Text><Text style={{ color: '#ffffff', fontSize: 27, marginTop: 13 }}>{trip.title}</Text></View><View style={{ width: 70, alignItems: 'flex-end' }}><Text style={{ color: '#ffffff', fontSize: 18 }}>RM</Text><Text style={{ color: '#9ceadf', fontSize: 7, marginTop: 4 }}>TICKET 001</Text></View></View><View style={{ flexDirection: 'row', backgroundColor: '#ffffff', borderTopWidth: 1, borderTopColor: '#62cfc4' }}><View style={{ flex: 1, padding: 11, borderRightWidth: 1, borderRightColor: '#d7f5f0' }}><Text style={{ color: '#78a9a4', fontSize: 7 }}>DEPARTURE</Text><Text style={{ color: '#086963', fontSize: 9, marginTop: 4 }}>{trip.travelStartDate}</Text></View><View style={{ flex: 1, padding: 11, borderRightWidth: 1, borderRightColor: '#d7f5f0' }}><Text style={{ color: '#78a9a4', fontSize: 7 }}>ARRIVAL</Text><Text style={{ color: '#086963', fontSize: 9, marginTop: 4 }}>{trip.travelEndDate}</Text></View><View style={{ width: 76, padding: 11 }}><Text style={{ color: '#78a9a4', fontSize: 7 }}>DAYS</Text><Text style={{ color: '#086963', fontSize: 9, marginTop: 4 }}>{trip.days.length} DAYS</Text></View></View></View>{trip.description ? <Text style={{ marginBottom: 14, color: '#527773', fontSize: 9, lineHeight: 1.6 }}>{trip.description}</Text> : null}<View style={{ borderLeftWidth: 2, borderLeftColor: '#43bdb2', marginLeft: 18, paddingLeft: 22 }}>{trip.days.map((day, index) => <View key={day.dayNumber} wrap={false} style={{ position: 'relative', marginBottom: 15, padding: 14, borderRadius: 12, backgroundColor: index % 2 === 0 ? '#e5faf6' : '#ffffff', borderWidth: 1, borderColor: '#c8eee8' }}><Text style={{ position: 'absolute', left: -36, top: 13, width: 25, height: 25, paddingTop: 8, borderRadius: 13, textAlign: 'center', color: '#ffffff', backgroundColor: '#0f9f9a', fontSize: 7 }}>{day.dayNumber}</Text><View style={{ flexDirection: 'row', justifyContent: 'space-between', paddingBottom: 7, borderBottomWidth: 1, borderBottomColor: '#b9e5de' }}><Text style={{ color: '#087f7a', fontSize: 13 }}>STOP {String(day.dayNumber).padStart(2, '0')}</Text><Text style={{ color: '#5d8c87', fontSize: 8 }}>{day.planDate}</Text></View><PdfScheduleRows day={day} accent={theme.accent} mode="timeline" /></View>)}</View><PdfPacking trip={trip} accent={theme.accent} /><PdfFooter accent={theme.accent} /></Page></Document>;

  if (theme.id === 'diary') return <Document><Page size="A4" style={{ ...commonPage, backgroundColor: '#f6efe1', paddingHorizontal: 48 }}><View fixed style={{ position: 'absolute', left: 24, top: 0, bottom: 0, width: 5, backgroundColor: '#b77b5a' }} /><View style={{ position: 'relative', padding: 22, marginBottom: 20, backgroundColor: '#fffaf0', borderWidth: 1, borderColor: '#d8c5ad', transform: 'rotate(-0.6deg)' }}><View style={{ position: 'absolute', top: -7, left: 180, width: 110, height: 15, backgroundColor: '#e9cf98aa', transform: 'rotate(2deg)' }} /><Text style={{ color: '#9a6748', fontSize: 10 }}>RouteMate · MY LITTLE TRAVEL BOOK</Text><Text style={{ color: '#49372c', fontSize: 30, marginTop: 20 }}>{trip.title}</Text><View style={{ flexDirection: 'row', marginTop: 14 }}><Text style={{ color: '#72513d', fontSize: 10 }}>from {trip.travelStartDate}</Text><Text style={{ color: '#b28a6c', fontSize: 10, marginHorizontal: 8 }}>······</Text><Text style={{ color: '#72513d', fontSize: 10 }}>to {trip.travelEndDate}</Text></View>{trip.description ? <Text style={{ color: '#786657', fontSize: 10, lineHeight: 1.7, marginTop: 15 }}>{trip.description}</Text> : null}</View>{trip.days.map((day, index) => <View key={day.dayNumber} wrap={false} style={{ position: 'relative', marginTop: 14, marginLeft: index % 2 === 0 ? 0 : 18, marginRight: index % 2 === 0 ? 18 : 0, padding: 17, backgroundColor: index % 2 === 0 ? '#fffdf6' : '#f9ead5', borderWidth: 1, borderColor: '#d9c3a7', transform: index % 2 === 0 ? 'rotate(0.35deg)' : 'rotate(-0.35deg)' }}><View style={{ position: 'absolute', top: -6, right: index % 2 === 0 ? 22 : 190, width: 72, height: 13, backgroundColor: index % 2 === 0 ? '#e7b5a7aa' : '#c7d9b8aa' }} /><View style={{ flexDirection: 'row', alignItems: 'center', marginBottom: 6 }}><Text style={{ color: '#9a6748', fontSize: 21 }}>{String(day.dayNumber).padStart(2, '0')}</Text><View style={{ marginLeft: 10 }}><Text style={{ color: '#664937', fontSize: 12 }}>오늘의 여행 기록</Text><Text style={{ color: '#a47f66', fontSize: 8, marginTop: 2 }}>{day.planDate}</Text></View></View><PdfScheduleRows day={day} accent={theme.accent} mode="notebook" /></View>)}<View style={{ marginTop: 18, padding: 12, borderWidth: 1, borderStyle: 'dashed', borderColor: '#bda78d', backgroundColor: '#fffaf0' }}><Text style={{ color: '#9a6748', fontSize: 12, marginBottom: 5 }}>MEMO & PACKING</Text><Text style={{ color: '#765c49', fontSize: 9, lineHeight: 1.6 }}>{trip.packingItems.length > 0 ? trip.packingItems.map((item) => `${item.required ? '✓' : '○'} ${item.item}`).join('    ') : '여행의 작은 순간들을 기록해 보세요.'}</Text></View><PdfFooter accent={theme.accent} /></Page></Document>;

  if (theme.id === 'lavender') return <Document><Page size="A4" style={commonPage}><View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingBottom: 12, borderBottomWidth: 1, borderBottomColor: '#c7d2fe' }}><Text style={{ color: theme.accent, fontSize: 16 }}>ROUTEMATE</Text><Text style={{ color: '#94a3b8', fontSize: 8 }}>TRAVEL ITINERARY</Text></View><Text style={{ color: '#111827', fontSize: 27, marginTop: 24 }}>{trip.title}</Text><View style={{ flexDirection: 'row', gap: 14, marginTop: 14, marginBottom: 18 }}><View style={{ flex: 1, padding: 11, backgroundColor: '#f8fafc' }}><Text style={{ color: '#94a3b8', fontSize: 7 }}>START</Text><Text style={{ color: '#334155', fontSize: 10, marginTop: 4 }}>{trip.travelStartDate}</Text></View><View style={{ flex: 1, padding: 11, backgroundColor: '#f8fafc' }}><Text style={{ color: '#94a3b8', fontSize: 7 }}>END</Text><Text style={{ color: '#334155', fontSize: 10, marginTop: 4 }}>{trip.travelEndDate}</Text></View></View>{trip.days.map((day) => <View key={day.dayNumber} wrap={false} style={{ marginTop: 12 }}><View style={{ flexDirection: 'row', alignItems: 'center', paddingBottom: 7, borderBottomWidth: 2, borderBottomColor: theme.accent }}><Text style={{ color: theme.accent, fontSize: 14 }}>DAY {String(day.dayNumber).padStart(2, '0')}</Text><Text style={{ marginLeft: 'auto', color: '#64748b', fontSize: 9 }}>{day.planDate}</Text></View><PdfScheduleRows day={day} accent={theme.accent} mode="table" /></View>)}<PdfPacking trip={trip} accent={theme.accent} /><PdfFooter accent={theme.accent} /></Page></Document>;

  if (theme.id === 'sunset') return <Document><Page size="A4" style={{ ...commonPage, padding: 0, backgroundColor: '#fff7ed' }}><View style={{ height: 220, padding: 40, backgroundColor: theme.accent, justifyContent: 'space-between' }}><View style={{ flexDirection: 'row', justifyContent: 'space-between' }}><Text style={{ color: '#ffffff', fontSize: 17 }}>RouteMate</Text><Text style={{ color: '#ffe4cc', fontSize: 9 }}>VACATION EDITION</Text></View><View><Text style={{ color: '#fff7ed', fontSize: 34 }}>{trip.title}</Text><Text style={{ color: '#ffe4cc', fontSize: 11, marginTop: 12 }}>{trip.travelStartDate}  /  {trip.travelEndDate}</Text></View></View><View style={{ paddingHorizontal: 40, paddingBottom: 48 }}>{trip.description ? <Text style={{ color: '#7c3f21', fontSize: 10, lineHeight: 1.6, marginVertical: 18 }}>{trip.description}</Text> : null}{trip.days.map((day, index) => <View key={day.dayNumber} wrap={false} style={{ marginTop: 14, padding: 16, borderRadius: 18, backgroundColor: index % 2 === 0 ? '#f28b50' : '#e6a83c' }}><Text style={{ color: '#ffffff', fontSize: 20 }}>DAY {day.dayNumber}</Text><Text style={{ color: '#fff1df', fontSize: 9, marginTop: 3 }}>{day.planDate}</Text><PdfScheduleRows day={day} accent="#ffffff" mode="poster" /></View>)}<PdfPacking trip={trip} accent={theme.accent} /></View><PdfFooter accent={theme.accent} /></Page></Document>;

  return <Document><Page size="A4" style={commonPage}><View style={{ alignItems: 'center', marginBottom: 22 }}><Text style={{ color: theme.accent, fontSize: 17 }}>♡ RouteMate ♡</Text><Text style={{ color: '#26324a', fontSize: 27, marginTop: 18 }}>{trip.title}</Text><Text style={{ color: theme.accent, fontSize: 10, marginTop: 8, paddingHorizontal: 13, paddingVertical: 6, borderRadius: 12, backgroundColor: '#ffe8ee' }}>{trip.travelStartDate} ~ {trip.travelEndDate}</Text>{trip.description ? <Text style={{ color: '#64748b', fontSize: 9, marginTop: 10, textAlign: 'center', lineHeight: 1.5 }}>{trip.description}</Text> : null}</View>{trip.days.map((day) => <View key={day.dayNumber} wrap={false} style={{ marginTop: 13, padding: 15, borderRadius: 16, backgroundColor: theme.card, borderWidth: 1, borderColor: '#f5c9d5' }}><View style={{ flexDirection: 'row', alignItems: 'center' }}><Text style={{ width: 38, height: 38, paddingTop: 11, borderRadius: 19, textAlign: 'center', color: '#ffffff', backgroundColor: theme.accent, fontSize: 9 }}>DAY{day.dayNumber}</Text><Text style={{ color: '#7a4052', fontSize: 12, marginLeft: 10 }}>{day.planDate}</Text></View><PdfScheduleRows day={day} accent={theme.accent} mode="cards" /></View>)}<PdfPacking trip={trip} accent={theme.accent} /><PdfFooter accent={theme.accent} /></Page></Document>;
};

export const TripDetail = ({ publicView = false }: TripDetailProps) => {
  const { planId } = useParams();
  const { user, loading: authLoading } = useAuth();
  const [trip, setTrip] = useState<TripDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const pdfRef = useRef<HTMLElement | null>(null);
  const [pdfThemeOpen, setPdfThemeOpen] = useState(false);
  const loadedPublicPlanRef = useRef<string | null>(null);

  useEffect(() => {
    if (!planId) {
      setLoading(false);
      setError('여행 일정을 찾을 수 없어요.');
      return;
    }
    if (!publicView && authLoading) return;
    if (!publicView && !user) {
      setLoading(false);
      setError('로그인 후 여행 일정을 확인할 수 있어요.');
      return;
    }
    if (publicView && loadedPublicPlanRef.current === planId) return;
    if (publicView) loadedPublicPlanRef.current = planId;

    const loadTrip = async () => {
      setLoading(true);
      setError(null);
      try {
        const endpoint = publicView
          ? `/api/public/travel-plans/${planId}`
          : `/api/my-travel-plans/${planId}`;
        const response = await axios.get<TripDetail>(endpoint);
        setTrip(response.data);
      } catch (loadError) {
        console.error('Failed to load trip detail', loadError);
        setError('여행 일정을 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    void loadTrip();
  }, [authLoading, planId, publicView, user]);

  if (loading) {
    return <main className="mx-auto flex w-full max-w-6xl flex-grow items-center justify-center px-6 py-24 text-slate-400">여행 일정을 불러오고 있어요.</main>;
  }

  if (error || !trip) {
    return (
      <main className="mx-auto flex w-full max-w-6xl flex-grow items-center justify-center px-6 py-24">
        <div className="theme-glass-card max-w-md text-center">
          <i className="fa-solid fa-triangle-exclamation mb-4 text-2xl text-rose-300" />
          <p className="text-slate-300">{error || '여행 일정을 찾을 수 없어요.'}</p>
          <Link to={publicView ? '/' : '/my-trips'} className="theme-btn-primary mt-6 px-5 py-3">
            {publicView ? '메인으로 돌아가기' : '내 여행으로 돌아가기'}
          </Link>
        </div>
      </main>
    );
  }

  const regionCount = trip.days.reduce((total, day) => total + day.regions.length, 0);

  return (
    <main className="relative z-10 w-full flex-grow">
      <div className="pointer-events-none absolute -left-36 top-0 h-[460px] w-[460px] rounded-full bg-indigo-500/10 blur-[140px]" />
      <div className="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6 md:py-12">
        <div className="print:hidden flex items-center justify-between gap-3">
          <Link to={publicView ? '/' : '/my-trips'} className="inline-flex items-center gap-2 text-sm font-semibold text-slate-400 transition hover:text-white"><i className="fa-solid fa-arrow-left" />{publicView ? '메인' : '내 여행'}</Link>
          {!publicView ? <Link to={`/my-trips/${planId}/edit`} className="theme-btn-primary inline-flex items-center gap-2 px-4 py-2.5 text-sm"><i className="fa-solid fa-pen-to-square" />수정하기</Link> : null}
        </div>
        <section ref={pdfRef} className="mt-5 overflow-hidden rounded-[30px] border border-white/10 bg-slate-950/55 shadow-[0_28px_90px_rgba(0,0,0,0.25)]">
          <div className="flex items-center gap-2 border-b border-white/10 bg-slate-950/80 px-5 py-4 text-indigo-200"><span className="flex h-8 w-8 items-center justify-center rounded-xl bg-indigo-500 text-sm font-black text-white">R</span><span className="text-lg font-extrabold tracking-tight text-white">RouteMate</span></div>
          <div className="relative h-56 md:h-72">
            {trip.imageUrl ? <img src={trip.imageUrl} alt={trip.title} className="h-full w-full object-cover" /> : <div className="h-full bg-gradient-to-br from-indigo-950 to-slate-950" />}
            <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/35 to-transparent" />
            <div className="absolute bottom-0 left-0 right-0 p-5 md:p-8">
              <div className="mb-3 flex flex-wrap gap-2"><span className={`rounded-full px-3 py-1 text-xs font-bold ${trip.isPublic === 'Y' ? 'bg-emerald-500/20 text-emerald-100' : 'bg-white/15 text-slate-100'}`}>{trip.isPublic === 'Y' ? '공개 여행' : '비공개 여행'}</span>{trip.travelStartDate && trip.travelEndDate ? <span className="rounded-full bg-black/30 px-3 py-1 text-xs font-bold text-white">{trip.travelStartDate.replaceAll('-', '.')} - {trip.travelEndDate.replaceAll('-', '.')}</span> : null}</div>
              <h1 className="text-3xl font-extrabold tracking-tight text-white md:text-5xl">{trip.title}</h1>
              {trip.description ? <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-200 md:text-base">{trip.description}</p> : null}
            </div>
          </div>
          <div className="grid gap-4 border-b border-white/10 p-5 sm:grid-cols-2 lg:grid-cols-4 md:p-6"><div><p className="text-xs text-slate-500">여행 기간</p><p className="mt-1 font-bold text-white">{formatTravelDuration(trip.travelStartDate, trip.travelEndDate)}</p></div><div><p className="text-xs text-slate-500">방문 지역</p><p className="mt-1 font-bold text-white">{regionCount}곳</p></div><div><p className="text-xs text-slate-500">작성자</p><p className="mt-1 font-bold text-white">{trip.userNicknm}</p></div><div><p className="text-xs text-slate-500">조회수</p><p className="mt-1 font-bold text-white">{trip.viewCount?.toLocaleString() ?? 0}회</p></div></div>
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
          <div className="print:hidden flex justify-end border-t border-white/10 p-5 md:p-6"><button type="button" onClick={() => setPdfThemeOpen(true)} className="theme-btn-primary inline-flex items-center gap-2 px-4 py-2.5 text-sm"><i className="fa-solid fa-file-pdf" />PDF 저장</button></div>
        </section>
        {pdfThemeOpen ? (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/85 p-3 backdrop-blur-md sm:p-6">
            <div className="max-h-[92vh] w-full max-w-5xl overflow-y-auto rounded-[30px] border border-white/10 bg-slate-900/95 p-5 shadow-[0_36px_120px_rgba(0,0,0,.55)] sm:p-7">
              <div className="flex items-start justify-between gap-5 border-b border-white/10 pb-5">
                <div><span className="inline-flex items-center gap-2 rounded-full border border-indigo-400/20 bg-indigo-500/10 px-3 py-1 text-[11px] font-bold tracking-[.18em] text-indigo-200"><i className="fa-solid fa-wand-magic-sparkles" />PDF STYLE STUDIO</span><h2 className="mt-3 text-2xl font-extrabold text-white">여행의 분위기를 골라보세요</h2><p className="mt-1 text-sm text-slate-400">폰트와 레이아웃이 모두 다른 5가지 일정표를 준비했어요.</p></div>
                <button type="button" onClick={() => setPdfThemeOpen(false)} className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full border border-white/10 bg-white/5 text-slate-400 transition hover:bg-white/10 hover:text-white" aria-label="닫기"><i className="fa-solid fa-xmark" /></button>
              </div>
              <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {pdfThemes.map((theme) => {
                  const presentation = pdfThemePresentation[theme.id];
                  return <PDFDownloadLink key={theme.id} document={<TripPdfDocument trip={trip} theme={theme} />} fileName={`${trip.title || 'travel-plan'}-${theme.id}.pdf`} className="group block overflow-hidden rounded-[22px] border border-white/10 bg-white/[.035] transition duration-300 hover:-translate-y-1 hover:border-white/25 hover:bg-white/[.06] hover:shadow-2xl">
                    {({ loading }) => <span className="block">
                      <span className="relative block h-52 overflow-hidden p-4" style={{ backgroundColor: theme.background, backgroundImage: presentation.pattern, backgroundSize: theme.id === 'lovely' ? '24px 24px' : undefined }}>
                        <span className="absolute right-3 top-3 flex h-8 w-8 items-center justify-center rounded-full text-white shadow-lg" style={{ backgroundColor: theme.accent }}><i className={`fa-solid ${presentation.icon} text-xs`} /></span>
                        <span className={`mx-auto block h-full max-w-[150px] bg-white p-3 shadow-xl ${theme.layout === 'poster' ? 'rounded-[18px]' : theme.layout === 'minimal' ? 'rounded-sm' : 'rounded-xl'}`}>
                          <span className="flex items-center gap-1.5 border-b pb-2" style={{ borderColor: theme.accent }}><span className="h-4 w-4 rounded-full" style={{ backgroundColor: theme.accent }} /><span className="text-[8px] font-black" style={{ color: theme.accent }}>RouteMate</span></span>
                          <span className="mt-3 block h-2.5 w-4/5 rounded-full bg-slate-700" /><span className="mt-1.5 block h-1.5 w-1/2 rounded-full" style={{ backgroundColor: `${theme.accent}99` }} />
                          <span className={`mt-4 block p-2 ${theme.layout === 'timeline' ? 'border-l-4' : theme.layout === 'notebook' ? 'border-b' : theme.layout === 'poster' ? 'rounded-xl' : 'rounded-lg'}`} style={{ backgroundColor: theme.card, borderColor: theme.accent }}><span className="block h-1.5 w-2/5 rounded-full" style={{ backgroundColor: theme.accent }} /><span className="mt-2 block h-1 w-full rounded-full bg-slate-300" /><span className="mt-1.5 block h-1 w-3/4 rounded-full bg-slate-200" /></span>
                          <span className="mt-2 block h-1 w-11/12 rounded-full bg-slate-200" /><span className="mt-1.5 block h-1 w-2/3 rounded-full bg-slate-200" />
                        </span>
                      </span>
                      <span className="block p-4"><span className="text-[10px] font-bold tracking-[.14em]" style={{ color: theme.accent }}>{presentation.badge}</span><span className="mt-1 flex items-center justify-between gap-3"><span><span className="block font-bold text-white">{theme.name}</span><span className="mt-1 block text-xs leading-5 text-slate-400">{theme.description}</span></span><span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-white/10 text-slate-300 transition group-hover:border-transparent group-hover:text-white" style={{ backgroundColor: loading ? '#334155' : theme.accent }}><i className={`fa-solid ${loading ? 'fa-spinner fa-spin' : 'fa-download'} text-xs`} /></span></span></span>
                    </span>}
                  </PDFDownloadLink>;
                })}
              </div>
              <p className="mt-5 text-center text-xs text-slate-500"><i className="fa-regular fa-lightbulb mr-1.5 text-amber-300" />카드를 선택하면 해당 테마로 PDF가 바로 다운로드됩니다.</p>
            </div>
          </div>
        ) : null}
      </div>
    </main>
  );
};
