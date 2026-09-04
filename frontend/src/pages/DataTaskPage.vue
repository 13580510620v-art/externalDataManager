<template>
  <div class="space-y-4">
    <PageHeader :title="locale.t('tasks.title')" :description="locale.t('tasks.description')" />

    <div class="rounded-lg border bg-card p-4 shadow-sm">
      <el-form inline class="flex flex-wrap gap-3" @submit.prevent="load">
        <el-form-item :label="locale.t('tasks.supplier')">
          <el-input v-model="filters.supplierCode" clearable class="w-44" @keyup.enter="load" />
        </el-form-item>
        <el-form-item :label="locale.t('tasks.status')">
          <el-select v-model="filters.status" clearable class="w-40">
            <el-option
              v-for="status in taskStatuses"
              :key="status"
              :value="status"
              :label="locale.t(`status.${status}`)"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="locale.t('tasks.fileName')">
          <el-input v-model="filters.fileName" clearable class="w-52" @keyup.enter="load" />
        </el-form-item>
        <el-form-item :label="locale.t('tasks.feedback')">
          <el-select v-model="filters.feedbackFlag" clearable class="w-28">
            <el-option label="Y" value="Y" />
            <el-option label="N" value="N" />
          </el-select>
        </el-form-item>
        <el-form-item :label="locale.t('tasks.timeRange')">
          <el-date-picker
            v-model="filters.timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DDTHH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">{{ locale.t('common.search') }}</el-button>
          <el-button @click="reset">{{ locale.t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-alert v-if="errorMessage" type="error" show-icon :closable="false" :title="errorMessage" />

    <div class="rounded-lg border bg-card shadow-sm">
      <el-table v-loading="loading" :data="records" class="w-full">
        <el-table-column prop="id" label="ID" width="80" fixed="left" />
        <el-table-column prop="supplierCode" :label="locale.t('tasks.fields.supplierCode')" min-width="130" />
        <el-table-column
          prop="sourceFileName"
          :label="locale.t('tasks.fields.sourceFileName')"
          min-width="220"
          show-overflow-tooltip
        />
        <el-table-column :label="locale.t('tasks.fields.status')" width="120">
          <template #default="{ row }">
            <StatusBadge :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="downloadRetryTimes" :label="locale.t('tasks.fields.downloadRetryTimes')" width="110" />
        <el-table-column prop="updateRetryTimes" :label="locale.t('tasks.fields.updateRetryTimes')" width="110" />
        <el-table-column prop="informRetryTimes" :label="locale.t('tasks.fields.informRetryTimes')" width="110" />
        <el-table-column
          prop="feedbackFlag"
          :label="locale.t('tasks.fields.feedbackFlag')"
          width="90"
        />
        <el-table-column
          prop="createTime"
          :label="locale.t('tasks.fields.createTime')"
          min-width="165"
          show-overflow-tooltip
        />
        <el-table-column :label="locale.t('common.actions')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">
              {{ locale.t('common.detail') }}
            </el-button>
            <el-button
              v-if="canRetry(row.status)"
              link
              type="warning"
              @click="retry(row)"
            >
              {{ locale.t('common.retry') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end p-4">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @current-change="load"
          @size-change="load"
        />
      </div>
    </div>

    <el-drawer v-model="detailVisible" size="480px" :title="locale.t('common.detail')">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item
          v-for="field in detailFields"
          :key="field.key"
          :label="field.label"
        >
          {{ detail[field.key] ?? '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

import { taskApi } from '../api';
import type { DataTask, DataTaskDetail, DataTaskStatus } from '../api/types';
import PageHeader from '../components/PageHeader.vue';
import StatusBadge from '../components/StatusBadge.vue';
import { useAuthStore } from '../stores/auth';
import { useLocaleStore } from '../stores/locale';

const locale = useLocaleStore();
const auth = useAuthStore();
const loading = ref(false);
const errorMessage = ref('');
const records = ref<DataTask[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const detailVisible = ref(false);
const detail = ref<DataTaskDetail | null>(null);
const filters = reactive({
  supplierCode: '',
  status: '',
  fileName: '',
  feedbackFlag: '',
  timeRange: [] as string[]
});

const taskStatuses: DataTaskStatus[] = [
  'PENDING',
  'DOWNLOADING',
  'DOWNLOAD_FAILED',
  'UPLOADED',
  'UPLOAD_FAILED',
  'INFORMED',
  'INFORM_FAILED',
  'COMPLETED'
];

const detailFields = computed(() => [
  { key: 'id', label: locale.t('tasks.fields.id') },
  { key: 'supplierCode', label: locale.t('tasks.fields.supplierCode') },
  { key: 'sourceUniqueKey', label: locale.t('tasks.fields.sourceUniqueKey') },
  { key: 'sourceFileName', label: locale.t('tasks.fields.sourceFileName') },
  { key: 'sourceRemotePath', label: locale.t('tasks.fields.sourceRemotePath') },
  { key: 'sourceFileSize', label: locale.t('tasks.fields.sourceFileSize') },
  { key: 'fetcherType', label: locale.t('tasks.fields.fetcherType') },
  { key: 'downloadRetryTimes', label: locale.t('tasks.fields.downloadRetryTimes') },
  { key: 'updateRetryTimes', label: locale.t('tasks.fields.updateRetryTimes') },
  { key: 'informRetryTimes', label: locale.t('tasks.fields.informRetryTimes') },
  { key: 's3Bucket', label: locale.t('tasks.fields.s3Bucket') },
  { key: 'targetS3Key', label: locale.t('tasks.fields.targetS3Key') },
  { key: 'feedbackFlag', label: locale.t('tasks.fields.feedbackFlag') },
  { key: 'createTime', label: locale.t('tasks.fields.createTime') },
  { key: 'updateTime', label: locale.t('tasks.fields.updateTime') }
] as const);

function canRetry(status: DataTaskStatus) {
  return auth.hasPermission('task:retry') && status.endsWith('_FAILED');
}

async function load() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await taskApi.page({
      supplierCode: filters.supplierCode || undefined,
      status: filters.status || undefined,
      fileName: filters.fileName || undefined,
      feedbackFlag: filters.feedbackFlag || undefined,
      startTime: filters.timeRange?.[0],
      endTime: filters.timeRange?.[1],
      page: page.value,
      size: size.value
    });
    records.value = result.records;
    total.value = Number(result.total);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : locale.t('common.requestFailed');
  } finally {
    loading.value = false;
  }
}

function reset() {
  filters.supplierCode = '';
  filters.status = '';
  filters.fileName = '';
  filters.feedbackFlag = '';
  filters.timeRange = [];
  page.value = 1;
  load();
}

async function openDetail(id: number) {
  try {
    detail.value = await taskApi.detail(id);
    detailVisible.value = true;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : locale.t('common.requestFailed'));
  }
}

async function retry(task: DataTask) {
  try {
    await ElMessageBox.confirm(locale.t('tasks.retryConfirm'), locale.t('common.confirm'), { type: 'warning' });
  } catch {
    return;
  }
  try {
    await taskApi.retry(task.id);
    ElMessage.success(locale.t('common.success'));
    await load();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : locale.t('common.requestFailed'));
  }
}

onMounted(load);
</script>
