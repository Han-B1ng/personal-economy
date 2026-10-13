<script setup lang="ts">
import { useRouter } from 'vue-router'
import PageState from '@/components/common/PageState.vue'
const router = useRouter()
</script>

<template>
  <PageState
    title="页面不存在"
    description="请从底部导航进入已有页面。"
    action-label="返回首页"
    @action="router.push('/')"
  />
</template>
