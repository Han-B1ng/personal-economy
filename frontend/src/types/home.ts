export const chartPeriods = ['1D', '1W', '1M', '3M', '1Y', 'ALL'] as const
export type ChartPeriod = (typeof chartPeriods)[number]

export interface IndexPoint {
  timestamp: number
  value: number
}
export interface RecentGrowthEvent {
  id: string
  title: string
  date: string
  categoryMark: string
  contribution: number | null
}
export interface HomeSnapshot {
  source: 'demo'
  nickname: string
  stockCode: string
  index: number
  change: number
  changePercent: number
  history: IndexPoint[]
  recentEvents: RecentGrowthEvent[]
}
