import { useState } from 'react';
import type { FormEvent } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

type TimelineItem = {
  time: string;
  title: string;
  location: string;
  memo: string;
};

const createTimelineItem = (): TimelineItem => ({
  time: '09:00',
  title: '',
  location: '',
  memo: '',
});

export const CreateTrip = () => {
  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [timeline, setTimeline] = useState<TimelineItem[]>([createTimelineItem()]);
  const [isPublic, setIsPublic] = useState<'Y' | 'N'>('Y');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const updateTimelineItem = (index: number, field: keyof TimelineItem, value: string) => {
    setTimeline((current) =>
      current.map((item, itemIndex) => (itemIndex === index ? { ...item, [field]: value } : item))
    );
  };

  const addTimelineItem = () => {
    setTimeline((current) => [...current, createTimelineItem()]);
  };

  const removeTimelineItem = (index: number) => {
    setTimeline((current) => (current.length === 1 ? current : current.filter((_, itemIndex) => itemIndex !== index)));
  };

  const compiledTimeline = timeline
    .map((item, index) => {
      const pieces = [
        item.time.trim(),
        item.title.trim(),
        item.location.trim(),
        item.memo.trim(),
      ].filter(Boolean);

      if (pieces.length === 0) {
        return null;
      }

      return `${String(index + 1).padStart(2, '0')}. ${pieces.join(' | ')}`;
    })
    .filter((item): item is string => item !== null);

  const spotCount = timeline.filter((item) => item.title.trim().length > 0).length;

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!user) {
      setError('로그인 후에 일정 만들기를 사용할 수 있어요.');
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const mergedDescription = [
        description.trim(),
        compiledTimeline.length > 0 ? `\n[타임라인]\n${compiledTimeline.join('\n')}` : '',
      ]
        .filter(Boolean)
        .join('\n')
        .trim();

      const response = await axios.post('/api/my-travel-plans', {
        title,
        description: mergedDescription || null,
        imageUrl: imageUrl.trim() || null,
        spotCount,
        isPublic,
      });

      navigate('/my-trips', { replace: true, state: { createdPlanId: response.data.planId } });
    } catch (err) {
      console.error('Failed to create travel plan', err);
      setError('일정을 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.');
    } finally {
      setSubmitting(false);
    }
  };

  if (authLoading) {
    return (
      <main className="max-w-5xl mx-auto px-6 py-16 flex-grow w-full">
        <div className="theme-glass-card text-center py-20">
          <i className="fa-solid fa-spinner fa-spin text-3xl mb-3 text-indigo-500" />
          <p className="text-gray-400">로그인 정보를 확인하는 중입니다...</p>
        </div>
      </main>
    );
  }

  if (!user) {
    return (
      <main className="max-w-5xl mx-auto px-6 py-16 flex-grow w-full">
        <div className="theme-glass-card text-center py-20">
          <div className="mx-auto mb-5 w-16 h-16 rounded-full bg-indigo-500/10 flex items-center justify-center text-indigo-300">
            <i className="fa-solid fa-lock text-2xl" />
          </div>
          <h1 className="text-2xl font-bold text-white mb-3">로그인이 필요해요</h1>
          <p className="text-gray-400 mb-6">내 여행 일정을 만들려면 먼저 로그인해 주세요.</p>
          <Link to="/login" className="theme-btn-primary px-6 py-3 inline-flex">
            로그인하러 가기
          </Link>
        </div>
      </main>
    );
  }

  return (
    <main className="max-w-5xl mx-auto px-6 py-10 flex-grow w-full relative z-10">
      <div className="absolute top-[-10%] left-[-8%] w-[520px] h-[520px] bg-indigo-600/10 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-8%] w-[520px] h-[520px] bg-purple-600/10 rounded-full blur-[120px] pointer-events-none" />

      <section className="mb-8">
        <p className="text-sm tracking-[0.24em] text-indigo-300/90 font-semibold mb-3">CREATE TRIP</p>
        <h1 className="text-3xl md:text-4xl font-extrabold tracking-tight text-white">새 여행 일정 만들기</h1>
        <p className="text-gray-400 mt-4 max-w-2xl leading-relaxed">
          제목, 설명, 공개 여부와 함께 시간대별 타임라인을 입력하면 더 구체적인 일정으로 저장됩니다.
        </p>
      </section>

      <form onSubmit={handleSubmit} className="theme-glass-card max-w-3xl mx-auto p-6 md:p-8 space-y-6">
        <div>
          <label className="block text-sm font-semibold text-gray-200 mb-2" htmlFor="title">
            일정 제목
          </label>
          <input
            id="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="예: 파리 3박 4일 감성 루트"
            className="w-full bg-white/5 border border-white/10 rounded-2xl px-4 py-3.5 text-sm text-white placeholder:text-gray-500 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
            maxLength={150}
            required
          />
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-200 mb-2" htmlFor="description">
            일정 요약
          </label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="전체 일정의 분위기나 핵심 포인트를 짧게 적어주세요."
            className="w-full min-h-[140px] bg-white/5 border border-white/10 rounded-2xl px-4 py-3.5 text-sm text-white placeholder:text-gray-500 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
            maxLength={500}
          />
        </div>

        <div>
          <div className="flex items-end justify-between gap-4 mb-3">
            <div>
              <label className="block text-sm font-semibold text-gray-200 mb-2">타임라인</label>
              <p className="text-xs text-gray-500">시간, 장소, 메모를 채우면 일정 카드에 반영됩니다.</p>
            </div>
            <button
              type="button"
              onClick={addTimelineItem}
              className="text-sm font-semibold text-indigo-300 hover:text-indigo-200 transition inline-flex items-center gap-2"
            >
              <i className="fa-solid fa-plus" />
              항목 추가
            </button>
          </div>

          <div className="space-y-4">
            {timeline.map((item, index) => (
              <div key={`${index}-${item.time}`} className="rounded-[24px] border border-white/10 bg-white/5 p-4 md:p-5">
                <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
                  <div className="inline-flex items-center gap-2 text-sm font-semibold text-indigo-200">
                    <span className="flex h-8 w-8 items-center justify-center rounded-full bg-indigo-500/15 text-indigo-200">
                      {index + 1}
                    </span>
                    타임라인 {index + 1}
                  </div>
                  <button
                    type="button"
                    onClick={() => removeTimelineItem(index)}
                    className="text-sm text-gray-400 hover:text-red-300 transition inline-flex items-center gap-2"
                    disabled={timeline.length === 1}
                  >
                    <i className="fa-solid fa-trash" />
                    삭제
                  </button>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-[140px_1fr] gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-400 mb-2">시간</label>
                    <input
                      type="time"
                      value={item.time}
                      onChange={(e) => updateTimelineItem(index, 'time', e.target.value)}
                      className="w-full bg-black/30 border border-white/10 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-400 mb-2">일정 제목</label>
                    <input
                      value={item.title}
                      onChange={(e) => updateTimelineItem(index, 'title', e.target.value)}
                      placeholder="예: 점심 먹고 루브르 입장"
                      className="w-full bg-black/30 border border-white/10 rounded-2xl px-4 py-3 text-sm text-white placeholder:text-gray-500 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-400 mb-2">장소</label>
                    <input
                      value={item.location}
                      onChange={(e) => updateTimelineItem(index, 'location', e.target.value)}
                      placeholder="예: 루브르 박물관"
                      className="w-full bg-black/30 border border-white/10 rounded-2xl px-4 py-3 text-sm text-white placeholder:text-gray-500 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-400 mb-2">메모</label>
                    <input
                      value={item.memo}
                      onChange={(e) => updateTimelineItem(index, 'memo', e.target.value)}
                      placeholder="예: 미리 예약 필수"
                      className="w-full bg-black/30 border border-white/10 rounded-2xl px-4 py-3 text-sm text-white placeholder:text-gray-500 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div>
            <label className="block text-sm font-semibold text-gray-200 mb-2" htmlFor="imageUrl">
              대표 이미지 URL
            </label>
            <input
              id="imageUrl"
              value={imageUrl}
              onChange={(e) => setImageUrl(e.target.value)}
              placeholder="https://..."
              className="w-full bg-white/5 border border-white/10 rounded-2xl px-4 py-3.5 text-sm text-white placeholder:text-gray-500 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
            />
          </div>

          <div className="rounded-[24px] border border-white/10 bg-white/5 p-5">
            <p className="text-xs font-semibold text-gray-400 mb-2">자동 계산된 정보</p>
            <div className="flex items-center justify-between gap-4">
              <div>
                <p className="text-gray-300 text-sm">스팟 수</p>
                <p className="text-2xl font-bold text-white">{spotCount}</p>
              </div>
              <div>
                <p className="text-gray-300 text-sm">타임라인 항목</p>
                <p className="text-2xl font-bold text-white">{timeline.length}</p>
              </div>
            </div>
            <p className="mt-3 text-xs text-gray-500">일정 제목이 입력된 항목만 스팟 수에 포함됩니다.</p>
          </div>
        </div>

        {compiledTimeline.length > 0 ? (
          <div className="rounded-[24px] border border-indigo-500/20 bg-indigo-500/5 p-5">
            <p className="text-sm font-semibold text-indigo-200 mb-4">미리보기</p>
            <div className="space-y-3">
              {compiledTimeline.map((line, index) => (
                <div key={line} className="flex gap-3">
                  <div className="flex flex-col items-center">
                    <div className="h-3 w-3 rounded-full bg-indigo-300 mt-2" />
                    {index < compiledTimeline.length - 1 ? (
                      <div className="w-px flex-1 min-h-[24px] bg-indigo-400/30" />
                    ) : null}
                  </div>
                  <p className="text-sm text-gray-200 leading-relaxed pb-3">{line}</p>
                </div>
              ))}
            </div>
          </div>
        ) : null}

        <div>
          <label className="block text-sm font-semibold text-gray-200 mb-2">공개 여부</label>
          <div className="flex flex-wrap gap-3">
            <button
              type="button"
              onClick={() => setIsPublic('Y')}
              className={`px-4 py-3 rounded-2xl border text-sm font-semibold transition ${
                isPublic === 'Y'
                  ? 'bg-white text-slate-950 border-white'
                  : 'bg-white/5 text-gray-300 border-white/10 hover:bg-white/10'
              }`}
            >
              공개
            </button>
            <button
              type="button"
              onClick={() => setIsPublic('N')}
              className={`px-4 py-3 rounded-2xl border text-sm font-semibold transition ${
                isPublic === 'N'
                  ? 'bg-white text-slate-950 border-white'
                  : 'bg-white/5 text-gray-300 border-white/10 hover:bg-white/10'
              }`}
            >
              비공개
            </button>
          </div>
        </div>

        {error ? (
          <p className="text-sm text-red-300 bg-red-500/10 border border-red-500/20 rounded-2xl px-4 py-3">
            {error}
          </p>
        ) : null}

        <div className="flex flex-col sm:flex-row gap-3 pt-2">
          <button
            type="submit"
            disabled={submitting}
            className="theme-btn-primary px-6 py-3.5 font-semibold disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {submitting ? '저장 중...' : '일정 만들기'}
          </button>
          <Link
            to="/my-trips"
            className="px-6 py-3.5 rounded-2xl border border-white/10 bg-white/5 text-gray-200 font-semibold text-center hover:bg-white/10 transition"
          >
            취소
          </Link>
        </div>
      </form>
    </main>
  );
};
