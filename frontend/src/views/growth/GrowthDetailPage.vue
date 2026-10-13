<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useWorkspace } from '@/hooks/useWorkspace'
import { categoryName, displayDate } from '@/utils/display'
import { signed } from '@/utils/chart'
import type { Goal, Task, GrowthEvent } from '@/types/workspace'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import AppButton from '@/components/common/AppButton.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
const route = useRoute(),
  router = useRouter(),
  store = useWorkspace(),
  confirm = ref(false),
  error = ref('')
let mounted = true
onBeforeUnmount(() => {
  mounted = false
})
const kind = computed(() => String(route.meta.kind)),
  id = computed(() => String(route.params.id)),
  title = computed(() => ({ goals: '目标', tasks: '待办', events: '成长事件' })[kind.value])
const item = computed(() =>
  store.data?.[kind.value as 'goals' | 'tasks' | 'events'].find((v) => v.id === id.value),
)
const goal = computed(() => (kind.value === 'goals' ? (item.value as Goal) : undefined)),
  task = computed(() => (kind.value === 'tasks' ? (item.value as Task) : undefined)),
  event = computed(() => (kind.value === 'events' ? (item.value as GrowthEvent) : undefined))
const relatedTasks = computed(() => store.data?.tasks.filter((t) => t.goalId === id.value) || [])
async function action() {
  if (store.saving) return
  const originPath = route.fullPath
  error.value = ''
  try {
    if (goal.value) {
      await store.save(`/goals/${id.value}/complete`, 'POST')
      if (mounted && route.fullPath === originPath)
        await router.push(`/growth/goals/${id.value}/completed`)
    } else if (task.value) {
      await store.save(`/tasks/${id.value}/status`, 'PATCH', { completed: !task.value.completed })
      if (mounted && route.fullPath === originPath)
        await router.push(`/growth/tasks/${id.value}/completed`)
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  }
}
async function remove() {
  if (store.saving) return
  const originPath = route.fullPath
  error.value = ''
  try {
    await store.save(`/${kind.value}/${id.value}`, 'DELETE')
    confirm.value = false
    if (mounted && route.fullPath === originPath) await router.push(`/growth/${kind.value}`)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败'
    confirm.value = false
  }
}
</script>
<template>
  <PageHeader :title="`${title}详情`" :back="`/growth/${kind}`" />
  <WorkspaceGate
    ><template v-if="item">
      <div class="meta-row">
        <span>{{
          goal
            ? categoryName(goal.categoryId, store.data!.categories) + '成长'
            : task
              ? task.priority
              : categoryName(event!.categoryId, store.data!.categories)
        }}</span
        ><span class="accent">{{
          goal
            ? goal.status === 'COMPLETED'
              ? '已完成'
              : '进行中'
            : task
              ? task.completed
                ? '已完成'
                : '未完成'
              : event?.evaluationStatus === 'PENDING'
                ? '待评估'
                : event?.importance
        }}</span>
      </div>
      <h2 class="detail-title">{{ item.title }}</h2>
      <p class="muted">
        {{
          goal
            ? `${displayDate(goal.startDate)} 开始 → ${displayDate(goal.dueDate)} 截止`
            : task
              ? `截止：${displayDate(task.dueDate)} ${task.dueTime}`
              : displayDate(event!.date)
        }}
      </p>
      <section class="detail-block">
        <h2>{{ event ? '成长记录' : '详细描述' }}</h2>
        <p class="detail-description">{{ item.description || '暂无描述' }}</p>
      </section>
      <template v-if="goal"
        ><div class="card">
          <div class="meta-row">
            <span>当前手动进度</span><strong class="accent">{{ goal.progress }}%</strong>
          </div>
          <div
            class="progress-track"
            role="progressbar"
            aria-label="目标手动进度"
            :aria-valuenow="goal.progress"
            :aria-valuemin="0"
            :aria-valuemax="100"
          >
            <span :style="{ width: goal.progress + '%' }" />
          </div>
          <p class="info-note">
            待办完成率：{{ relatedTasks.filter((t) => t.completed).length }} /
            {{ relatedTasks.length }}，与手动进度独立。
          </p>
        </div>
        <h2 class="section-title">
          关联待办<RouterLink class="text-link" :to="`/growth/tasks/new?goal=${goal.id}`"
            >＋ 添加待办</RouterLink
          >
        </h2>
        <RouterLink
          v-for="t in relatedTasks"
          :key="t.id"
          :to="`/growth/tasks/${t.id}`"
          class="list-row"
          ><span>{{ t.completed ? '☑' : '□' }}</span
          ><span class="row-content"
            ><strong>{{ t.title }}</strong
            ><small>{{ displayDate(t.dueDate) }} · {{ t.priority }}</small></span
          ></RouterLink
        >
        <p v-if="!relatedTasks.length" class="empty-note">暂无关联待办</p>
        <h2 class="section-title">目标更新记录</h2>
        <p class="info-note">进度历史尚待接入，当前展示已保存的手动进度。</p>
        <p class="info-note">
          标记目标完成不会自动把手动进度改成100%，也不会直接改变指数。
        </p></template
      >
      <template v-if="task"
        ><h2 class="section-title">关联成长目标</h2>
        <RouterLink v-if="task.goalId" class="menu-row" :to="`/growth/goals/${task.goalId}`"
          >{{ store.data?.goals.find((g) => g.id === task?.goalId)?.title }} ›</RouterLink
        >
        <p v-else class="muted">无关联目标</p>
        <p class="info-note">
          完成待办只更新自身状态，不自动修改目标手动进度、创建事件或更新指数。
        </p></template
      >
      <template v-if="event"
        ><div class="card">
          <p class="muted">{{ event.contribution === null ? '指数评估' : '模拟指数影响' }}</p>
          <p
            class="large-number"
            :class="
              event.contribution === null
                ? 'muted'
                : event.contribution < 0
                  ? 'negative'
                  : 'positive'
            "
          >
            {{ event.contribution === null ? '待评估' : signed(event.contribution) }}
          </p>
          <p class="info-note">
            {{
              event.contribution === null
                ? '事件已保存，成长模型尚未接入。'
                : '演示数值，不代表实际评分。'
            }}
            指数不代表人的实际价值。
          </p>
        </div>
        <RouterLink class="text-link" to="/">查看成长行情 ›</RouterLink
        ><RouterLink
          v-if="event.sourceGoalId"
          class="menu-row"
          :to="`/growth/goals/${event.sourceGoalId}`"
          >查看来源目标 ›</RouterLink
        ></template
      >
      <p v-if="error" class="form-error" role="alert">{{ error }}</p>
      <div class="button-stack">
        <AppButton
          variant="secondary"
          :disabled="store.saving"
          @click="router.push(`/growth/${kind}/${id}/edit`)"
          >编辑{{ title }}</AppButton
        ><AppButton
          v-if="goal?.status !== 'COMPLETED' && (goal || task)"
          :loading="store.saving"
          @click="action"
          >{{ task?.completed ? '撤销完成' : '标记完成' }}</AppButton
        ><AppButton
          v-if="task || event"
          variant="secondary"
          :disabled="store.saving"
          @click="confirm = true"
          >删除{{ title }}</AppButton
        >
      </div>
    </template>
    <p v-else class="empty-note">记录不存在或已被删除。</p></WorkspaceGate
  >
  <ConfirmDialog
    :open="confirm"
    :title="`删除${title}？`"
    message="删除后该记录将从本地工作区移除。当前演示行情不会因此自动重算。"
    :busy="store.saving"
    @cancel="confirm = false"
    @confirm="remove"
  />
</template>
