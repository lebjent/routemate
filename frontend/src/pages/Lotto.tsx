import { useEffect, useRef, useState } from 'react';
import axios from 'axios';

const ballThemes = [
  { label: '01 - 10', className: 'from-amber-300 via-amber-400 to-orange-500 text-amber-950' },
  { label: '11 - 20', className: 'from-sky-300 via-sky-400 to-blue-600 text-sky-950' },
  { label: '21 - 30', className: 'from-rose-300 via-rose-400 to-red-600 text-rose-950' },
  { label: '31 - 40', className: 'from-slate-200 via-slate-400 to-slate-600 text-slate-950' },
  { label: '41 - 45', className: 'from-lime-300 via-lime-400 to-emerald-600 text-emerald-950' },
] as const;

type DrawMode = 'RANDOM' | 'FREQUENT';

type NumberFrequency = {
  number: number;
  count: number;
};

type FrequencyDrawResponse = {
  numbers: number[];
  topNumbers: NumberFrequency[];
  analyzedDrawCount: number;
  latestDrawNumber: number;
  refreshedAt: string;
};

const getBallTheme = (number: number) => ballThemes[Math.min(Math.floor((number - 1) / 10), ballThemes.length - 1)];

const formatDrawTime = (value: Date | null) => {
  if (!value) return '번호를 준비하고 있어요';
  return value.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
};

