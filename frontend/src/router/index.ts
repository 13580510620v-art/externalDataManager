import { createRouter, createWebHistory } from 'vue-router';

import AdminLayout from '../layouts/AdminLayout.vue';
import LoginPage from '../pages/LoginPage.vue';
import DashboardPage from '../pages/DashboardPage.vue';
import DataTaskPage from '../pages/DataTaskPage.vue';
import SupplierPage from '../pages/SupplierPage.vue';
import UserPage from '../pages/system/UserPage.vue';
import GroupPage from '../pages/system/GroupPage.vue';
import PermissionPage from '../pages/system/PermissionPage.vue';
import LogPage from '../pages/system/LogPage.vue';
import { useAuthStore } from '../stores/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginPage,
      meta: { public: true }
    },
    {
      path: '/',
      component: AdminLayout,
      children: [
        {
          path: '',
          redirect: '/dashboard'
        },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: DashboardPage,
          meta: { permission: 'dashboard:read' }
        },
        {
          path: 'tasks',
          name: 'tasks',
          component: DataTaskPage,
          meta: { permission: 'task:read' }
        },
        {
          path: 'suppliers',
          name: 'suppliers',
          component: SupplierPage,
          meta: { permission: 'supplier:read' }
        },
        {
          path: 'logs',
          name: 'logs',
          component: LogPage,
          meta: { permission: 'audit:read' }
        },
        {
          path: 'system/users',
          name: 'system-users',
          component: UserPage,
          meta: { permission: 'user:read' }
        },
        {
          path: 'system/groups',
          name: 'system-groups',
          component: GroupPage,
          meta: { permission: 'group:read' }
        },
        {
          path: 'system/permissions',
          name: 'system-permissions',
          component: PermissionPage,
          meta: { permission: 'permission:read' }
        },
        {
          path: 'forbidden',
          name: 'forbidden',
          component: () => import('../pages/ForbiddenPage.vue')
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard'
    }
  ]
});

router.beforeEach(async to => {
  const auth = useAuthStore();

  if (to.meta.public) {
    if (to.name === 'login' && auth.isLoggedIn) {
      return { name: 'dashboard' };
    }
    return true;
  }

  if (!auth.loaded) {
    try {
      await auth.fetchMe();
    } catch {
      return { name: 'login', query: { redirect: to.fullPath } };
    }
  }

  if (!auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }

  const permission = to.meta.permission;
  if (typeof permission === 'string' && !auth.hasPermission(permission)) {
    return { name: 'forbidden' };
  }

  return true;
});

export default router;
