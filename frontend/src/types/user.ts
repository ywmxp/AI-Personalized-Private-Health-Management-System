/** 用户信息（对齐后端 CurrentUserResponse / UserSummaryResponse） */
export interface User {
  userId: number
  username: string
  phone: string
  birthDate?: string
  gender?: 'MALE' | 'FEMALE' | 'UNKNOWN'
  height?: number
  role: 'USER' | 'ADMIN'
}

/** 登录请求 */
export interface LoginRequest {
  phone: string
  password: string
}

/** 注册请求 */
export interface RegisterRequest {
  phone: string
  username: string
  password: string
  confirmPassword: string
}

/** 登录响应 */
export interface LoginResponse {
  token: string
  user: User
}
