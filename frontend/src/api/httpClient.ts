export interface ApiResponse<T> {
  code: string
  message: string
  data: T | null
}

export class ApiError extends Error {
  readonly status: number
  readonly code: string

  constructor(message: string, status: number, code: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

const baseUrl = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/$/, '')

export async function apiGet<T>(path: string, signal?: AbortSignal): Promise<T> {
  return apiRequest<T>(path, 'GET', undefined, signal)
}

export async function apiRequest<T>(
  path: string,
  method: string,
  payload?: unknown,
  signal?: AbortSignal,
): Promise<T> {
  const controller = new AbortController()
  const abort = () => controller.abort()
  if (signal?.aborted) abort()
  else signal?.addEventListener('abort', abort, { once: true })
  let timedOut = false
  const timeout = window.setTimeout(() => {
    timedOut = true
    controller.abort()
  }, 15000)
  try {
    const response = await fetch(baseUrl + path, {
      method,
      headers:
        payload === undefined
          ? { Accept: 'application/json' }
          : { Accept: 'application/json', 'Content-Type': 'application/json' },
      body: payload === undefined ? undefined : JSON.stringify(payload),
      signal: controller.signal,
    })
    let raw: unknown
    try {
      raw = await response.json()
    } catch {
      throw new ApiError('服务响应格式不正确', response.status, 'INVALID_RESPONSE')
    }
    if (
      !raw ||
      typeof raw !== 'object' ||
      !('code' in raw) ||
      !('message' in raw) ||
      typeof raw.code !== 'string' ||
      typeof raw.message !== 'string' ||
      !('data' in raw)
    ) {
      throw new ApiError('服务响应格式不正确', response.status, 'INVALID_RESPONSE')
    }
    const body = raw as ApiResponse<T>
    if (!response.ok || body.code !== 'OK' || body.data === null || body.data === undefined) {
      throw new ApiError(body.message || '请求失败', response.status, body.code || 'HTTP_ERROR')
    }
    return body.data
  } catch (error) {
    if (timedOut) throw new ApiError('请求超时，请确认记录是否已保存后重试', 0, 'REQUEST_TIMEOUT')
    throw error
  } finally {
    window.clearTimeout(timeout)
    signal?.removeEventListener('abort', abort)
  }
}
