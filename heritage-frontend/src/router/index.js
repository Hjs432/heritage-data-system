import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/project',
    children: [
      {
        path: 'search',
        name: 'SmartSearch',
        component: () => import('../views/search/SmartSearch.vue'),
        meta: { title: '智能检索', desc: '用日常口语描述，语义匹配非遗项目' }
      },
      {
        path: 'project',
        name: 'ProjectManage',
        component: () => import('../views/project/ProjectManage.vue'),
        meta: { title: '非遗项目管理', desc: '项目档案的检索、录入与关联维护' }
      },
      {
        path: 'inheritor',
        name: 'InheritorManage',
        component: () => import('../views/inheritor/InheritorManage.vue'),
        meta: { title: '传承人档案管理', desc: '记录代表性传承人及其传承贡献' }
      },
      {
        path: 'activity',
        name: 'ActivityManage',
        component: () => import('../views/activity/ActivityManage.vue'),
        meta: { title: '民俗活动管理', desc: '梳理节庆、赛会与祭祀等民俗活动' }
      },
      {
        path: 'source',
        name: 'SourceManage',
        component: () => import('../views/source/SourceManage.vue'),
        meta: { title: '资料来源溯源管理', desc: '登记资料来源与原始链接，保证可追溯' }
      },
      {
        path: 'vision',
        name: 'VisionPage',
        component: () => import('../views/vision/VisionPage.vue'),
        meta: { title: '资源视界', desc: '在中国版图上俯视非遗项目的地理分布' }
      },
      {
        path: 'statistic',
        name: 'StatisticPage',
        component: () => import('../views/statistic/StatisticPage.vue'),
        meta: { title: '数据统计分析', desc: '按地区与级别观察非遗资源分布' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/project' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    return next()
  }
  if (!token) {
    return next('/login')
  }
  next()
})

export default router
