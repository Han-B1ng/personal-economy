import { createRouter, createWebHistory } from 'vue-router'
import MobileLayout from '@/layout/MobileLayout.vue'

const growthList = () => import('@/views/growth/GrowthListPage.vue')
const growthForm = () => import('@/views/growth/GrowthFormPage.vue')
const growthDetail = () => import('@/views/growth/GrowthDetailPage.vue')
const result = () => import('@/views/growth/ResultPage.vue')
const flow = { hideNavigation: true }

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MobileLayout,
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/home/HomePage.vue'),
          meta: { title: '首页' },
        },
        { path: 'growth', redirect: '/growth/goals' },
        {
          path: 'growth/goals',
          name: 'growth-goals',
          component: growthList,
          meta: { title: '成长目标', growthTab: '目标' },
        },
        {
          path: 'growth/tasks',
          name: 'growth-tasks',
          component: growthList,
          meta: { title: '成长待办', growthTab: '待办' },
        },
        {
          path: 'growth/events',
          name: 'growth-events',
          component: growthList,
          meta: { title: '成长事件', growthTab: '事件' },
        },
        ...(['goals', 'tasks', 'events'] as const).flatMap((kind) => [
          {
            path: `growth/${kind}/new`,
            name: `${kind}-new`,
            component: growthForm,
            meta: { title: '新增记录', kind, ...flow },
          },
          {
            path: `growth/${kind}/:id/edit`,
            name: `${kind}-edit`,
            component: growthForm,
            meta: { title: '编辑记录', kind, ...flow },
          },
          {
            path: `growth/${kind}/:id`,
            name: `${kind}-detail`,
            component: growthDetail,
            meta: { title: '记录详情', kind, ...flow },
          },
          {
            path: `growth/${kind}/:id/${kind === 'events' ? 'saved' : 'completed'}`,
            name: `${kind}-result`,
            component: result,
            meta: { title: '操作结果', kind, ...flow },
          },
        ]),
        {
          path: 'growth/categories',
          name: 'growth-categories',
          component: () => import('@/views/growth/CategoriesPage.vue'),
          meta: { title: '事件分类管理', ...flow },
        },
        {
          path: 'analytics',
          name: 'analytics',
          component: () => import('@/views/analytics/AnalyticsPage.vue'),
          meta: { title: '行情分析' },
        },
        {
          path: 'analytics/chart',
          name: 'analytics-chart',
          component: () => import('@/views/analytics/AnalyticsPage.vue'),
          meta: { title: '历史行情详情', ...flow },
        },
        {
          path: 'analytics/breakdown',
          name: 'analytics-breakdown',
          component: () => import('@/views/analytics/AnalyticsPage.vue'),
          meta: { title: '事件分类分析', ...flow },
        },
        {
          path: 'finance',
          name: 'finance',
          component: () => import('@/views/finance/FinanceOverviewPage.vue'),
          meta: { title: '资产与财务' },
        },
        {
          path: 'finance/records',
          name: 'finance-records',
          component: () => import('@/views/finance/FinanceRecordsPage.vue'),
          meta: { title: '财务记录', ...flow },
        },
        {
          path: 'finance/records/new',
          name: 'finance-record-new',
          component: () => import('@/views/finance/FinanceFormPage.vue'),
          meta: { title: '新增财务记录', ...flow },
        },
        {
          path: 'finance/records/:id/saved',
          name: 'finance-record-saved',
          component: result,
          meta: { title: '财务记录已保存', kind: 'finance', ...flow },
        },
        {
          path: 'finance/assets',
          name: 'finance-assets',
          component: () => import('@/views/finance/AssetsPage.vue'),
          meta: { title: '资产信息', ...flow },
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/profile/ProfilePage.vue'),
          meta: { title: '我的' },
        },
        {
          path: 'profile/edit',
          name: 'profile-edit',
          component: () => import('@/views/profile/ProfileEditPage.vue'),
          meta: { title: '编辑资料', ...flow },
        },
        {
          path: 'profile/settings',
          name: 'profile-settings',
          component: () => import('@/views/profile/SettingsPage.vue'),
          meta: { title: '账户设置', ...flow },
        },
        {
          path: 'profile/data',
          name: 'profile-data',
          component: () => import('@/views/profile/DataPage.vue'),
          meta: { title: '数据管理', ...flow },
        },
        {
          path: ':pathMatch(.*)*',
          name: 'not-found',
          component: () => import('@/views/system/NotFoundPage.vue'),
          meta: { title: '页面不存在' },
        },
      ],
    },
    {
      path: '/dev/connection',
      name: 'connection',
      component: () => import('@/views/system/ConnectionCheckPage.vue'),
      meta: { title: '工程连接检查' },
    },
  ],
  scrollBehavior: (_to, _from, saved) => saved || { top: 0 },
})
router.afterEach((to) => {
  document.title = `${String(to.meta.title || '首页')} · Personal Economy`
})
