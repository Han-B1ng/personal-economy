import { apiGet, ApiError } from './client'

export interface ServiceHealth {
  status: 'UP'
  service: string
}

export async function getHealth(signal?: AbortSignal): Promise<ServiceHealth> {
  const data = await apiGet<ServiceHealth>('/health', signal)
  if (data.status !== 'UP' || typeof data.service !== 'string') {
    throw new ApiError('服务状态响应不正确', 200, 'INVALID_RESPONSE')
  }
  return data
}
