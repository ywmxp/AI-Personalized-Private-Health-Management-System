import request from './request'
import { isMockEnabled, mockSuccess } from './mock'
import type { ApiResponse, AiProfileData, AiProfileRequest, AiPlanData, AiPlanRequest, AiPushData, AiPushRequest } from '../types'

// ---- Mock 数据 ----
const mockProfile: AiProfileData = {
  profileId: 1,
  healthTags: ['睡眠不足', '运动达标', '体重正常', '血压正常'],
  riskLevel: 'LOW',
  analysis: '根据您近期的健康数据，整体健康状况良好。体重和血压均在正常范围内，运动习惯保持较好。但睡眠时长略有不足，建议保证每天7-8小时的优质睡眠，睡前减少电子屏幕使用时间，有助于进一步提升健康水平。',
}

const mockPlan: AiPlanData = {
  planId: 1,
  planName: '改善睡眠专属健康计划',
  dietSuggest: '晚餐宜清淡，避免高油高盐及辛辣食物。晚餐时间建议在19:00前完成，睡前3小时不再进食。可适量摄入含色氨酸的食物如牛奶、香蕉、坚果，有助于促进睡眠。减少咖啡因摄入，下午2点后不再饮用咖啡或浓茶。',
  sportSuggest: '每天保持30-45分钟中等强度运动，如快走、慢跑、游泳或骑行。运动时间建议安排在下午4-6点，此时身体状态最佳。避免睡前2小时内进行剧烈运动，以免过度兴奋影响入睡。每周至少5天坚持运动，周末可适当增加户外活动。',
  sleepSuggest: '固定作息时间，建议每晚22:30前上床、早上6:30起床。睡前1小时远离手机、电脑等电子屏幕，蓝光会抑制褪黑素分泌。营造舒适的睡眠环境：保持卧室温度18-22°C，使用遮光窗帘，必要时使用白噪音或耳塞。睡前可进行10分钟冥想或深呼吸练习，帮助身心放松。',
}

// ---- API ----

/** 生成健康画像 */
export async function generateProfile(_data: AiProfileRequest) {
  if (isMockEnabled()) {
    await new Promise((r) => setTimeout(r, 800)) // 模拟 AI 生成延迟
    return mockSuccess(mockProfile)
  }
  return request.post<ApiResponse<AiProfileData>>('/api/ai/profile', _data)
}

/** 生成健康计划 */
export async function generatePlan(_data: AiPlanRequest) {
  if (isMockEnabled()) {
    await new Promise((r) => setTimeout(r, 800))
    return mockSuccess(mockPlan)
  }
  return request.post<ApiResponse<AiPlanData>>('/api/ai/plan', _data)
}

/** 生成健康知识推送 */
export async function pushKnowledge(_data: AiPushRequest) {
  if (isMockEnabled()) return mockSuccess({ pushIds: [1, 2] })
  return request.post<ApiResponse<AiPushData>>('/api/ai/knowledge-push', _data)
}
