import request from './request'
<<<<<<< HEAD
import { isMockEnabled, mockSuccess, mockHealthRecords, mockTrends } from './mock'
import type { ApiResponse, HealthRecord, HealthDataUpload } from '../types'

/** 获取健康数据列表 */
export async function getHealthRecords(params?: { dataType?: string; startTime?: string; endTime?: string; page?: number; size?: number }) {
  if (isMockEnabled()) return mockSuccess(mockHealthRecords)
  return request.get<ApiResponse<{ items: HealthRecord[]; page: number; size: number; total: number }>>('/api/health-data', { params })
}

/** 上传健康数据 */
export async function uploadHealthData(data: HealthDataUpload) {
  if (isMockEnabled()) return mockSuccess({ id: Date.now() } as HealthRecord)
  return request.post<ApiResponse<HealthRecord>>('/api/health-data', data)
}

/** 删除健康数据 */
export async function deleteHealthData(_dataId: number) {
  if (isMockEnabled()) return mockSuccess(null)
  return request.delete<ApiResponse<null>>(`/api/health-data/${_dataId}`)
}

/** 健康趋势数据 */
export async function getHealthTrends(_params: { dataType: string; startTime: string; endTime: string }) {
  if (isMockEnabled()) return mockSuccess(mockTrends)
  return request.get<ApiResponse<{ dataType: string; points: { date: string; value: string }[] }>>('/api/health-data/trends', { params: _params })
}

/** 批量导入健康数据 */
export async function importHealthData(_file: File) {
  if (isMockEnabled()) return mockSuccess({ importedCount: 5 })
  const formData = new FormData()
  formData.append('file', _file)
  return request.post<ApiResponse<{ importedCount: number }>>('/api/health-data/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
=======
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
>>>>>>> dev
}
