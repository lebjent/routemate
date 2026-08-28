import { api } from '../../lib/http';
import type { ProductDetail, ProductOrder, ProductSummary } from './model';

export type CreateProductOrderRequest = {
  productId: number;
  optionId: number;
  useDate: string;
  quantity: number;
  buyerName: string;
  buyerEmail: string;
  buyerPhone: string | null;
};

export const productApi = {
  getPublicProducts: async () => (await api.get<ProductSummary[]>('/api/public/products')).data,
  getPublicProduct: async (productId: string) => (await api.get<ProductDetail>(`/api/public/products/${productId}`)).data,
  createOrder: async (request: CreateProductOrderRequest) => (await api.post<ProductOrder>('/api/product-orders', request)).data,
  getMyOrders: async () => (await api.get<ProductOrder[]>('/api/product-orders/my')).data,
  getScheduleCandidates: async (params: { countryCode: string; regionCode: string; useDate: string }) =>
    (await api.get<ProductOrder[]>('/api/product-orders/my/schedule-candidates', { params })).data,
};
