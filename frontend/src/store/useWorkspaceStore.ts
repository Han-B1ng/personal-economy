import { defineStore } from 'pinia'
import { ref } from 'vue'
import { fetchWorkspace, mutateWorkspace } from '@/api/workspaceApi'
import type { Workspace } from '@/types/workspace'
import { useHomeStore } from './useHomeStore'

export const useWorkspaceStore = defineStore('workspace', () => {
  const data = ref<Workspace | null>(null),
    loading = ref(false),
    saving = ref(false),
    error = ref('')
  let activeLoad: Promise<void> | undefined
  async function load(force = false) {
    if (activeLoad) return activeLoad
    if (data.value && !force) return
    loading.value = true
    error.value = ''
    activeLoad = (async () => {
      try {
        data.value = await fetchWorkspace()
      } catch (e) {
        error.value = e instanceof Error ? e.message : '加载失败'
      } finally {
        loading.value = false
        activeLoad = undefined
      }
    })()
    return activeLoad
  }
  async function save(path: string, method: string, body?: unknown) {
    if (saving.value) throw new Error('正在保存，请稍候')
    saving.value = true
    error.value = ''
    try {
      const result = await mutateWorkspace(path, method, body)
      data.value = result.snapshot
      useHomeStore().status = 'idle'
      return result.affectedId
    } finally {
      saving.value = false
    }
  }
  return { data, loading, saving, error, load, save }
})
