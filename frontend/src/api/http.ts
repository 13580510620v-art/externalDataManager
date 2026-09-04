import axios, { AxiosAdapter, AxiosRequestConfig } from 'axios';

import type { ApiResponse } from './types';

export class ApiError extends Error {
  constructor(
    readonly code: number,
    message: string
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

type EnvLike = {
  DEV: boolean;
  PROD: boolean;
  VITE_API_BASE_URL?: string;
};

export function resolveApiBaseUrl(env: EnvLike): string {
  if (env.DEV) {
    return '';
  }
  return env.VITE_API_BASE_URL ?? '';
}

function csrfToken(): string | undefined {
  return document.cookie
    .split('; ')
    .find(cookie => cookie.startsWith('XSRF-TOKEN='))
    ?.split('=')[1];
}

export function createHttp(baseUrl: string, adapter?: AxiosAdapter) {
  const instance = axios.create({
    baseURL: baseUrl,
    withCredentials: true,
    adapter
  });

  instance.interceptors.request.use(config => {
    const token = csrfToken();
    if (token) {
      config.headers.set('X-XSRF-TOKEN', token);
    }
    return config;
  });

  instance.interceptors.response.use(
    response => {
      const payload = response.data as ApiResponse<unknown>;
      if (payload && payload.code !== 0) {
        throw new ApiError(payload.code, payload.message || '请求失败');
      }
      return response;
    },
    error => {
      const payload = error.response?.data as ApiResponse<unknown> | undefined;
      if (payload && payload.code !== 0) {
        throw new ApiError(payload.code, payload.message || '请求失败');
      }
      throw new ApiError(error.response?.status ?? 500, '网络请求失败，请稍后重试');
    }
  );

  return {
    get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
      return instance.get<ApiResponse<T>>(url, config).then(response => response.data.data);
    },
    post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
      return instance.post<ApiResponse<T>>(url, data, config).then(response => response.data.data);
    }
  };
}

export const http = createHttp(
  resolveApiBaseUrl({
    DEV: import.meta.env.DEV,
    PROD: import.meta.env.PROD,
    VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL
  })
);
