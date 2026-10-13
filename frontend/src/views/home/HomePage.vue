<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useHomeStore } from '@/store/useHomeStore'
import { chartPeriods, type ChartPeriod } from '@/types/home'
import { filterHistory, formatIndex, signed } from '@/utils/chart'
import GrowthChart from '@/views/home/components/GrowthChart.vue'
import RecentEvents from '@/views/home/components/RecentEvents.vue'
import SegmentedControl from '@/components/common/SegmentedControl.vue'
import PageState from '@/components/common/PageState.vue'

const store = useHomeStore()
const route = useRoute()
const history = computed(() => filterHistory(store.snapshot?.history || [], store.period))
watch(
  () => route.query.preview,
  (value, previous) => {
    const preview =
      import.meta.env.DEV && (value === 'empty' || value === 'error') ? value : undefined
    if (preview || previous === 'empty' || previous === 'error' || store.status !== 'ready')
      void store.load(preview)
  },
  { immediate: true },
)
function changePeriod(value: string) {
  if (chartPeriods.includes(value as ChartPeriod)) store.period = value as ChartPeriod
}
</script>

<template>
  <header class="home-header">
    <h1>Personal Economy</h1>
    <div v-if="store.snapshot" class="stock-identity">
      <span>{{ store.snapshot.nickname }}</span>
      <span class="stock-code">{{ store.snapshot.stockCode }}</span>
      <span class="stock-status"><i aria-hidden="true" />成长中</span>
    </div>
    <div v-else class="stock-identity">行情尚未加载</div>
  </header>
  <PageState
    v-if="store.status === 'loading' || store.status === 'idle'"
    title="正在读取行情"
    description="演示数据加载中"
    busy
  />
  <PageState
    v-else-if="store.status === 'error'"
    title="行情暂时无法加载"
    description="请确认服务可用后重试。"
    action-label="重新加载"
    @action="store.load()"
  />
  <template v-else-if="store.snapshot">
    <section class="market-summary" aria-label="个人成长指数，演示数据">
      <p class="index-label">个人成长指数</p>
      <p class="current-index">{{ formatIndex(store.snapshot.index) }}</p>
      <div class="market-change">
        <span :class="{ negative: store.snapshot.change < 0 }"
          >{{ signed(store.snapshot.change) }} &nbsp;({{
            signed(store.snapshot.changePercent)
          }}%)</span
        ><span class="demo-badge">演示数据</span>
      </div>
    </section>
    <section class="market-history" aria-label="成长历史">
      <GrowthChart v-if="history.length" :points="history" />
      <div v-else class="empty-chart">
        <p>暂无行情记录</p>
        <span>记录成长事件后，在这里回顾成长历史。</span>
      </div>
      <SegmentedControl
        :model-value="store.period"
        :options="chartPeriods"
        label="行情周期"
        compact
        @update:model-value="changePeriod"
      />
    </section>
    <RecentEvents :events="store.snapshot.recentEvents" />
  </template>
</template>

<style scoped>
.home-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  line-height: 32px;
  letter-spacing: -0.7px;
}
.stock-identity {
  display: flex;
  align-items: center;
  gap: 0;
  margin-top: 7px;
  min-height: 20px;
  font-size: 14px;
  font-weight: 500;
}
.stock-code {
  margin-left: 58px;
  font-size: 13px;
  color: var(--color-muted);
}
.stock-status {
  display: inline-flex;
  align-items: center;
  gap: 11px;
  margin-left: auto;
  color: var(--color-muted);
  font-size: 12px;
  font-weight: 400;
}
.stock-status i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--color-positive);
}
.market-summary {
  margin-top: 28px;
}
.index-label {
  margin: 0;
  font-size: 13px;
  line-height: 20px;
  color: var(--color-muted);
}
.current-index {
  margin: 4px 0 0 -2px;
  font-size: 46px;
  font-weight: 500;
  line-height: 58px;
  letter-spacing: -1px;
  font-variant-numeric: tabular-nums;
}
.market-change {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -1px;
  min-height: 23px;
  color: var(--color-positive);
  font-size: 17px;
  font-weight: 500;
}
.market-change .negative {
  color: var(--color-error);
}
.demo-badge {
  font-size: 10px;
  color: var(--color-subtle);
  font-weight: 400;
}
.market-history {
  margin-top: 27px;
  padding-bottom: 15px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}
.market-history :deep(.segmented-control) {
  margin-top: 0;
}
.empty-chart {
  height: 214px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  color: var(--color-muted);
}
.empty-chart p {
  font-size: 14px;
}
.empty-chart span {
  font-size: 12px;
}
@media (max-width: 360px) {
  .stock-code {
    margin-left: 32px;
  }
  .stock-status {
    gap: 6px;
  }
  .current-index {
    font-size: 42px;
  }
}
</style>
