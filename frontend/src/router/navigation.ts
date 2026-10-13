import type { IconName } from '@/components/common/AppIcon.vue'

export const navigation = [
  { path: '/', label: '首页', icon: 'home' },
  { path: '/growth', label: '成长', icon: 'growth' },
  { path: '/analytics', label: '分析', icon: 'analytics' },
  { path: '/finance', label: '资产', icon: 'finance' },
  { path: '/profile', label: '我的', icon: 'profile' },
] satisfies { path: string; label: string; icon: IconName }[]
