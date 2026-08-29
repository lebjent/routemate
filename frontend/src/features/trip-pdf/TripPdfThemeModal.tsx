import { Document, Font, Page, PDFDownloadLink, StyleSheet, Text, View } from '@react-pdf/renderer';

Font.register({ family: 'NotoSansKR', src: '/fonts/NotoSansKR-Regular.ttf' });
Font.register({ family: 'NotoSansKR', src: '/fonts/NotoSansKR-Regular.ttf', fontWeight: 700 });
Font.register({ family: 'GowunDodum', src: '/fonts/GowunDodum-Regular.ttf' });
Font.register({ family: 'Jua', src: '/fonts/Jua-Regular.ttf' });
Font.register({ family: 'Gaegu', src: '/fonts/Gaegu-Regular.ttf' });
Font.register({ family: 'DoHyeon', src: '/fonts/DoHyeon-Regular.ttf' });

/** PDF 문서에 필요한 여행 일정의 읽기 전용 데이터 계약이다. */
export type TripPdfData = {
  title: string;
  description: string | null;
  travelStartDate: string | null;
  travelEndDate: string | null;
  days: Array<{
    dayNumber: number;
    planDate: string;
    regions: Array<{
      countryName: string;
      regionName: string;
      schedules: Array<{ time: string | null; title: string | null; location: string | null; memo: string | null }>;
    }>;
  }>;
  packingItems: Array<{ item: string; required: boolean }>;
};

type PdfTheme = {
  id: string;
  name: string;
  description: string;
  accent: string;
  background: string;
  card: string;
  fontFamily: string;
};

const themes: PdfTheme[] = [
  { id: 'lovely', name: '러블리 핑크', description: '따뜻하고 귀여운 여행 다이어리', accent: '#d45b7c', background: '#fffdfb', card: '#fff4f5', fontFamily: 'GowunDodum' },
  { id: 'mint', name: '민트 트래블', description: '산뜻한 바다 여행 느낌', accent: '#0f9f9a', background: '#f7fffd', card: '#e8fbf7', fontFamily: 'Jua' },
  { id: 'diary', name: '감성 다이어리', description: '손글씨 감성의 기록장', accent: '#9a6748', background: '#fffaf0', card: '#fff1d6', fontFamily: 'Gaegu' },
  { id: 'lavender', name: '모던 라벤더', description: '깔끔하고 세련된 일정표', accent: '#4f46e5', background: '#ffffff', card: '#f5f7ff', fontFamily: 'NotoSansKR' },
  { id: 'sunset', name: '선셋 바캉스', description: '활기찬 휴양지 포스터 스타일', accent: '#e76f2f', background: '#fffaf5', card: '#fff0df', fontFamily: 'DoHyeon' },
];

const styles = StyleSheet.create({
  page: { padding: 42, fontSize: 10, color: '#334155' },
  header: { borderBottomWidth: 2, paddingBottom: 14, marginBottom: 20 },
  day: { marginTop: 12, padding: 14, borderRadius: 12, borderWidth: 1 },
  schedule: { flexDirection: 'row', marginTop: 5 },
  footer: { position: 'absolute', bottom: 20, left: 42, right: 42, borderTopWidth: 1, paddingTop: 7, flexDirection: 'row', justifyContent: 'space-between' },
});

