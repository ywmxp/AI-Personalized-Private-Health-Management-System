/** 提醒类型 */
export type ReminderType = 'EXERCISE' | 'MEDICATION' | 'SLEEP' | 'WATER' | 'CUSTOM'

/** 提醒记录 */
export interface Reminder {
  reminderId: number
  userId: number
  reminderType: ReminderType
  reminderTime: string   // HH:mm:ss
  status: number         // 1=开启, 0=关闭
  createTime?: string
}

/** 新增/修改提醒请求 */
export interface ReminderRequest {
  reminderType: ReminderType
  reminderTime: string
  status: number
}
