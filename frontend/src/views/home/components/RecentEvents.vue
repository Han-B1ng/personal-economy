<script setup lang="ts">
import AppIcon from '@/components/common/AppIcon.vue'
import { signed } from '@/utils/chart'
import type { RecentGrowthEvent } from '@/types/home'
defineProps<{ events: RecentGrowthEvent[] }>()
</script>

<template>
  <section class="recent-events" aria-labelledby="recent-events-title">
    <div class="section-heading">
      <h2 id="recent-events-title">最近成长事件</h2>
      <RouterLink to="/growth/events" class="all-events"
        >全部 <AppIcon name="chevron" :size="14"
      /></RouterLink>
    </div>
    <div v-if="!events.length" class="empty-events">
      <span>还没有成长事件</span
      ><RouterLink to="/growth/events/new" class="text-link"
        >记录第一条成长事件 <AppIcon name="chevron" :size="14"
      /></RouterLink>
    </div>
    <ul v-else>
      <li v-for="event in events" :key="event.id">
        <RouterLink :to="`/growth/events/${event.id}`" class="event-row">
          <span class="event-category" aria-hidden="true">{{ event.categoryMark }}</span>
          <span class="event-description"
            ><span class="event-title">{{ event.title }}</span
            ><span class="event-date">{{ event.date.replaceAll('-', '.') }}</span></span
          >
          <span
            class="event-contribution"
            :class="{
              negative: event.contribution !== null && event.contribution < 0,
              pending: event.contribution === null,
            }"
            ><span class="sr-only">演示指数贡献 </span
            >{{ event.contribution === null ? '待评估' : signed(event.contribution) }}</span
          >
        </RouterLink>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0;
}
h2 {
  font-size: 17px;
  font-weight: 600;
  margin: 0;
}
.all-events {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  min-width: 44px;
  min-height: 44px;
  color: var(--color-accent);
  font-size: 12px;
  text-decoration: none;
}
ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.event-row {
  display: flex;
  align-items: center;
  min-height: 60px;
  padding: 6px 0;
  border-radius: 8px;
  transition:
    background-color var(--motion-fast),
    opacity var(--motion-fast);
  gap: 12px;
  color: var(--color-text);
  text-decoration: none;
}
.event-category {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  background: var(--color-surface-raised);
  border-radius: 8px;
  color: var(--color-muted);
  font-size: 12px;
}
.event-description {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
  padding-block: 2px;
}
.event-title {
  font-size: 14px;
  font-weight: 500;
  line-height: 21px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.event-date {
  font-size: 11px;
  color: var(--color-muted);
  line-height: 16px;
}
.event-contribution {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-positive);
  white-space: nowrap;
}
.event-contribution.negative {
  color: var(--color-error);
}
li + li {
  position: relative;
}
li + li::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  left: 44px;
  border-top: 1px solid var(--color-border);
}
.event-row:active {
  opacity: 0.7;
}
.empty-events {
  color: var(--color-muted);
  font-size: 14px;
  padding: 16px 0;
}

.event-contribution.pending {
  color: var(--color-muted);
  font-size: 12px;
  font-weight: 400;
}
.empty-events {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding-top: 16px;
}
.all-events :deep(svg) {
  transition: transform var(--motion-fast) var(--ease-standard);
}
@media (hover: hover) and (pointer: fine) {
  .event-row:hover {
    background: var(--color-hover);
  }
  .all-events:hover :deep(svg) {
    transform: translateX(2px);
  }
}
@media (prefers-reduced-motion: reduce) {
  .all-events:hover :deep(svg) {
    transform: none;
  }
}
</style>
