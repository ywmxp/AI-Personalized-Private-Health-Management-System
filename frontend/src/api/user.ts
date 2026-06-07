import request from './request'
import type { ApiResponse, User } from '../types'

/** 获取当前用户信息 */
export function getCurrentUser() {
  return request.get<ApiResponse<User>>('/api/users/me')
}

/** 修改当前用户信息 */
export function updateProfile(data: {
  username: string
  birthDate?: string
  gender?: string
  height?: number
}) {
  return request.put<ApiResponse<null>>('/api/users/me', data)
}
