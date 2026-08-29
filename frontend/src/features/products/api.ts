import { api } from '../../lib/http';
import type { ProductDetail, ProductOrder, ProductSummary } from './model';

/** 옵션 상품 예약 생성 API에 전달하는 요청 본문이다. */
export type CreateProductOrderRequest = {
  productId: number;
  optionId: number;
  useDate: string;
  quantity: number;
  buyerName: string;
  buyerEmail: string;
  buyerPhone: string | null;
};

/** 공개 상품 조회와 로그인 사용자의 예약 API를 한곳에서 제공한다. */
export const productApi = {
  getPublicProducts: async () => (await api.get<ProductSummary[]>('/api/public/products')).data,
  getPublicProduct: async (productId: string) => (await api.get<ProductDetail>(`/api/public/products/${productId}`)).data,
  createOrder: async (request: CreateProductOrderRequest) => (await api.post<ProductOrder>('/api/product-orders', request)).data,
  getMyOrders: async () => (await api.get<ProductOrder[]>('/api/product-orders/my')).data,
  getScheduleCandidates: async (params: { countryCode: string; regionCode: string; useDate: string }) =>
    (await api.get<ProductOrder[]>('/api/product-orders/my/schedule-candidates', { params })).data,
};
