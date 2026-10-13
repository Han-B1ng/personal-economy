import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useHomeStore } from '@/store/useHomeStore'
import { readDemoHome } from '@/views/home/mocks/homeMock'
import { readHomeSnapshot } from '@/api/homeApi'

vi.mock('@/api/homeApi', () => ({ readHomeSnapshot: vi.fn() }))
beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
  vi.mocked(readHomeSnapshot).mockImplementation((preview) => readDemoHome(preview))
})
afterEach(() => vi.useRealTimers())
describe('home load states', () => {
  it('cancels a timed-out request and exposes the error state', async () => {
    vi.useFakeTimers()
    let signal: AbortSignal | undefined
    vi.mocked(readHomeSnapshot).mockImplementationOnce((_preview, requestSignal) => {
      signal = requestSignal
      return new Promise((_, reject) =>
        requestSignal?.addEventListener('abort', () =>
          reject(new DOMException('timeout', 'AbortError')),
        ),
      )
    })
    const store = useHomeStore()
    const pending = store.load()
    await vi.advanceTimersByTimeAsync(10000)
    await pending
    expect(signal?.aborted).toBe(true)
    expect(store.status).toBe('error')
    expect(store.snapshot).toBeNull()
  })
  it('supports error, retry and empty states without persisted business effects', async () => {
    const store = useHomeStore()
    await store.load('error')
    expect(store.status).toBe('error')
    expect(store.snapshot).toBeNull()
    await store.load()
    expect(store.status).toBe('ready')
    expect(store.snapshot?.source).toBe('demo')
    await store.load('empty')
    expect(store.snapshot?.history).toEqual([])
    expect(store.snapshot?.recentEvents).toEqual([])
  })
  it('keeps the last successful snapshot visibly stale when a refresh fails', async () => {
    const store = useHomeStore()
    await store.load()
    const previous = store.snapshot
    const readAt = store.lastFetchedAt
    vi.mocked(readHomeSnapshot).mockRejectedValueOnce(new Error('network unavailable'))
    await store.load()
    expect(store.status).toBe('error')
    expect(store.snapshot).toBe(previous)
    expect(store.lastFetchedAt).toBe(readAt)
  })
  it('clears old data when explicitly previewing a failed initial load', async () => {
    const store = useHomeStore()
    await store.load()
    await store.load('error')
    expect(store.status).toBe('error')
    expect(store.snapshot).toBeNull()
  })
  it('ignores stale responses when a newer request has completed', async () => {
    let failOld!: (reason?: unknown) => void
    vi.mocked(readHomeSnapshot).mockImplementationOnce(
      () =>
        new Promise((_, reject) => {
          failOld = reject
        }),
    )
    const store = useHomeStore()
    const old = store.load()
    await store.load('empty')
    failOld(new Error('stale failure'))
    await old
    expect(store.status).toBe('ready')
    expect(store.snapshot?.history).toEqual([])
  })
})
