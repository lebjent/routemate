import { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import {
  PRODUCT_TYPES,
  formatProductPrice,
  productTypeLabel,
  type ProductSummary,
} from '../features/products/model';

export const Products = () => {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const selectedType = searchParams.get('productType') ?? '';
  const queryParam = searchParams.get('query') ?? '';
  const [queryInput, setQueryInput] = useState(queryParam);
  const countryId = Number(searchParams.get('countryId')) || null;
  const regionId = Number(searchParams.get('regionId')) || null;

  useEffect(() => {
    setLoading(true);
    setError(null);
    axios.get<ProductSummary[]>('/api/public/products')
      .then((response) => setProducts(response.data))
      .catch(() => setError('옵션상품을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    setQueryInput(queryParam);
  }, [queryParam]);

  const filteredProducts = useMemo(() => {
    const normalizedQuery = queryInput.trim().toLowerCase();
    return products.filter((product) => {
      const typeMatched = !selectedType || product.productType === selectedType;
      const locationMatched = (!countryId || product.countryId === countryId)
        && (!regionId || product.regionId === regionId);
      const queryMatched = !normalizedQuery || [
        product.productName,
        product.productSummary,
        product.destinationName,
        product.countryName,
        product.regionName,
      ].some((value) => value?.toLowerCase().includes(normalizedQuery));
      return typeMatched && locationMatched && queryMatched;
    });
  }, [countryId, products, queryInput, regionId, selectedType]);

  const selectedLocation = useMemo(() => {
    if (!countryId && !regionId) return null;
    const matched = products.find((product) => (!countryId || product.countryId === countryId)
      && (!regionId || product.regionId === regionId));
    if (!matched) return null;
    return {
      countryName: matched.countryName,
      regionName: regionId ? matched.regionName : null,
    };
  }, [countryId, products, regionId]);

  const updateFilter = (key: 'productType' | 'query', value: string) => {
    const nextParams = new URLSearchParams(searchParams);
    if (value) nextParams.set(key, value);
    else nextParams.delete(key);
    setSearchParams(nextParams, { replace: key === 'query' });
  };

  const commitQuery = (value: string) => {
    updateFilter('query', value);
  };

  return (
    <main className="relative z-10 mx-auto w-full max-w-7xl flex-grow px-6 py-12">
      <div className="pointer-events-none absolute left-[-10%] top-0 h-[420px] w-[420px] rounded-full bg-indigo-600/10 blur-[110px]" />

      <section className="relative mb-9 overflow-hidden rounded-3xl border border-white/10 bg-gradient-to-br from-indigo-950/80 via-slate-950/80 to-purple-950/70 px-7 py-10 md:px-11">
        <span className="mb-4 inline-flex rounded-full border border-indigo-400/20 bg-indigo-500/10 px-3 py-1 text-xs font-semibold text-indigo-200">
          ROUTEMATE OPTION SHOP
        </span>
        <h1 className="mb-3 text-3xl font-extrabold text-white md:text-4xl">여행 준비도 한곳에서 끝내세요</h1>
        <p className="max-w-2xl text-sm leading-7 text-slate-300 md:text-base">
          플레이스별 입장권, 현지 투어, 교통과 eSIM 옵션을 비교하고 원하는 이용일로 주문할 수 있습니다.
        </p>
      </section>

      <section className="mb-8 rounded-2xl border border-white/10 bg-white/[0.035] p-4 md:flex md:items-center md:justify-between md:gap-5">
        <div className="min-w-0 flex-grow">
          {selectedLocation ? (
            <div className="mb-4 flex flex-wrap items-center gap-2 border-b border-white/10 pb-4">
              <span className="text-xs font-semibold text-slate-500">선택 지역</span>
              <span className="rounded-full border border-indigo-400/20 bg-indigo-500/10 px-3 py-1.5 text-sm font-semibold text-indigo-200">
                <i className="fa-solid fa-location-dot mr-1.5" />{selectedLocation.countryName}{selectedLocation.regionName ? ` → ${selectedLocation.regionName}` : ' 전체'}
              </span>
              <Link to="/products" className="rounded-full px-3 py-1.5 text-xs text-slate-400 transition hover:bg-white/5 hover:text-white"><i className="fa-solid fa-xmark mr-1" />전체 옵션상품</Link>
            </div>
          ) : null}
          <div className="flex flex-wrap gap-2">
          {PRODUCT_TYPES.map((type) => (
            <button
              key={type.value}
              type="button"
              onClick={() => updateFilter('productType', type.value)}
              className={`rounded-xl px-4 py-2.5 text-sm font-semibold transition ${selectedType === type.value ? 'bg-indigo-500 text-white shadow-lg shadow-indigo-600/20' : 'bg-white/5 text-slate-300 hover:bg-white/10 hover:text-white'}`}
            >
              {type.label}
            </button>
          ))}
          </div>
        </div>
        <div className="relative mt-4 min-w-64 md:mt-0 md:w-80">
          <i className="fa-solid fa-magnifying-glass absolute left-4 top-1/2 -translate-y-1/2 text-sm text-slate-500" />
          <input
            value={queryInput}
            onChange={(event) => setQueryInput(event.target.value)}
            onCompositionEnd={(event) => commitQuery(event.currentTarget.value)}
            onBlur={(event) => commitQuery(event.currentTarget.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter' && !event.nativeEvent.isComposing) commitQuery(event.currentTarget.value);
            }}
            placeholder="상품명, 국가, 지역 검색"
            className="w-full rounded-xl border border-white/10 bg-slate-950/70 py-3 pl-10 pr-10 text-sm text-white outline-none transition placeholder:text-slate-600 focus:border-indigo-400"
          />
          {queryInput ? (
            <button
              type="button"
              aria-label="검색어 지우기"
              onClick={() => {
                setQueryInput('');
                commitQuery('');
              }}
              className="absolute right-3 top-1/2 flex h-6 w-6 -translate-y-1/2 items-center justify-center rounded-full text-xs text-slate-500 transition hover:bg-white/10 hover:text-white"
            >
              <i className="fa-solid fa-xmark" />
            </button>
          ) : null}
        </div>
      </section>

      {loading ? (
        <div className="py-24 text-center text-slate-400"><i className="fa-solid fa-spinner fa-spin mb-3 text-3xl text-indigo-400" /><p>상품을 불러오고 있습니다...</p></div>
      ) : error ? (
        <div role="alert" className="rounded-2xl border border-red-400/20 bg-red-500/10 px-5 py-10 text-center text-red-200">{error}</div>
      ) : filteredProducts.length === 0 ? (
        <div className="rounded-2xl border border-white/10 bg-white/[0.03] px-5 py-20 text-center text-slate-400">
          <i className="fa-regular fa-face-frown-open mb-3 text-3xl" />
          <p>조건에 맞는 옵션상품이 없습니다.</p>
        </div>
      ) : (
        <>
          <div className="mb-5 flex items-end justify-between">
            <div><h2 className="text-xl font-bold text-white">판매 중인 옵션상품</h2><p className="mt-1 text-sm text-slate-500">총 {filteredProducts.length.toLocaleString()}개</p></div>
          </div>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {filteredProducts.map((product) => (
              <Link key={product.productId} to={`/products/${product.productId}`} className="group overflow-hidden rounded-2xl border border-white/10 bg-slate-900/70 transition hover:-translate-y-1 hover:border-indigo-400/40 hover:shadow-2xl hover:shadow-indigo-950/30">
                <div className="relative h-56 overflow-hidden bg-slate-900">
                  {product.imageUrl ? <img src={product.imageUrl} alt={product.productName} className="h-full w-full object-cover transition duration-500 group-hover:scale-105" /> : <div className="flex h-full items-center justify-center text-4xl text-slate-700"><i className="fa-solid fa-ticket" /></div>}
                  <span className="absolute left-4 top-4 rounded-full bg-slate-950/75 px-3 py-1.5 text-xs font-semibold text-white backdrop-blur">{productTypeLabel(product.productType)}</span>
                </div>
                <div className="p-5">
                  <p className="mb-2 truncate text-xs font-medium text-indigo-300"><i className="fa-solid fa-location-dot mr-1.5" />{product.countryName} · {product.regionName} · {product.destinationName}</p>
                  <h3 className="mb-2 line-clamp-2 min-h-14 text-lg font-bold leading-7 text-white transition group-hover:text-indigo-300">{product.productName}</h3>
                  <p className="line-clamp-2 min-h-10 text-sm leading-5 text-slate-400">{product.productSummary ?? '여행지에서 바로 이용할 수 있는 RouteMate 추천 상품입니다.'}</p>
                  <div className="mt-5 flex items-end justify-between border-t border-white/10 pt-4">
                    <span className="text-xs text-slate-500">옵션 {product.optionCount}개</span>
                    <div className="text-right"><strong className="text-lg text-white">{formatProductPrice(product.minimumPrice, product.currency)}</strong><span className="ml-1 text-xs text-slate-500">부터</span></div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </>
      )}
    </main>
  );
};
