import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../contexts/AuthContext';

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

interface BannerItem {
  title: string;
  description: string;
  imageUrl: string;
  badge: string;
  period: string;
  href: string;
  cta: string;
}

export const Home: React.FC = () => {
  const { user } = useAuth();
  const isLoggedIn = Boolean(user);
  const [plans, setPlans] = useState<TravelPlan[]>([]);
  const [destinations, setDestinations] = useState<Destination[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentBanner, setCurrentBanner] = useState(0);

  const banners: BannerItem[] = [
    {
      title: '여름 시즌 여행 이벤트',
      description: '이번 달 인기 루트를 확인하고, 마음에 드는 일정은 바로 저장해보세요.',
      imageUrl: '/banners/routemateEvent1.png',
      badge: 'HOT EVENT',
      period: '7월 한정',
      href: '#popular-plans',
      cta: '인기 일정 보기',
    },
    {
      title: '새 여행지 추천 배너',
      description: '이번 주 가장 반응이 좋은 명소를 골라서, 여행 계획에 바로 담아보세요.',
      imageUrl: '/banners/routemateEvent2.png',
      badge: 'NEW PICK',
      period: '매주 업데이트',
      href: '#recommended-destinations',
      cta: '추천 명소 보기',
    },
  ];

  const activeBanner = banners[currentBanner];

  const topBadgeText = isLoggedIn
    ? `${user?.userNicknm ?? '회원'}님, 오늘도 여행을 이어가볼까요?`
    : '로그인 없이 둘러보고, 필요할 때 바로 시작할 수 있어요';

  const heroDescription = isLoggedIn
    ? '최근 인기 일정과 추천 여행지를 빠르게 둘러보고, 필요한 순간에 바로 계획을 이어가세요.'
    : '동행과의 실시간 일정 조율, 스마트 환율 정산까지 RouteMate 하나로 끝내세요.';

  useEffect(() => {
    axios.get('/api/home/data')
      .then(res => {
        setPlans(res.data.plans || []);
        setDestinations(res.data.destinations || []);
      })
      .catch(err => {
        console.error('Failed to load home page data', err);
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  useEffect(() => {
    const timer = window.setInterval(() => {
      setCurrentBanner((current) => (current + 1) % banners.length);
    }, 4500);

    return () => window.clearInterval(timer);
  }, [banners.length]);

  return (
    <main className="max-w-7xl mx-auto px-6 py-16 text-center flex-grow flex flex-col justify-center items-center relative z-10">
      <div className="absolute top-[-10%] left-[-10%] w-[600px] h-[600px] bg-indigo-600/10 rounded-full blur-[120px] pointer-events-none"></div>
      <div className="absolute top-[40%] right-[-10%] w-[500px] h-[500px] bg-purple-600/10 rounded-full blur-[120px] pointer-events-none"></div>

      <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium bg-gradient-to-r from-indigo-500/10 to-purple-500/10 text-indigo-300 border border-indigo-500/20 mb-6 animate-pulse">
        <i className="fa-solid fa-earth-americas"></i> {topBadgeText}
      </span>

      <h1 className="text-5xl md:text-6xl font-extrabold tracking-tight mb-6 leading-[1.15] text-white">
        지금 떠나도 늦지 않아요,<br />
        <span className="theme-gradient-text">가장 편한 여행의 시작</span>입니다
      </h1>

      <p className="text-base md:text-lg text-gray-400 max-w-2xl mb-10 leading-relaxed font-light">
        {heroDescription}
      </p>

      <div className="flex flex-col sm:flex-row gap-4 justify-center items-center w-full max-w-md mb-10">
        {isLoggedIn ? (
          <Link
            to="/my-trips"
            className="w-full sm:w-auto text-base theme-btn-primary px-8 py-4 group"
          >
            내 여행 둘러보기
            <i className="fa-solid fa-arrow-right transition-transform group-hover:translate-x-1 ml-1"></i>
          </Link>
        ) : (
          <Link to="/join" className="w-full sm:w-auto text-base theme-btn-primary px-8 py-4 group">
            회원가입
            <i className="fa-solid fa-arrow-right transition-transform group-hover:translate-x-1 ml-1"></i>
          </Link>
        )}

        {isLoggedIn ? (
          <a
            href="#popular-plans"
            className="w-full sm:w-auto text-base bg-white/[0.04] hover:bg-white/[0.08] text-gray-200 hover:text-white border border-white/[0.08] px-8 py-4 rounded-2xl font-semibold transition duration-300 flex items-center justify-center gap-2"
          >
            인기 일정 둘러보기
          </a>
        ) : (
          <Link
            to="/login"
            className="w-full sm:w-auto text-base bg-white/[0.04] hover:bg-white/[0.08] text-gray-200 hover:text-white border border-white/[0.08] px-8 py-4 rounded-2xl font-semibold transition duration-300 flex items-center justify-center gap-2"
          >
            로그인하고 내 일정 시작하기
          </Link>
        )}
      </div>

      <div className="w-full max-w-4xl mb-16">
        <div className="relative overflow-hidden rounded-3xl border border-white/10 bg-slate-950/60 shadow-lg shadow-black/20">
          {activeBanner.href.startsWith('#') ? (
            <a href={activeBanner.href} aria-label={activeBanner.title} className="block">
              <img
                src={activeBanner.imageUrl}
                alt={activeBanner.title}
                className="h-[180px] md:h-[220px] w-full object-cover"
              />
            </a>
          ) : (
            <Link to={activeBanner.href} aria-label={activeBanner.title} className="block">
              <img
                src={activeBanner.imageUrl}
                alt={activeBanner.title}
                className="h-[180px] md:h-[220px] w-full object-cover"
              />
            </Link>
          )}
        </div>
      </div>

      {loading ? (
        <div className="text-gray-400 py-12">
          <i className="fa-solid fa-spinner fa-spin text-3xl mb-2 text-indigo-500"></i>
          <p>홈 데이터를 불러오고 있습니다...</p>
        </div>
      ) : (
        <>
          <div id="popular-plans" className="w-full text-left mt-2 mb-6">
            <h2 className="text-2xl font-bold text-white mb-2 flex items-center gap-2">
              <i className="fa-solid fa-compass text-purple-400"></i> 지금 가장 인기 있는 여행 루트
            </h2>
            <p className="text-sm text-gray-400 font-light">
              다른 여행자들이 많이 살펴본 루트를 먼저 확인하고, 마음에 드는 일정부터 골라보세요.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 w-full text-left mb-16">
            {plans.map((plan, i) => (
              <div key={i} className="theme-glass-card !p-0 overflow-hidden flex flex-col group cursor-pointer">
                <div className="relative h-56 w-full overflow-hidden bg-gray-900">
                  {plan.imageUrl ? (
                    <img src={plan.imageUrl} alt={plan.title} className="w-full h-full object-cover transition duration-500 group-hover:scale-105" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-gray-600 bg-gray-800">
                      <i className="fa-solid fa-image text-3xl"></i>
                    </div>
                  )}
                  <span className="absolute top-4 left-4 bg-black/60 backdrop-blur-md text-xs text-white px-3 py-1.5 rounded-full font-medium">
                    <i className="fa-solid fa-user text-indigo-300 mr-1"></i>
                    <span>{plan.userNicknm || '작성자'}</span>
                  </span>
                </div>
                <div className="p-6 flex-grow flex flex-col justify-between">
                  <div>
                    <h3 className="text-lg font-bold text-white mb-2 group-hover:text-indigo-400 transition">{plan.title}</h3>
                    <p className="text-sm text-gray-400 font-light leading-relaxed mb-4">{plan.description}</p>
                  </div>
                  <div className="flex justify-between items-center text-xs text-gray-500 border-t border-gray-800/60 pt-4">
                    <span className="flex items-center gap-1">
                      <i className="fa-solid fa-route"></i>
                      <span>{plan.spotCount}개 스팟</span>
                    </span>
                    <span className="flex items-center gap-1">
                      <i className="fa-solid fa-heart text-pink-500"></i>
                      <span>{plan.likeCount?.toLocaleString()}</span>
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div id="recommended-destinations" className="w-full text-left mb-6">
            <h2 className="text-2xl font-bold text-white mb-2 flex items-center gap-2">
              <i className="fa-solid fa-map-pin text-indigo-400"></i> RouteMate 추천 명소
            </h2>
            <p className="text-sm text-gray-400 font-light">
              지금 바로 떠나기 좋은 지역과 장소를 한눈에 확인해보세요.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 w-full text-left">
            {destinations.map((dest, i) => (
              <div key={i} className="theme-glass-card !p-0 overflow-hidden flex flex-col group cursor-pointer">
                <div className="relative h-56 w-full overflow-hidden bg-gray-900">
                  {dest.imageUrl ? (
                    <img src={dest.imageUrl} alt={dest.destName} className="w-full h-full object-cover transition duration-500 group-hover:scale-105" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-gray-600 bg-gray-800">
                      <i className="fa-solid fa-image text-3xl"></i>
                    </div>
                  )}
                  <span className="absolute top-4 left-4 bg-black/60 backdrop-blur-md text-xs text-white px-3 py-1.5 rounded-full font-medium">
                    <i className="fa-solid fa-location-dot text-red-400 mr-1"></i>
                    <span>{dest.country} {dest.city}</span>
                  </span>
                </div>
                <div className="p-6 flex-grow flex flex-col justify-between">
                  <div>
                    <h3 className="text-lg font-bold text-white mb-2 group-hover:text-indigo-400 transition">{dest.destName}</h3>
                    <p className="text-sm text-gray-400 font-light leading-relaxed mb-4">{dest.destDesc}</p>
                  </div>
                  <div className="flex justify-between items-center text-xs text-gray-500 border-t border-gray-800/60 pt-4">
                    <span className="flex items-center gap-1">
                      <i className="fa-solid fa-tags"></i>
                      <span>{dest.category}</span>
                    </span>
                    <span className="flex items-center gap-1">
                      <i className="fa-solid fa-heart text-pink-500"></i>
                      <span>{dest.likeCount?.toLocaleString()}</span>
                    </span>
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
