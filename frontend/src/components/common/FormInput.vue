<script setup lang="ts">
import { useId } from 'vue'
defineProps<{
  modelValue: string
  label: string
  error?: string
  hint?: string
  placeholder?: string
  readonly?: boolean
  disabled?: boolean
  type?: 'text' | 'email' | 'password' | 'date' | 'number'
}>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
const id = useId()
</script>

<template>
  <div class="form-field">
    <label :for="id">{{ label }}</label>
    <input
      :id="id"
      :type="type || 'text'"
      :value="modelValue"
      :placeholder="placeholder"
      :readonly="readonly"
      :disabled="disabled"
      :aria-invalid="!!error"
      :aria-describedby="error || hint ? `${id}-help` : undefined"
      @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
    <p v-if="error || hint" :id="`${id}-help`" :class="{ 'field-error': error }">
      {{ error || hint }}
    </p>
  </div>
</template>

<style scoped>
.form-field {
  display: grid;
  gap: 8px;
}
label {
  font-size: 14px;
}
input {
  width: 100%;
  min-height: 48px;
  padding: 12px 16px;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-input);
  font: inherit;
}
input::placeholder,
p {
  color: var(--color-muted);
}
input:focus {
  border-color: var(--color-accent);
}
input[aria-invalid='true'] {
  border-color: var(--color-error);
}
input:read-only,
input:disabled {
  color: var(--color-muted);
}
p {
  margin: 0;
  font-size: 12px;
}
.field-error {
  color: var(--color-error);
}
</style>
