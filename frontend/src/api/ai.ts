import request from './request'
import type { ApiResponse, AiProfileData, AiProfileRequest, AiPlanData, AiPlanRequest, AiPushData, AiPushRequest } from '../types'

/** 生成健康画像 */
export function generateProfile(data: AiProfileRequest) {
  return request.post<ApiResponse<AiProfileData>>('/api/ai/profile', data)
}

/** 生成健康计划 */
export function generatePlan(data: AiPlanRequest) {
  return request.post<ApiResponse<AiPlanData>>('/api/ai/plan', data)
}

/** 生成健康知识推送 */
export function pushKnowledge(data: AiPushRequest) {
  return request.post<ApiResponse<AiPushData>>('/api/ai/knowledge-push', data)
}
