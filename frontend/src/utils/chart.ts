import type { ChartPeriod, IndexPoint } from '@/types/home'

const day = 86_400_000
const dateFormatter = new Intl.DateTimeFormat('en-US', {
  month: '2-digit',
  day: '2-digit',
  timeZone: 'UTC',
})
const indexFormatter = new Intl.NumberFormat('en-US', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})
export function filterHistory(history: readonly IndexPoint[], period: ChartPeriod): IndexPoint[] {
  const last = history.at(-1)
  if (!last) return []
  const days = { '1D': 1, '1W': 7, '1M': 31, '3M': 93, '1Y': 366, ALL: Infinity }[period]
  return history.filter((point) => point.timestamp > last.timestamp - days * day)
}

export function chartGeometry(points: readonly IndexPoint[]) {
  if (!points.length) return { coordinates: [], line: '', area: '', ticks: [] }
  const values = points.map((point) => point.value)
  const min = Math.floor(Math.min(...values) / 100) * 100
  const max = Math.max(min + 100, Math.ceil(Math.max(...values) / 100) * 100)
  const first = points[0]?.timestamp || 0
  const duration = (points.at(-1)?.timestamp || first) - first
  const coordinates = points.map((point) => ({
    x: duration ? ((point.timestamp - first) / duration) * 291 : 145.5,
    y: 162 - ((point.value - min) / (max - min)) * 147,
  }))
  const line = coordinates
    .map((point, i) => `${i ? 'L' : 'M'}${point.x.toFixed(2)},${point.y.toFixed(2)}`)
    .join(' ')
  const area = coordinates.length > 1 ? `${line} L291,162 L0,162 Z` : ''
  const ticks = Array.from({ length: 4 }, (_, i) => ({
    y: 15 + i * 49,
    value: max - ((max - min) * i) / 3,
  }))
  return { coordinates, line, area, ticks }
}

export function nearestPoint(points: readonly IndexPoint[], ratio: number): number {
  const first = points[0]
  const last = points.at(-1)
  if (!first || !last) return 0
  const timestamp =
    first.timestamp + Math.min(1, Math.max(0, ratio)) * (last.timestamp - first.timestamp)
  return points.reduce(
    (best, point, index) =>
      Math.abs(point.timestamp - timestamp) < Math.abs(points[best]!.timestamp - timestamp)
        ? index
        : best,
    0,
  )
}

export function shortDate(timestamp: number) {
  return dateFormatter.format(timestamp)
}
export const formatIndex = (value: number) => indexFormatter.format(value)
export const signed = (value: number) => `${value >= 0 ? '+' : ''}${value.toFixed(2)}`
