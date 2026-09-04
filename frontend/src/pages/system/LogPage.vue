<template>
  <div class="space-y-4">
    <PageHeader :title="locale.t('nav.logs')" :description="locale.t('logs.description')" />

    <div class="rounded-lg border bg-card shadow-sm">
      <div class="flex flex-wrap items-center gap-3 border-b border-border p-4">
        <el-input
          v-model="keyword"
          class="w-full sm:w-72"
          clearable
          :placeholder="locale.t('common.search')"
          @keyup.enter="loadLogs(1)"
        />
        <el-button type="primary" :loading="loading" @click="loadLogs(1)">
          {{ locale.t('common.search') }}
        </el-button>
      </div>

      <el-alert v-if="errorMessage" type="error" show-icon :closable="false" :title="errorMessage" class="m-4" />

      <el-table v-loading="loading" :data="logs" min-width="860">
        <el-table-column prop="operator" :label="locale.t('logs.fields.operator')" min-width="120" />
        <el-table-column prop="action" :label="locale.t('logs.fields.action')" min-width="160" />
        <el-table-column prop="targetType" :label="locale.t('logs.fields.targetType')" min-width="120" />
        <el-table-column prop="targetId" :label="locale.t('logs.fields.targetId')" min-width="100" />
        <el-table-column prop="detail" :label="locale.t('logs.fields.detail')" min-width="220" show-overflow-tooltip />
        <el-table-column :label="locale.t('logs.fields.createTime')" min-width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end p-4">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          background
          layout="total, prev, pager, next"
          :total="total"
          @current-change="loadLogs()"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { auditLogApi } from '../../api';
import type { AuditLog } from '../../api/types';
import PageHeader from '../../components/PageHeader.vue';
import { useLocaleStore } from '../../stores/locale';

const locale = useLocaleStore();
const loading = ref(false);
const errorMessage = ref('');
const logs = ref<AuditLog[]>([]);
const keyword = ref('');
const page = ref(1);
const size = ref(10);
const total = ref(0);

async function loadLogs(nextPage?: number) {
  if (typeof nextPage === 'number') {
    page.value = nextPage;
  }
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await auditLogApi.page({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined
    });
    logs.value = result.records;
    total.value = result.total;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : locale.t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

function formatTime(value?: string | null) {
  if (!value) {
    return locale.t('common.noData');
  }
  return new Date(value).toLocaleString(locale.locale, { hour12: false });
}

onMounted(() => loadLogs());
</script>
