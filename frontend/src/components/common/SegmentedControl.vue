<script setup lang="ts">
const props = defineProps<{
  modelValue: string
  options: readonly string[]
  label: string
  compact?: boolean
}>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
function navigate(event: KeyboardEvent, index: number) {
  let next: number
  switch (event.key) {
    case 'ArrowRight':
    case 'ArrowDown':
      next = (index + 1) % props.options.length
      break
    case 'ArrowLeft':
    case 'ArrowUp':
      next = (index - 1 + props.options.length) % props.options.length
      break
    case 'Home':
      next = 0
      break
    case 'End':
      next = props.options.length - 1
      break
    default:
      return
  }
  const value = props.options[next]
  if (!value) return
  event.preventDefault()
  emit('update:modelValue', value)
  const buttons = (event.currentTarget as HTMLElement).parentElement?.querySelectorAll('button')
  buttons?.[next]?.focus()
}
</script>

<template>
  <div class="segmented-control" :class="{ compact }" role="radiogroup" :aria-label="label">
    <button
      v-for="(option, index) in options"
      :key="option"
      type="button"
      role="radio"
      :aria-checked="modelValue === option"
      :tabindex="modelValue === option ? 0 : -1"
      :class="{ selected: modelValue === option }"
      @click="emit('update:modelValue', option)"
      @keydown="navigate($event, index)"
    >
      {{ option }}
    </button>
  </div>
</template>

<style scoped>
.segmented-control {
  display: flex;
  gap: 4px;
}
button {
  flex: 1;
  min-width: 0;
  min-height: 44px;
  padding: 0 4px;
  background: transparent;
  border: 0;
  border-radius: 8px;
  color: var(--color-muted);
  font-size: 14px;
  transition:
    color var(--motion-duration),
    background var(--motion-duration);
}
button.selected {
  background: var(--color-accent-surface);
  color: var(--color-accent);
  font-weight: 600;
}
.compact button {
  font-size: 12px;
  position: relative;
  isolation: isolate;
  background: transparent;
}
.compact button.selected::before {
  content: '';
  position: absolute;
  inset: 6px 0;
  border-radius: 8px;
  background: var(--color-accent-surface);
  z-index: -1;
}
</style>
