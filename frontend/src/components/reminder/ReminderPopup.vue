<template>
  <Teleport to="body">
    <Transition name="popup-fade">
      <div v-if="visible" class="reminder-popup-overlay" @click.self="handleDismiss">
        <div class="reminder-popup">
          <div class="popup-header">
            <div class="popup-icon">{{ typeIcon }}</div>
            <div class="popup-title">
              <h4>{{ typeLabel }}</h4>
              <span class="popup-time">⏰ {{ reminder.reminderTime }}</span>
            </div>
            <el-button :icon="Close" circle size="small" @click="handleDismiss" />
          </div>
          <div class="popup-body">
            <p>{{ reminderMessage }}</p>
          </div>
          <div class="popup-footer">
            <el-button size="small" @click="handleSnooze">稍后提醒 (10分钟)</el-button>
            <el-button size="small" type="primary" @click="handleDone">已完成</el-button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Close } from '@element-plus/icons-vue'
import { getReminders } from '../../api/reminder'
import type { Reminder } from '../../types'

// ============ Props & Emits ============
const emit = defineEmits<{
  dismiss: []
  done: [reminder: Reminder]
  snooze: [reminder: Reminder]
}>()

// ============ 状态 ============
const visible = ref(false)
const reminder = ref<Reminder | null>(null)

const typeLabel = computed(() => {
  const labels: Record<string, string> = {
    MEDICATION: '💊 吃药时间到',
    EXERCISE: '🏃 该运动啦',
    SLEEP: '😴 准备睡觉',
    WATER: '💧 记得喝水',
    CUSTOM: '📌 自定义提醒',
  }
  return labels[reminder.value?.reminderType || ''] || '健康提醒'
})

const typeIcon = computed(() => {
  const icons: Record<string, string> = {
    MEDICATION: '💊',
    EXERCISE: '🏃',
    SLEEP: '😴',
    WATER: '💧',
    CUSTOM: '📌',
  }
  return icons[reminder.value?.reminderType || ''] || '🔔'
})

const reminderMessage = computed(() => {
  const messages: Record<string, string> = {
    MEDICATION: '请按时服药，保持健康从规律用药开始。',
    EXERCISE: '运动让身体更健康，一起动起来吧！',
    SLEEP: '充足的睡眠是健康的基石，该休息了。',
    WATER: '每天喝足够的水，保持身体水分平衡。',
    CUSTOM: '别忘了您设定的提醒事项哦。',
  }
  return messages[reminder.value?.reminderType || ''] || '您有一个健康提醒！'
})

// ============ 方法 ============
let checkTimer: ReturnType<typeof setInterval> | null = null
let snoozeTimer: ReturnType<typeof setTimeout> | null = null
const checkedReminders = new Set<number>()

async function checkReminders() {
  try {
    const res = await getReminders()
    const list = (res.data.data || []) as Reminder[]
    if (visible.value || list.length === 0) return

    // 只弹出开启的提醒，且距离上次弹出超过检查间隔
    const now = new Date()
    const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:00`

    for (const r of list) {
      if (r.status !== 1) continue
      if (checkedReminders.has(r.reminderId)) continue
      // 当前时间匹配（±1 分钟容差）
      if (isTimeMatch(r.reminderTime, currentTime)) {
        reminder.value = r
        visible.value = true
        checkedReminders.add(r.reminderId)
        break
      }
    }
  } catch {
    // 静默处理
  }
}

function isTimeMatch(t1: string, t2: string): boolean {
  const [h1, m1] = t1.split(':').map(Number)
  const [h2, m2] = t2.split(':').map(Number)
  return h1 === h2 && Math.abs(m1 - m2) <= 1
}

function handleDismiss() {
  visible.value = false
  emit('dismiss')
}

function handleDone() {
  visible.value = false
  if (reminder.value) emit('done', reminder.value)
}

function handleSnooze() {
  visible.value = false
  if (reminder.value) {
    emit('snooze', reminder.value)
    // 10 分钟后重新检查该提醒
    snoozeTimer = setTimeout(() => {
      checkedReminders.delete(reminder.value!.reminderId)
    }, 10 * 60 * 1000)
  }
}

// ============ 生命周期 ============
onMounted(() => {
  // 每 30 秒检查一次提醒
  checkTimer = setInterval(checkReminders, 30000)
  // 初始检查
  checkReminders()
})

onUnmounted(() => {
  if (checkTimer) clearInterval(checkTimer)
  if (snoozeTimer) clearTimeout(snoozeTimer)
})
</script>

<style scoped>
.reminder-popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  padding: 24px;
  z-index: 9999;
}

.reminder-popup {
  width: 380px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.18);
  overflow: hidden;
  animation: slideUp 0.35s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(40px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.popup-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 20px 0;
}
.popup-icon {
  font-size: 36px;
}
.popup-title {
  flex: 1;
}
.popup-title h4 {
  margin: 0;
  font-size: 17px;
  color: #303133;
}
.popup-time {
  color: #909399;
  font-size: 13px;
}

.popup-body {
  padding: 16px 20px;
}
.popup-body p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
}

.popup-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 20px 20px;
}

/* Transition */
.popup-fade-enter-active,
.popup-fade-leave-active {
  transition: opacity 0.3s ease;
}
.popup-fade-enter-from,
.popup-fade-leave-to {
  opacity: 0;
}
</style>
