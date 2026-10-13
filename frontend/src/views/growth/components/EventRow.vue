<script setup lang="ts">
import type { GrowthEvent } from '@/types/workspace'
import { displayDate } from '@/utils/display'
import { signed } from '@/utils/chart'
defineProps<{ event: GrowthEvent; category: string }>()
</script>
<template>
  <RouterLink :to="`/growth/events/${event.id}`" class="event-row list-row"
    ><span class="category-badge" aria-hidden="true">{{ Array.from(category)[0] }}</span
    ><span class="row-content"
      ><strong>{{ event.title }}</strong
      ><small>{{ category }} · {{ displayDate(event.date) }}</small
      ><span class="event-importance">用户参考 · {{ event.importance }}</span></span
    ><span
      class="event-impact"
      :class="{
        pending: event.contribution === null,
        negative: event.contribution !== null && event.contribution < 0,
      }"
      ><span class="sr-only">{{
        event.contribution === null ? '指数评估状态' : '演示指数贡献'
      }}</span
      >{{ event.contribution === null ? '待评估' : signed(event.contribution) }}</span
    ></RouterLink
  >
</template>
<style scoped>
.event-row {
  padding-block: 16px;
  align-items: flex-start;
}
.category-badge {
  margin-top: 2px;
}
.row-content strong {
  overflow-wrap: anywhere;
}
.row-content small {
  margin-top: 5px;
}
.event-importance {
  display: block;
  color: var(--color-muted);
  font-size: 10px;
  margin-top: 4px;
  line-height: 1.6;
}
.event-impact {
  color: var(--color-positive);
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  margin-top: 2px;
  font-variant-numeric: tabular-nums;
}
.event-impact.pending {
  color: var(--color-muted);
  font-size: 12px;
  font-weight: 400;
}
.event-impact.negative {
  color: var(--color-error);
}
</style>
