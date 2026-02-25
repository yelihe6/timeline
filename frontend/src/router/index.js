import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { guest: true, title: '登录' } },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue'), meta: { guest: true, title: '注册' } },
  {
    path: '/main',
    component: () => import('../views/MainLayout.vue'),
    meta: { requiresAuth: true, title: '首页' },
    children: [
      { path: '', redirect: '/main/home' },
      { path: 'home', name: 'Home', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
      { path: 'events', name: 'Events', component: () => import('../views/Main.vue'), meta: { title: '事件列表' } },
      { path: 'calendar', name: 'Calendar', component: () => import('../views/Calendar.vue'), meta: { title: '日历' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = JSON.parse(sessionStorage.getItem('user') || 'null')
  if (to.meta.requiresAuth && !user) {
    next('/login')
  } else if (to.meta.guest && user && (to.path === '/login' || to.path === '/register')) {
    next('/main')
  } else {
    next()
  }
})

router.afterEach((to) => {
  // 取最深层子路由的 title，使页面名称与当前路由绑定
  const title = [...to.matched].reverse().find(r => r.meta?.title)?.meta?.title
  document.title = title ? `${title} - 时间线` : '时间线 - 时间管理'
})

export default router
