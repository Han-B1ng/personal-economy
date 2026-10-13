<script setup lang="ts">
import { useRoute } from 'vue-router'
import BottomNavigation from '@/layout/components/BottomNavigation.vue'
const route = useRoute()
</script>

<template>
  <div class="app-shell">
    <a class="skip-link" href="#page-content">跳到页面内容</a>
    <main
      id="page-content"
      class="page-content"
      :class="{ flow: route.meta.hideNavigation }"
      tabindex="-1"
    >
      <RouterView />
    </main>
    <BottomNavigation v-if="!route.meta.hideNavigation" />
  </div>
</template>

<style scoped>
.app-shell {
  width: min(100%, 480px);
  min-height: 100dvh;
  margin-inline: auto;
}
.page-content {
  padding: max(65px, env(safe-area-inset-top)) var(--page-gutter)
    calc(var(--nav-height) + max(23px, env(safe-area-inset-bottom)) + 8px);
}
.skip-link {
  position: fixed;
  top: 8px;
  left: 8px;
  padding: 12px;
  background: var(--color-surface);
  z-index: 20;
  transform: translateY(-150%);
}
.skip-link:focus {
  transform: translateY(0);
}
.page-content.flow {
  padding-bottom: max(32px, env(safe-area-inset-bottom));
}
</style>
