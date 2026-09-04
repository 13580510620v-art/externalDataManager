import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';

import { useLocaleStore } from '../locale';

describe('多语言状态', () => {
  const storage = new Map<string, string>();

  beforeEach(() => {
    storage.clear();
    Object.defineProperty(window, 'localStorage', {
      configurable: true,
      value: {
        getItem: (key: string) => storage.get(key) ?? null,
        setItem: (key: string, value: string) => storage.set(key, value),
        removeItem: (key: string) => storage.delete(key),
        clear: () => storage.clear()
      }
    });
    setActivePinia(createPinia());
  });

  it('默认使用简体中文并可切换语言', () => {
    const store = useLocaleStore();

    expect(store.locale).toBe('zh-CN');
    expect(store.t('app.name')).toBe('外部数据管理平台');

    store.setLocale('en');
    expect(store.locale).toBe('en');
    expect(store.t('app.name')).toBe('External Data Manager');
    expect(storage.get('edm-locale')).toBe('en');
  });

  it('缺失翻译回退到简体中文', () => {
    const store = useLocaleStore();

    store.setLocale('zh-TW');
    expect(store.t('app.name')).toBe('外部數據管理平台');
  });
});
