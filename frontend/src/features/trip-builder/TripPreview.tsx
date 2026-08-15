import { formatDate, formatDateWithYear, transportByType, type PreviewDay } from './model';

type TripPreviewProps = {
  isPublic: 'Y' | 'N';
  travelStartDate: string;
  travelEndDate: string;
  totalDays: number;
  title: string;
  summary: string;
  imageUrl: string;
  regionCount: number;
  scheduleCount: number;
  days: PreviewDay[];
  panelClassName: string;
};

export const TripPreview = ({
  isPublic,
  travelStartDate,
  travelEndDate,
  totalDays,
  title,
  summary,
  imageUrl,
  regionCount,
  scheduleCount,
  days,
  panelClassName,
}: TripPreviewProps) => (
  <aside className={`${panelClassName} overflow-hidden xl:sticky xl:top-6`}>
    <div className="border-b border-white/10 px-5 py-5 md:px-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-xs font-bold tracking-[0.22em] text-indigo-300">LIVE PREVIEW</p>
          <h2 className="mt-2 text-xl font-bold text-white">현재 여행 계획</h2>
        </div>
        <span className={`rounded-full px-3 py-1.5 text-xs font-semibold ${isPublic === 'Y' ? 'bg-emerald-500/15 text-emerald-200' : 'bg-white/10 text-slate-300'}`}>
          {isPublic === 'Y' ? '공개' : '비공개'}
        </span>
      </div>
    </div>
    <div className="relative h-48 overflow-hidden">
      <img src={imageUrl} alt={title} className="h-full w-full object-cover" />
      <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/25 to-transparent" />
      <div className="absolute bottom-4 left-5 right-5">
        <p className="text-xs font-semibold text-indigo-200">
          {travelStartDate && travelEndDate
            ? `${formatDateWithYear(travelStartDate)} - ${formatDateWithYear(travelEndDate)} · ${totalDays}일`
            : '여행 기간 미선택'}
        </p>
        <h3 className="mt-1 text-2xl font-extrabold leading-tight text-white">{title}</h3>
      </div>
    </div>
    <div className="space-y-5 p-5 md:p-6">
      <p className="text-sm leading-6 text-slate-300">{summary}</p>
      <div className="grid grid-cols-2 gap-3 rounded-2xl border border-white/10 bg-slate-950/30 p-4">
        <div><p className="text-xs text-slate-500">여행지</p><p className="mt-1 text-lg font-bold text-white">{regionCount}곳</p></div>
        <div><p className="text-xs text-slate-500">입력한 일정</p><p className="mt-1 text-lg font-bold text-white">{scheduleCount}개</p></div>
      </div>
      <div className="max-h-[min(54vh,720px)] space-y-3 overflow-y-auto pr-1">
        {days.length === 0 ? (
          <p className="rounded-2xl border border-dashed border-white/10 p-4 text-sm text-slate-500">여행 기간을 선택하면 일차별 미리보기가 표시됩니다.</p>
        ) : days.map((day) => (
          <section key={day.date} className="rounded-2xl border border-white/10 bg-white/[0.025] p-4">
            <div className="flex items-start justify-between gap-3">
              <div><p className="text-xs font-bold text-indigo-200">{day.day}일차</p><h4 className="mt-1 font-bold text-white">{formatDate(day.date)}</h4></div>
              <span className="text-xs text-slate-500">{day.regions.reduce((sum, region) => sum + region.schedules.length, 0)}개 일정</span>
            </div>
            {day.regions.length === 0 ? (
              <p className="mt-3 text-sm text-slate-500">아직 여행지를 추가하지 않았어요.</p>
            ) : (
              <div className="mt-3 space-y-3">
                {day.regions.map((region) => (
                  <div key={region.id}>
                    <p className="inline-flex items-center gap-1.5 rounded-full bg-cyan-400/10 px-2.5 py-1 text-xs font-semibold text-cyan-100"><i className="fa-solid fa-location-dot" />{region.label}</p>
                    {region.schedules.length > 0 ? (
                      <div className="mt-2 space-y-2">
                        {region.schedules.map((schedule) => {
                          const transport = schedule.transportType ? transportByType[schedule.transportType] : null;
                          return (
                            <div key={schedule.id} className="text-sm">
                              <div className="flex gap-2"><span className="w-11 shrink-0 font-semibold text-indigo-200">{schedule.time || '--:--'}</span><span className="min-w-0 text-slate-300">{schedule.title || schedule.location || schedule.memo || transport?.label}</span></div>
                              {transport ? <p className="ml-[52px] mt-1 text-xs text-slate-500"><i className={`fa-solid ${transport.icon} mr-1 text-indigo-300`} />{transport.label}{schedule.transportName ? ` · ${schedule.transportName}` : ''}{schedule.departureTime || schedule.arrivalTime ? ` · ${schedule.departureTime || '--:--'} → ${schedule.arrivalTime || '--:--'}` : ''}</p> : null}
                            </div>
                          );
                        })}
                      </div>
                    ) : null}
                  </div>
                ))}
              </div>
            )}
          </section>
        ))}
      </div>
    </div>
  </aside>
);
