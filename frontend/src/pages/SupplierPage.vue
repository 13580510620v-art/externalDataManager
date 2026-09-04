<template>
  <div class="space-y-4">
    <PageHeader :title="locale.t('suppliers.title')" :description="locale.t('suppliers.description')">
      <template #actions>
        <el-button v-if="canWrite" type="primary" @click="openCreate">
          {{ locale.t('common.create') }}
        </el-button>
      </template>
    </PageHeader>

    <div class="rounded-lg border bg-card p-4 shadow-sm">
      <el-form inline class="flex flex-wrap gap-3" @submit.prevent="load">
        <el-form-item :label="locale.t('suppliers.keyword')">
          <el-input v-model="filters.keyword" clearable class="w-56" @keyup.enter="load" />
        </el-form-item>
        <el-form-item :label="locale.t('suppliers.fetcherType')">
          <el-select v-model="filters.fetcherType" clearable class="w-32">
            <el-option label="SFTP" value="SFTP" />
            <el-option label="REST" value="REST" />
          </el-select>
        </el-form-item>
        <el-form-item :label="locale.t('suppliers.enabledStatus')">
          <el-select v-model="filters.enabled" clearable class="w-28">
            <el-option :label="locale.t('common.enabled')" :value="true" />
            <el-option :label="locale.t('common.disabled')" :value="false" />
          </el-select>
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
        <el-table-column prop="supplierCode" :label="locale.t('suppliers.supplierCode')" min-width="140" fixed="left" />
        <el-table-column prop="supplierName" :label="locale.t('suppliers.supplierName')" min-width="180" />
        <el-table-column prop="fetcherType" :label="locale.t('suppliers.fetcherType')" width="100" />
        <el-table-column prop="remoteSubDir" :label="locale.t('suppliers.remoteSubDir')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updateFrequency" :label="locale.t('suppliers.updateFrequency')" width="150" />
        <el-table-column prop="s3Bucket" :label="locale.t('suppliers.s3Bucket')" min-width="160" show-overflow-tooltip />
        <el-table-column :label="locale.t('suppliers.enabledStatus')" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? locale.t('common.enabled') : locale.t('common.disabled') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" :label="locale.t('tasks.fields.updateTime')" min-width="165" show-overflow-tooltip />
        <el-table-column v-if="canWrite" :label="locale.t('common.actions')" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">{{ locale.t('common.edit') }}</el-button>
            <el-button v-if="row.enabled" link type="danger" @click="setEnabled(row, false)">
              {{ locale.t('common.disable') }}
            </el-button>
            <el-button v-else link type="success" @click="setEnabled(row, true)">
              {{ locale.t('common.enable') }}
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

    <el-dialog v-model="dialogVisible" :title="editingId ? locale.t('common.edit') : locale.t('common.create')" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="150px">
        <el-form-item :label="locale.t('suppliers.supplierCode')" prop="supplierCode">
          <el-input v-model="form.supplierCode" />
        </el-form-item>
        <el-form-item :label="locale.t('suppliers.supplierName')" prop="supplierName">
          <el-input v-model="form.supplierName" />
        </el-form-item>
        <el-form-item :label="locale.t('suppliers.fetcherType')" prop="fetcherType">
          <el-select v-model="form.fetcherType" class="w-full">
            <el-option label="SFTP" value="SFTP" />
            <el-option label="REST" value="REST" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.fetcherType === 'SFTP'" :label="locale.t('suppliers.remoteSubDir')">
          <el-input v-model="form.remoteSubDir" />
        </el-form-item>
        <el-form-item :label="locale.t('suppliers.updateFrequency')" prop="updateFrequency">
          <el-input-number v-model="form.updateFrequency" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item :label="locale.t('suppliers.fileNameRule')">
          <el-input v-model="form.fileNameRule" />
        </el-form-item>
        <el-form-item :label="locale.t('suppliers.s3Bucket')">
          <el-input v-model="form.s3Bucket" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ locale.t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="save">{{ locale.t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';

import { supplierApi } from '../api';
import type { Supplier } from '../api/types';
import PageHeader from '../components/PageHeader.vue';
import { useAuthStore } from '../stores/auth';
import { useLocaleStore } from '../stores/locale';

const locale = useLocaleStore();
const auth = useAuthStore();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const records = ref<Supplier[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInstance>();
const filters = reactive({ keyword: '', fetcherType: '', enabled: undefined as boolean | undefined });
const form = reactive({
  supplierCode: '',
  supplierName: '',
  fetcherType: 'SFTP' as 'SFTP' | 'REST',
  remoteSubDir: '',
  updateFrequency: 1,
  fileNameRule: '',
  s3Bucket: ''
});

const canWrite = computed(() => auth.hasPermission('supplier:write'));
const rules = computed<FormRules>(() => ({
  supplierCode: [{ required: true, message: locale.t('common.required'), trigger: 'blur' }],
  supplierName: [{ required: true, message: locale.t('common.required'), trigger: 'blur' }],
  fetcherType: [{ required: true, message: locale.t('common.required'), trigger: 'change' }],
  updateFrequency: [
    { required: true, message: locale.t('common.required'), trigger: 'change' },
    { type: 'number', min: 1, message: locale.t('common.frequencyInvalid'), trigger: 'change' }
  ]
}));

async function load() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await supplierApi.page({
      keyword: filters.keyword || undefined,
      fetcherType: filters.fetcherType || undefined,
      enabled: filters.enabled,
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
  filters.keyword = '';
  filters.fetcherType = '';
  filters.enabled = undefined;
  page.value = 1;
  load();
}

function openCreate() {
  editingId.value = null;
  Object.assign(form, {
    supplierCode: '',
    supplierName: '',
    fetcherType: 'SFTP',
    remoteSubDir: '',
    updateFrequency: 1,
    fileNameRule: '',
    s3Bucket: ''
  });
  dialogVisible.value = true;
}

function openEdit(supplier: Supplier) {
  editingId.value = supplier.id;
  Object.assign(form, {
    supplierCode: supplier.supplierCode,
    supplierName: supplier.supplierName,
    fetcherType: supplier.fetcherType,
    remoteSubDir: supplier.remoteSubDir ?? '',
    updateFrequency: supplier.updateFrequency ?? 1,
    fileNameRule: supplier.fileNameRule ?? '',
    s3Bucket: supplier.s3Bucket ?? ''
  });
  dialogVisible.value = true;
}

async function save() {
  if (!formRef.value || !(await formRef.value.validate().catch(() => false))) {
    return;
  }
  saving.value = true;
  try {
    const data = {
      ...form,
      remoteSubDir: form.fetcherType === 'REST' ? null : form.remoteSubDir || null,
      fileNameRule: form.fileNameRule || null,
      s3Bucket: form.s3Bucket || null
    };
    if (editingId.value === null) {
      await supplierApi.create(data);
    } else {
      await supplierApi.update(editingId.value, data);
    }
    dialogVisible.value = false;
    ElMessage.success(locale.t('common.success'));
    await load();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : locale.t('common.requestFailed'));
  } finally {
    saving.value = false;
  }
}

async function setEnabled(supplier: Supplier, enabled: boolean) {
  if (!enabled) {
    try {
      await ElMessageBox.confirm(locale.t('suppliers.disableConfirm'), locale.t('common.confirm'), { type: 'warning' });
    } catch {
      return;
    }
  }
  try {
    if (enabled) {
      await supplierApi.enable(supplier.id);
    } else {
      await supplierApi.disable(supplier.id);
    }
    ElMessage.success(locale.t('common.success'));
    await load();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : locale.t('common.requestFailed'));
  }
}

onMounted(load);
</script>