export const Lotto = () => {
  const [numbers, setNumbers] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [drawCount, setDrawCount] = useState(0);
  const [drawnAt, setDrawnAt] = useState<Date | null>(null);
  const [drawMode, setDrawMode] = useState<DrawMode>('RANDOM');
  const [frequencyData, setFrequencyData] = useState<FrequencyDrawResponse | null>(null);
  const requestId = useRef(0);

  const drawNumbers = async (nextMode: DrawMode = drawMode) => {
    const currentRequestId = requestId.current + 1;
    requestId.current = currentRequestId;
    setLoading(true);
    setError(null);

    try {
      const response = nextMode === 'FREQUENT'
        ? await axios.get<FrequencyDrawResponse>('/api/lotto/frequent-numbers')
        : await axios.get<number[]>('/api/lotto/numbers');
      const lottoNumbers = nextMode === 'FREQUENT'
        ? (response.data as FrequencyDrawResponse).numbers
        : response.data as number[];
      const hasValidNumbers = Array.isArray(lottoNumbers)
        && lottoNumbers.length === 6
        && new Set(lottoNumbers).size === 6
        && lottoNumbers.every((number) => Number.isInteger(number) && number >= 1 && number <= 45);

      if (!hasValidNumbers) {
        throw new Error('Invalid lotto number response');
      }
      if (currentRequestId !== requestId.current) return;

      setNumbers([...lottoNumbers].sort((first, second) => first - second));
      setDrawMode(nextMode);
      setFrequencyData(nextMode === 'FREQUENT' ? response.data as FrequencyDrawResponse : null);
      setDrawnAt(new Date());
      setDrawCount((count) => count + 1);
    } catch (drawError) {
      if (currentRequestId !== requestId.current) return;
      console.error('Failed to fetch lotto numbers', drawError);
      setNumbers([]);
      setError('번호를 불러오지 못했습니다. 네트워크를 확인한 뒤 다시 시도해 주세요.');
    } finally {
      if (currentRequestId === requestId.current) setLoading(false);
    }
  };

  useEffect(() => {
    void drawNumbers();
  }, []);

  const selectDrawMode = (nextMode: DrawMode) => {
    if (loading || nextMode === drawMode) return;
    void drawNumbers(nextMode);
  };

  const drawDescription = loading
    ? '새 조합을 섞고 있어요'
    : drawMode === 'FREQUENT' && frequencyData
      ? `역대 ${frequencyData.analyzedDrawCount.toLocaleString()}회 당첨번호 기준 · ${formatDrawTime(drawnAt)} 생성`
      : `${formatDrawTime(drawnAt)}에 생성된 무작위 조합`;

  return (
    <main className="relative z-10 flex w-full flex-grow items-center overflow-hidden px-4 py-8 sm:px-6 md:py-12">
      <div className="pointer-events-none absolute -left-32 top-8 h-[420px] w-[420px] rounded-full bg-amber-400/10 blur-[130px]" />
      <div className="pointer-events-none absolute -bottom-28 -right-20 h-[480px] w-[480px] rounded-full bg-indigo-500/15 blur-[150px]" />

      <div className="relative mx-auto grid w-full max-w-6xl items-stretch gap-5 lg:grid-cols-[minmax(0,1.35fr)_minmax(300px,0.65fr)]">
        <section className="lotto-panel overflow-hidden p-5 sm:p-8 md:p-10">
          <div className="flex flex-col justify-between gap-5 sm:flex-row sm:items-start">
            <div className="max-w-xl">
              <p className="text-xs font-extrabold tracking-[0.28em] text-amber-200">LUCKY DRAW</p>
              <h1 className="mt-3 text-3xl font-black tracking-tight text-white sm:text-4xl md:text-5xl">오늘의 행운 조합</h1>
              <p className="mt-3 text-sm leading-6 text-slate-400 sm:text-base">완전 무작위 조합과 역대 빈출 번호군 조합을 원하는 방식으로 가볍게 확인해 보세요.</p>
            </div>
            <div className="inline-flex w-fit items-center gap-2 rounded-full border border-white/10 bg-white/[0.045] px-3.5 py-2 text-xs font-semibold text-slate-300">
              <span className="relative flex h-2 w-2"><span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-300 opacity-75" /><span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-300" /></span>
              서버 난수 생성
            </div>
          </div>

          <div className="mt-7 grid gap-2 sm:grid-cols-2">
            <button type="button" onClick={() => selectDrawMode('RANDOM')} disabled={loading} className={`lotto-mode-button ${drawMode === 'RANDOM' ? 'lotto-mode-button-active' : ''}`}>
              <i className="fa-solid fa-shuffle" />
              <span><strong>일반 랜덤</strong><small>1부터 45까지 무작위</small></span>
            </button>
            <button type="button" onClick={() => selectDrawMode('FREQUENT')} disabled={loading} className={`lotto-mode-button ${drawMode === 'FREQUENT' ? 'lotto-mode-button-active' : ''}`}>
              <i className="fa-solid fa-chart-column" />
              <span><strong>역대 빈출 조합</strong><small>상위 15개 번호군에서 추첨</small></span>
            </button>
          </div>

          <div className="lotto-result-stage mt-8 rounded-[28px] p-5 sm:mt-10 sm:p-8">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <p className="text-xs font-bold tracking-[0.16em] text-indigo-200">{drawMode === 'FREQUENT' ? 'HISTORY FREQUENCY MIX' : 'YOUR NUMBERS'}</p>
                <p className="mt-1 text-sm text-slate-400">{drawDescription}</p>
              </div>
              <span className="rounded-full border border-white/10 bg-slate-950/45 px-3 py-1.5 text-xs font-semibold text-slate-300">{drawMode === 'FREQUENT' ? '빈출 상위 15개 · 6개' : '중복 없음 · 6개'}</span>
            </div>

            <div className="my-8 grid grid-cols-3 gap-3 sm:my-10 sm:grid-cols-6 sm:gap-4">
              {loading ? Array.from({ length: 6 }, (_, index) => (
                <div key={index} className="lotto-ball-skeleton aspect-square" aria-label="번호 생성 중" />
              )) : numbers.map((number, index) => {
                const theme = getBallTheme(number);
                return (
                  <div
                    key={`${drawCount}-${number}`}
                    className={`lotto-ball aspect-square bg-gradient-to-br ${theme.className}`}
                    style={{ animationDelay: `${index * 90}ms` }}
                    aria-label={`${number}번`}
                  >
                    <span className="relative z-10 text-2xl font-black tracking-tight sm:text-3xl">{String(number).padStart(2, '0')}</span>
                  </div>
                );
              })}
            </div>

            {error ? <p className="rounded-2xl border border-rose-400/20 bg-rose-500/10 px-4 py-3 text-center text-sm text-rose-100">{error}</p> : null}

            <div className="flex flex-col gap-3 border-t border-white/10 pt-5 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-xs leading-5 text-slate-500">{drawMode === 'FREQUENT' ? '과거 빈도는 미래 당첨 확률을 높이지 않습니다.' : '번호는 요청할 때마다 새로 생성되며, 별도로 저장되지 않습니다.'}</p>
              <button type="button" onClick={() => void drawNumbers()} disabled={loading} className="lotto-draw-button">
                <i className={`fa-solid ${loading ? 'fa-spinner fa-spin' : 'fa-shuffle'}`} />
                {loading ? '번호 생성 중' : drawMode === 'FREQUENT' ? '빈출 조합 다시 뽑기' : '새 조합 뽑기'}
              </button>
            </div>
          </div>
        </section>

        <aside className="flex flex-col gap-5">
          <section className="lotto-panel p-5 sm:p-6">
            <p className="text-xs font-extrabold tracking-[0.22em] text-indigo-200">NUMBER GUIDE</p>
            <h2 className="mt-2 text-xl font-bold text-white">번호 구간</h2>
            <p className="mt-2 text-sm leading-6 text-slate-400">색상은 번호 범위를 빠르게 구분하기 위한 표시입니다.</p>
            <div className="mt-5 space-y-2.5">
              {ballThemes.map((theme) => (
                <div key={theme.label} className="flex items-center justify-between rounded-2xl border border-white/8 bg-white/[0.025] px-3.5 py-3">
                  <span className={`h-7 w-7 rounded-full bg-gradient-to-br ${theme.className} shadow-lg`} />
                  <span className="ml-3 mr-auto text-sm font-semibold text-slate-200">{theme.label}</span>
                  <span className="text-xs text-slate-500">번호</span>
                </div>
              ))}
            </div>
          </section>

          <section className="relative overflow-hidden rounded-[28px] border border-indigo-400/20 bg-gradient-to-br from-indigo-500/15 via-slate-950/80 to-slate-950 p-5 sm:p-6">
            <div className="pointer-events-none absolute -right-10 -top-10 h-36 w-36 rounded-full bg-indigo-300/15 blur-3xl" />
            <div className="relative">
              <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-indigo-400/15 text-indigo-100"><i className="fa-solid fa-chart-simple" /></div>
              <h2 className="mt-4 text-lg font-bold text-white">역대 빈출 번호</h2>
              {frequencyData ? (
                <>
                  <p className="mt-2 text-sm leading-6 text-slate-400">최근 집계 회차는 {frequencyData.latestDrawNumber.toLocaleString()}회입니다.</p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    {frequencyData.topNumbers.slice(0, 10).map((item) => (
                      <span key={item.number} className="inline-flex items-center gap-1 rounded-full border border-white/10 bg-slate-950/45 px-2.5 py-1.5 text-xs font-semibold text-indigo-100">
                        {item.number}<small className="font-medium text-slate-500">{item.count}회</small>
                      </span>
                    ))}
                  </div>
                </>
              ) : <p className="mt-2 text-sm leading-6 text-slate-400">역대 빈출 조합을 선택하면 상위 번호와 출현 횟수를 확인할 수 있어요.</p>}
            </div>
          </section>
        </aside>
      </div>
    </main>
  );
};
