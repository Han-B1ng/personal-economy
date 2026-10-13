<script setup lang="ts">
import { useRoute } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import { navigation } from '@/router/navigation'
const route = useRoute()
function active(path: string) {
  return path === '/'
    ? route.path === '/'
    : route.path === path || route.path.startsWith(`${path}/`)
}
</script>

<template>
  <nav class="bottom-navigation" aria-label="主导航">
    <RouterLink
      v-for="item in navigation"
      :key="item.path"
      :to="item.path"
      :class="{ active: active(item.path) }"
      :aria-current="active(item.path) ? 'page' : undefined"
    >
      <AppIcon :name="item.icon" />
      <span>{{ item.label }}</span>
    </RouterLink>
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
  padding-bottom: max(23px, env(safe-area-inset-bottom));
  background: var(--color-background);
  border-top: 1px solid var(--color-border);
  z-index: 10;
}
a {
  display: flex;
  min-height: var(--nav-height);
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  color: var(--color-nav);
  font-size: 10px;
  text-decoration: none;
}
a.active {
  color: var(--color-accent);
}
a:active {
  background: var(--color-surface);
}
</style>
