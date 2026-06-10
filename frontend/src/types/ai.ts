/** AI 健康画像 */
export interface AiProfileData {
  profileId: number
  healthTags: string[]
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH'
  analysis: string
}

export interface AiProfileRequest {
  timeRange: string // e.g. "LAST_7_DAYS", "LAST_30_DAYS"
}

/** AI 健康计划 */
export interface AiPlanData {
  planId: number
  planName: string
  dietSuggest: string
  sportSuggest: string
  sleepSuggest: string
}

export interface AiPlanRequest {
  healthNeed: string
}

/** AI 知识推送 */
export interface AiPushData {
  pushIds: number[]
}

export interface AiPushRequest {
  profileId: number
}
