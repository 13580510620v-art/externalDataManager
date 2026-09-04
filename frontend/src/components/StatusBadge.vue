<script setup lang="ts">
import { computed } from 'vue';

import { useLocaleStore } from '../stores/locale';

const props = defineProps<{
  status: string;
}>();

const locale = useLocaleStore();
const type = computed(() => {
  if (props.status === 'COMPLETED') return 'success';
  if (props.status.endsWith('_FAILED')) return 'danger';
  if (props.status === 'PENDING') return 'warning';
  return 'info';
});
</script>

<template>
  <el-tag :type="type" size="small" class="rounded-sm">{{ locale.t(`status.${status}`) }}</el-tag>
</template>
