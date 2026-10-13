import { defineStore } from 'pinia'
import { ref } from 'vue'
import { readHomeSnapshot } from '@/api/homeApi'
import type { ChartPeriod, HomeSnapshot } from '@/types/home'

export const useHomeStore = defineStore('home', () => {
  const snapshot = ref<HomeSnapshot | null>(null)
  const status = ref<'idle' | 'loading' | 'ready' | 'error'>('idle')
  const period = ref<ChartPeriod>('1M')
  const lastFetchedAt = ref<number | null>(null)
  let generation = 0
  let request: AbortController | undefined
  async function load(preview?: 'empty' | 'error') {
    request?.abort()
    const current = ++generation
    const controller = new AbortController()
    request = controller
    const timeout = window.setTimeout(() => controller.abort(), 10000)
    if (preview) snapshot.value = null
    status.value = 'loading'
    try {
      const data = await readHomeSnapshot(preview, controller.signal)
      if (current !== generation) return
      snapshot.value = data
      lastFetchedAt.value = Date.now()
      status.value = 'ready'
    } catch {
      if (current !== generation) return
      status.value = 'error'
    } finally {
      window.clearTimeout(timeout)
      if (current === generation) request = undefined
    }
  }
  return { snapshot, status, period, lastFetchedAt, load }
})
