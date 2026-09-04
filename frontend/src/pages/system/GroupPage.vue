<template>
  <div class="space-y-4">
    <PageHeader :title="locale.t('system.groups.title')" :description="locale.t('system.groups.description')">
      <template #actions>
        <el-button v-if="canWrite" type="primary" @click="openCreate">{{ locale.t('common.create') }}</el-button>
      </template>
    </PageHeader>

    <div class="rounded-lg border bg-card p-4 shadow-sm">
      <el-form inline @submit.prevent="load">
        <el-form-item :label="locale.t('suppliers.keyword')">
          <el-input v-model="filters.keyword" clearable class="w-56" @keyup.enter="load" />
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
        <el-table-column prop="groupCode" :label="locale.t('system.groups.groupCode')" min-width="140" fixed="left" />
        <el-table-column prop="groupName" :label="locale.t('system.groups.groupName')" min-width="160" />
        <el-table-column prop="description" :label="locale.t('system.groups.descriptionLabel')" min-width="220" show-overflow-tooltip />
        <el-table-column :label="locale.t('suppliers.enabledStatus')" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? locale.t('common.enabled') : locale.t('common.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="locale.t('system.groups.permissions')" min-width="260">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1">
              <el-tag v-for="code in row.permissionCodes" :key="code" size="small" class="rounded-sm">{{ code }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="canWrite" :label="locale.t('common.actions')" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">{{ locale.t('common.edit') }}</el-button>
            <el-button link type="primary" @click="openPermissions(row)">
              {{ locale.t('system.groups.permissions') }}
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

    <el-dialog v-model="dialogVisible" :title="editingId ? locale.t('common.edit') : locale.t('common.create')" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item :label="locale.t('system.groups.groupCode')" prop="groupCode">
          <el-input v-model="form.groupCode" />
        </el-form-item>
        <el-form-item :label="locale.t('system.groups.groupName')" prop="groupName">
          <el-input v-model="form.groupName" />
        </el-form-item>
        <el-form-item :label="locale.t('system.groups.descriptionLabel')">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="locale.t('suppliers.enabledStatus')">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ locale.t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="save">{{ locale.t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionDialogVisible" :title="locale.t('system.groups.permissions')" width="560px">
      <el-checkbox-group v-model="selectedPermissionIds" class="grid grid-cols-1 gap-2 md:grid-cols-2">
        <el-checkbox v-for="permission in permissions" :key="permission.id" :value="permission.id">
          <span class="text-sm">{{ permission.permissionName }}</span>
          <span class="ml-1 text-xs text-muted-foreground">{{ permission.permissionCode }}</span>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">{{ locale.t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="savePermissions">{{ locale.t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';

import { groupApi, permissionApi } from '../../api';
import type { Group, Permission } from '../../api/types';
import PageHeader from '../../components/PageHeader.vue';
import { useAuthStore } from '../../stores/auth';
import { useLocaleStore } from '../../stores/locale';

const locale = useLocaleStore();
const auth = useAuthStore();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const records = ref<Group[]>([]);
const permissions = ref<Permission[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const dialogVisible = ref(false);
const permissionDialogVisible = ref(false);
const editingId = ref<number | null>(null);
const permissionGroupId = ref<number | null>(null);
const selectedPermissionIds = ref<number[]>([]);
const formRef = ref<FormInstance>();
const filters = reactive({ keyword: '', enabled: undefined as boolean | undefined });
const form = reactive({
  groupCode: '',
  groupName: '',
  description: '',
  enabled: true
});

const canWrite = computed(() => auth.hasPermission('group:write'));
const rules = computed<FormRules>(() => ({
  groupCode: [{ required: true, message: locale.t('common.required'), trigger: 'blur' }],
  groupName: [{ required: true, message: locale.t('common.required'), trigger: 'blur' }]
}));

async function load() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await groupApi.page({
      keyword: filters.keyword || undefined,
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

async function loadPermissions() {
  permissions.value = await permissionApi.list();
}

function reset() {
  filters.keyword = '';
  filters.enabled = undefined;
  page.value = 1;
  load();
}

function openCreate() {
  editingId.value = null;
  Object.assign(form, { groupCode: '', groupName: '', description: '', enabled: true });
  dialogVisible.value = true;
}

function openEdit(group: Group) {
  editingId.value = group.id;
  Object.assign(form, {
    groupCode: group.groupCode,
    groupName: group.groupName,
    description: group.description ?? '',
    enabled: group.enabled
  });
  dialogVisible.value = true;
}

async function save() {
  if (!formRef.value || !(await formRef.value.validate().catch(() => false))) {
    return;
  }
  saving.value = true;
  try {
    const data = { ...form, description: form.description || null };
    if (editingId.value === null) {
      await groupApi.create(data);
    } else {
      await groupApi.update(editingId.value, data);
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

function openPermissions(group: Group) {
  permissionGroupId.value = group.id;
  selectedPermissionIds.value = [...group.permissionIds];
  permissionDialogVisible.value = true;
}

async function savePermissions() {
  if (permissionGroupId.value === null) {
    return;
  }
  saving.value = true;
  try {
    await groupApi.assignPermissions(permissionGroupId.value, selectedPermissionIds.value);
    permissionDialogVisible.value = false;
    ElMessage.success(locale.t('common.success'));
    await load();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : locale.t('common.requestFailed'));
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  await Promise.all([load(), loadPermissions()]);
});
</script>
