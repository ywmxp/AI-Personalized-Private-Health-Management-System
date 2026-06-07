/** 用户信息 */
export interface User {
  id: number
  username: string
  email: string
  role: 'admin' | 'user'
  avatar?: string
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
