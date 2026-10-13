<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useWorkspace } from '@/hooks/useWorkspace'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import AppButton from '@/components/common/AppButton.vue'
import { money, displayDate } from '@/utils/display'
const fields = [
  { key: 'cash', label: '现金及存款' },
  { key: 'other', label: '其他资产' },
  { key: 'debt', label: '负债' },
] as const
const store = useWorkspace(),
  editing = ref(false),
  error = ref(''),
  saved = ref(false),
  form = reactive({ cash: 0, other: 0, debt: 0 })
watch(
  () => store.data?.assets,
  (value) => {
    if (value && !editing.value) Object.assign(form, value)
  },
  { immediate: true },
)
function editAssets() {
  editing.value = true
  saved.value = false
}
async function submit() {
  try {
    await store.save('/finance/assets', 'PUT', {
      cash: Number(form.cash),
      other: Number(form.other),
      debt: Number(form.debt),
    })
    editing.value = false
    saved.value = true
    error.value = ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  }
}
</script>
<template>
  <PageHeader
    :title="editing ? '更新资产快照' : '资产信息'"
    subtitle="资产与日常收支采用独立口径"
    back="/finance"
  /><WorkspaceGate
    ><form v-if="editing" class="form-layout" @submit.prevent="submit">
      <label v-for="field in fields" :key="field.key" class="field-label"
        >{{ field.label }}（人民币）<input
          v-model.number="form[field.key]"
          type="number"
          min="0"
          max="9999999999.99"
          step="0.01"
          required
      /></label>
      <p class="form-error" role="alert">{{ error }}</p>
      <AppButton type="submit" :loading="store.saving">保存资产快照</AppButton
      ><AppButton type="button" variant="secondary" @click="editing = false">取消</AppButton>
    </form>
    <template v-else
      ><p v-if="saved" class="positive" role="status">资产快照已保存</p>
      <p class="muted">净资产</p>
      <p class="large-number">{{ money(form.cash + form.other - form.debt) }}</p>
      <p class="info-note">
        手动快照 · 更新于 {{ displayDate(store.data?.assets.updatedAt || '') }}
      </p>
      <div class="card">
        <div class="menu-row">
          <span>现金及存款</span><span>{{ money(form.cash) }}</span>
        </div>
        <div class="menu-row">
          <span>其他资产</span><span>{{ money(form.other) }}</span>
        </div>
        <div class="menu-row">
          <span>负债</span><span>{{ money(form.debt) }}</span>
        </div>
      </div>
      <p class="info-note">每笔收入或支出不会自动增减此处余额，净资产为资产合计减负债。</p>
      <AppButton @click="editAssets">手动更新资产信息</AppButton></template
    ></WorkspaceGate
  >
</template>
