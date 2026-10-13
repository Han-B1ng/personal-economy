<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import { navigation } from '@/router/navigation'
const route = useRoute()
function active(path: string) {
  return path === '/'
    ? route.path === '/'
    : route.path === path || route.path.startsWith(`${path}/`)
}
const activeIndex = computed(() => navigation.findIndex((item) => active(item.path)))
</script>

<template>
  <nav class="bottom-navigation" aria-label="主导航">
    <span
      v-if="activeIndex >= 0"
      class="nav-indicator"
      :style="{ transform: `translateX(${activeIndex * 100}%)` }"
      aria-hidden="true"
    />
    <RouterLink
      v-for="item in navigation"
      :key="item.path"
      :to="item.path"
      :class="{ active: active(item.path) }"
      :aria-current="active(item.path) ? 'page' : undefined"
      ><AppIcon :name="item.icon" /><span>{{ item.label }}</span></RouterLink
    >
  </nav>
</template>

<style scoped>
.bottom-navigation {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: min(100%, 480px);
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  padding-bottom: max(16px, env(safe-area-inset-bottom));
  background: var(--color-background);
  border-top: 1px solid var(--color-border);
  z-index: 10;
  isolation: isolate;
}
.nav-indicator {
  position: absolute;
  top: 7px;
  left: 0;
  width: 20%;
  height: 30px;
  transition: transform var(--motion-standard) var(--ease-emphasized);
  pointer-events: none;
}
.nav-indicator::after {
  content: '';
  position: absolute;
  width: 40px;
  height: 100%;
  left: calc(50% - 20px);
  border-radius: 10px;
  background: var(--color-accent-surface);
}
a {
  position: relative;
  display: flex;
  min-height: var(--nav-height);
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  color: var(--color-nav);
  font-size: 11px;
  font-weight: 500;
  text-decoration: none;
  -webkit-tap-highlight-color: transparent;
  transition:
    color var(--motion-fast),
    opacity var(--motion-fast);
}
a :deep(svg) {
  transition: transform var(--motion-fast) var(--ease-standard);
}
a.active {
  color: var(--color-accent);
}
a:active :deep(svg) {
  transform: scale(0.9);
}
a:focus-visible {
  outline-offset: -4px;
  border-radius: var(--radius-input);
}
@media (hover: hover) and (pointer: fine) {
  a:hover {
    color: var(--color-text);
  }
  a.active:hover {
    color: var(--color-accent);
  }
}
@media (prefers-reduced-motion: reduce) {
  a:active :deep(svg) {
    transform: none;
  }
}
</style>
