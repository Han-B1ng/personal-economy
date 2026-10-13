<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkspace } from '@/hooks/useWorkspace'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import AppButton from '@/components/common/AppButton.vue'
const store = useWorkspace(),
  router = useRouter(),
  error = ref(''),
  form = reactive({ nickname: '', bio: '', avatar: '' })
let initialized = false
watch(
  () => store.data,
  (value) => {
    if (value && !initialized) {
      Object.assign(form, value.profile)
      initialized = true
    }
  },
  { immediate: true },
)
async function image(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!['image/png', 'image/jpeg', 'image/webp'].includes(file.type) || file.size > 400000) {
    error.value = '请选择400KB以内的PNG、JPEG或WebP图片'
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    form.avatar = String(reader.result)
    error.value = ''
  }
  reader.readAsDataURL(file)
}
async function submit() {
  try {
    await store.save('/profile', 'PUT', form)
    router.push('/profile')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  }
}
</script>
<template>
  <PageHeader title="编辑资料" back="/profile" /><WorkspaceGate
    ><form class="form-layout" @submit.prevent="submit">
      <div class="avatar" style="margin: auto">
        <img v-if="form.avatar" :src="form.avatar" alt="头像预览" /><span v-else>{{
          form.nickname.slice(0, 2).toUpperCase()
        }}</span>
      </div>
      <label class="field-label"
        >更换头像<input
          type="file"
          accept="image/png,image/jpeg,image/webp"
          @change="image" /></label
      ><label class="field-label"
        >昵称<input v-model.trim="form.nickname" required maxlength="40" /></label
      ><label class="field-label"
        >个人股票代码<input :value="store.data?.profile.stockCode" readonly
      /></label>
      <p class="info-note">代码为个人标识，修改规则待确认。</p>
      <label class="field-label">个人简介<textarea v-model="form.bio" maxlength="500" /></label>
      <p v-if="error" class="form-error" role="alert">{{ error }}</p>
      <AppButton type="submit" :loading="store.saving">保存资料</AppButton>
    </form></WorkspaceGate
  >
</template>
