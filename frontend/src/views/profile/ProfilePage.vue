<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useHomeStore } from '@/store/useHomeStore'
import { signed } from '@/utils/chart'
import { useWorkspace } from '@/hooks/useWorkspace'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import PageHeader from '@/components/common/PageHeader.vue'
const store = useWorkspace()
const home = useHomeStore()
onMounted(() => {
  if (home.status !== 'ready') void home.load()
})
const initials = computed(
  () =>
    store.data?.profile.nickname
      .replace(/[^A-Za-z0-9\u4e00-\u9fa5]/g, '')
      .slice(0, 2)
      .toUpperCase() || 'PE',
)
const days = computed(() => new Set(store.data?.events.map((e) => e.date)).size)
const menus = [
  { title: '个人资料', to: '/profile/edit' },
  { title: '成长目标', to: '/growth/goals' },
  { title: '事件分类管理', to: '/growth/categories' },
  { title: '数据管理', to: '/profile/data' },
  { title: '账户设置', to: '/profile/settings' },
]
</script>
<template>
  <PageHeader title="我的" /><WorkspaceGate
    ><div class="profile-identity">
      <div class="avatar">
        <img
          v-if="store.data?.profile.avatar"
          :src="store.data.profile.avatar"
          alt="个人头像"
        /><span v-else>{{ initials }}</span>
      </div>
      <div>
        <h2>{{ store.data?.profile.nickname }}</h2>
        <p class="muted">{{ store.data?.profile.stockCode }} · 个人成长股票</p>
      </div>
    </div>
    <div class="metric-grid three detail-block">
      <div class="metric">
        <span>成长事件</span><strong>{{ store.data?.events.length }}</strong>
      </div>
      <div class="metric">
        <span>事件记录天数</span><strong>{{ days }}</strong>
      </div>
      <div class="metric">
        <span>演示累计变化</span
        ><strong>{{
          home.snapshot ? signed((home.snapshot.index - 1000) / 10) + '%' : '—'
        }}</strong>
      </div>
    </div>
    <RouterLink v-for="menu in menus" :key="menu.to" :to="menu.to" class="menu-row"
      ><span>{{ menu.title }}</span
      ><span class="muted">›</span></RouterLink
    >
    <p class="info-note" style="text-align: center; margin-top: 44px">如果人生也有一张走势图。</p>
    <p class="local-note">本地单用户演示模式 · 账号身份体系尚未接入</p></WorkspaceGate
  >
</template>
<style scoped>
.profile-identity {
  display: flex;
  align-items: center;
  gap: 20px;
}
.profile-identity h2 {
  font-size: 22px;
  margin: 0 0 10px;
}
.profile-identity p {
  margin: 0;
}
</style>
