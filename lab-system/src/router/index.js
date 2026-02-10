import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import MainLayout from '../layout/MainLayout.vue' // 引入刚写好的布局

const routes = [
  { path: '/login', name: 'Login', component: Login },
  {
    path: '/',
    component: MainLayout, // 关键：用布局组件包裹子路由
    children: [
      { path: '', name: '首页', component: () => import('../views/Home.vue') },
      { path: 'users', name: '人员管理', component: () => import('../views/Users.vue') },
      { path: 'appointment', name: '预约', component: () => import('../views/Appointment.vue') },
      { path: 'consumables', name: '耗材', component: () => import('../views/Consumables.vue') },
      { path: 'monitor', name: '监控', component: () => import('../views/Monitor.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('satoken')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router