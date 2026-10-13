<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { usePageTransition } from '@/hooks/usePageTransition'
import BottomNavigation from '@/layout/components/BottomNavigation.vue'
const route = useRoute()
const content = ref<HTMLElement>()
const { transition, disableLeavingPage, enableEnteringPage } = usePageTransition()
watch(
  () => route.path,
  async () => {
    await nextTick()
    // Keep the new page reachable by keyboard without overriding restored scroll.
    content.value?.focus({ preventScroll: true })
  },
  { flush: 'post' },
)
</script>

<template>
  <div class="app-shell">
    <a class="skip-link" href="#page-content">跳到页面内容</a>
    <main
      id="page-content"
      ref="content"
      class="page-content"
      :class="{ flow: route.meta.hideNavigation }"
      tabindex="-1"
      :aria-label="String(route.meta.title || '首页')"
    >
      <div class="route-stage">
        <RouterView v-slot="{ Component, route: pageRoute }">
          <Transition
            :name="transition"
            @before-leave="disableLeavingPage"
            @before-enter="enableEnteringPage"
          >
            <div :key="pageRoute.path" class="page-view"><component :is="Component" /></div>
          </Transition>
        </RouterView>
      </div>
    </main>
    <BottomNavigation v-if="!route.meta.hideNavigation" />
  </div>
</template>

<style scoped>
.app-shell {
  width: min(100%, 480px);
  min-height: 100dvh;
  margin-inline: auto;
  background: var(--color-background);
}
.page-content {
  padding: calc(env(safe-area-inset-top) + 20px) var(--page-gutter)
    calc(var(--nav-height) + max(16px, env(safe-area-inset-bottom)) + 24px);
  outline: none;
}
.route-stage {
  position: relative;
}
.page-view {
  min-width: 0;
}
.skip-link {
  position: fixed;
  top: 8px;
  left: 8px;
  padding: 12px;
  background: var(--color-surface-raised);
  color: var(--color-text);
  border-radius: var(--radius-input);
  z-index: 30;
  transform: translateY(-150%);
}
.skip-link:focus {
  transform: translateY(0);
}
.page-content.flow {
  padding-bottom: max(32px, env(safe-area-inset-bottom));
}
@media (min-width: 560px) {
  .app-shell {
    border-inline: 1px solid var(--color-border);
  }
}
</style>
