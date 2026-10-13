<script setup lang="ts">
import AppButton from '@/components/common/AppButton.vue'
defineProps<{ title: string; description: string; actionLabel?: string; busy?: boolean }>()
defineEmits<{ action: [] }>()
</script>

<template>
  <section class="page-state" :aria-busy="busy || undefined">
    <p v-if="busy" role="status">正在加载…</p>
    <h2>{{ title }}</h2>
    <p>{{ description }}</p>
    <AppButton v-if="actionLabel" variant="secondary" @click="$emit('action')">{{
      actionLabel
    }}</AppButton>
  </section>
</template>

<style scoped>
.page-state {
  padding: 48px 0;
}
h2 {
  font-size: 18px;
  margin: 0 0 12px;
}
p {
  margin: 0 0 24px;
  color: var(--color-muted);
  font-size: 14px;
  line-height: 1.8;
}
</style>
