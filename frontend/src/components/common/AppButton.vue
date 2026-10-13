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
    <span v-if="loading" class="spinner" aria-hidden="true" />
    <slot />
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
  transition:
    transform var(--motion-duration),
    opacity var(--motion-duration);
}
.app-button--primary {
  color: #000;
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
  transform: scale(0.98);
}
.app-button:disabled {
  opacity: 0.45;
  cursor: default;
}
.spinner {
  width: 15px;
  height: 15px;
  border: 2px solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
