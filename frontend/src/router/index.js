import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '听写' }
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('@/views/History.vue'),
    meta: { title: '历史记录' }
  },
  {
    path: '/difficult',
    name: 'DifficultWords',
    component: () => import('@/views/DifficultWords.vue'),
    meta: { title: '生词本' }
  },
  {
    path: '/reports',
    name: 'Reports',
    component: () => import('@/views/Reports.vue'),
    meta: { title: '报表' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '听写助手'} - 小学生听写助手`
  next()
})

export default router