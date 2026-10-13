<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useWorkspace } from '@/hooks/useWorkspace'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import SegmentedControl from '@/components/common/SegmentedControl.vue'
import { categoryName, displayDate } from '@/utils/display'
import { signed } from '@/utils/chart'
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
  visible = ref(20)
const categories = computed(() => store.data?.categories.filter((c) => c.active) || [])
const goals = computed(() => store.data?.goals || [])
const tasks = computed(() =>
  [...(store.data?.tasks || [])].sort(
    (a, b) => Number(a.completed) - Number(b.completed) || a.dueDate.localeCompare(b.dueDate),
  ),
)
const events = computed(() =>
  [...(store.data?.events || [])]
    .filter(
      (e) =>
        (category.value === '全部' || e.categoryId === category.value) &&
        (!month.value || e.date.startsWith(month.value)),
    )
    .sort((a, b) => b.date.localeCompare(a.date)),
)
async function toggle(id: string, completed: boolean) {
  error.value = ''
  try {
    await store.save(`/tasks/${id}/status`, 'PATCH', { completed })
  } catch (e) {
    error.value = e instanceof Error ? e.message : '更新失败'
  }
}
function navigate(value: string) {
  router.push(`/growth/${{ 目标: 'goals', 待办: 'tasks', 事件: 'events' }[value]}`)
}
</script>
<template>
  <PageHeader title="成长中心" subtitle="Growth Center"
    ><RouterLink class="text-link" :to="`/growth/${kind}/new`"
      >＋ {{ tab === '目标' ? '新增目标' : tab === '待办' ? '新增待办' : '记录' }}</RouterLink
    ></PageHeader
  >
  <SegmentedControl
    :model-value="tab"
    :options="['目标', '待办', '事件']"
    label="成长中心模块"
    @update:model-value="navigate"
  />
  <WorkspaceGate>
    <p v-if="error" class="form-error" role="alert">{{ error }}</p>
    <template v-if="tab === '目标'">
      <template v-for="status in ['ACTIVE', 'COMPLETED']" :key="status">
        <h2 class="section-title">
          {{ status === 'ACTIVE' ? '进行中的目标' : '已完成的目标'
          }}<small>{{ goals.filter((g) => g.status === status).length }} 个</small>
        </h2>
        <RouterLink
          v-for="goal in goals.filter((g) => g.status === status)"
          :key="goal.id"
          :to="`/growth/goals/${goal.id}`"
          class="card card-link"
        >
          <h3>{{ goal.title }}</h3>
          <div class="meta-row">
            <span>{{ categoryName(goal.categoryId, store.data?.categories || []) }}成长</span
            ><span class="accent">{{ status === 'ACTIVE' ? '进行中' : '已完成' }}</span>
          </div>
          <div class="progress-track"><span :style="{ width: goal.progress + '%' }" /></div>
          <div class="meta-row">
            <span>{{ goal.progress }}%</span
            ><span
              >{{ status === 'ACTIVE' ? '预计完成' : '已完成' }}
              {{ displayDate(goal.completedDate || goal.dueDate) }}</span
            >
          </div>
        </RouterLink>
        <p v-if="!goals.some((g) => g.status === status)" class="empty-note">
          暂无{{ status === 'ACTIVE' ? '进行中' : '已完成' }}目标
        </p>
      </template>
    </template>
    <template v-else-if="tab === '待办'">
      <h2 class="section-title">
        全部待办<small>已完成 {{ tasks.filter((t) => t.completed).length }} 项</small>
      </h2>
      <div v-for="task in tasks" :key="task.id" class="list-row">
        <button
          class="task-check"
          :class="{ done: task.completed }"
          :disabled="store.saving"
          :aria-label="`${task.completed ? '撤销完成' : '完成'} ${task.title}`"
          :aria-pressed="task.completed"
          @click="toggle(task.id, !task.completed)"
        >
          {{ task.completed ? '☑' : '□' }}
        </button>
        <RouterLink class="row-content card-link" :to="`/growth/tasks/${task.id}`"
          ><strong :class="{ 'task-done': task.completed }">{{ task.title }}</strong
          ><small
            >{{ goals.find((g) => g.id === task.goalId)?.title || '无关联目标' }}<br />{{
              displayDate(task.dueDate)
            }}
            {{ task.dueTime }} · {{ task.priority }}</small
          ></RouterLink
        >
      </div>
      <p v-if="!tasks.length" class="empty-note">暂无待办，添加一个具体行动吧。</p>
      <p class="info-note">待办完成仅更新自身状态，不自动修改目标手动进度或成长指数。</p>
    </template>
    <template v-else>
      <div class="filter-row" aria-label="事件分类">
        <button
          class="filter-chip"
          :class="{ active: category === '全部' }"
          @click="category = '全部'"
        >
          全部</button
        ><button
          v-for="item in categories"
          :key="item.id"
          class="filter-chip"
          :class="{ active: category === item.id }"
          @click="category = item.id"
        >
          {{ item.name }}
        </button>
      </div>
      <div class="form-layout">
        <label class="field-label"
          >记录月份<input v-model="month" type="month" aria-label="筛选记录月份"
        /></label>
      </div>
      <h2 class="section-title">
        成长记录<small>{{ events.length }} 条</small>
      </h2>
      <RouterLink
        v-for="event in events.slice(0, visible)"
        :key="event.id"
        :to="`/growth/events/${event.id}`"
        class="list-row"
        ><span class="category-badge">{{
          categoryName(event.categoryId, store.data?.categories || []).slice(0, 1)
        }}</span
        ><span class="row-content"
          ><strong>{{ event.title }}</strong
          ><small
            >{{ categoryName(event.categoryId, store.data?.categories || []) }} ·
            {{ displayDate(event.date) }} · {{ event.importance }}</small
          ></span
        ><span :class="event.contribution === null ? 'muted' : 'positive'">{{
          event.contribution === null ? '待评估' : signed(event.contribution)
        }}</span></RouterLink
      >
      <p v-if="!events.length" class="empty-note">暂无成长事件，记录一件值得记住的成长吧。</p>
      <button v-if="events.length > visible" class="text-link filter-chip" @click="visible += 20">
        加载更多
      </button>
    </template>
    <p class="local-note">本地演示工作区 · 新事件尚未接入指数评估</p>
  </WorkspaceGate>
</template>
