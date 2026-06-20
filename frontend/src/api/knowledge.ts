import request from './request'
import { isMockEnabled, mockSuccess, mockPage, mockKnowledgeItems, mockKnowledgePushes } from './mock'
import type { ApiResponse, KnowledgeItem, KnowledgePush, PageData } from '../types'

/** 查询健康知识列表 */
export async function getKnowledgeList(_params: { keyword?: string; tag?: string; page?: number; size?: number }) {
  if (isMockEnabled()) return mockPage(mockKnowledgeItems, _params.page || 1, _params.size || 10)
  return request.get<ApiResponse<PageData<KnowledgeItem>>>('/api/knowledge', { params: _params })
}

/** 查询我的知识推送 */
export async function getMyPushes(_params: { page?: number; size?: number }) {
  if (isMockEnabled()) return mockPage(mockKnowledgePushes, _params.page || 1, _params.size || 10)
  return request.get<ApiResponse<PageData<KnowledgePush>>>('/api/knowledge/pushes', { params: _params })
}

/** 标记推送为已读 */
export async function markPushRead(_pushId: number) {
  if (isMockEnabled()) return mockSuccess(null)
  return request.patch<ApiResponse<null>>(`/api/knowledge/pushes/${_pushId}/read`)
}
