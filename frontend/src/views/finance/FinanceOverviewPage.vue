<script setup lang="ts">
import { computed, ref } from 'vue'
import { useWorkspace } from '@/hooks/useWorkspace'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { money, today } from '@/utils/display'
const store = useWorkspace(),
  month = ref(today().slice(0, 7))
const records = computed(
  () => store.data?.financeRecords.filter((r) => r.date.startsWith(month.value)) || [],
)
const income = computed(() =>
    records.value.filter((r) => r.type === 'INCOME').reduce((sum, r) => sum + r.amount, 0),
  ),
  expense = computed(() =>
    records.value.filter((r) => r.type === 'EXPENSE').reduce((sum, r) => sum + r.amount, 0),
  )
const total = computed(() => (store.data?.assets.cash || 0) + (store.data?.assets.other || 0))
const cashPercent = computed(() =>
  total.value ? Math.round(((store.data?.assets.cash || 0) / total.value) * 100) : 0,
)
const months = computed(() => {
  const [year, mon] = month.value.split('-').map(Number)
  return [2, 1, 0].map((delta) => {
    const date = new Date(Date.UTC(year!, mon! - 1 - delta, 1))
    const key = date.toISOString().slice(0, 7)
    const rows = store.data?.financeRecords.filter((r) => r.date.startsWith(key)) || []
    return {
      key,
      label: String(date.getUTCMonth() + 1).padStart(2, '0') + '月',
      income: rows.filter((r) => r.type === 'INCOME').reduce((s, r) => s + r.amount, 0),
      expense: rows.filter((r) => r.type === 'EXPENSE').reduce((s, r) => s + r.amount, 0),
    }
  })
})
const max = computed(() => Math.max(1, ...months.value.flatMap((m) => [m.income, m.expense])))
</script>
<template>
  <PageHeader title="资产与财务" subtitle="手动资产快照 · 本地工作区"
    ><RouterLink class="text-link" to="/finance/records/new">＋ 记录</RouterLink></PageHeader
  ><WorkspaceGate>
    <p class="muted">净资产</p>
    <p class="large-number">{{ money(total - (store.data?.assets.debt || 0)) }}</p>
    <div class="metric-grid">
      <div class="metric">
        <span>总资产</span><strong>{{ money(total) }}</strong>
      </div>
      <div class="metric">
        <span>总负债</span><strong>{{ money(store.data?.assets.debt || 0) }}</strong>
      </div>
    </div>
    <div class="form-layout detail-block">
      <label class="field-label">统计月份<input v-model="month" type="month" /></label>
    </div>
    <div class="metric-grid three">
      <div class="metric">
        <span>本月收入</span><strong class="positive">{{ money(income) }}</strong>
      </div>
      <div class="metric">
        <span>本月支出</span><strong>{{ money(expense) }}</strong>
      </div>
      <div class="metric">
        <span>本月结余</span><strong>{{ money(income - expense) }}</strong>
      </div>
    </div>
    <h2 class="section-title">
      资产构成<RouterLink class="text-link" to="/finance/assets">管理资产 ›</RouterLink>
    </h2>
    <div class="progress-track"><span :style="{ width: cashPercent + '%' }" /></div>
    <div class="meta-row">
      <span>现金及存款 {{ cashPercent }}%</span
      ><span>其他资产 {{ total ? 100 - cashPercent : 0 }}%</span>
    </div>
    <h2 class="section-title">月度收支</h2>
    <div class="finance-bars">
      <div v-for="m in months" :key="m.key">
        <div class="bar-pair">
          <span
            class="income-bar"
            :style="{ height: (m.income / max) * 110 + 'px' }"
            :title="money(m.income)"
          ></span
          ><span
            class="expense-bar"
            :style="{ height: (m.expense / max) * 110 + 'px' }"
            :title="money(m.expense)"
          ></span>
        </div>
        <p>{{ m.label }}</p>
        <small>{{ money(m.income) }} / {{ money(m.expense) }}</small>
      </div>
    </div>
    <p class="info-note">绿色 收入 · 灰色 支出；统计仅包含当前工作区已有记录。</p>
    <h2 class="section-title">
      最近收支<RouterLink class="text-link" to="/finance/records">全部 ›</RouterLink>
    </h2>
    <RouterLink
      v-for="r in [...(store.data?.financeRecords || [])]
        .sort((a, b) => b.date.localeCompare(a.date))
        .slice(0, 3)"
      :key="r.id"
      to="/finance/records"
      class="list-row"
      ><span class="row-content"
        ><strong>{{ r.note || r.category }}</strong
        ><small>{{ r.date }} · {{ r.category }}</small></span
      ><span :class="r.type === 'INCOME' ? 'positive' : 'muted'"
        >{{ r.type === 'INCOME' ? '+' : '−' }}{{ money(r.amount) }}</span
      ></RouterLink
    >
    <p class="info-note">收支记录与手动资产快照采用独立口径，不自动修改彼此余额。</p>
  </WorkspaceGate>
</template>
<style scoped>
.finance-bars {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  text-align: center;
}
.bar-pair {
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 8px;
}
.bar-pair span {
  width: 20px;
  border-radius: 3px 3px 0 0;
  min-height: 1px;
}
.income-bar {
  background: var(--color-positive);
}
.expense-bar {
  background: var(--color-muted);
}
.finance-bars p {
  font-size: 12px;
  color: var(--color-muted);
}
.finance-bars small {
  font-size: 9px;
  color: var(--color-subtle);
}
</style>
