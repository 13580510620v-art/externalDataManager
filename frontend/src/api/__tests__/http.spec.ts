import { describe, expect, it } from 'vitest';

import { ApiError, createHttp, resolveApiBaseUrl } from '../http';

describe('HTTP 客户端', () => {
  it('开发环境使用同域代理地址', () => {
    expect(resolveApiBaseUrl({ DEV: true, PROD: false, VITE_API_BASE_URL: 'https://api.example.com' })).toBe('');
  });

  it('生产环境使用显式 API 来源', () => {
    expect(resolveApiBaseUrl({ DEV: false, PROD: true, VITE_API_BASE_URL: 'https://api.example.com' })).toBe(
      'https://api.example.com'
    );
  });

  it('写请求自动携带 CSRF 请求头并携带 Cookie', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token; path=/';
    let requestConfig: Record<string, unknown> | undefined;
    const client = createHttp('', async config => {
      requestConfig = config as unknown as Record<string, unknown>;
      return {
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
        data: { code: 0, message: 'success', data: { ok: true } }
      };
    });
    const result = await client.post('/api/auth/login', { username: 'admin', password: 'secret' });

    expect(result).toEqual({ ok: true });
    expect(requestConfig?.withCredentials).toBe(true);
    expect(requestConfig?.headers).toMatchObject({ 'X-XSRF-TOKEN': 'test-csrf-token' });
  });

  it('业务失败码抛出后端错误信息', async () => {
    const client = createHttp('', async config => ({
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
      data: { code: 403, message: '没有权限执行该操作', data: null }
    }));

    await expect(client.get('/api/tasks')).rejects.toMatchObject({
      code: 403,
      message: '没有权限执行该操作'
    });
  });

  it('HTTP 错误转换为用户可读错误', async () => {
    const client = createHttp('', async () => {
      throw new Error('Network Error');
    });

    await expect(client.get('/api/tasks')).rejects.toBeInstanceOf(ApiError);
  });
});
