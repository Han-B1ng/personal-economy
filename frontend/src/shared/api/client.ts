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
  const response = await fetch(baseUrl + path, {
    headers: { Accept: 'application/json' },
    signal,
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
}
