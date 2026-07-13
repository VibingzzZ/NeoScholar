import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录 - NeoScholar' }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '学习仪表盘', icon: 'Odometer' }
        },
        {
          path: 'profile',
          name: 'ProfileManage',
          component: () => import('@/views/ProfileManage.vue'),
          meta: { title: '学生画像', icon: 'UserFilled' }
        },
        {
          path: 'profile/merge',
          name: 'ProfileMerge',
          component: () => import('@/views/ProfileMerge.vue'),
          meta: { title: '画像合并', icon: 'Connection' }
        },
        {
          path: 'learning-path',
          name: 'LearningPath',
          component: () => import('@/views/LearningPath.vue'),
          meta: { title: '学习路径', icon: 'MapLocation' }
        },
        {
          path: 'consultant',
          name: 'AIConsultant',
          component: () => import('@/views/AIConsultant.vue'),
          meta: { title: '智能辅导', icon: 'ChatDotRound' }
        }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title || 'NeoScholar 智能学伴'

  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') {
    next('/login')
    return
  }
  if (token && to.path === '/login') {
    next('/dashboard')
    return
  }
  next()
})

export default router
