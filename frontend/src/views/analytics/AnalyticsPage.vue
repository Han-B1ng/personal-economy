<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useWorkspace } from '@/hooks/useWorkspace'
import { useHomeStore } from '@/store/useHomeStore'
import { chartPeriods, type ChartPeriod } from '@/types/home'
import { filterHistory, formatIndex, signed } from '@/utils/chart'
import GrowthChart from '@/views/home/components/GrowthChart.vue'
import SegmentedControl from '@/components/common/SegmentedControl.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import PageState from '@/components/common/PageState.vue'
const store = useWorkspace(),
  home = useHomeStore(),
  route = useRoute()
const breakdown = computed(() => route.name === 'analytics-breakdown'),
  detail = computed(() => route.name === 'analytics-chart')
onMounted(() => {
  if (home.status !== 'ready') home.load()
})
const points = computed(() => filterHistory(home.snapshot?.history || [], home.period))
const categories = computed(
  () =>
    store.data?.categories
      .map((c) => {
        const events = store.data!.events.filter((e) => e.categoryId === c.id)
        return {
          ...c,
          count: events.length,
          pending: events.filter((e) => e.contribution === null).length,
          contribution: events.reduce((s, e) => s + (e.contribution || 0), 0),
        }
      })
      .filter((c) => c.count) || [],
)
const max = computed(() => Math.max(1, ...categories.value.map((c) => Math.abs(c.contribution))))
</script>
<template>
  <PageHeader
    :title="breakdown ? '事件分类分析' : detail ? '历史行情详情' : '行情分析'"
    :back="breakdown || detail ? '/analytics' : undefined"
    subtitle="PE001 · 个人成长指数 · 演示数据"
  /><WorkspaceGate
    ><template v-if="!breakdown"
      ><PageState
        v-if="home.status === 'error'"
        title="行情加载失败"
        description="请检查服务后重试。"
        action-label="重试"
        @action="home.load()"
      /><template v-else-if="home.snapshot"
        ><p class="large-number">{{ formatIndex(home.snapshot.index) }}</p>
        <p class="positive">
          演示行情累计 {{ signed(home.snapshot.index - 1000) }}（{{
            signed((home.snapshot.index - 1000) / 10)
          }}%）
        </p>
        <SegmentedControl
          :model-value="home.period"
          :options="chartPeriods"
          label="分析周期"
          compact
          @update:model-value="(value) => (home.period = value as ChartPeriod)"
        />
        <div class="detail-block"><GrowthChart :points="points" /></div>
        <RouterLink v-if="!detail" class="text-link" to="/analytics/chart"
          >查看历史行情详情 ›</RouterLink
        >
        <div class="metric-grid">
          <div class="metric">
            <span>成长事件</span><strong>{{ store.data?.events.length }} 条</strong>
          </div>
          <div class="metric">
            <span>本期演示指数变化</span
            ><strong class="positive">{{ signed(home.snapshot.change) }}</strong>
          </div>
        </div>
        <p class="info-note">
          新事件保存为待评估，尚未接入动能衰减、历史重算或评分模型。
        </p></template
      >
      <p v-else class="empty-note">正在加载行情…</p></template
    >
    <h2 class="section-title">
      分类贡献<RouterLink v-if="!breakdown" class="text-link" to="/analytics/breakdown"
        >查看详情 ›</RouterLink
      >
    </h2>
    <div v-for="c in categories" :key="c.id">
      <div class="bar-row">
        <span>{{ c.name }}</span>
        <div class="bar-track">
          <span :style="{ width: (Math.abs(c.contribution) / max) * 100 + '%' }" />
        </div>
        <span class="positive">{{ signed(c.contribution) }}</span>
      </div>
      <p class="info-note">{{ c.count }} 条记录 · {{ c.pending }} 条待评估</p>
    </div>
    <p v-if="!categories.length" class="empty-note">暂无可分析的成长事件</p>
    <p class="info-note">
      分类合计仅统计已有记录中的演示贡献，不代表完整行情的累计变化，也不把待评估事件当作0分。
    </p></WorkspaceGate
  >
</template>
