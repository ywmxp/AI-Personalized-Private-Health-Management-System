import request from './request'
import { isMockEnabled, mockSuccess, mockPage, mockAdminUsers, mockPlatformStatistics, mockLoginLogs } from './mock'
import type { ApiResponse, AdminUserItem, PlatformStatistics, PageData, LoginLog } from '../types'

/** 获取用户列表（管理员） */
export async function getAdminUsers(_params: { username?: string; phone?: string; status?: number | null; pageNum: number; pageSize: number }) {
  if (isMockEnabled()) return mockPage(mockAdminUsers, _params.pageNum, _params.pageSize)
  return request.get<ApiResponse<PageData<AdminUserItem>>>('/api/admin/users', { params: _params })
}

/** 启用/禁用用户 */
export async function toggleUserStatus(_userId: number, _status: number) {
  if (isMockEnabled()) return mockSuccess(null)
  return request.patch<ApiResponse<null>>(`/api/admin/users/${_userId}/status`, { status: _status })
}

/** 查询登录日志 */
export async function getLoginLogs(_params: { userId?: number; result?: number; pageNum: number; pageSize: number }) {
  if (isMockEnabled()) return mockPage(mockLoginLogs, _params.pageNum, _params.pageSize)
  return request.get<ApiResponse<PageData<LoginLog>>>('/api/admin/login-logs', { params: _params })
}

/** 平台统计 */
export async function getPlatformStatistics(_params: { startDate?: string; endDate?: string }) {
  if (isMockEnabled()) return mockSuccess(mockPlatformStatistics)
  return request.get<ApiResponse<PlatformStatistics>>('/api/admin/statistics', { params: _params })
}

/** 健康知识管理 - 新增 */
export async function createKnowledge(data: { title: string; content: string; relateTag: string; source: string }) {
  if (isMockEnabled()) return mockSuccess(null)
  return request.post<ApiResponse<null>>('/api/admin/knowledge', data)
}

/** 健康知识管理 - 修改 */
export async function updateKnowledge(knowledgeId: number, data: { title: string; content: string; relateTag: string; source: string }) {
  if (isMockEnabled()) return mockSuccess(null)
  return request.put<ApiResponse<null>>(`/api/admin/knowledge/${knowledgeId}`, data)
}

/** 健康知识管理 - 删除 */
export async function deleteKnowledge(knowledgeId: number) {
  if (isMockEnabled()) return mockSuccess(null)
  return request.delete<ApiResponse<null>>(`/api/admin/knowledge/${knowledgeId}`)
}
