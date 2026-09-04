import { computed, ref } from 'vue';
import { defineStore } from 'pinia';

import { authApi } from '../api';
import type { CurrentUser } from '../api/types';

export const useAuthStore = defineStore('auth', () => {
  const user = ref<CurrentUser | null>(null);
  const loaded = ref(false);
  const isLoggedIn = computed(() => user.value !== null);

  async function login(username: string, password: string) {
    const currentUser = await authApi.login(username, password);
    user.value = currentUser;
    loaded.value = true;
    return currentUser;
  }

  async function fetchMe() {
    try {
      const currentUser = await authApi.fetchMe();
      user.value = currentUser;
      loaded.value = true;
      return currentUser;
    } catch (error) {
      user.value = null;
      loaded.value = true;
      throw error;
    }
  }

  async function logout() {
    await authApi.logout();
    user.value = null;
    loaded.value = false;
  }

  function hasPermission(permission: string) {
    return user.value?.permissions.includes(permission) ?? false;
  }

  return { user, loaded, isLoggedIn, login, fetchMe, logout, hasPermission };
});
