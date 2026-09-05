import axios from 'axios';

/**
 * 브라우저에서 사용하는 공통 HTTP 클라이언트입니다.
 * API 관련 기본 설정과 인터셉터는 이 파일에만 추가합니다.
 */
export const api = axios.create({
  headers: {
    Accept: 'application/json',
  },
});

/** 화면에서 직접 axios 인스턴스를 만들지 않도록 공통 오류 판별도 함께 제공한다. */
export const isApiError = axios.isAxiosError;
