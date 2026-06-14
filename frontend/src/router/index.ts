import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
    },
    {
      path: '/',
      component: () => import('../views/LayoutView.vue'),
      children: [
        {
          path: 'home',
          name: 'home',
          component: () => import('../views/HomeView.vue'),
        },
        // 健康数据
        {
          path: 'health-data',
          name: 'health-data',
          component: () => import('../views/HealthDataView.vue'),
        },
        // 健康趋势图 (3.5.1)
        {
          path: 'health-trends',
          name: 'health-trends',
          component: () => import('../views/HealthTrendView.vue'),
        },
        // AI 健康画像 (3.3.1)
        {
          path: 'ai-profile',
          name: 'ai-profile',
          component: () => import('../views/AiHealthProfile.vue'),
        },
        // AI 健康计划 (3.3.2)
        {
          path: 'ai-plan',
          name: 'ai-plan',
          component: () => import('../views/AiHealthPlan.vue'),
        },
        // 健康知识 (3.3.3)
        {
          path: 'knowledge',
          name: 'knowledge',
          component: () => import('../views/KnowledgeView.vue'),
        },
        // 健康提醒 (3.4.1)
        {
          path: 'reminders',
          name: 'reminders',
          component: () => import('../views/ReminderView.vue'),
        },
        // 个人中心
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/ProfileView.vue'),
        },
        // ---- 管理员路由 ----
        {
          path: 'admin/users',
          name: 'user-management',
          component: () => import('../views/admin/UserManagement.vue'),
          meta: { requiresAdmin: true, title: '用户管理' },
        },
        {
          path: 'admin/statistics',
          name: 'admin-statistics',
          component: () => import('../views/admin/StatisticsView.vue'),
          meta: { requiresAdmin: true, title: '健康数据统计' },
        },
      ],
    },
  ],
})

// 路由守卫
router.beforeEach((to, _from) => {
  const token = localStorage.getItem('token')

  if (to.path === '/login' || to.path === '/register') {
    return token ? '/home' : true
  }

  if (!token) return '/login'

  if (to.meta.requiresAdmin) {
    const role = localStorage.getItem('role')
    if (role !== 'admin') return '/home'
  }

  return true
})

export default router
