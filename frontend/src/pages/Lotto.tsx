import { useEffect, useState } from 'react';
import axios from 'axios';

export const Lotto = () => {
  const [numbers, setNumbers] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchLottoNumbers = () => {
    setLoading(true);
    axios.get('/api/lotto/numbers')
      .then(res => {
        setNumbers(res.data || []);
      })
      .catch(err => {
        console.error("Failed to fetch lotto numbers", err);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchLottoNumbers();
  }, []);

  const getBallColorClass = (num: number) => {
    if (num <= 10) return 'bg-[#fbc400]'; // yellow
    if (num <= 20) return 'bg-[#69c8f2]'; // blue
    if (num <= 30) return 'bg-[#ff7272]'; // red
    if (num <= 40) return 'bg-[#aaaaaa]'; // gray
    return 'bg-[#b0d840]'; // green
  };

  return (
    <main className="flex-grow flex items-center justify-center px-6 py-12 relative z-10">
      <div className="absolute top-[20%] left-[10%] w-[300px] h-[300px] bg-indigo-600/10 rounded-full blur-[100px] pointer-events-none"></div>
      <div className="absolute bottom-[20%] right-[10%] w-[300px] h-[300px] bg-purple-600/10 rounded-full blur-[100px] pointer-events-none"></div>

      <div className="theme-glass-card w-full max-w-lg p-10 shadow-2xl relative border-white/[0.05] text-center">
        <h2 className="text-3xl font-extrabold text-white tracking-tight mb-2">🎰 RouteMate 행운의 번호 🎰</h2>
        <p className="text-sm text-gray-400 font-light mb-8">이번 주 대박을 기원합니다!</p>

        {loading ? (
          <div className="py-10 text-gray-400">
            <i className="fa-solid fa-spinner fa-spin text-3xl mb-2 text-indigo-500"></i>
            <p>행운의 번호를 생성 중입니다...</p>
          </div>
        ) : (
          <>
            <div className="flex justify-center gap-4 my-8 flex-wrap">
              {numbers.map((num, idx) => (
                <div
                  key={idx}
                  className={`w-14 h-14 rounded-full flex items-center justify-center font-bold text-white text-xl shadow-lg border border-white/10 ${getBallColorClass(num)}`}
                  style={{
                    textShadow: '1px 1px 2px rgba(0,0,0,0.4)',
                    boxShadow: 'inset -3px -3px 5px rgba(0,0,0,0.3), 2px 2px 5px rgba(0,0,0,0.2)'
                  }}
                >
                  {num}
                </div>
              ))}
            </div>

            <button
              onClick={fetchLottoNumbers}
              className="theme-btn-primary px-8 py-3.5 mt-4 text-sm font-semibold tracking-wide"
            >
              새로 뽑기
              <i className="fa-solid fa-rotate-right ml-1"></i>
            </button>
          </>
        )}
      </div>
    </main>
  );
};
