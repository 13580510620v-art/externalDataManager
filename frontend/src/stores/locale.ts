import { ref } from 'vue';
import { defineStore } from 'pinia';

import en from '../locales/en';
import zhCN from '../locales/zh-CN';
import zhTW from '../locales/zh-TW';

export type Locale = 'zh-CN' | 'zh-TW' | 'en';

const messages = {
  'zh-CN': zhCN,
  'zh-TW': zhTW,
  en
};

const storageKey = 'edm-locale';

function initialLocale(): Locale {
  const saved = window.localStorage.getItem(storageKey);
  return saved === 'zh-TW' || saved === 'en' ? saved : 'zh-CN';
}

export const useLocaleStore = defineStore('locale', () => {
  const locale = ref<Locale>(initialLocale());

  function setLocale(nextLocale: Locale) {
    locale.value = nextLocale;
    window.localStorage.setItem(storageKey, nextLocale);
    document.documentElement.lang = nextLocale;
  }

  function translate(message: Record<string, unknown>, path: string): unknown {
    return path.split('.').reduce<unknown>((current, key) => {
      if (current && typeof current === 'object' && key in current) {
        return (current as Record<string, unknown>)[key];
      }
      return undefined;
    }, message);
  }

  function t(path: string) {
    const value = translate(messages[locale.value], path) ?? translate(messages['zh-CN'], path);
    return typeof value === 'string' ? value : path;
  }

  return { locale, setLocale, t };
});
