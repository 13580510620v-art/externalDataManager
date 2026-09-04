<script setup lang="ts">
defineProps<{
  title: string;
  description: string;
}>();
</script>

<template>
  <div class="mb-6 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
    <div>
      <h1 class="text-2xl font-semibold">{{ title }}</h1>
      <p class="mt-1 text-sm text-muted-foreground">{{ description }}</p>
    </div>
    <div class="flex flex-wrap items-center gap-2">
      <slot name="actions" />
    </div>
  </div>
</template>
