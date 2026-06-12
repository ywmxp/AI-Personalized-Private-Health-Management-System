/**
 * Mock 工具 — 后端 API 未就绪时返回模拟数据
 * 设置环境变量 VITE_MOCK_API=true 启用
 * 后端接口补全后设为 false 即可切换到真实 API
 */
import type { ApiResponse } from '../types'

// 是否开启全局 Mock（后端 API 未就绪时默认开启）
// 设为 VITE_MOCK_API=false 可切换到真实后端 API
const MOCK_API = import.meta.env.VITE_MOCK_API !== 'false'

/** 创建 mock 成功的 ApiResponse */
export function mockSuccess<T>(data: T): { data: ApiResponse<T> } {
  return {
    data: {
      code: 0,
      message: 'success',
      data,
    },
  }
}

/** 创建 mock 分页 ApiResponse */
export function mockPage<T>(items: T[], page = 1, size = 10, total = 0): { data: ApiResponse<{ items: T[]; page: number; size: number; total: number }> } {
  return {
    data: {
      code: 0,
      message: 'success',
      data: { items, page, size, total: total || items.length },
    },
  }
}

// ---- 模拟数据 ----

export const mockHealthRecords = {
  items: [
    { id: 1, userId: 1, type: 'WEIGHT', value: 70.5, unit: 'kg', recordedAt: '2026-06-01 08:30:00' },
    { id: 2, userId: 1, type: 'WEIGHT', value: 69.8, unit: 'kg', recordedAt: '2026-06-03 08:30:00' },
    { id: 3, userId: 1, type: 'BLOOD_PRESSURE', value: 120, unit: 'mmHg', recordedAt: '2026-06-01 09:00:00' },
    { id: 4, userId: 1, type: 'SLEEP_HOURS', value: 7.5, unit: '小时', recordedAt: '2026-06-05 07:00:00' },
    { id: 5, userId: 1, type: 'EXERCISE_MINUTES', value: 45, unit: '分钟', recordedAt: '2026-06-04 18:00:00' },
  ],
  page: 1,
  size: 10,
  total: 5,
}

export const mockTrends = {
  dataType: 'WEIGHT',
  points: [
    { date: '2026-05-01', value: '71.2' },
    { date: '2026-05-05', value: '70.8' },
    { date: '2026-05-10', value: '70.5' },
    { date: '2026-05-15', value: '70.1' },
    { date: '2026-05-20', value: '69.8' },
    { date: '2026-05-25', value: '69.5' },
    { date: '2026-06-01', value: '70.5' },
  ],
}

export const mockReminders = [
  { reminderId: 1, userId: 1, reminderType: 'MEDICATION', reminderTime: '08:30:00', status: 1 },
  { reminderId: 2, userId: 1, reminderType: 'EXERCISE', reminderTime: '18:00:00', status: 1 },
  { reminderId: 3, userId: 1, reminderType: 'SLEEP', reminderTime: '22:30:00', status: 0 },
]

export const mockKnowledgeItems = [
  { knowledgeId: 1, title: '每日饮水指南', content: '成年人每天建议饮水1500-2000ml，少量多次饮用效果更佳。晨起一杯温水有助唤醒身体。', relateTag: '饮水', source: '系统内置', createTime: '2026-05-01 10:00:00' },
  { knowledgeId: 2, title: '改善睡眠质量的10个方法', content: '保持规律作息、睡前1小时远离电子屏幕、保持卧室温度适宜、适度运动都有助于改善睡眠。', relateTag: '睡眠', source: '系统内置', createTime: '2026-05-02 10:00:00' },
  { knowledgeId: 3, title: '科学运动建议', content: '每周至少150分钟中等强度有氧运动，结合力量训练效果更佳。运动前后注意热身和拉伸。', relateTag: '运动', source: '系统内置', createTime: '2026-05-03 10:00:00' },
]

export const mockKnowledgePushes = [
  { pushId: 1, knowledgeId: 1, title: '每日饮水指南', relateTag: '饮水', pushTime: '2026-06-01 08:00:00', isRead: 0 },
  { pushId: 2, knowledgeId: 2, title: '改善睡眠质量的10个方法', relateTag: '睡眠', pushTime: '2026-06-01 08:00:00', isRead: 1 },
]

export const mockAdminUsers = [
  { userId: 1, username: 'admin', phone: '138****8000', email: 'admin@health.app', role: 'admin', status: 1, createTime: '2026-05-01 10:00:00', birthDate: '1995-03-15', gender: 'MALE', height: 175 },
  { userId: 2, username: 'test_user', phone: '139****9000', email: 'test@health.app', role: 'user', status: 1, createTime: '2026-05-05 14:00:00', birthDate: '1998-07-22', gender: 'FEMALE', height: 162 },
  { userId: 3, username: 'zhangsan', phone: '137****7000', email: 'zhangsan@example.com', role: 'user', status: 0, createTime: '2026-05-10 09:00:00', birthDate: '2000-01-10', gender: 'UNKNOWN', height: 170 },
]

export const mockLoginLogs = [
  { logId: 1, userId: 2, phone: '139****9000', loginIp: '192.168.1.100', loginTime: '2026-06-09 08:30:00', loginResult: 1 },
  { logId: 2, userId: 3, phone: '137****7000', loginIp: '192.168.1.101', loginTime: '2026-06-09 09:15:00', loginResult: 0 },
  { logId: 3, userId: 2, phone: '139****9000', loginIp: '192.168.1.100', loginTime: '2026-06-10 07:45:00', loginResult: 1 },
  { logId: 4, userId: 1, phone: '138****8000', loginIp: '10.0.0.1', loginTime: '2026-06-10 08:00:00', loginResult: 1 },
  { logId: 5, userId: 3, phone: '137****7000', loginIp: '192.168.1.101', loginTime: '2026-06-10 08:20:00', loginResult: 1 },
]

export const mockPlatformStatistics = {
  totalUsers: 128,
  totalHealthData: 3560,
  riskDistribution: { low: 85, medium: 32, high: 11 },
  dailyDataCount: [
    { date: '2026-05-01', count: 42 },
    { date: '2026-05-05', count: 55 },
    { date: '2026-05-10', count: 38 },
    { date: '2026-05-15', count: 61 },
    { date: '2026-05-20', count: 48 },
    { date: '2026-05-25', count: 52 },
    { date: '2026-06-01', count: 45 },
  ],
}

export function isMockEnabled() {
  return MOCK_API
}
