/** 상품 목록 카드에 표시할 최소 정보다. */
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

/** 상품 상세에서 선택 가능한 판매 옵션과 이용 조건이다. */
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

/** 상품 상세 화면의 설명, 안내 문구, 판매 옵션을 포함하는 모델이다. */
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

/** 로그인 사용자의 옵션 상품 예약 내역 모델이다. */
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

/** 공개 상품 탐색 필터에서 사용할 상품 유형과 표시명이다. */
export const PRODUCT_TYPES = [
  { value: '', label: '전체' },
  { value: 'TICKET', label: '입장권·패스' },
  { value: 'TOUR', label: '투어·액티비티' },
  { value: 'TRANSFER', label: '공항·교통' },
  { value: 'SIM', label: 'eSIM·통신' },
  { value: 'ETC', label: '기타 체험' },
];

/** 서버 상품 유형 코드를 사용자용 표시명으로 변환한다. */
export const productTypeLabel = (value: string) =>
  PRODUCT_TYPES.find((type) => type.value === value)?.label ?? value;

/** 가격과 통화 코드를 한국어 형식의 표시 문자열로 변환한다. */
export const formatProductPrice = (amount: number, currency: string) => {
  try {
    return new Intl.NumberFormat('ko-KR', { style: 'currency', currency, maximumFractionDigits: 2 }).format(amount);
  } catch {
    return `${amount.toLocaleString()} ${currency}`;
  }
};

/** 예약 처리 상태 코드를 화면 표시 문구로 변환한다. */
export const orderStatusLabel = (status: string) => ({
  ORDERED: '주문 접수',
  CONFIRMED: '예약 확정',
  CANCELLED: '주문 취소',
}[status] ?? status);

/** 결제 상태 코드를 화면 표시 문구로 변환한다. */
export const paymentStatusLabel = (status: string) => ({
  PENDING: '결제 대기',
  PAID: '결제 완료',
  FAILED: '결제 실패',
  REFUNDED: '환불 완료',
}[status] ?? status);

/** 외부 예약 페이지로 이동할 수 있는 유효한 URL인지 확인한다. */
export const hasExternalBookingUrl = (bookingUrl: string | null) => {
  if (!bookingUrl) return false;
  try {
    const url = new URL(bookingUrl);
    return ['http:', 'https:'].includes(url.protocol) && url.hostname !== 'demo.routemate.local';
  } catch {
    return false;
  }
};
