<script setup lang="ts">
import type { Task } from '@/types/workspace'
import AppIcon from '@/components/common/AppIcon.vue'
import { displayDate } from '@/utils/display'
const props = defineProps<{ task: Task; goalTitle?: string; busy?: boolean; disabled?: boolean }>()
const emit = defineEmits<{ toggle: [id: string, completed: boolean] }>()
function toggle() {
  if (!props.disabled && !props.busy) emit('toggle', props.task.id, !props.task.completed)
}
</script>
<template>
  <div
    class="task-row list-row"
    :class="{ 'task-row--done': task.completed }"
    :aria-busy="busy || undefined"
  >
    <button
      type="button"
      class="task-control"
      :class="{ checked: task.completed }"
      :disabled="disabled && !busy"
      :aria-disabled="disabled || busy || undefined"
      :aria-busy="busy || undefined"
      :aria-pressed="task.completed"
      :aria-label="`${busy ? '正在保存' : task.completed ? '撤销完成' : '完成'} ${task.title}`"
      @click="toggle"
    >
      <span v-if="busy" class="task-spinner" aria-hidden="true" />
      <span v-else class="check-box" aria-hidden="true"
        ><AppIcon v-if="task.completed" name="check" :size="16"
      /></span>
    </button>
    <RouterLink class="row-content" :to="`/growth/tasks/${task.id}`"
      ><strong :class="{ 'task-done': task.completed }">{{ task.title }}</strong
      ><small>{{ goalTitle || '独立待办' }}</small
      ><span class="task-meta"
        ><span>{{ displayDate(task.dueDate) }} {{ task.dueTime }}</span
        ><span class="priority" :class="{ urgent: task.priority === '紧急' && !task.completed }">{{
          task.priority
        }}</span></span
      ></RouterLink
    >
    <AppIcon name="chevron" :size="14" class="row-chevron" />
  </div>
</template>
<style scoped>
.task-row {
  gap: 8px;
  align-items: flex-start;
  padding-block: 16px;
}
.task-control {
  display: grid;
  place-items: center;
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
  margin: -3px 0 0 -8px;
  border: 0;
  background: transparent;
  color: var(--color-accent);
  border-radius: 8px;
  transition:
    background-color var(--motion-fast),
    transform var(--motion-fast);
}
.check-box {
  width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  border: 1px solid var(--color-muted);
  border-radius: 6px;
  transition:
    background-color var(--motion-fast),
    border-color var(--motion-fast);
}
.checked .check-box {
  background: var(--color-accent);
  border-color: var(--color-accent);
  color: var(--color-background);
}
.checked :deep(svg) {
  animation: check-enter var(--motion-fast) var(--ease-standard);
}
.task-control:disabled {
  cursor: default;
}
.task-control:disabled:not([aria-busy='true']) {
  opacity: 0.7;
}
.task-control:active:not(:disabled) {
  transform: scale(0.92);
}
.row-content {
  display: block;
  color: var(--color-text);
  text-decoration: none;
}
.row-content strong {
  overflow-wrap: anywhere;
}
.row-content small {
  margin-top: 4px;
}
.task-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 6px;
  color: var(--color-muted);
  font-size: 11px;
  line-height: 1.6;
  flex-wrap: wrap;
}
.priority {
  padding: 1px 5px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: 10px;
}
.priority.urgent {
  color: var(--color-error);
}
.row-chevron {
  color: var(--color-muted);
  flex: 0 0 auto;
  margin-top: 10px;
}
.task-spinner {
  width: 18px;
  height: 18px;
  border: 1.5px solid var(--color-border);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: task-spin 0.8s linear infinite;
}
@keyframes task-spin {
  to {
    transform: rotate(360deg);
  }
}
@keyframes check-enter {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
@media (hover: hover) and (pointer: fine) {
  .task-control:hover:not(:disabled) {
    background: var(--color-hover);
  }
  .row-content:hover strong {
    color: var(--color-accent);
  }
}
@media (prefers-reduced-motion: reduce) {
  .task-control:active:not(:disabled) {
    transform: none;
  }
  .checked :deep(svg) {
    animation: none;
  }
}
</style>
