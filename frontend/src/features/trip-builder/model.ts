/** 여행 일정 편집기에서 선택하는 국가 기준 데이터다. */
export type CountryOption = {
  countryId: number;
  countryName: string;
  countryCode: string;
};

/** 선택한 국가에 속한 지역 기준 데이터다. */
export type RegionOption = {
  regionId: number;
  regionName: string;
  regionCode: string;
  countryId: number;
  countryCode: string;
};

export type TransportType = '' | 'TRAIN' | 'CAR' | 'FLIGHT' | 'CRUISE' | 'OTHER';

/** 여행 일차의 한 시간대에 기록하는 세부 일정과 선택 교통편이다. */
export type Schedule = {
  id: string;
  time: string;
  title: string;
  location: string;
  memo: string;
  transportType: TransportType;
  transportName: string;
  departureTime: string;
  arrivalTime: string;
  transportMemo: string;
  productOrderId: number | null;
  productOrderNo: string | null;
};

/** 특정 일차에 방문할 국가·지역과 하위 세부 일정 목록이다. */
export type DayRegion = {
  id: string;
  countryCode: string;
  regionCode: string;
  note: string;
  schedules: Schedule[];
};

/** 여행 계획 편집 상태에서 하루를 표현하는 모델이다. */
export type DayPlan = {
  day: number;
  date: string;
  regions: DayRegion[];
};

/** 여행 계획에 추가하는 준비물과 필수 여부다. */
export type PackingItem = {
  id: string;
  item: string;
  required: boolean;
};

export type DayDescriptor = Pick<DayPlan, 'day' | 'date'>;

export type PreviewRegion = DayRegion & {
  label: string;
  schedules: Schedule[];
};

export type PreviewDay = Omit<DayPlan, 'regions'> & {
  regions: PreviewRegion[];
};

export type TransportOption = {
  value: Exclude<TransportType, ''>;
  label: string;
  icon: string;
  nameLabel: string;
  namePlaceholder: string;
  memoPlaceholder: string;
};

const makeId = () => crypto.randomUUID();

/** 새 세부 일정을 추가할 때 사용할 빈 편집 상태를 만든다. */
export const createSchedule = (): Schedule => ({
  id: makeId(),
  time: '',
  title: '',
  location: '',
  memo: '',
  transportType: '',
  transportName: '',
  departureTime: '',
  arrivalTime: '',
  transportMemo: '',
  productOrderId: null,
  productOrderNo: null,
});

/** 새 방문 지역을 추가할 때 사용할 빈 편집 상태를 만든다. */
export const createDayRegion = (): DayRegion => ({
  id: makeId(),
  countryCode: '',
  regionCode: '',
  note: '',
  schedules: [],
});

/** 새 준비물 입력 행의 기본 상태를 만든다. */
export const createPackingItem = (item = '', required = true): PackingItem => ({
  id: makeId(),
  item,
  required,
});

export const parseLocalDate = (value: string) => new Date(`${value}T00:00:00`);

const toDateInputValue = (date: Date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/** ISO 날짜 문자열을 한국어 날짜 표시용 문자열로 변환한다. */
export const formatDate = (value: string) => {
  if (!value) return '';
  const date = parseLocalDate(value);
  const weekday = ['일', '월', '화', '수', '목', '금', '토'][date.getDay()];
  return `${date.getMonth() + 1}월 ${date.getDate()}일 (${weekday})`;
};

export const formatShortDate = (value: string) => {
  if (!value) return '';
  const date = parseLocalDate(value);
  return `${date.getMonth() + 1}/${date.getDate()}`;
};

export const formatDateWithYear = (value: string) => {
  if (!value) return '';
  const date = parseLocalDate(value);
  return `${date.getFullYear()}. ${date.getMonth() + 1}. ${date.getDate()}.`;
};

/** 시작일과 종료일을 포함한 실제 여행 일수를 계산한다. */
export const getTravelDurationDays = (startDate?: string | null, endDate?: string | null) => {
  if (!startDate || !endDate) return 0;
  const start = parseLocalDate(startDate);
  const end = parseLocalDate(endDate);
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || start > end) return 0;
  return Math.floor((end.getTime() - start.getTime()) / 86_400_000) + 1;
};

export const formatTravelDuration = (startDate?: string | null, endDate?: string | null) => {
  const days = getTravelDurationDays(startDate, endDate);
  return days > 0 ? `${days}일 여행` : '기간 미정';
};

/** 여행 기간의 모든 날짜를 일차 번호와 함께 편집기용 배열로 만든다. */
export const createDays = (travelStartDate: string, travelEndDate: string): DayDescriptor[] => {
  if (!travelStartDate || !travelEndDate) return [];
  const start = parseLocalDate(travelStartDate);
  const end = parseLocalDate(travelEndDate);
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || start > end) return [];

  const days: DayDescriptor[] = [];
  const cursor = new Date(start);
  while (cursor <= end) {
    days.push({ day: days.length + 1, date: toDateInputValue(cursor) });
    cursor.setDate(cursor.getDate() + 1);
  }
  return days;
};

/** 세부 일정이 저장할 만한 사용자 입력을 하나 이상 포함하는지 판별한다. */
export const hasScheduleContent = (schedule: Schedule) =>
  Boolean(schedule.title.trim() || schedule.location.trim() || schedule.memo.trim() || schedule.transportType || schedule.productOrderId);

export const formatTimeInput = (value: string) => {
  const digits = value.replace(/\D/g, '').slice(0, 4);
  return digits.length <= 2 ? digits : `${digits.slice(0, 2)}:${digits.slice(2)}`;
};

export const transportOptions: TransportOption[] = [
  { value: 'TRAIN', label: '기차', icon: 'fa-train-subway', nameLabel: '열차편', namePlaceholder: '예: KTX 123', memoPlaceholder: '좌석·역 정보' },
  { value: 'CAR', label: '자가용', icon: 'fa-car-side', nameLabel: '차량·렌터카', namePlaceholder: '예: 렌터카 수령', memoPlaceholder: '주차·도로 정보' },
  { value: 'FLIGHT', label: '항공', icon: 'fa-plane-departure', nameLabel: '항공편명', namePlaceholder: '예: KE 901', memoPlaceholder: '터미널·좌석 정보' },
  { value: 'CRUISE', label: '크루즈', icon: 'fa-ship', nameLabel: '크루즈명', namePlaceholder: '예: MSC Seaview', memoPlaceholder: '선착장·객실 정보' },
  { value: 'OTHER', label: '기타', icon: 'fa-route', nameLabel: '이동수단', namePlaceholder: '예: 공항 리무진', memoPlaceholder: '이동 관련 메모' },
];

export const transportByType = Object.fromEntries(
  transportOptions.map((option) => [option.value, option])
) as Record<Exclude<TransportType, ''>, TransportOption>;
