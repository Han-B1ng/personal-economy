import { onMounted } from 'vue'
import { useWorkspaceStore } from '@/store/useWorkspaceStore'
export function useWorkspace() {
  const store = useWorkspaceStore()
  onMounted(() => store.load())
  return store
}
