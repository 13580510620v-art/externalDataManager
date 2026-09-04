<script setup lang="ts">
import { ref } from 'vue';

import AppSidebar from '../components/AppSidebar.vue';
import AppTopbar from '../components/AppTopbar.vue';

const sidebarCollapsed = ref(false);
</script>

<template>
  <div class="flex min-h-screen bg-background">
    <AppSidebar
      :collapsed="sidebarCollapsed"
      @collapse="sidebarCollapsed = $event"
    />
    <div class="flex min-w-0 flex-1 flex-col">
      <AppTopbar @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed" />
      <main class="flex-1 p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>
