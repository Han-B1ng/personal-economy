<script setup lang="ts">
import { ref, computed } from 'vue'
import { useWorkspace } from '@/hooks/useWorkspace'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import SegmentedControl from '@/components/common/SegmentedControl.vue'
import { money, today, displayDate } from '@/utils/display'
const store = useWorkspace(),
  type = ref('全部'),
  month = ref(today().slice(0, 7)),
  visible = ref(20)
const monthly = computed(
  () =>
    store.data?.financeRecords.filter((r) => !month.value || r.date.startsWith(month.value)) || [],
)
const records = computed(() =>
  [...monthly.value]
    .filter(
      (r) => type.value === '全部' || r.type === (type.value === '收入' ? 'INCOME' : 'EXPENSE'),
    )
    .sort((a, b) => b.date.localeCompare(a.date)),
)
const income = computed(() =>
    monthly.value.filter((r) => r.type === 'INCOME').reduce((s, r) => s + r.amount, 0),
  ),
  expense = computed(() =>
    monthly.value.filter((r) => r.type === 'EXPENSE').reduce((s, r) => s + r.amount, 0),
  )
</script>
<template>
  <PageHeader title="财务记录" back="/finance"
    ><RouterLink class="text-link" to="/finance/records/new">＋ 新增</RouterLink></PageHeader
  ><WorkspaceGate
    ><SegmentedControl v-model="type" :options="['全部', '收入', '支出']" label="收支类型" />
    <div class="form-layout detail-block">
      <label class="field-label">记录月份<input v-model="month" type="month" /></label>
    </div>
    <div class="metric-grid">
      <div class="metric">
        <span>本月收入</span><strong class="positive">{{ money(income) }}</strong>
      </div>
      <div class="metric">
        <span>本月支出</span><strong>{{ money(expense) }}</strong>
      </div>
    </div>
    <div v-for="r in records.slice(0, visible)" :key="r.id" class="list-row">
      <span class="row-content"
        ><strong>{{ r.category }}</strong
        ><small>{{ displayDate(r.date) }} · {{ r.note || '无备注' }}</small></span
      ><span :class="r.type === 'INCOME' ? 'positive' : 'muted'"
        >{{ r.type === 'INCOME' ? '+' : '−' }}{{ money(r.amount) }}</span
      >
    </div>
    <p v-if="!records.length" class="empty-note">
      本月暂无{{ type === '全部' ? '财务' : type }}记录
    </p>
    <button v-if="records.length > visible" class="filter-chip text-link" @click="visible += 20">
      加载更多
    </button>
    <p class="info-note">
      日常支出是生活记录，不影响成长事件评价。当前设计未包含财务记录编辑与删除流程。
    </p></WorkspaceGate
  >
</template>
