<template>
  <div class="space-y-6">
    <PageHeader :title="locale.t('dashboard.title')" :description="locale.t('dashboard.description')" />

    <el-alert v-if="errorMessage" type="error" show-icon :closable="false" :title="errorMessage" />

    <div v-loading="loading" class="grid grid-cols-1 gap-5 md:grid-cols-2 xl:grid-cols-3">
      <section class="rounded-lg border border-border bg-card p-5 shadow-sm">
        <div class="flex items-start justify-between gap-3">
          <div class="grid size-11 place-items-center rounded-lg bg-primary/10">
            <img :src="TotalFileIcon" alt="" class="size-5" />
          </div>
          <span class="flex items-center gap-1 rounded-full bg-emerald-50 px-2 py-1 text-xs font-medium text-emerald-700">
            <img :src="TrendUpIcon" alt="" class="size-2.5" />
            {{ formatChange(summary?.totalChange ?? 0) }}
          </span>
        </div>
        <p class="mt-4 text-xs text-muted-foreground">{{ locale.t('dashboard.total') }}</p>
        <p class="mt-1 text-2xl font-semibold">{{ summary?.total ?? 0 }} <span class="text-xs font-normal text-muted-foreground">{{ locale.t('dashboard.items') }}</span></p>
        <p class="mt-1 text-xs text-muted-foreground">{{ locale.t('dashboard.comparedLastWeek') }}</p>
      </section>

      <section class="rounded-lg border border-border bg-card p-5 shadow-sm">
        <div class="flex items-start justify-between gap-3">
          <div class="grid size-11 place-items-center rounded-lg bg-emerald-50">
            <img :src="SuccessIcon" alt="" class="size-5" />
          </div>
          <span class="flex items-center gap-1 rounded-full bg-emerald-50 px-2 py-1 text-xs font-medium text-emerald-700">
            <img :src="TrendUpIcon" alt="" class="size-2.5" />
            {{ successRate }}
          </span>
        </div>
        <p class="mt-4 text-xs text-muted-foreground">{{ locale.t('dashboard.completed') }}</p>
        <p class="mt-1 text-2xl font-semibold">{{ summary?.completed ?? 0 }} <span class="text-xs font-normal text-muted-foreground">{{ locale.t('dashboard.items') }}</span></p>
        <p class="mt-1 text-xs text-muted-foreground">{{ locale.t('dashboard.successRate') }}</p>
      </section>

      <section class="rounded-lg border border-border bg-card p-5 shadow-sm">
        <div class="flex items-start justify-between gap-3">
          <div class="grid size-11 place-items-center rounded-lg bg-rose-50">
            <img :src="FailureIcon" alt="" class="size-5" />
          </div>
          <span
            class="flex items-center gap-1 rounded-full px-2 py-1 text-xs font-medium"
            :class="(summary?.failureChange ?? 0) > 0 ? 'bg-rose-50 text-rose-700' : 'bg-emerald-50 text-emerald-700'"
          >
            <img :src="(summary?.failureChange ?? 0) > 0 ? TrendDownIcon : TrendUpIcon" alt="" class="size-2.5" />
            {{ formatChange(summary?.failureChange ?? 0) }}
          </span>
        </div>
        <p class="mt-4 text-xs text-muted-foreground">{{ locale.t('dashboard.failed') }}</p>
        <p class="mt-1 text-2xl font-semibold">{{ failedCount }} <span class="text-xs font-normal text-muted-foreground">{{ locale.t('dashboard.items') }}</span></p>
        <p class="mt-1 text-xs text-muted-foreground">{{ locale.t('dashboard.comparedLastWeek') }}</p>
      </section>
    </div>

    <section class="overflow-hidden rounded-lg border border-border bg-card shadow-sm">
      <div class="flex min-h-14 items-center justify-between gap-3 border-b border-border px-5 py-4">
        <h2 class="flex items-center gap-2 text-base font-semibold">
          <img :src="ClockIcon" alt="" class="size-4" />
          {{ locale.t('dashboard.recentRecords') }}
        </h2>
        <span class="text-xs text-muted-foreground">{{ locale.t('common.total') }} {{ summary?.recentTasks.length ?? 0 }}</span>
      </div>

      <div v-loading="loading" class="divide-y divide-border">
        <article
          v-for="task in summary?.recentTasks ?? []"
          :key="task.id"
          class="flex flex-col gap-3 px-5 py-4 sm:flex-row sm:items-start sm:gap-4"
        >
          <div class="grid size-8 shrink-0 place-items-center rounded-lg bg-primary/10">
            <img :src="ListFileIcon" alt="" class="size-4" />
          </div>
          <div class="min-w-0 flex-1 space-y-2">
            <div class="flex flex-wrap items-center gap-2">
              <p class="truncate text-sm font-medium">{{ task.sourceFileName }}</p>
              <StatusBadge :status="task.status" />
            </div>
            <div class="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-muted-foreground">
              <span class="flex items-center gap-1.5">
                <img :src="SupplierMetaIcon" alt="" class="size-2.5" />
                {{ task.supplierName }}
              </span>
              <span>{{ formatFileSize(task.sourceFileSize) }}</span>
              <span class="flex items-center gap-1.5">
                <img :src="TimeIcon" alt="" class="size-2.5" />
                {{ formatDateTime(task.createTime) }}
              </span>
            </div>
          </div>
        </article>

        <p v-if="!loading && (summary?.recentTasks.length ?? 0) === 0" class="px-5 py-10 text-center text-sm text-muted-foreground">
          {{ locale.t('dashboard.emptyRecords') }}
        </p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

