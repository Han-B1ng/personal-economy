<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useWorkspace } from '@/hooks/useWorkspace'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import SegmentedControl from '@/components/common/SegmentedControl.vue'
import GoalCard from './components/GoalCard.vue'
import TaskRow from './components/TaskRow.vue'
import EventRow from './components/EventRow.vue'
import { categoryName } from '@/utils/display'
const route = useRoute(),
  router = useRouter(),
  store = useWorkspace()
const tab = computed(() => String(route.meta.growthTab || '目标'))
const kind = computed(
  () => ({ 目标: 'goals', 待办: 'tasks', 事件: 'events' })[tab.value] || 'goals',
)
const category = ref('全部'),
  month = ref(''),
  error = ref(''),
  visible = ref(20),
  pendingTask = ref(''),
  feedback = ref('')
const allEvents = computed(() => store.data?.events || [])
// Disabled categories remain available for filtering their historical records.
const categories = computed(
  () =>
    store.data?.categories.filter(
      (c) => c.active || allEvents.value.some((e) => e.categoryId === c.id),
    ) || [],
)
const goals = computed(() => store.data?.goals || [])
const goalGroups = computed(() => [
  {
    status: 'ACTIVE',
    title: '进行中的目标',
    goals: goals.value.filter((g) => g.status === 'ACTIVE'),
  },
  {
    status: 'COMPLETED',
    title: '已完成的目标',
    goals: goals.value.filter((g) => g.status === 'COMPLETED'),
  },
])
const tasks = computed(() =>
  [...(store.data?.tasks || [])].sort(
    (a, b) =>
      Number(a.completed) - Number(b.completed) ||
      a.dueDate.localeCompare(b.dueDate) ||
      a.dueTime.localeCompare(b.dueTime),
  ),
)
const events = computed(() =>
  [...allEvents.value]
    .filter(
      (e) =>
        (category.value === '全部' || e.categoryId === category.value) &&
        (!month.value || e.date.startsWith(month.value)),
    )
    .sort((a, b) => b.date.localeCompare(a.date)),
)
const filtered = computed(() => category.value !== '全部' || !!month.value)
watch([category, month], () => {
  visible.value = 20
})
function clearFilters() {
  category.value = '全部'
  month.value = ''
}
async function toggle(id: string, completed: boolean) {
  if (store.saving) return
  error.value = ''
  feedback.value = ''
  pendingTask.value = id
  try {
    await store.save(`/tasks/${id}/status`, 'PATCH', { completed })
    feedback.value = completed ? '待办已完成，目标手动进度和指数保持不变。' : '已撤销待办完成状态。'
  } catch (e) {
    error.value = e instanceof Error ? e.message : '更新失败，请重试'
  } finally {
    pendingTask.value = ''
  }
}
function navigate(value: string) {
  router.push(`/growth/${{ 目标: 'goals', 待办: 'tasks', 事件: 'events' }[value]}`)
}
</script>
<template>
  <PageHeader title="成长中心" subtitle="Growth Center"
    ><RouterLink class="text-link growth-create" :to="`/growth/${kind}/new`"
      >＋ {{ tab === '目标' ? '新增目标' : tab === '待办' ? '新增待办' : '记录事件' }}</RouterLink
    ></PageHeader
  >
  <SegmentedControl
    :model-value="tab"
    :options="['目标', '待办', '事件']"
    label="成长中心模块"
    @update:model-value="navigate"
  />
  <WorkspaceGate>
    <p v-if="error" class="operation-message form-error" role="alert">{{ error }}</p>
    <p class="sr-only" role="status">{{ pendingTask ? '正在保存待办状态' : feedback }}</p>
    <template v-if="tab === '目标'">
      <section v-for="group in goalGroups" :key="group.status" :aria-label="group.title">
        <h2 class="section-title">
          {{ group.title }}<small>{{ group.goals.length }} 个</small>
        </h2>
        <GoalCard
          v-for="goal in group.goals"
          :key="goal.id"
          :goal="goal"
          :category="categoryName(goal.categoryId, store.data?.categories || [])"
        />
        <div v-if="!group.goals.length" class="growth-empty">
          <p>暂无{{ group.status === 'ACTIVE' ? '进行中' : '已完成' }}目标</p>
          <RouterLink v-if="group.status === 'ACTIVE'" to="/growth/goals/new" class="text-link"
            >创建一个成长目标 ›</RouterLink
          >
        </div>
      </section>
    </template>
    <template v-else-if="tab === '待办'">
      <h2 class="section-title">
        全部待办<small
          >已完成 {{ tasks.filter((t) => t.completed).length }} / {{ tasks.length }}</small
        >
      </h2>
      <TaskRow
        v-for="task in tasks"
        :key="task.id"
        :task="task"
        :goal-title="goals.find((g) => g.id === task.goalId)?.title"
        :busy="pendingTask === task.id"
        :disabled="store.saving"
        @toggle="toggle"
      />
      <div v-if="!tasks.length" class="growth-empty">
        <p>还没有待办，从一个具体行动开始。</p>
        <RouterLink to="/growth/tasks/new" class="text-link">添加待办 ›</RouterLink>
      </div>
      <p class="info-note">待办完成仅更新自身状态，不自动修改目标手动进度或成长指数。</p>
    </template>
    <template v-else>
      <div class="filter-row" role="group" aria-label="事件分类">
        <button
          type="button"
          class="filter-chip"
          :class="{ active: category === '全部' }"
          :aria-pressed="category === '全部'"
          @click="category = '全部'"
        >
          全部
        </button>
        <button
          v-for="item in categories"
          :key="item.id"
          type="button"
          class="filter-chip"
          :class="{ active: category === item.id }"
          :aria-pressed="category === item.id"
          @click="category = item.id"
        >
          {{ item.name }}{{ item.active ? '' : ' · 已停用' }}
        </button>
      </div>
      <div class="event-month-row">
        <label class="month-field"
          >记录月份<input v-model="month" type="month" aria-label="筛选记录月份" /></label
        ><button v-if="filtered" type="button" class="text-link reset-filter" @click="clearFilters">
          清除筛选
        </button>
      </div>
      <h2 class="section-title">
        成长记录<small>{{ events.length }} 条</small>
      </h2>
      <EventRow
        v-for="event in events.slice(0, visible)"
        :key="event.id"
        :event="event"
        :category="categoryName(event.categoryId, store.data?.categories || [])"
      />
      <div v-if="!events.length" class="growth-empty">
        <p>{{ filtered ? '没有符合筛选条件的事件' : '还没有成长事件' }}</p>
        <button v-if="filtered" type="button" class="text-link" @click="clearFilters">
          查看全部事件 ›</button
        ><RouterLink v-else to="/growth/events/new" class="text-link"
          >记录第一条成长事件 ›</RouterLink
        >
      </div>
      <button
        v-if="events.length > visible"
        type="button"
        class="text-link load-more"
        @click="visible += 20"
      >
        显示更多（剩余 {{ Math.max(0, events.length - visible) }} 条）
      </button>
    </template>
    <p class="local-note">
      {{
        store.data?.source === 'preview-mysql'
          ? '服务器测试工作区'
          : store.data?.source === 'local-mysql'
            ? '本机 MySQL 工作区'
            : '本地文件工作区'
      }}
      · 新事件待评估
    </p>
  </WorkspaceGate>
</template>
<style scoped>
.growth-create {
  white-space: nowrap;
}
.operation-message {
  margin-top: 18px;
  line-height: 1.7;
}
.growth-empty {
  padding: 20px 16px;
  margin-bottom: 12px;
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-card);
}
.growth-empty p {
  margin: 0;
  color: var(--color-muted);
  font-size: 13px;
  line-height: 1.8;
}
.growth-empty .text-link {
  background: none;
  border: 0;
  padding: 0;
  margin-top: 4px;
}
.event-month-row {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
.month-field {
  display: grid;
  gap: 8px;
  min-width: 0;
  flex: 1;
  font-size: 12px;
  color: var(--color-muted);
}
.month-field input {
  width: 100%;
  min-width: 0;
  min-height: 44px;
  padding: 10px 12px;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-input);
  color-scheme: dark;
  font: inherit;
  font-size: 13px;
}
.reset-filter {
  border: 0;
  padding: 0;
  background: none;
  font: inherit;
  font-size: 12px;
  white-space: nowrap;
}
.load-more {
  width: 100%;
  justify-content: center;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-input);
  margin-top: 16px;
}
</style>
