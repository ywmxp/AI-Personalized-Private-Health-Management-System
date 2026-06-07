import request from './request'
import type { ApiResponse, User } from '../types'

const MOCK_EDIT = import.meta.env.VITE_MOCK_EDIT === 'true'

/** 获取当前用户信息 */
export function getCurrentUser() {
  return request.get<ApiResponse<User>>('/api/users/me')
}

/** 修改当前用户信息 —— VITE_MOCK_EDIT=true 时使用 mock */
export async function updateProfile(data: {
  username: string
  birthDate?: string
  gender?: string
  height?: number
}) {
  if (MOCK_EDIT) {
    await new Promise((r) => setTimeout(r, 300))
    return {
      data: {
        code: 0,
        message: '修改成功',
        data: null,
      } as ApiResponse<null>,
    }
  }
  return request.put<ApiResponse<null>>('/api/users/me', data)
}