import ClockIcon from '../assets/dashboard/ClockIcon.svg';
import FailureIcon from '../assets/dashboard/FailureIcon.svg';
import ListFileIcon from '../assets/dashboard/ListFileIcon.svg';
import SuccessIcon from '../assets/dashboard/SuccessIcon.svg';
import SupplierMetaIcon from '../assets/dashboard/SupplierMetaIcon.svg';
import TimeIcon from '../assets/dashboard/TimeIcon.svg';
import TotalFileIcon from '../assets/dashboard/TotalFileIcon.svg';
import TrendDownIcon from '../assets/dashboard/TrendDownIcon.svg';
import TrendUpIcon from '../assets/dashboard/TrendUpIcon.svg';
import { dashboardApi } from '../api';
import type { DashboardSummary } from '../api/types';
import PageHeader from '../components/PageHeader.vue';
import StatusBadge from '../components/StatusBadge.vue';
import { useLocaleStore } from '../stores/locale';

const locale = useLocaleStore();
const loading = ref(false);
const errorMessage = ref('');
const summary = ref<DashboardSummary | null>(null);

const successRate = computed(() => `${((summary.value?.successRate ?? 0) * 100).toFixed(1)}%`);
const failedCount = computed(() => {
  const counts = summary.value?.statusCounts ?? {};
  return (counts.DOWNLOAD_FAILED ?? 0) + (counts.UPLOAD_FAILED ?? 0) + (counts.INFORM_FAILED ?? 0);
});

function formatChange(value: number) {
  if (value > 0) {
    return `+${value}`;
  }
  return String(value);
}

function formatFileSize(value?: number | null) {
  if (!value) {
    return locale.t('common.noData');
  }
  const megabyte = 1024 * 1024;
  if (value < megabyte) {
    return `${(value / 1024).toFixed(1)} KB`;
  }
  return `${(value / megabyte).toFixed(1)} MB`;
}

function formatDateTime(value?: string | null) {
  if (!value) {
    return locale.t('common.noData');
  }
  return new Date(value).toLocaleString(locale.locale, {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  });
}

onMounted(async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    summary.value = await dashboardApi.today();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : locale.t('common.requestFailed');
  } finally {
    loading.value = false;
  }
});
</script>
