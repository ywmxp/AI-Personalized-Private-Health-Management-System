/** 健康数据记录（对齐 API 文档 §3.2 列表项） */
export interface HealthRecord {
  dataId: number
  dataType: string
  dataValue: string
  recordTime: string
}

/** 录入健康数据请求（对齐 API 文档 §3.1） */
export interface HealthDataCreateRequest {
  dataType: string
  dataValue: string
  recordTime: string
}

/** 分页响应 */
export interface PaginatedData<T> {
  items: T[]
  page: number
  size: number
  total: number
}

/** 趋势数据点（对齐 API 文档 §3.5） */
export interface TrendPoint {
  date: string
  value: string
}

/** 趋势响应 */
export interface TrendResponse {
  dataType: string
  points: TrendPoint[]
}
