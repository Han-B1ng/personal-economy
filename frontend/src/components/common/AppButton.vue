<script setup lang="ts">
withDefaults(
  defineProps<{
    loading?: boolean
    disabled?: boolean
    variant?: 'primary' | 'secondary'
    state?: 'default' | 'success' | 'error'
    type?: 'button' | 'submit' | 'reset'
  }>(),
  { variant: 'primary', state: 'default', type: 'button' },
)
</script>
<template>
  <button
    :type="type"
    class="app-button"
    :class="[`app-button--${variant}`, `app-button--${state}`]"
    :disabled="disabled || loading"
    :aria-busy="loading || undefined"
  >
    <Transition name="busy-icon"
      ><span v-if="loading" class="spinner" aria-hidden="true"
    /></Transition>
    <span class="button-label"><slot /></span>
  </button>
</template>
<style scoped>
.app-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  min-height: 48px;
  padding: 12px 16px;
  border: 1px solid transparent;
  border-radius: var(--radius-button);
  font-size: 14px;
  font-weight: 600;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
  transition:
    transform var(--motion-standard) var(--ease-emphasized),
    background-color var(--motion-fast),
    opacity var(--motion-fast),
    border-color var(--motion-fast);
}
.app-button--primary {
  color: #090811;
  background: var(--color-accent);
}
.app-button--secondary {
  color: var(--color-text);
  background: var(--color-surface);
  border-color: var(--color-border);
}
.app-button--success {
  background: var(--color-positive);
  color: #000;
}
.app-button--error {
  background: var(--color-error);
  color: #000;
}
.app-button:active:not(:disabled) {
  transform: scale(0.975) translateY(1px);
  transition-duration: var(--motion-fast);
}
.app-button:disabled {
  opacity: 0.45;
  cursor: default;
}
.app-button[aria-busy='true'] {
  opacity: 0.8;
  cursor: progress;
}
.button-label {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.spinner {
  width: 15px;
  height: 15px;
  flex: 0 0 auto;
  border: 2px solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
.busy-icon-enter-active,
.busy-icon-leave-active {
  transition: opacity var(--motion-fast);
}
.busy-icon-enter-from,
.busy-icon-leave-to {
  opacity: 0;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
@media (hover: hover) and (pointer: fine) {
  .app-button--primary:hover:not(:disabled) {
    background: #b6afff;
  }
  .app-button--secondary:hover:not(:disabled) {
    background: var(--color-hover);
    border-color: #383840;
  }
}
@media (prefers-reduced-motion: reduce) {
  .app-button:active:not(:disabled) {
    transform: none;
  }
}
</style>
