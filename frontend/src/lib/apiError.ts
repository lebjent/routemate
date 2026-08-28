import axios from 'axios';

type ErrorPayload = {
  detail?: string;
  message?: string;
};

export const getApiErrorMessage = (error: unknown, fallback: string) => {
  if (!axios.isAxiosError<ErrorPayload>(error)) return fallback;
  return error.response?.data?.detail || error.response?.data?.message || fallback;
};

export const isUnauthorizedError = (error: unknown) =>
  axios.isAxiosError(error) && [401, 403].includes(error.response?.status ?? 0);
