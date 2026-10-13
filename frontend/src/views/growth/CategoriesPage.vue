<script setup lang="ts">
import { ref } from 'vue'
import { useWorkspace } from '@/hooks/useWorkspace'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import AppButton from '@/components/common/AppButton.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
const store = useWorkspace(),
  name = ref(''),
  error = ref(''),
  selected = ref('')
async function add() {
  try {
    await store.save('/categories', 'POST', { name: name.value.trim() })
    name.value = ''
    error.value = ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  }
}
async function remove() {
  try {
    await store.save(`/categories/${selected.value}`, 'DELETE')
    selected.value = ''
    error.value = ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败'
    selected.value = ''
  }
}
</script>
<template>
  <PageHeader
    title="事件分类管理"
    back="/profile"
    subtitle="分类用于整理成长事实，不直接决定指数权重。"
  /><WorkspaceGate
    ><h2 class="section-title">系统分类</h2>
    <div v-for="c in store.data?.categories.filter((c) => c.system)" :key="c.id" class="menu-row">
      <span>{{ c.name }}</span
      ><span class="muted">系统预设</span>
    </div>
    <h2 class="section-title">自定义分类</h2>
    <div
      v-for="c in store.data?.categories.filter((c) => !c.system && c.active)"
      :key="c.id"
      class="menu-row"
    >
      <span>{{ c.name }}</span
      ><button class="filter-chip negative" @click="selected = c.id">删除</button>
    </div>
    <form class="form-layout" @submit.prevent="add">
      <label class="field-label"
        >新增分类<input v-model.trim="name" maxlength="24" required placeholder="分类名称" /></label
      ><AppButton type="submit" :loading="store.saving">添加分类</AppButton>
    </form>
    <p v-if="error" class="form-error" role="alert">{{ error }}</p>
    <p class="info-note">
      删除自定义分类会停用该分类，历史事件与目标保留原分类名称。
    </p></WorkspaceGate
  ><ConfirmDialog
    :open="!!selected"
    title="停用分类？"
    message="分类将从新记录的选项中移除，已有记录不会删除。"
    :busy="store.saving"
    @cancel="selected = ''"
    @confirm="remove"
  />
</template>
