import { afterEach, describe, expect, it, vi } from 'vitest'
import { demoHome } from '@/views/home/mocks/homeMock'
import { readHomeSnapshot, validateHomeSnapshot } from '@/api/homeApi'

afterEach(() => {
  vi.unstubAllGlobals()
  vi.unstubAllEnvs()
})
describe('home query contract', () => {
  it('requests the backend by default and keeps the demo source explicit', async () => {
    vi.stubEnv('VITE_HOME_DATA_SOURCE', 'api')
    const fetchMock = vi
      .fn()
      .mockResolvedValue(
        new Response(JSON.stringify({ code: 'OK', message: '成功', data: demoHome })),
      )
    vi.stubGlobal('fetch', fetchMock)
    await expect(readHomeSnapshot()).resolves.toEqual(demoHome)
    expect(fetchMock).toHaveBeenCalledWith('/api/v1/home', expect.any(Object))
  })
  it('does not silently replace HTTP failures with a local fixture', async () => {
    vi.stubEnv('VITE_HOME_DATA_SOURCE', 'api')
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(JSON.stringify({ code: 'HTTP_ERROR', message: '不可用', data: null }), {
          status: 503,
        }),
      ),
    )
    await expect(readHomeSnapshot()).rejects.toMatchObject({ status: 503 })
  })
  it('does not silently replace network failures with a local fixture', async () => {
    vi.stubEnv('VITE_HOME_DATA_SOURCE', 'api')
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('offline')))
    await expect(readHomeSnapshot()).rejects.toThrow('offline')
  })
  it.each([
    { ...demoHome, source: 'unknown' },
    { ...demoHome, index: NaN },
    {
      ...demoHome,
      history: [
        { timestamp: 2, value: 1000 },
        { timestamp: 1, value: 1010 },
      ],
    },
    { ...demoHome, recentEvents: [{ ...demoHome.recentEvents[0], date: '2026-02-31' }] },
  ])('rejects malformed payloads before rendering', (value) => {
    expect(() => validateHomeSnapshot(value)).toThrow('首页数据格式不正确')
  })
  it('retains an explicit offline demonstration mode', async () => {
    vi.stubEnv('VITE_HOME_DATA_SOURCE', 'demo')
    const fetchMock = vi.fn()
    vi.stubGlobal('fetch', fetchMock)
    await expect(readHomeSnapshot()).resolves.toEqual(demoHome)
    expect(fetchMock).not.toHaveBeenCalled()
  })
})
