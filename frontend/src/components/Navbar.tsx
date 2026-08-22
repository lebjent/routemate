import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../hooks/useAuth';
import type { ProductSummary } from '../features/products/model';

interface ProductCountryMenu {
  countryId: number;
  countryName: string;
  regions: Array<{
    regionId: number;
    regionName: string;
  }>;
}

export const Navbar = () => {
  const { user, loading, logout } = useAuth();
  const [productMenuOpen, setProductMenuOpen] = useState(false);
  const [hoveredCountryId, setHoveredCountryId] = useState<number | null>(null);
  const [productLocations, setProductLocations] = useState<ProductSummary[]>([]);

  useEffect(() => {
    axios.get<ProductSummary[]>('/api/public/products')
      .then((response) => setProductLocations(response.data))
      .catch(() => setProductLocations([]));
  }, []);

  const countryMenus = useMemo<ProductCountryMenu[]>(() => {
    const countries = new Map<number, ProductCountryMenu>();
    productLocations.forEach((product) => {
      const country = countries.get(product.countryId) ?? {
        countryId: product.countryId,
        countryName: product.countryName,
        regions: [],
      };
      if (!country.regions.some((region) => region.regionId === product.regionId)) {
        country.regions.push({ regionId: product.regionId, regionName: product.regionName });
      }
      countries.set(product.countryId, country);
    });
    return [...countries.values()]
      .map((country) => ({ ...country, regions: country.regions.sort((a, b) => a.regionName.localeCompare(b.regionName, 'ko')) }))
      .sort((a, b) => a.countryName.localeCompare(b.countryName, 'ko'));
  }, [productLocations]);

  const activeCountry = countryMenus.find((country) => country.countryId === hoveredCountryId) ?? countryMenus[0];

  const openProductMenu = () => {
    setProductMenuOpen(true);
    if (hoveredCountryId === null && countryMenus[0]) setHoveredCountryId(countryMenus[0].countryId);
  };

  return (
    <nav className="w-full max-w-7xl mx-auto px-6 py-5 flex justify-between items-center z-50 relative">
      <Link to="/" className="text-2xl font-bold tracking-tight flex items-center gap-2 cursor-pointer text-white">
        <span className="text-brand-primary"><i className="fa-solid fa-map-location-dot"></i></span>
        <span>Route<span className="text-indigo-400">Mate</span></span>
      </Link>
      <div className="flex items-center gap-6">
        <Link to="/" className="text-sm text-gray-400 hover:text-white transition hidden md:block">탐색하기</Link>
        <div className="relative hidden sm:block" onMouseEnter={openProductMenu} onMouseLeave={() => setProductMenuOpen(false)}>
          <Link
            to="/products"
            onFocus={openProductMenu}
            onClick={() => setProductMenuOpen(false)}
            className="flex items-center gap-1.5 py-2 text-sm text-gray-400 transition hover:text-white"
          >
            옵션상품 <i className={`fa-solid fa-chevron-down text-[9px] transition ${productMenuOpen ? 'rotate-180' : ''}`} />
          </Link>
          {productMenuOpen ? (
            <div className="absolute left-1/2 top-full w-[560px] -translate-x-1/2 pt-3">
              <div className="grid max-h-[430px] grid-cols-[220px_1fr] overflow-hidden rounded-2xl border border-white/10 bg-slate-950/95 shadow-2xl shadow-black/50 backdrop-blur-xl">
                <div className="border-r border-white/10 bg-white/[0.025] p-3">
                  <Link
                    to="/products"
                    onClick={() => setProductMenuOpen(false)}
                    className="mb-2 flex items-center gap-2 rounded-xl bg-indigo-500/15 px-3 py-3 text-sm font-bold text-indigo-200 transition hover:bg-indigo-500/25"
                  >
                    <i className="fa-solid fa-border-all" /> 전체 옵션상품
                  </Link>
                  <p className="px-3 pb-2 pt-1 text-[10px] font-bold tracking-widest text-slate-600">국가</p>
                  <div className="option-scrollbar option-scrollbar-compact max-h-[330px] space-y-1 overflow-y-auto pr-1">
                    {countryMenus.map((country) => (
                      <Link
                        key={country.countryId}
                        to={`/products?countryId=${country.countryId}`}
                        onMouseEnter={() => setHoveredCountryId(country.countryId)}
                        onFocus={() => setHoveredCountryId(country.countryId)}
                        onClick={() => setProductMenuOpen(false)}
                        className={`flex items-center justify-between rounded-lg px-3 py-2.5 text-sm transition ${activeCountry?.countryId === country.countryId ? 'bg-white/10 text-white' : 'text-slate-400 hover:bg-white/5 hover:text-white'}`}
                      >
                        <span>{country.countryName}</span>
                        <i className="fa-solid fa-chevron-right text-[9px] text-slate-600" />
                      </Link>
                    ))}
                  </div>
                </div>
                <div className="p-4">
                  {activeCountry ? (
                    <>
                      <div className="mb-3 flex items-center justify-between border-b border-white/10 pb-3">
                        <div><p className="text-[10px] font-bold tracking-widest text-indigo-300">REGION</p><p className="mt-1 font-bold text-white">{activeCountry.countryName}</p></div>
                        <Link to={`/products?countryId=${activeCountry.countryId}`} onClick={() => setProductMenuOpen(false)} className="text-xs text-slate-400 hover:text-indigo-300">국가 전체 보기</Link>
                      </div>
                      <div className="option-scrollbar option-scrollbar-compact grid max-h-[330px] grid-cols-2 gap-2 overflow-y-auto pr-1">
                        {activeCountry.regions.map((region) => (
                          <Link
                            key={region.regionId}
                            to={`/products?countryId=${activeCountry.countryId}&regionId=${region.regionId}`}
                            onClick={() => setProductMenuOpen(false)}
                            className="rounded-xl border border-white/[0.06] bg-white/[0.025] px-3 py-3 text-sm text-slate-300 transition hover:border-indigo-400/30 hover:bg-indigo-500/10 hover:text-white"
                          >
                            <i className="fa-solid fa-location-dot mr-2 text-xs text-indigo-400" />{region.regionName}
                          </Link>
                        ))}
                      </div>
                    </>
                  ) : (
                    <div className="flex h-full items-center justify-center text-sm text-slate-500">판매 중인 옵션상품 지역이 없습니다.</div>
                  )}
                </div>
              </div>
            </div>
          ) : null}
        </div>
        <Link to="/my-trips" className="text-sm text-gray-400 hover:text-white transition hidden md:block">내 여행</Link>
        <Link to="/lotto" className="text-sm text-gray-400 hover:text-white transition hidden md:block">행운의 로또</Link>
        {!loading && (user ? (
          <>
            <Link to="/my-product-orders" aria-label="구매내역" title="구매내역" className="text-sm text-gray-400 hover:text-white transition hidden lg:block">구매내역</Link>
            <span className="text-sm font-medium text-indigo-200">{user.userNicknm}님</span>
            <button
              type="button"
              onClick={() => void logout()}
              className="text-sm text-gray-300 hover:text-indigo-400 font-medium transition"
            >
              로그아웃
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="text-sm text-gray-300 hover:text-indigo-400 font-medium transition">로그인</Link>
            <Link to="/join" className="text-sm bg-brand-primary hover:bg-indigo-500 text-white px-5 py-2.5 rounded-xl font-semibold shadow-lg shadow-indigo-600/20 transition duration-300">
              시작하기
            </Link>
          </>
        ))}
      </div>
    </nav>
  );
};
