import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, getUserInfo } from '@/utils/auth'

const routes = [
  // 公开路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', guest: true }
  },

  // 管理端路由
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理首页' }
      },
      {
        path: 'doctors',
        name: 'DoctorManage',
        component: () => import('@/views/admin/DoctorManage.vue'),
        meta: { title: '医生账号管理' }
      },
      {
        path: 'children',
        name: 'ChildManage',
        component: () => import('@/views/admin/ChildManage.vue'),
        meta: { title: '儿童基本信息管理' }
      },
      {
        path: 'videos',
        name: 'VideoManage',
        component: () => import('@/views/admin/VideoManage.vue'),
        meta: { title: '视频管理' }
      },
      {
        path: 'reports',
        name: 'ReportManage',
        component: () => import('@/views/admin/ReportManage.vue'),
        meta: { title: '报告表管理' }
      },
      {
        path: 'tasks',
        name: 'TaskManage',
        component: () => import('@/views/admin/TaskManage.vue'),
        meta: { title: '评估任务管理' }
      }
    ]
  },

  // 用户端（医生）路由
  {
    path: '/doctor',
    component: () => import('@/layouts/DoctorLayout.vue'),
    meta: { requireAuth: true, requireDoctor: true },
    children: [
      {
        path: '',
        redirect: '/doctor/dashboard'
      },
      {
        path: 'dashboard',
        name: 'DoctorDashboard',
        component: () => import('@/views/doctor/Dashboard.vue'),
        meta: { title: '我的任务' }
      },
      {
        path: 'profile',
        name: 'DoctorProfile',
        component: () => import('@/views/doctor/Profile.vue'),
        meta: { title: '个人主页' }
      },
      {
        path: 'assessment/:taskId',
        name: 'Assessment',
        component: () => import('@/views/doctor/Assessment.vue'),
        meta: { title: '评估工作台' }
      }
    ]
  },

  // 根路径重定向
  {
    path: '/',
    redirect: '/login'
  },

  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/Login.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫
router.beforeEach((to) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 儿童早期疾病筛查评估系统` : '儿童早期疾病筛查评估系统'

  const loggedIn = isLoggedIn()
  const userInfo = getUserInfo()
  // 后端 Jackson 将 Long 序列化为 String，统一转 Number
  const role = userInfo ? Number(userInfo.role) : null

  // 已登录用户访问登录/注册页 → 跳转对应首页
  if (to.meta.guest && loggedIn) {
    return role === 1 ? '/admin/dashboard' : '/doctor/dashboard'
  }

  // 需要认证的路由 → 检查登录状态
  if (to.matched.some(record => record.meta.requireAuth)) {
    if (!loggedIn) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }

    // 检查管理员权限
    if (to.matched.some(record => record.meta.requireAdmin) && role !== 1) {
      return '/doctor/dashboard'
    }

    // 检查医生权限
    if (to.matched.some(record => record.meta.requireDoctor) && role !== 0) {
      return '/admin/dashboard'
    }
  }
})

export default router
