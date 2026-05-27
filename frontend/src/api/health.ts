import request from './request'
import type { ApiResponse, HealthRecord, HealthDataUpload } from '../types'

/** 获取健康数据列表 */
export function getHealthRecords() {
  return request.get<ApiResponse<HealthRecord[]>>('/api/health/records')
}

/** 上传健康数据 */
export function uploadHealthData(data: HealthDataUpload) {
  return request.post<ApiResponse<HealthRecord>>('/api/health/records', data)
}
