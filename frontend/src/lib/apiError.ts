import axios from 'axios';

type ErrorPayload = {
  detail?: string;
  message?: string;
};

/** Axios 오류 응답의 서버 메시지를 우선 사용하고, 없으면 화면별 기본 문구를 반환한다. */
export const getApiErrorMessage = (error: unknown, fallback: string) => {
  if (!axios.isAxiosError<ErrorPayload>(error)) return fallback;
  return error.response?.data?.detail || error.response?.data?.message || fallback;
};

/** 오류가 로그인 필요 또는 권한 부족 응답인지 판별한다. */
export const isUnauthorizedError = (error: unknown) =>
  axios.isAxiosError(error) && [401, 403].includes(error.response?.status ?? 0);
