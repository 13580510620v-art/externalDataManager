<script setup lang="ts">
import { storeToRefs } from 'pinia';

import BellIcon from '../assets/dashboard/BellIcon.svg';
import ConsoleIcon from '../assets/dashboard/ConsoleIcon.svg';
import UserIcon from '../assets/dashboard/UserIcon.svg';
import { useAuthStore } from '../stores/auth';
import { useLocaleStore, type Locale } from '../stores/locale';

const emit = defineEmits<{ (event: 'toggle-sidebar'): void }>();

const auth = useAuthStore();
const localeStore = useLocaleStore();
const { locale } = storeToRefs(localeStore);

const localeOptions: Array<{ value: Locale; label: string }> = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'zh-TW', label: '繁體中文' },
  { value: 'en', label: 'English' }
];

async function handleLogout() {
  await auth.logout();
}
</script>

<template>
  <header class="sticky top-0 z-10 flex h-12 items-center justify-between border-b border-border bg-card px-3">
    <div class="flex min-w-0 items-center gap-3">
      <button
        type="button"
        class="grid size-6 place-items-center rounded-[5px] hover:bg-muted"
        aria-label="Toggle sidebar"
        @click="emit('toggle-sidebar')"
      >
        <span class="flex h-3.5 w-3.5 flex-col justify-center gap-[3px]">
          <span class="h-px bg-foreground"></span>
          <span class="h-px bg-foreground"></span>
        </span>
      </button>
      <div class="h-12 w-px bg-border"></div>
      <div class="flex items-center gap-1.5 rounded-[5px] bg-primary/10 px-2.5 py-1.5">
        <img :src="ConsoleIcon" alt="" class="size-3" />
        <span class="text-xs text-primary">{{ localeStore.t('nav.dashboard') }}</span>
      </div>
    </div>
    <div class="flex items-center gap-2">
      <el-select v-model="locale" size="small" class="w-32" @change="localeStore.setLocale(locale)">
        <el-option v-for="item in localeOptions" :key="item.value" :value="item.value" :label="item.label" />
      </el-select>
      <button
        type="button"
        class="grid size-7 place-items-center rounded-[5px] hover:bg-muted"
        disabled
        :title="localeStore.t('dashboard.notificationDisabled')"
        :aria-label="localeStore.t('dashboard.notificationDisabled')"
      >
        <img :src="BellIcon" alt="" class="size-4" />
      </button>
      <el-dropdown v-if="auth.user">
        <button class="grid size-7 place-items-center rounded-[5px] hover:bg-muted" :aria-label="auth.user.fullName">
          <img :src="UserIcon" alt="" class="size-4" />
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleLogout">{{ localeStore.t('common.logout') }}</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>
