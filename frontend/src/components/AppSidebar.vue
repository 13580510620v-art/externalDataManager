<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import DatabaseIcon from '../assets/DatabaseIcon.svg';
import DataIcon from '../assets/dashboard/DataIcon.svg';
import LogIcon from '../assets/dashboard/LogIcon.svg';
import DashboardIcon from '../assets/dashboard/DashboardIcon.svg';
import SupplierIcon from '../assets/dashboard/SupplierIcon.svg';
import SystemIcon from '../assets/dashboard/SystemIcon.svg';
import { useAuthStore } from '../stores/auth';
import { useLocaleStore } from '../stores/locale';

defineProps<{ collapsed?: boolean }>();
const route = useRoute();
const auth = useAuthStore();
const locale = useLocaleStore();
const systemOpen = ref(false);

const navigation = computed(() => [
  { name: 'dashboard', path: '/dashboard', label: locale.t('nav.dashboard'), permission: 'dashboard:read', icon: DashboardIcon },
  { name: 'suppliers', path: '/suppliers', label: locale.t('nav.suppliers'), permission: 'supplier:read', icon: SupplierIcon },
  { name: 'tasks', path: '/tasks', label: locale.t('nav.tasks'), permission: 'task:read', icon: DataIcon },
  { name: 'logs', path: '/logs', label: locale.t('nav.logs'), permission: 'audit:read', icon: LogIcon }
].filter(item => auth.hasPermission(item.permission)));

const systemItems = computed(() => [
  { name: 'system-users', path: '/system/users', label: locale.t('nav.users'), permission: 'user:read' },
  { name: 'system-groups', path: '/system/groups', label: locale.t('nav.groups'), permission: 'group:read' },
  { name: 'system-permissions', path: '/system/permissions', label: locale.t('nav.permissions'), permission: 'permission:read' }
].filter(item => auth.hasPermission(item.permission)));

const systemActive = computed(() => systemItems.value.some(item => route.name === item.name));

watch(systemActive, active => {
  if (active) {
    systemOpen.value = true;
  }
}, { immediate: true });
</script>

<template>
  <aside
    class="hidden shrink-0 border-r border-border bg-card transition-all md:block"
    :class="collapsed ? 'w-0 overflow-hidden' : 'w-56'"
  >
    <div class="flex items-center gap-3 border-b border-border px-[17px] py-[14px]">
      <div class="grid h-7 w-5 shrink-0 place-items-center rounded-[7px] bg-primary">
        <img :src="DatabaseIcon" alt="" class="size-3.5" />
      </div>
      <div class="min-w-0">
        <p class="truncate text-xs font-semibold leading-4">{{ locale.t('app.brandTitle') }}</p>
        <p class="text-xs text-muted-foreground">{{ locale.t('app.subtitle') }}</p>
      </div>
    </div>
    <nav class="flex flex-col gap-1 p-3">
      <RouterLink
        v-for="item in navigation"
        :key="item.name"
        :to="item.path"
        class="flex h-7 items-center gap-3 rounded-[5px] px-2.5 text-xs transition-colors"
        :class="route.name === item.name
          ? 'bg-primary text-primary-foreground'
          : 'text-foreground hover:bg-muted'"
      >
        <img :src="item.icon" alt="" class="size-3.5 shrink-0" />
        <span class="truncate">{{ item.label }}</span>
      </RouterLink>

      <template v-if="systemItems.length > 0">
        <button
          type="button"
          class="flex h-7 items-center gap-3 rounded-[5px] px-2.5 text-xs transition-colors"
          :class="systemActive ? 'bg-primary text-primary-foreground' : 'text-foreground hover:bg-muted'"
          @click="systemOpen = !systemOpen"
        >
          <img :src="SystemIcon" alt="" class="size-3.5 shrink-0" />
          <span class="truncate">{{ locale.t('nav.system') }}</span>
        </button>
        <div v-if="systemOpen" class="ml-6 flex flex-col gap-1 border-l border-border pl-3">
          <RouterLink
            v-for="item in systemItems"
            :key="item.name"
            :to="item.path"
            class="rounded-[5px] px-2.5 py-1.5 text-xs transition-colors"
            :class="route.name === item.name ? 'bg-primary text-primary-foreground' : 'text-foreground hover:bg-muted'"
          >
            {{ item.label }}
          </RouterLink>
        </div>
      </template>
    </nav>
  </aside>
</template>
