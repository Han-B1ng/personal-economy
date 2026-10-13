<script setup lang="ts">
import { ref } from 'vue'
import { useWorkspace } from '@/hooks/useWorkspace'
import PageHeader from '@/components/common/PageHeader.vue'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import AppButton from '@/components/common/AppButton.vue'
const store = useWorkspace(),
  message = ref('')
function exportData() {
  if (!store.data) return
  const url = URL.createObjectURL(
    new Blob([JSON.stringify(store.data, null, 2)], { type: 'application/json' }),
  )
  const link = document.createElement('a')
  link.href = url
  link.download = 'personal-economy-workspace.json'
  link.click()
  window.setTimeout(() => URL.revokeObjectURL(url), 1000)
  message.value = '当前工作区已导出'
}
</script>
<template>
  <PageHeader title="数据管理" back="/profile" subtitle="导出当前工作区快照" /><WorkspaceGate
    ><p class="info-note">
      导出文件包含个人资料、目标、待办、成长事件、分类、收支和手动资产快照。请妥善保存个人数据。
    </p>
    <AppButton @click="exportData">导出 JSON 数据</AppButton>
    <p v-if="message" class="positive" role="status">{{ message }}</p>
    <p class="info-note">
      此处不导出 Penpot 设计文件。数据导入、账户删除及清空功能尚无已确认设计，不在本轮添加。
    </p></WorkspaceGate
  >
</template>
