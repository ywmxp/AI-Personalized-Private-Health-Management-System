/** 健康数据记录 */
export interface HealthRecord {
  id: number
  userId: number
  type: string
  value: number
  unit: string
  recordedAt: string
}

/** 上传健康数据请求 */
export interface HealthDataUpload {
  type: string
  value: number
  unit: string
}
