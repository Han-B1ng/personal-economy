<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useHomeStore } from '@/store/useHomeStore'
import { chartPeriods, type ChartPeriod } from '@/types/home'
import { filterHistory, formatIndex, signed } from '@/utils/chart'
import GrowthChart from '@/views/home/components/GrowthChart.vue'
import RecentEvents from '@/views/home/components/RecentEvents.vue'
import HomeSkeleton from '@/views/home/components/HomeSkeleton.vue'
import SegmentedControl from '@/components/common/SegmentedControl.vue'
import PageState from '@/components/common/PageState.vue'
import AppIcon from '@/components/common/AppIcon.vue'
const store = useHomeStore()
const route = useRoute()
const history = computed(() => filterHistory(store.snapshot?.history || [], store.period))
const busy = computed(() => store.status === 'loading' || store.status === 'idle')
const lastRead = computed(() =>
  store.lastFetchedAt
    ? new Intl.DateTimeFormat('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      }).format(store.lastFetchedAt)
    : '',
)
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
    <div class="home-title-row">
      <h1>Personal Economy</h1>
      <button
        type="button"
        class="icon-button refresh-button"
        :disabled="busy"
        :aria-busy="busy || undefined"
        aria-label="刷新行情与最近事件"
        @click="store.load()"
      >
        <AppIcon name="refresh" :size="18" :class="{ refreshing: busy }" />
      </button>
    </div>
    <div v-if="store.snapshot" class="stock-identity">
      <span class="stock-name">{{ store.snapshot.nickname }}</span
      ><span class="identity-divider" aria-hidden="true">/</span>
      <span class="stock-code">{{ store.snapshot.stockCode }}</span>
      <span class="stock-status"><i aria-hidden="true" />成长中</span>
    </div>
    <div v-else class="stock-identity">你的个人成长行情</div>
  </header>
  <HomeSkeleton v-if="busy && !store.snapshot" />
  <PageState
    v-else-if="store.status === 'error' && !store.snapshot"
    title="行情暂时无法加载"
    description="请确认服务可用后重试。"
    action-label="重新加载"
    @action="store.load()"
  />
  <div v-else-if="store.snapshot" class="home-content" :aria-busy="busy || undefined">
    <p v-if="store.status === 'error'" class="refresh-warning" role="alert">
      刷新失败，当前显示上次读取的数据。请重试。
    </p>
    <section class="market-summary" aria-label="个人成长指数，演示数据">
      <p class="index-label">个人成长指数</p>
      <p class="current-index">
        {{ formatIndex(store.snapshot.index) }}<span class="index-unit">点</span>
      </p>
      <div class="market-change">
        <span class="change-value" :class="{ negative: store.snapshot.change < 0 }">
          <AppIcon
            name="arrow-up-right"
            :size="14"
            :class="{ falling: store.snapshot.change < 0 }"
          />
          {{ signed(store.snapshot.change) }}
          <span class="change-percent">({{ signed(store.snapshot.changePercent) }}%)</span>
        </span>
        <span class="demo-badge">演示数据</span>
      </div>
    </section>
    <section class="market-history" aria-label="成长历史">
      <GrowthChart v-if="history.length" :points="history" />
      <div v-else class="empty-chart">
        <AppIcon name="analytics" :size="28" />
        <p>暂无行情记录</p>
        <span>指数规则接入后，成长历史将在这里展示。</span>
      </div>
      <SegmentedControl
        :model-value="store.period"
        :options="chartPeriods"
        label="行情周期"
        compact
        @update:model-value="changePeriod"
      />
      <div class="history-caption">
        <span>固定演示行情 · 新事件待评估</span
        ><span>{{
          busy
            ? '读取中…'
            : store.status === 'error'
              ? '刷新失败'
              : lastRead
                ? `读取于 ${lastRead}`
                : ''
        }}</span>
      </div>
    </section>
    <RecentEvents :events="store.snapshot.recentEvents" />
  </div>
  <p class="sr-only" role="status">
    {{
      busy
        ? '正在读取行情'
        : store.status === 'error'
          ? '行情读取失败'
          : '行情已读取，指数为演示数据'
    }}
  </p>
</template>

<style scoped>
.home-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.home-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  line-height: 32px;
  letter-spacing: -0.7px;
}
.icon-button {
  width: 44px;
  min-width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border: 1px solid var(--color-border);
  border-radius: 50%;
  background: transparent;
  color: var(--color-muted);
}
.refresh-button {
  margin-right: -4px;
}
.refresh-button:disabled {
  cursor: progress;
}
.refreshing {
  animation: refresh-turn 1.1s linear infinite;
}
.stock-identity {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  min-height: 20px;
  font-size: 13px;
  font-weight: 500;
}
.stock-name {
  max-width: 44%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.identity-divider {
  color: var(--color-border);
  font-size: 12px;
}
.stock-code {
  font-size: 12px;
  color: var(--color-muted);
  letter-spacing: 0.4px;
}
.stock-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
  color: var(--color-muted);
  font-size: 11px;
  font-weight: 400;
  white-space: nowrap;
}
.stock-status i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--color-positive);
}
.market-summary {
  margin-top: 24px;
}
.index-label {
  margin: 0;
  font-size: 12px;
  line-height: 20px;
  color: var(--color-muted);
}
.current-index {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 4px 0 0 -2px;
  font-size: clamp(42px, 11.7vw, 50px);
  font-weight: 500;
  line-height: 1.25;
  letter-spacing: -1.7px;
  font-variant-numeric: tabular-nums;
}
.index-unit {
  font-size: 12px;
  color: var(--color-muted);
  letter-spacing: 0;
  font-weight: 400;
}
.market-change {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  min-height: 23px;
}
.change-value {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--color-positive);
  font-size: 16px;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}
.change-value.negative {
  color: var(--color-error);
}
.falling {
  transform: rotate(90deg);
}
.change-percent {
  font-size: 14px;
}
.demo-badge {
  font-size: 10px;
  color: var(--color-muted);
  font-weight: 400;
  padding: 3px 6px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  white-space: nowrap;
}
.market-history {
  margin-top: 16px;
  padding-bottom: 12px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}
.history-caption {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  margin-top: 6px;
  font-size: 10px;
  line-height: 12px;
  color: var(--color-muted);
}
.history-caption span:last-child {
  white-space: nowrap;
}
.empty-chart {
  min-height: 254px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  color: var(--color-muted);
}
.empty-chart p {
  margin: 12px 0 8px;
  font-size: 14px;
}
.empty-chart span {
  max-width: 240px;
  text-align: center;
  font-size: 12px;
  line-height: 1.7;
}
.refresh-warning {
  margin: 20px 0 0;
  color: var(--color-error);
  font-size: 12px;
  line-height: 1.7;
}
@keyframes refresh-turn {
  to {
    transform: rotate(360deg);
  }
}
@media (max-width: 360px) {
  .home-header h1 {
    font-size: 22px;
  }
  .history-caption {
    font-size: 9px;
  }
  .change-value {
    font-size: 15px;
  }
}
</style>
