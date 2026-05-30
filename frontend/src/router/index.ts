import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/layout/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'DataBoard' }
      },
      {
        path: 'project',
        name: 'Project',
        component: () => import('@/views/project/index.vue'),
        meta: { title: '项目管理', icon: 'Files' }
      },
      {
        path: 'testcase',
        name: 'TestCase',
        component: () => import('@/views/testcase/index.vue'),
        meta: { title: '用例管理', icon: 'List' }
      },
      {
        path: 'execution',
        name: 'Execution',
        component: () => import('@/views/execution/index.vue'),
        meta: { title: '执行中心', icon: 'VideoPlay' }
      },
      {
        path: 'execution/:id',
        name: 'ExecutionDetail',
        component: () => import('@/views/execution/detail.vue'),
        meta: { title: '执行详情', hidden: true }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/views/report/index.vue'),
        meta: { title: '报告中心', icon: 'Document' }
      },
      {
        path: 'report/:id',
        name: 'ReportDetail',
        component: () => import('@/views/report/detail.vue'),
        meta: { title: '报告详情', hidden: true }
      },
      {
        path: 'system',
        name: 'System',
        redirect: '/system/user',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
          {
            path: 'user',
            name: 'UserManagement',
            component: () => import('@/views/system/user/index.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'role',
            name: 'RoleManagement',
            component: () => import('@/views/system/role/index.vue'),
            meta: { title: '角色管理' }
          },
          {
            path: 'environment',
            name: 'EnvironmentManagement',
            component: () => import('@/views/system/environment/index.vue'),
            meta: { title: '环境管理' }
          },
          {
            path: 'jenkins',
            name: 'JenkinsIntegration',
            component: () => import('@/views/system/jenkins/index.vue'),
            meta: { title: 'Jenkins集成' }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth !== false && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/login' && token) {
    next({ path: '/' })
  } else {
    next()
  }
})

export default router
