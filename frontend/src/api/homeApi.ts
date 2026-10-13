import { apiGet, ApiError } from '@/api/httpClient'
import { readDemoHome } from '@/views/home/mocks/homeMock'
import type { HomeSnapshot } from '@/types/home'

const record = (value: unknown): value is Record<string, unknown> =>
  !!value && typeof value === 'object' && !Array.isArray(value)
const text = (value: unknown): value is string =>
  typeof value === 'string' && value.trim().length > 0
const number = (value: unknown): value is number =>
  typeof value === 'number' && Number.isFinite(value)
const date = (value: unknown): value is string =>
  typeof value === 'string' &&
  /^\d{4}-\d{2}-\d{2}$/.test(value) &&
  Number.isFinite(Date.parse(value)) &&
  new Date(value).toISOString().slice(0, 10) === value

export function validateHomeSnapshot(value: unknown): HomeSnapshot {
  if (
    !record(value) ||
    value.source !== 'demo' ||
    !text(value.nickname) ||
    !text(value.stockCode) ||
    !number(value.index) ||
    !number(value.change) ||
    !number(value.changePercent) ||
    !Array.isArray(value.history) ||
    !Array.isArray(value.recentEvents)
  ) {
    throw new ApiError('首页数据格式不正确', 200, 'INVALID_RESPONSE')
  }
  const history = value.history
  if (
    !history.every(
      (point: unknown, i: number) =>
        record(point) &&
        number(point.timestamp) &&
        Math.abs(point.timestamp) <= 8.64e15 &&
        number(point.value) &&
        (i === 0 || point.timestamp >= history[i - 1].timestamp),
    ) ||
    !value.recentEvents.every(
      (event: unknown) =>
        record(event) &&
        text(event.id) &&
        text(event.title) &&
        date(event.date) &&
        text(event.categoryMark) &&
        (event.contribution === null || number(event.contribution)),
    )
  ) {
    throw new ApiError('首页数据格式不正确', 200, 'INVALID_RESPONSE')
  }
  return value as unknown as HomeSnapshot
}

export async function readHomeSnapshot(
  preview?: 'empty' | 'error',
  signal?: AbortSignal,
): Promise<HomeSnapshot> {
  if (import.meta.env.DEV && preview) return readDemoHome(preview)
  if (import.meta.env.VITE_HOME_DATA_SOURCE === 'demo') return readDemoHome()
  return validateHomeSnapshot(await apiGet<unknown>('/home', signal))
}
