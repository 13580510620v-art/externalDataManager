import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';

import { authApi } from '../../api';
import { useAuthStore } from '../auth';

vi.mock('../../api', () => ({
  authApi: {
    login: vi.fn(),
    fetchMe: vi.fn(),
    logout: vi.fn()
  }
}));

describe('认证状态', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.mocked(authApi.login).mockReset();
    vi.mocked(authApi.fetchMe).mockReset();
    vi.mocked(authApi.logout).mockReset();
  });

  it('登录后保存当前用户', async () => {
    const user = { id: 1, username: 'admin', fullName: '管理员', permissions: ['dashboard:read'] };
    vi.mocked(authApi.login).mockResolvedValue(user);
    const store = useAuthStore();

    await expect(store.login('admin', 'Admin@123')).resolves.toEqual(user);
    expect(store.user).toEqual(user);
    expect(store.hasPermission('dashboard:read')).toBe(true);
  });

  it('初始化时获取当前用户并在失败时清除状态', async () => {
    const user = { id: 1, username: 'admin', fullName: '管理员', permissions: [] };
    vi.mocked(authApi.fetchMe).mockResolvedValueOnce(user).mockRejectedValueOnce(new Error('unauthorized'));
    const store = useAuthStore();

    await store.fetchMe();
    expect(store.user).toEqual(user);

    await expect(store.fetchMe()).rejects.toThrow('unauthorized');
    expect(store.user).toBeNull();
  });

  it('退出登录后清除用户', async () => {
    vi.mocked(authApi.fetchMe).mockResolvedValue({ id: 1, username: 'admin', fullName: '管理员', permissions: [] });
    const store = useAuthStore();
    await store.fetchMe();

    await store.logout();
    expect(store.user).toBeNull();
    expect(store.loaded).toBe(false);
  });
});
