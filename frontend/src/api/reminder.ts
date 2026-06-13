import request from './request'
import { isMockEnabled, mockSuccess, mockReminders, addMockReminder, updateMockReminder, toggleMockReminderStatus } from './mock'
import type { ApiResponse, Reminder, ReminderRequest } from '../types'

/** 获取提醒列表 */
export async function getReminders() {
  if (isMockEnabled()) return mockSuccess([...mockReminders])
  return request.get<ApiResponse<Reminder[]>>('/api/reminders')
}

/** 新增提醒 */
export async function createReminder(data: ReminderRequest) {
  if (isMockEnabled()) return mockSuccess(addMockReminder(data) as Reminder)
  return request.post<ApiResponse<Reminder>>('/api/reminders', data)
}

/** 修改提醒 */
export async function updateReminder(reminderId: number, data: ReminderRequest) {
  if (isMockEnabled()) return mockSuccess(updateMockReminder(reminderId, data) as Reminder)
  return request.put<ApiResponse<Reminder>>(`/api/reminders/${reminderId}`, data)
}

/** 启用/关闭提醒 */
export async function toggleReminderStatus(_reminderId: number, _status: number) {
  if (isMockEnabled()) { toggleMockReminderStatus(_reminderId, _status); return mockSuccess(null) }
  return request.patch<ApiResponse<null>>(`/api/reminders/${_reminderId}/status`, { status: _status })
}
