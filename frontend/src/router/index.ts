// 路由配置
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
        {
          path: 'health-data',
          name: 'health-data',
          component: () => import('../views/HealthDataView.vue'),
        },
        {
          path: 'ai-advice',
          name: 'ai-advice',
          component: () => import('../views/AiAdviceView.vue'),
        },
        {
          path: 'reminders',
          name: 'reminders',
          component: () => import('../views/ReminderView.vue'),
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/ProfileView.vue'),
        },
      ],
    },
  ],
})

// 路由守卫：未登录重定向到 /login，已登录访问 /login 或 /register 重定向到 /home
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login' || to.path === '/register') {
    token ? next('/home') : next()
  } else {
    token ? next() : next('/login')
  }
})

export default router
