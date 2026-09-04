<template>
  <div class="space-y-4">
    <PageHeader :title="locale.t('system.permissions.title')" :description="locale.t('system.permissions.description')" />

    <el-alert v-if="errorMessage" type="error" show-icon :closable="false" :title="errorMessage" />

    <div class="rounded-lg border bg-card shadow-sm">
      <el-table v-loading="loading" :data="records" class="w-full">
        <el-table-column prop="permissionCode" :label="locale.t('system.permissions.permissionCode')" min-width="180" fixed="left" />
        <el-table-column prop="permissionName" :label="locale.t('system.permissions.permissionName')" min-width="170" />
        <el-table-column prop="resourceType" :label="locale.t('system.permissions.resourceType')" min-width="140" />
        <el-table-column prop="action" :label="locale.t('system.permissions.action')" min-width="120" />
        <el-table-column :label="locale.t('suppliers.enabledStatus')" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? locale.t('common.enabled') : locale.t('common.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { permissionApi } from '../../api';
import type { Permission } from '../../api/types';
import PageHeader from '../../components/PageHeader.vue';
import { useLocaleStore } from '../../stores/locale';

const locale = useLocaleStore();
const loading = ref(false);
const errorMessage = ref('');
const records = ref<Permission[]>([]);

onMounted(async () => {
  loading.value = true;
  try {
    records.value = await permissionApi.list();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : locale.t('common.requestFailed');
  } finally {
    loading.value = false;
  }
});
</script>
