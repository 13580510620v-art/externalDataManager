<template>
  <div class="space-y-4">
    <PageHeader :title="locale.t('system.users.title')" :description="locale.t('system.users.description')">
      <template #actions>
        <el-button v-if="canWrite" type="primary" @click="openCreate">{{ locale.t('common.create') }}</el-button>
      </template>
    </PageHeader>

    <div class="rounded-lg border bg-card p-4 shadow-sm">
      <el-form inline @submit.prevent="load">
        <el-form-item :label="locale.t('system.users.keyword')">
          <el-input v-model="filters.keyword" clearable class="w-56" @keyup.enter="load" />
        </el-form-item>
        <el-form-item :label="locale.t('suppliers.enabledStatus')">
          <el-select v-model="filters.enabled" clearable class="w-28">
            <el-option :label="locale.t('common.enabled')" :value="true" />
            <el-option :label="locale.t('common.disabled')" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item :label="locale.t('system.users.groups')">
          <el-select v-model="filters.groupId" clearable filterable class="w-48">
            <el-option v-for="group in groups" :key="group.id" :value="group.id" :label="group.groupName" />
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
        <el-table-column prop="username" :label="locale.t('system.users.username')" min-width="140" fixed="left" />
        <el-table-column prop="fullName" :label="locale.t('system.users.fullName')" min-width="140" />
        <el-table-column prop="email" :label="locale.t('system.users.email')" min-width="210" show-overflow-tooltip />
        <el-table-column prop="samlNameId" :label="locale.t('system.users.samlNameId')" min-width="180" show-overflow-tooltip />
        <el-table-column :label="locale.t('suppliers.enabledStatus')" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? locale.t('common.enabled') : locale.t('common.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="locale.t('system.users.groups')" min-width="200">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1">
              <el-tag v-for="groupId in row.groupIds" :key="groupId" size="small" class="rounded-sm">
                {{ groupName(groupId) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
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
        <el-form-item :label="locale.t('system.users.username')" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item :label="editingId ? locale.t('system.users.newPassword') : locale.t('system.users.password')" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item :label="locale.t('system.users.fullName')" prop="fullName">
          <el-input v-model="form.fullName" />
        </el-form-item>
        <el-form-item :label="locale.t('system.users.email')" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item :label="locale.t('system.users.samlNameId')">
          <el-input v-model="form.samlNameId" />
        </el-form-item>
        <el-form-item :label="locale.t('suppliers.enabledStatus')">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item :label="locale.t('system.users.groups')">
          <el-select v-model="form.groupIds" multiple filterable class="w-full">
            <el-option v-for="group in groups" :key="group.id" :value="group.id" :label="group.groupName" />
          </el-select>
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
import { ElMessage } from 'element-plus';

import { groupApi, userApi } from '../../api';
import type { Group, User } from '../../api/types';
import PageHeader from '../../components/PageHeader.vue';
import { useAuthStore } from '../../stores/auth';
import { useLocaleStore } from '../../stores/locale';

const locale = useLocaleStore();
const auth = useAuthStore();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const records = ref<User[]>([]);
const groups = ref<Group[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInstance>();
const filters = reactive({ keyword: '', enabled: undefined as boolean | undefined, groupId: undefined as number | undefined });
const form = reactive({
  username: '',
  password: '',
  fullName: '',
  email: '',
  samlNameId: '',
  enabled: true,
  groupIds: [] as number[]
});

const canWrite = computed(() => auth.hasPermission('user:write'));
const rules = computed<FormRules>(() => ({
  username: [{ required: true, message: locale.t('common.required'), trigger: 'blur' }],
  password: editingId.value ? [] : [{ required: true, message: locale.t('common.required'), trigger: 'blur' }],
  fullName: [{ required: true, message: locale.t('common.required'), trigger: 'blur' }],
  email: [
    { required: true, message: locale.t('common.required'), trigger: 'blur' },
    { type: 'email', message: locale.t('system.users.email'), trigger: 'blur' }
  ]
}));

function groupName(groupId: number) {
  return groups.value.find(group => group.id === groupId)?.groupName ?? String(groupId);
}

async function load() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await userApi.page({
      keyword: filters.keyword || undefined,
      enabled: filters.enabled,
      groupId: filters.groupId,
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

async function loadGroups() {
  const result = await groupApi.page({ page: 1, size: 100 });
  groups.value = result.records;
}

function reset() {
  filters.keyword = '';
  filters.enabled = undefined;
  filters.groupId = undefined;
  page.value = 1;
  load();
}

function openCreate() {
  editingId.value = null;
  Object.assign(form, {
    username: '',
    password: '',
    fullName: '',
    email: '',
    samlNameId: '',
    enabled: true,
    groupIds: []
  });
  dialogVisible.value = true;
}

function openEdit(user: User) {
  editingId.value = user.id;
  Object.assign(form, {
    username: user.username,
    password: '',
    fullName: user.fullName,
    email: user.email,
    samlNameId: user.samlNameId ?? '',
    enabled: user.enabled,
    groupIds: [...user.groupIds]
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
      username: form.username,
      password: form.password || undefined,
      fullName: form.fullName,
      email: form.email,
      samlNameId: form.samlNameId || null,
      enabled: form.enabled,
      groupIds: form.groupIds
    };
    if (editingId.value === null) {
      await userApi.create({ ...data, password: form.password });
    } else {
      await userApi.update(editingId.value, data);
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

async function setEnabled(user: User, enabled: boolean) {
  try {
    if (enabled) {
      await userApi.enable(user.id);
    } else {
      await userApi.disable(user.id);
    }
    ElMessage.success(locale.t('common.success'));
    await load();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : locale.t('common.requestFailed'));
  }
}

onMounted(async () => {
  await Promise.all([load(), loadGroups()]);
});
</script>
