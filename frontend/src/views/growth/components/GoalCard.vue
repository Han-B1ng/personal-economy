<script setup lang="ts">
import type { Goal } from '@/types/workspace'
import AppIcon from '@/components/common/AppIcon.vue'
import { displayDate } from '@/utils/display'
defineProps<{ goal: Goal; category: string }>()
</script>
<template>
  <RouterLink :to="`/growth/goals/${goal.id}`" class="goal-card card card-link">
    <div class="goal-heading">
      <h3>{{ goal.title }}</h3>
      <AppIcon name="chevron" :size="16" />
    </div>
    <div class="meta-row">
      <span>{{ category }}成长</span
      ><span class="goal-status" :class="{ completed: goal.status === 'COMPLETED' }">{{
        goal.status === 'COMPLETED' ? '已完成' : '进行中'
      }}</span>
    </div>
    <div class="goal-progress-heading">
      <span>手动进度</span><strong>{{ goal.progress }}<small>%</small></strong>
    </div>
    <div
      class="progress-track"
      role="progressbar"
      :aria-label="`${goal.title}的手动进度`"
      :aria-valuenow="goal.progress"
      :aria-valuemin="0"
      :aria-valuemax="100"
      :aria-valuetext="`手动进度${goal.progress}%，与待办完成率独立`"
    >
      <span :style="{ width: goal.progress + '%' }" />
    </div>
    <p class="goal-date">
      {{ goal.status === 'COMPLETED' ? '完成于' : '预计完成' }}
      {{ displayDate(goal.completedDate || goal.dueDate) }}
    </p>
  </RouterLink>
</template>
<style scoped>
.goal-card {
  padding: 18px;
  border: 1px solid var(--color-border);
}
.goal-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.goal-heading h3 {
  margin: 0 0 12px;
  line-height: 1.5;
  overflow-wrap: anywhere;
}
.goal-heading :deep(svg) {
  flex: 0 0 auto;
  margin-top: 5px;
  color: var(--color-muted);
}
.goal-status {
  color: var(--color-accent);
  font-size: 11px;
  white-space: nowrap;
}
.goal-status.completed {
  color: var(--color-muted);
}
.goal-progress-heading {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-top: 20px;
}
.goal-progress-heading > span {
  font-size: 11px;
  color: var(--color-muted);
}
.goal-progress-heading strong {
  font-size: 22px;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}
.goal-progress-heading small {
  margin-left: 2px;
  font-size: 12px;
  color: var(--color-muted);
}
.progress-track {
  margin: 10px 0 12px;
  height: 4px;
}
.goal-date {
  margin: 0;
  color: var(--color-muted);
  font-size: 11px;
}
</style>
