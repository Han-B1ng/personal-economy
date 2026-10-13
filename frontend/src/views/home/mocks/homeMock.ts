import type { HomeSnapshot } from '@/types/home'

// Demonstration fixtures reproduce the design's line; they are not an index algorithm.
const values = [
  1000, 1013, 1007, 1020, 1014, 1032, 1021, 1051, 1042, 1059, 1049, 1055, 1078, 1063, 1081, 1110,
  1096, 1106, 1083, 1101, 1126, 1118, 1137, 1125, 1152, 1145, 1176, 1162, 1181, 1169, 1203, 1189,
  1214, 1200, 1237, 1219, 1249, 1241, 1266, 1252, 1278, 1261, 1284.68,
]
const start = Date.UTC(2026, 8, 12)
const duration = 30 * 24 * 60 * 60 * 1000

export const demoHome: HomeSnapshot = {
  source: 'demo',
  nickname: 'Han_B1ng',
  stockCode: 'PE001',
  index: 1284.68,
  change: 41.76,
  changePercent: 3.36,
  history: values.map((value, i) => ({
    timestamp: Math.round(start + (duration * i) / (values.length - 1)),
    value,
  })),
  recentEvents: [
    {
      id: 'demo-cet4',
      title: '通过英语四级',
      date: '2026-10-12',
      categoryMark: '证',
      contribution: 18.5,
    },
    {
      id: 'demo-project',
      title: '完成软件开发项目',
      date: '2026-10-08',
      categoryMark: '码',
      contribution: 15.26,
    },
    {
      id: 'demo-savings',
      title: '达成储蓄目标',
      date: '2026-10-03',
      categoryMark: '财',
      contribution: 8,
    },
  ],
}

export async function readDemoHome(preview?: 'empty' | 'error'): Promise<HomeSnapshot> {
  await new Promise((resolve) => setTimeout(resolve, 180))
  if (preview === 'error') throw new Error('演示加载失败')
  return structuredClone(
    preview === 'empty' ? { ...demoHome, history: [], recentEvents: [] } : demoHome,
  )
}
