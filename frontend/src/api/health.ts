import request from './request'
import { isMockEnabled, mockSuccess, getMockHealthRecords, addMockHealthRecord, deleteMockHealthRecord, mockTrends } from './mock'
import type { ApiResponse, HealthRecord } from '../types'

/** 获取健康数据列表 */
export async function getHealthRecords(_params?: { dataType?: string; startTime?: string; endTime?: string; page?: number; size?: number }) {
  if (isMockEnabled()) {
    const data = getMockHealthRecords()
    return mockSuccess({ items: [...data.items], page: data.page, size: data.size, total: data.total })
  }
  return request.get<ApiResponse<{ items: HealthRecord[]; page: number; size: number; total: number }>>('/api/health-data', { params: _params })
}

/** 上传健康数据 */
export async function uploadHealthData(data: { dataType: string; dataValue: string; unit: string }) {
  if (isMockEnabled()) return mockSuccess(addMockHealthRecord(data) as unknown as HealthRecord)
  return request.post<ApiResponse<HealthRecord>>('/api/health-data', data)
}

/** 删除健康数据 */
export async function deleteHealthData(_dataId: number) {
  if (isMockEnabled()) { deleteMockHealthRecord(_dataId); return mockSuccess(null) }
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
}
