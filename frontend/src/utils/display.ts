export const today = () => new Date().toLocaleDateString('en-CA')
export const displayDate = (value: string) => (value ? value.slice(0, 10).replaceAll('-', '.') : '')
export const money = (value: number) =>
  new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    maximumFractionDigits: 2,
  }).format(value)
export const categoryName = (id: string, categories: { id: string; name: string }[]) =>
  categories.find((c) => c.id === id)?.name || '未分类'
