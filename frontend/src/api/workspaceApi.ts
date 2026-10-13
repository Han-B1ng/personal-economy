import { apiGet, apiRequest, ApiError } from './httpClient'
import type { Workspace, WorkspaceMutation } from '@/types/workspace'
function validate(value: Workspace) {
  if (
    !value ||
    !['local-demo', 'local-mysql', 'preview-mysql'].includes(value.source) ||
    !value.profile ||
    !value.assets ||
    !['goals', 'tasks', 'events', 'categories', 'financeRecords'].every((key) =>
      Array.isArray(value[key as keyof Workspace]),
    )
  )
    throw new ApiError('工作区响应不正确', 200, 'INVALID_RESPONSE')
  return value
}
export async function fetchWorkspace() {
  return validate(await apiGet<Workspace>('/workspace'))
}
export async function mutateWorkspace(path: string, method: string, body?: unknown) {
  const response = await apiRequest<WorkspaceMutation>(path, method, body)
  validate(response.snapshot)
  return response
}
