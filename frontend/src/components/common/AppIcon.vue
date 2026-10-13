<script setup lang="ts">
export type IconName =
  'home' | 'growth' | 'analytics' | 'finance' | 'profile' | 'chevron' | 'arrow-left' | 'close'
defineProps<{ name: IconName; size?: number }>()
const paths: Record<IconName, string> = {
  home: 'M3 11L12 3L21 11V21H15V14H9V21H3Z',
  growth: 'M4 3H20V21H4ZM8 8H16M8 13H16M8 18H13',
  analytics: 'M2 22V13M9 22V7M16 22V3M22 22V10',
  finance: 'M2 4H20V19H2ZM20 9H23V15H15V9Z',
  profile: 'M4 22C4 16.67 6.67 14 12 14C17.33 14 20 16.67 20 22M8 7a4 4 0 1 0 8 0a4 4 0 1 0-8 0',
  chevron: 'M9 6L15 12L9 18',
  'arrow-left': 'M15 5L8 12L15 19M8 12H22',
  close: 'M6 6L18 18M18 6L6 18',
}
</script>

<template>
  <svg
    :width="size || 24"
    :height="size || 24"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.6"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    focusable="false"
  >
    <path :d="paths[name]" />
  </svg>
</template>
