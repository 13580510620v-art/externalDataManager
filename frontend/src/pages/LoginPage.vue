<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

import { authApi } from '../api';
import DatabaseIcon from '../assets/DatabaseIcon.svg';
import { useAuthStore } from '../stores/auth';
import { useLocaleStore } from '../stores/locale';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const locale = useLocaleStore();
const loading = ref(false);
const samlEnabled = ref(false);
const form = reactive({ username: '', password: '' });

onMounted(async () => {
  if (route.query.samlError) {
    ElMessage.error(locale.t('login.samlError'));
  }
  try {
    samlEnabled.value = await authApi.samlEnabled();
  } catch {
    samlEnabled.value = false;
  }
});

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning(locale.t('login.required'));
    return;
  }
  loading.value = true;
  try {
    await auth.login(form.username, form.password);
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard';
    await router.push(redirect);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : locale.t('common.requestFailed'));
  } finally {
    loading.value = false;
  }
}

function startSamlLogin() {
  window.location.href = '/saml2/authenticate/edm';
}
</script>

<template>
  <main
    class="flex min-h-dvh flex-col items-center justify-center bg-background px-4 py-14 sm:px-6"
    style="background-image: linear-gradient(146deg, #eff6ff 0%, #f8fafc 50%, #faf5ff 100%);"
  >
    <div class="w-full max-w-[392px]">
      <div class="flex flex-col items-center">
        <div class="grid size-14 place-items-center rounded-[11px] bg-primary shadow-[0_10px_8px_rgba(0,0,0,0.08),0_4px_3px_rgba(0,0,0,0.08)]">
          <img :src="DatabaseIcon" alt="" class="size-7" />
        </div>
        <h1 class="mt-3 text-center text-[26px] font-bold leading-8 text-foreground">
          {{ locale.t('login.title') }}
        </h1>
        <p class="mt-1 text-center text-xs text-muted-foreground">{{ locale.t('login.subtitle') }}</p>
      </div>

      <section
        class="mt-7 rounded-[11px] border border-border bg-card/95 p-[21px] shadow-[0_20px_25px_rgba(0,0,0,0.08),0_8px_10px_rgba(0,0,0,0.06)] backdrop-blur"
      >
        <header>
          <h2 class="text-[21px] font-semibold leading-7 text-foreground">{{ locale.t('login.welcome') }}</h2>
          <p class="mt-1 text-xs text-muted-foreground">{{ locale.t('login.description') }}</p>
        </header>
        <form class="mt-5 space-y-4" @submit.prevent="submit">
          <label class="block space-y-1.5">
            <span class="text-xs font-medium text-foreground">{{ locale.t('login.username') }}</span>
            <el-input
              v-model="form.username"
              :placeholder="locale.t('login.usernamePlaceholder')"
              autocomplete="username"
            />
          </label>
          <label class="block space-y-1.5">
            <span class="text-xs font-medium text-foreground">{{ locale.t('login.password') }}</span>
            <el-input
              v-model="form.password"
              :placeholder="locale.t('login.passwordPlaceholder')"
              type="password"
              autocomplete="current-password"
              show-password
            />
          </label>
          <el-button type="primary" size="large" class="w-full" native-type="submit" :loading="loading">
            {{ locale.t('login.submit') }}
          </el-button>
        </form>
        <el-divider class="login-divider">{{ locale.t('login.or') }}</el-divider>
        <el-button
          size="large"
          class="w-full"
          :disabled="!samlEnabled"
          :title="samlEnabled ? undefined : locale.t('login.samlDisabled')"
          @click="startSamlLogin"
        >
          {{ locale.t('login.sso') }}
        </el-button>
      </section>
    </div>
    <p class="mt-6 text-center text-xs text-muted-foreground">
      © 2026 {{ locale.t('login.title') }}. All rights reserved.
    </p>
  </main>
</template>

<style scoped>
.login-divider :deep(.el-divider__text) {
  color: var(--color-muted-foreground);
  font-size: 12px;
}
</style>
