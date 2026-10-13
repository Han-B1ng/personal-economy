<script setup lang="ts">
import { useWorkspace } from '@/hooks/useWorkspace'
import PageState from './PageState.vue'
const store = useWorkspace()
</script>
<template>
  <PageState
    v-if="store.loading && !store.data"
    title="正在加载"
    description="正在读取本地工作区"
    busy
  />
  <PageState
    v-else-if="!store.data"
    title="暂时无法加载"
    :description="store.error || '请确认后端已启动'"
    action-label="重试"
    @action="store.load(true)"
  />
  <slot v-else />
</template>
