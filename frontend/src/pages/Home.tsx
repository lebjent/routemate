import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';

interface TravelPlan {
  title: string;
  description: string;
  imageUrl: string;
  userNicknm: string;
  spotCount: number;
  likeCount: number;
}

interface Destination {
  destName: string;
  destDesc: string;
  imageUrl: string;
  country: string;
  city: string;
  category: string;
  likeCount: number;
}

export const Home: React.FC = () => {
  const [plans, setPlans] = useState<TravelPlan[]>([]);
  const [destinations, setDestinations] = useState<Destination[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axios.get('/api/home/data')
      .then(res => {
        setPlans(res.data.plans || []);
        setDestinations(res.data.destinations || []);
      })
      .catch(err => {
        console.error("Failed to load home page data", err);
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  return (
    <main className="max-w-7xl mx-auto px-6 py-16 text-center flex-grow flex flex-col justify-center items-center relative z-10">
      <div className="absolute top-[-10%] left-[-10%] w-[600px] h-[600px] bg-indigo-600/10 rounded-full blur-[120px] pointer-events-none"></div>
      <div className="absolute top-[40%] right-[-10%] w-[500px] h-[500px] bg-purple-600/10 rounded-full blur-[120px] pointer-events-none"></div>

      <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium bg-gradient-to-r from-indigo-500/10 to-purple-500/10 text-indigo-300 border border-indigo-500/20 mb-6 animate-pulse">
        <i className="fa-solid fa-earth-americas"></i> 전 세계 도심 루트 최적화 엔진 탑재
      </span>

      <h1 className="text-5xl md:text-6xl font-extrabold tracking-tight mb-6 leading-[1.15] text-white">
        지구 반대편 여정까지,<br />
        <span className="theme-gradient-text">가장 완벽한 동선</span>을 잇다
      </h1>

      <p className="text-base md:text-lg text-gray-400 max-w-2xl mb-10 leading-relaxed font-light">
        복잡한 도심 속 스팟부터 국경을 넘나드는 장거리 복합 경로까지.<br />
        동행과의 실시간 일정 조율, 스마트 환율 정산까지 RouteMate 하나로 끝내세요.
      </p>

      <div className="flex flex-col sm:flex-row gap-4 justify-center items-center w-full max-w-md mb-24">
        <Link to="/join" className="w-full sm:w-auto text-base theme-btn-primary px-8 py-4 group">
          플래너 생성하기
          <i className="fa-solid fa-arrow-right transition-transform group-hover:translate-x-1 ml-1"></i>
        </Link>
        <Link to="/login" className="w-full sm:w-auto text-base bg-white/[0.04] hover:bg-white/[0.08] text-gray-200 hover:text-white border border-white/[0.08] px-8 py-4 rounded-2xl font-semibold transition duration-300 flex items-center justify-center gap-2">
          내 일정 불러오기 (로그인)
        </Link>
      </div>

      {loading ? (
        <div className="text-gray-400 py-12">
          <i className="fa-solid fa-spinner fa-spin text-3xl mb-2 text-indigo-500"></i>
          <p>여정을 불러오고 있습니다...</p>
        </div>
      ) : (
        <>
          {/* 1. 인기 글로벌 루트 */}
          <div className="w-full text-left mb-6">
            <h2 className="text-2xl font-bold text-white mb-2 flex items-center gap-2">
              <i className="fa-solid fa-compass text-purple-400"></i> 지금 이 순간 가장 인기 있는 글로벌 루트
            </h2>
            <p className="text-sm text-gray-400 font-light">다른 여행자들이 최적화한 무결한 동선을 확인하고 내 플래너로 가져오세요.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 w-full text-left mb-16">
            {plans.map((plan, i) => (
              <div key={i} className="theme-glass-card !p-0 overflow-hidden flex flex-col group cursor-pointer">
                <div className="relative h-56 w-full overflow-hidden bg-gray-900">
                  {plan.imageUrl ? (
                    <img src={plan.imageUrl} alt={plan.title} className="w-full h-full object-cover transition duration-500 group-hover:scale-105" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-gray-600 bg-gray-800"><i className="fa-solid fa-image text-3xl"></i></div>
                  )}
                  <span className="absolute top-4 left-4 bg-black/60 backdrop-blur-md text-xs text-white px-3 py-1.5 rounded-full font-medium">
                    <i className="fa-solid fa-user text-indigo-300 mr-1"></i> <span>{plan.userNicknm || '작성자'}</span>
                  </span>
                </div>
                <div className="p-6 flex-grow flex flex-col justify-between">
                  <div>
                    <h3 className="text-lg font-bold text-white mb-2 group-hover:text-indigo-400 transition">{plan.title}</h3>
                    <p className="text-sm text-gray-400 font-light leading-relaxed mb-4">{plan.description}</p>
                  </div>
                  <div className="flex justify-between items-center text-xs text-gray-500 border-t border-gray-800/60 pt-4">
                    <span className="flex items-center gap-1"><i className="fa-solid fa-route"></i> <span>{plan.spotCount}개 스팟</span></span>
                    <span className="flex items-center gap-1"><i className="fa-solid fa-heart text-pink-500"></i> <span>{plan.likeCount?.toLocaleString()}</span></span>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* 2. 추천 명소 */}
          <div className="w-full text-left mb-6">
            <h2 className="text-2xl font-bold text-white mb-2 flex items-center gap-2">
              <i className="fa-solid fa-map-pin text-indigo-400"></i> RouteMate 추천 명소
            </h2>
            <p className="text-sm text-gray-400 font-light">지금 바로 떠나기 좋은 세계 각지의 인기 명소 정보입니다.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 w-full text-left">
            {destinations.map((dest, i) => (
              <div key={i} className="theme-glass-card !p-0 overflow-hidden flex flex-col group cursor-pointer">
                <div className="relative h-56 w-full overflow-hidden bg-gray-900">
                  {dest.imageUrl ? (
                    <img src={dest.imageUrl} alt={dest.destName} className="w-full h-full object-cover transition duration-500 group-hover:scale-105" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-gray-600 bg-gray-800"><i className="fa-solid fa-image text-3xl"></i></div>
                  )}
                  <span className="absolute top-4 left-4 bg-black/60 backdrop-blur-md text-xs text-white px-3 py-1.5 rounded-full font-medium">
                    <i className="fa-solid fa-location-dot text-red-400 mr-1"></i> <span>{dest.country} {dest.city}</span>
                  </span>
                </div>
                <div className="p-6 flex-grow flex flex-col justify-between">
                  <div>
                    <h3 className="text-lg font-bold text-white mb-2 group-hover:text-indigo-400 transition">{dest.destName}</h3>
                    <p className="text-sm text-gray-400 font-light leading-relaxed mb-4">{dest.destDesc}</p>
                  </div>
                  <div className="flex justify-between items-center text-xs text-gray-500 border-t border-gray-800/60 pt-4">
                    <span className="flex items-center gap-1"><i className="fa-solid fa-tags"></i> <span>{dest.category}</span></span>
                    <span className="flex items-center gap-1"><i className="fa-solid fa-heart text-pink-500"></i> <span>{dest.likeCount?.toLocaleString()}</span></span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </main>
  );
};
