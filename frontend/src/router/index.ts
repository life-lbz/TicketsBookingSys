import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { ElMessage } from 'element-plus';

const routes: Array<RouteRecordRaw> = [
  // 用户端路由
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/user/Home.vue'),
  },
  {
    path: '/seat-selection/:id',
    name: 'SeatSelection',
    component: () => import('../views/user/SeatSelection.vue'),
  },
  {
    path: '/order-confirmation',
    name: 'OrderConfirmation',
    component: () => import('../views/user/OrderConfirmation.vue'),
  },
  {
    path: '/personal-center',
    name: 'PersonalCenter',
    component: () => import('../views/user/PersonalCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/user/Login.vue'),
  },
  
  // 管理端路由
  {
    path: '/admin',
    name: 'AdminLogin',
    component: () => import('../views/admin/AdminLogin.vue'),
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('../views/admin/AdminDashboard.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/events',
    name: 'AdminEvents',
    component: () => import('../views/admin/AdminEvents.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/tickets',
    name: 'AdminTickets',
    component: () => import('../views/admin/AdminTickets.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: () => import('../views/admin/AdminOrders.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'UserManagement',
    component: () => import('../views/admin/UserManagement.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/admins',
    name: 'AdminManagement',
    component: () => import('../views/admin/AdminManagement.vue'),
    meta: { requiresAdmin: true, requiresSuperAdmin: true }
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, _from) => {
  const isUserLoggedIn = localStorage.getItem('user_isLoggedIn') === 'true';
  const isAdminLoggedIn = localStorage.getItem('admin_isLoggedIn') === 'true';
  const isSuperAdmin = localStorage.getItem('admin_role') === 'SUPER_ADMIN';
  
  // 如果是用户端需要登录的页面
  if (to.meta.requiresAuth) {
    // 如果管理员登录了，不能访问用户端需要登录的页面
    if (isAdminLoggedIn) {
      ElMessage.warning('管理员账号不能访问用户端功能，请使用普通用户账号登录');
      return '/admin/dashboard';
    }
    // 如果用户未登录，跳转到登录页面
    if (!isUserLoggedIn) {
      return '/login';
    }
  }
  
  // 如果是管理端需要登录的页面
  if (to.meta.requiresAdmin && !isAdminLoggedIn) {
    return '/admin';
  }
  
  // 如果是超级管理员页面
  if (to.meta.requiresSuperAdmin && !isSuperAdmin) {
    return '/admin/dashboard';
  }
  
  return true;
});

export default router;
