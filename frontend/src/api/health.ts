import request from './request'
import type { ApiResponse, HealthRecord, HealthDataCreateRequest, PaginatedData, TrendResponse } from '../types'

/** 查询参数 */
export interface HealthDataQuery {
  dataType?: string
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}

/** 录入健康数据（API §3.1） */
export function createRecord(data: HealthDataCreateRequest) {
  return request.post<ApiResponse<{ dataId: number }>>('/api/health-data', data)
}

/** 查询健康数据列表（API §3.2） */
export function queryRecords(params: HealthDataQuery) {
  return request.get<ApiResponse<PaginatedData<HealthRecord>>>('/api/health-data', { params })
}

/** 删除健康数据（API §3.3） */
export function deleteRecord(dataId: number) {
  return request.delete<ApiResponse<null>>(`/api/health-data/${dataId}`)
}

/** 批量导入 CSV（API §3.4） */
export function importCsv(file: File) {
  const form = new FormData()
  form.append('file', file)
  return request.post<ApiResponse<null>>('/api/health-data/import', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 查询健康趋势（API §3.5） */
export function getTrends(params: { dataType: string; startTime: string; endTime: string }) {
  return request.get<ApiResponse<TrendResponse>>('/api/health-data/trends', { params })
}
