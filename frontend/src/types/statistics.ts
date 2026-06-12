/** 平台统计数据 */
export interface PlatformStatistics {
  totalUsers: number
  totalHealthData: number
  riskDistribution: RiskDistribution
  dailyDataCount: DailyDataCount[]
}

export interface RiskDistribution {
  low: number
  medium: number
  high: number
}

export interface DailyDataCount {
  date: string
  count: number
}

/** 用户列表项（管理员视角） */
export interface AdminUserItem {
  userId: number
  username: string
  phone: string
  email?: string
  role: string
  status: number    // 1=启用, 0=禁用
  createTime: string
<<<<<<< HEAD
  birthDate?: string
  gender?: string
  height?: number
}

/** 登录日志 */
export interface LoginLog {
  logId: number
  userId: number
  phone: string
  loginIp: string
  loginTime: string
  loginResult: number  // 1=成功, 0=失败
=======
>>>>>>> dev
}
