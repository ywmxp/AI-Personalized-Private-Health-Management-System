import request from './request'
import type { ApiResponse, LoginResponse } from '../types'

const MOCK_AUTH = import.meta.env.VITE_MOCK_AUTH === 'true'

/** 登录 —— 后端就绪后删掉 mock 分支即可，调用方零改动 */
export async function login(phone: string, password: string) {
  if (MOCK_AUTH) {
    await new Promise((r) => setTimeout(r, 300))
    return {
      data: {
        code: 0,
        message: 'success',
        data: {
          token: 'mock-token-' + Date.now(),
          user: {
            userId: 1,
            username: phone,
            phone: phone,
            role: 'USER',
          },
        },
      } as ApiResponse<LoginResponse>,
    }
  }
  return request.post<ApiResponse<LoginResponse>>('/api/auth/login', { phone, password })
}

/** 注册 —— 后端就绪后删掉 mock 分支即可，调用方零改动 */
export async function register(phone: string, username: string, password: string, confirmPassword: string) {
  if (MOCK_AUTH) {
    await new Promise((r) => setTimeout(r, 300))
    return {
      data: {
        code: 0,
        message: '注册成功',
        data: null,
      } as ApiResponse<null>,
    }
  }
  return request.post<ApiResponse<null>>('/api/auth/register', { phone, username, password, confirmPassword })
}
