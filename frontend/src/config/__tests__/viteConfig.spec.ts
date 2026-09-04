// @vitest-environment node

import { describe, expect, it } from 'vitest';

import viteConfig from '../../../vite.config';

describe('Vite 配置', () => {
  it('开发环境通过同域代理访问后端', () => {
    const configuration = viteConfig as { server?: { proxy?: Record<string, { target: string }> } };

    expect(configuration.server?.proxy?.['/api']?.target).toBe('http://localhost:8080');
    expect(configuration.server?.proxy?.['/v3/api-docs']?.target).toBe('http://localhost:8080');
  });
});
