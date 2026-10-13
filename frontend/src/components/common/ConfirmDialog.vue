<script setup lang="ts">
import { ref, watch, nextTick, useId } from 'vue'
import AppButton from './AppButton.vue'
const props = defineProps<{ open: boolean; title: string; message: string; busy?: boolean }>()
const emit = defineEmits<{ confirm: []; cancel: [] }>()
const dialog = ref<HTMLDialogElement>()
const id = useId()
watch(
  () => props.open,
  async (open) => {
    await nextTick()
    if (open && !dialog.value?.open) dialog.value?.showModal()
    else if (!open && dialog.value?.open) dialog.value.close()
  },
  { immediate: true },
)
</script>
<template>
  <Teleport to="body"
    ><dialog
      ref="dialog"
      class="confirm-dialog"
      :aria-labelledby="id"
      @cancel.prevent="!busy && emit('cancel')"
    >
      <h2 :id="id">{{ title }}</h2>
      <p>{{ message }}</p>
      <div class="button-stack">
        <AppButton state="error" :loading="busy" @click="emit('confirm')">确认删除</AppButton
        ><AppButton variant="secondary" :disabled="busy" @click="emit('cancel')">取消</AppButton>
      </div>
    </dialog></Teleport
  >
</template>
