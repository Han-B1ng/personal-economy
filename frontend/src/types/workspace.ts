export interface Profile {
  nickname: string
  stockCode: string
  bio: string
  avatar: string
}
export interface Category {
  id: string
  name: string
  system: boolean
  active: boolean
}
export interface Goal {
  id: string
  title: string
  categoryId: string
  description: string
  startDate: string
  dueDate: string
  progress: number
  status: 'ACTIVE' | 'COMPLETED'
  completedDate: string | null
}
export interface Task {
  id: string
  title: string
  description: string
  dueDate: string
  dueTime: string
  priority: string
  goalId: string | null
  completed: boolean
}
export interface GrowthEvent {
  id: string
  title: string
  categoryId: string
  description: string
  date: string
  importance: string
  sourceGoalId: string | null
  evaluationStatus: 'DEMO' | 'PENDING'
  contribution: number | null
}
export interface FinanceRecord {
  id: string
  type: 'INCOME' | 'EXPENSE'
  amount: number
  category: string
  date: string
  note: string
}
export interface Assets {
  cash: number
  other: number
  debt: number
  updatedAt: string
}
export interface Workspace {
  source: 'local-demo' | 'local-mysql' | 'preview-mysql'
  updatedAt: string
  profile: Profile
  goals: Goal[]
  tasks: Task[]
  events: GrowthEvent[]
  categories: Category[]
  financeRecords: FinanceRecord[]
  assets: Assets
}
export interface WorkspaceMutation {
  snapshot: Workspace
  affectedId: string | null
}
