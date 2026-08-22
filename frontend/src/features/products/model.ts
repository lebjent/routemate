export interface ProductSummary {
  productId: number;
  productName: string;
  productSummary: string | null;
  productType: string;
  providerName: string | null;
  imageUrl: string | null;
  destinationId: number;
  destinationName: string;
  countryId: number;
  countryName: string;
  regionId: number;
  regionName: string;
  minimumPrice: number;
  currency: string;
  optionCount: number;
}

export interface ProductOption {
  optionId: number;
  optionName: string;
  optionDesc: string | null;
  price: number;
  currency: string;
  cancellationPolicy: string | null;
  validityText: string | null;
  confirmationType: string;
}

export interface ProductDetail extends Omit<ProductSummary, 'minimumPrice' | 'currency' | 'optionCount'> {
  productDesc: string | null;
  detailImageUrl: string | null;
  courseText: string | null;
  includedText: string | null;
  excludedText: string | null;
  usageGuideText: string | null;
  noticeText: string | null;
  cancellationPolicyText: string | null;
  faqText: string | null;
  meetingTime: string | null;
  meetingPlace: string | null;
  bookingUrl: string | null;
  options: ProductOption[];
}

export interface ProductOrder {
  orderId: number;
  orderNo: string;
  productId: number | null;
  productName: string;
  optionId: number | null;
  optionName: string;
  productImageUrl: string | null;
  destinationName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  currency: string;
  useDate: string;
  buyerName: string;
  buyerEmail: string;
  buyerPhone: string | null;
  orderStatus: string;
  paymentStatus: string;
  bookingUrl: string | null;
  createDt: string;
}

export const PRODUCT_TYPES = [
  { value: '', label: '전체' },
  { value: 'TICKET', label: '입장권·패스' },
  { value: 'TOUR', label: '투어·액티비티' },
  { value: 'TRANSFER', label: '공항·교통' },
  { value: 'SIM', label: 'eSIM·통신' },
  { value: 'ETC', label: '기타 체험' },
];

export const productTypeLabel = (value: string) =>
  PRODUCT_TYPES.find((type) => type.value === value)?.label ?? value;

export const formatProductPrice = (amount: number, currency: string) => {
  try {
    return new Intl.NumberFormat('ko-KR', { style: 'currency', currency, maximumFractionDigits: 2 }).format(amount);
  } catch {
    return `${amount.toLocaleString()} ${currency}`;
  }
};

export const orderStatusLabel = (status: string) => ({
  ORDERED: '주문 접수',
  CONFIRMED: '예약 확정',
  CANCELLED: '주문 취소',
}[status] ?? status);

export const paymentStatusLabel = (status: string) => ({
  PENDING: '결제 대기',
  PAID: '결제 완료',
  FAILED: '결제 실패',
  REFUNDED: '환불 완료',
}[status] ?? status);

export const hasExternalBookingUrl = (bookingUrl: string | null) => {
  if (!bookingUrl) return false;
  try {
    const url = new URL(bookingUrl);
    return ['http:', 'https:'].includes(url.protocol) && url.hostname !== 'demo.routemate.local';
  } catch {
    return false;
  }
};
