import { describe, expect, it } from 'vitest';

import router from '../index';

describe('路由', () => {
  it('包含管理端页面并声明权限', () => {
    expect(router.resolve('/login').name).toBe('login');
    expect(router.resolve('/dashboard').name).toBe('dashboard');
    expect(router.resolve('/tasks').name).toBe('tasks');
    expect(router.resolve('/suppliers').name).toBe('suppliers');
    expect(router.resolve('/system/users').name).toBe('system-users');
    expect(router.resolve('/system/groups').name).toBe('system-groups');
    expect(router.resolve('/system/permissions').name).toBe('system-permissions');
  });

  it('管理页面声明后端权限编码', () => {
    expect(router.resolve('/dashboard').meta.permission).toBe('dashboard:read');
    expect(router.resolve('/tasks').meta.permission).toBe('task:read');
    expect(router.resolve('/suppliers').meta.permission).toBe('supplier:read');
    expect(router.resolve('/system/users').meta.permission).toBe('user:read');
    expect(router.resolve('/system/groups').meta.permission).toBe('group:read');
    expect(router.resolve('/system/permissions').meta.permission).toBe('permission:read');
  });
});
