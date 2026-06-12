import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '../types'

const request = axios.create({
  timeout: 30000,
})

// 请求拦截器：自动带 token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResponse
    if (data.code !== 0) {
      // AI 服务异常时精简提示
      const msg = data.code === 50000
        ? 'AI 服务异常，请稍后重试'
        : (data.message || '请求失败')
      ElMessage.error(msg)
      // reject 时附带原始 code，便于调用方按需处理
      const err = new Error(data.message) as Error & { code: number }
      err.code = data.code
      return Promise.reject(err)
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    }
    // 请求超时
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      ElMessage.error('请求超时，AI 正在处理中，请稍后重试')
      return Promise.reject(error)
    }
    ElMessage.error(error.response?.data?.message || '网络错误')
    return Promise.reject(error)
  },
)

export default request
