<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkspace } from '@/hooks/useWorkspace'
import { today } from '@/utils/display'
import PageHeader from '@/components/common/PageHeader.vue'
import AppButton from '@/components/common/AppButton.vue'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import SegmentedControl from '@/components/common/SegmentedControl.vue'
const router = useRouter(),
  store = useWorkspace(),
  error = ref(''),
  type = ref('收入')
const form = reactive({ amount: '', category: '工作收入', date: today(), note: '' })
const categories = computed(() =>
  type.value === '收入'
    ? ['工作收入', '兼职收入', '其他收入']
    : ['餐饮', '交通', '学习', '生活', '其他支出'],
)
async function submit() {
  error.value = ''
  try {
    const id = await store.save('/finance/records', 'POST', {
      ...form,
      amount: Number(form.amount),
      type: type.value === '收入' ? 'INCOME' : 'EXPENSE',
    })
    router.push(`/finance/records/${id}/saved`)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  }
}
</script>
<template>
  <PageHeader title="新增财务记录" subtitle="手动记录日常收支" back="/finance" /><WorkspaceGate
    ><form class="form-layout" @submit.prevent="submit">
      <SegmentedControl
        :model-value="type"
        :options="['收入', '支出']"
        label="收支类型"
        @update:model-value="
          (value) => {
            type = value
            form.category = value === '收入' ? '工作收入' : '餐饮'
          }
        "
      /><label class="field-label"
        >金额（人民币）<input
          v-model="form.amount"
          type="number"
          min="0.01"
          max="9999999999.99"
          step="0.01"
          required
          placeholder="0.00"
          inputmode="decimal" /></label
      ><label class="field-label"
        >分类<select v-model="form.category">
          <option v-for="category in categories" :key="category">{{ category }}</option>
        </select></label
      ><label class="field-label">日期<input v-model="form.date" type="date" required /></label
      ><label class="field-label"
        >备注<textarea v-model="form.note" maxlength="500" placeholder="记录这笔收支的用途" />
      </label>
      <p class="info-note">收支记录不会自动修改手动资产快照。</p>
      <p v-if="error" class="form-error" role="alert">{{ error }}，内容已保留，可以重试。</p>
      <AppButton type="submit" :loading="store.saving">{{
        store.saving ? '保存中…' : '保存记录'
      }}</AppButton>
    </form></WorkspaceGate
  >
</template>
