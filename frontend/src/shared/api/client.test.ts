import { afterEach, describe, expect, it, vi } from 'vitest'
import { apiGet, ApiError } from './client'
import { getHealth } from './health'

afterEach(() => vi.unstubAllGlobals())

describe('API transport boundary', () => {
  it.each([null, 42, 'unexpected', {}])('rejects an invalid envelope: %j', async (body) => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify(body))))
    await expect(getHealth()).rejects.toMatchObject({ code: 'INVALID_RESPONSE' })
  })
  it('reads the backend health envelope', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          code: 'OK',
          message: '成功',
          data: { status: 'UP', service: 'backend' },
        }),
        { status: 200 },
      ),
    )
    vi.stubGlobal('fetch', fetchMock)
    await expect(getHealth()).resolves.toEqual({ status: 'UP', service: 'backend' })
    expect(fetchMock).toHaveBeenCalledWith('/api/v1/health', expect.any(Object))
  })

  it('keeps HTTP failures from becoming successful data', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(JSON.stringify({ code: 'INTERNAL_ERROR', message: '不可用', data: null }), {
          status: 500,
        }),
      ),
    )
    await expect(apiGet('/health')).rejects.toMatchObject({ status: 500, code: 'INTERNAL_ERROR' })
  })

  it('rejects HTML or malformed responses', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('<html>Proxy error</html>')))
    await expect(getHealth()).rejects.toBeInstanceOf(ApiError)
  })

  it('does not display connected for an invalid health payload', async () => {
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValue(
          new Response(JSON.stringify({ code: 'OK', message: '成功', data: { status: 'DOWN' } })),
        ),
    )
    await expect(getHealth()).rejects.toMatchObject({ code: 'INVALID_RESPONSE' })
  })
})