/** 선택 테마와 여행 일정으로 실제 PDF 문서를 만든다. */
const TripPdfDocument = ({ trip, theme }: { trip: TripPdfData; theme: PdfTheme }) => (
  <Document>
    <Page size="A4" style={{ ...styles.page, paddingBottom: 48, fontFamily: theme.fontFamily, backgroundColor: theme.background }}>
      <View style={{ ...styles.header, borderBottomColor: theme.accent }}>
        <Text style={{ color: theme.accent, fontSize: 17 }}>RouteMate · MY TRAVEL PLAN</Text>
        <Text style={{ color: '#172033', fontSize: 26, marginTop: 16 }}>{trip.title}</Text>
        <Text style={{ color: theme.accent, marginTop: 7 }}>{trip.travelStartDate} ~ {trip.travelEndDate}</Text>
        {trip.description ? <Text style={{ marginTop: 12, lineHeight: 1.55 }}>{trip.description}</Text> : null}
      </View>
      {trip.days.map((day) => (
        <View key={day.dayNumber} wrap={false} style={{ ...styles.day, backgroundColor: theme.card, borderColor: `${theme.accent}55` }}>
          <Text style={{ color: theme.accent, fontSize: 15 }}>DAY {String(day.dayNumber).padStart(2, '0')} · {day.planDate}</Text>
          {day.regions.map((region) => (
            <View key={`${day.dayNumber}-${region.regionName}`} style={{ marginTop: 8 }}>
              <Text style={{ color: '#475569', fontSize: 11 }}>{region.countryName} · {region.regionName}</Text>
              {region.schedules.length === 0 ? <Text style={{ color: '#94a3b8', marginTop: 4 }}>등록된 일정이 없습니다.</Text> : region.schedules.map((schedule, index) => (
                <View key={`${region.regionName}-${index}`} style={styles.schedule}>
                  <Text style={{ width: 54, color: theme.accent }}>{schedule.time || '--:--'}</Text>
                  <Text style={{ flex: 1 }}>{schedule.title || schedule.location || '일정'}{schedule.location ? ` · ${schedule.location}` : ''}{schedule.memo ? ` — ${schedule.memo}` : ''}</Text>
                </View>
              ))}
            </View>
          ))}
        </View>
      ))}
      {trip.packingItems.length > 0 ? <View style={{ marginTop: 18, padding: 12, borderWidth: 1, borderColor: `${theme.accent}55` }}><Text style={{ color: theme.accent, fontSize: 11 }}>PACKING LIST</Text><Text style={{ marginTop: 7, lineHeight: 1.6 }}>{trip.packingItems.map((item) => `✓ ${item.item}`).join('\n')}</Text></View> : null}
      <View fixed style={{ ...styles.footer, borderTopColor: `${theme.accent}55` }}><Text style={{ color: theme.accent }}>RouteMate</Text><Text render={({ pageNumber, totalPages }) => `${pageNumber} / ${totalPages}`} /></View>
    </Page>
  </Document>
);

/**
 * PDF 테마를 선택하고 다운로드 링크를 제공하는 지연 로딩 모달이다.
 *
 * 이 파일 자체가 동적 import되므로 PDF 렌더러는 사용자가 PDF 저장을 요청하기 전까지 내려받지 않는다.
 */
export const TripPdfThemeModal = ({ trip, onClose }: { trip: TripPdfData; onClose: () => void }) => (
  <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/85 p-3 backdrop-blur-md sm:p-6">
    <div className="max-h-[92vh] w-full max-w-5xl overflow-y-auto rounded-[30px] border border-white/10 bg-slate-900/95 p-5 shadow-[0_36px_120px_rgba(0,0,0,.55)] sm:p-7">
      <div className="flex items-start justify-between gap-5 border-b border-white/10 pb-5"><div><span className="text-[11px] font-bold tracking-[.18em] text-indigo-200">PDF STYLE STUDIO</span><h2 className="mt-3 text-2xl font-extrabold text-white">여행의 분위기를 골라보세요</h2><p className="mt-1 text-sm text-slate-400">테마를 선택하면 PDF를 바로 다운로드합니다.</p></div><button type="button" onClick={onClose} className="flex h-10 w-10 items-center justify-center rounded-full border border-white/10 text-slate-400 hover:text-white" aria-label="닫기"><i className="fa-solid fa-xmark" /></button></div>
      <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">{themes.map((theme) => <PDFDownloadLink key={theme.id} document={<TripPdfDocument trip={trip} theme={theme} />} fileName={`${trip.title || 'travel-plan'}-${theme.id}.pdf`} className="group rounded-[22px] border border-white/10 bg-white/[.035] p-5 transition hover:-translate-y-1 hover:border-white/25"><span className="block h-24 rounded-2xl" style={{ background: `linear-gradient(135deg, ${theme.accent}, ${theme.card})` }} /><span className="mt-4 flex items-center justify-between"><span><strong className="block text-white">{theme.name}</strong><small className="mt-1 block text-slate-400">{theme.description}</small></span><i className="fa-solid fa-download text-white" /></span></PDFDownloadLink>)}</div>
    </div>
  </div>
);
