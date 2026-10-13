<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps<{
  modelValue: string
  options: readonly string[]
  label: string
  compact?: boolean
}>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
const selectedIndex = computed(() => Math.max(0, props.options.indexOf(props.modelValue)))
function choose(value: string) {
  if (value !== props.modelValue) emit('update:modelValue', value)
}
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
  choose(value)
  const buttons = (event.currentTarget as HTMLElement).parentElement?.querySelectorAll('button')
  buttons?.[next]?.focus()
}
</script>

<template>
  <div
    class="segmented-control"
    :class="{ compact }"
    :style="{ '--segment-count': Math.max(1, options.length), '--active-index': selectedIndex }"
    role="radiogroup"
    :aria-label="label"
  >
    <span v-if="options.length" class="segment-indicator" aria-hidden="true" />
    <button
      v-for="(option, index) in options"
      :key="option"
      type="button"
      role="radio"
      :aria-checked="modelValue === option"
      :tabindex="selectedIndex === index ? 0 : -1"
      :class="{ selected: modelValue === option }"
      @click="choose(option)"
      @keydown="navigate($event, index)"
    >
      {{ option }}
    </button>
  </div>
</template>

<style scoped>
.segmented-control {
  --segment-gap: 4px;
  position: relative;
  display: grid;
  grid-template-columns: repeat(var(--segment-count), minmax(0, 1fr));
  gap: var(--segment-gap);
  isolation: isolate;
}
.segment-indicator {
  position: absolute;
  inset-block: 2px;
  left: 0;
  width: calc((100% - (var(--segment-count) - 1) * var(--segment-gap)) / var(--segment-count));
  background: var(--color-accent-surface);
  border-radius: var(--radius-input);
  transform: translateX(calc(var(--active-index) * (100% + var(--segment-gap))));
  transition: transform var(--motion-standard) var(--ease-emphasized);
  pointer-events: none;
}
button {
  position: relative;
  min-width: 0;
  min-height: 44px;
  padding: 0 4px;
  background: transparent;
  border: 0;
  border-radius: var(--radius-input);
  color: var(--color-muted);
  font-size: 14px;
  font-weight: 500;
  transition:
    color var(--motion-fast),
    transform var(--motion-fast) var(--ease-standard);
}
button.selected {
  color: var(--color-accent);
}
button:active {
  transform: scale(0.96);
}
.compact button {
  font-size: 12px;
}
.compact .segment-indicator {
  inset-block: 6px;
}
@media (hover: hover) and (pointer: fine) {
  button:hover:not(.selected) {
    color: var(--color-text);
  }
}
@media (prefers-reduced-motion: reduce) {
  button:active {
    transform: none;
  }
}
</style>
