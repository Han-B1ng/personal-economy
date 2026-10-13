<script setup lang="ts">
import { useRouter } from 'vue-router'
import AppIcon from './AppIcon.vue'
const props = defineProps<{ title: string; subtitle?: string; back?: string }>()
const router = useRouter()
function goBack() {
  if (window.history.state?.back) router.back()
  else router.push(props.back || '/')
}
</script>
<template>
  <header class="page-heading">
    <button v-if="back" class="back-button" aria-label="返回" @click="goBack">
      <AppIcon name="arrow-left" :size="20" />
    </button>
    <div class="heading-row">
      <h1>{{ title }}</h1>
      <slot />
    </div>
    <p v-if="subtitle">{{ subtitle }}</p>
  </header>
</template>
