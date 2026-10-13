import { describe, expect, it } from 'vitest'
import { demoHome } from '@/views/home/mocks/homeMock'
import { chartGeometry, filterHistory, nearestPoint } from '@/utils/chart'

describe('demo history selection', () => {
  it('filters from the latest recorded time rather than the current clock', () => {
    const week = filterHistory(demoHome.history, '1W')
    expect(week.length).toBeLessThan(demoHome.history.length)
    expect(week.at(-1)?.value).toBe(1284.68)
    expect(filterHistory(demoHome.history, 'ALL')).toEqual(demoHome.history)
    expect(filterHistory([], '1M')).toEqual([])
  })
  it('handles a single point without dividing by zero', () => {
    expect(chartGeometry([])).toEqual({ coordinates: [], line: '', area: '', ticks: [] })
    const geometry = chartGeometry([{ timestamp: 0, value: 1000 }])
    expect(geometry.coordinates).toEqual([{ x: 145.5, y: 162 }])
    expect(geometry.area).toBe('')
    expect(geometry.line).not.toMatch(/NaN|Infinity/)
  })
  it('chooses by time for irregular samples and clamps beyond the chart', () => {
    const points = [
      { timestamp: 0, value: 1000 },
      { timestamp: 10, value: 1010 },
      { timestamp: 100, value: 1020 },
    ]
    expect(nearestPoint(points, 0.12)).toBe(1)
    expect(nearestPoint(points, -1)).toBe(0)
    expect(nearestPoint(points, 2)).toBe(2)
  })
})
