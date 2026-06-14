<template>
  <div class="reminder-page">
    <div class="page-header">
      <h2>⏰ 健康提醒设置</h2>
      <p class="subtitle">设置定时提醒，帮助您养成健康生活习惯</p>
    </div>

    <!-- 提醒类型提示 -->
    <el-alert
      title="提示"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 20px"
    >
      设置提醒后，系统将在指定时间弹出提醒通知。您可以随时开启或关闭提醒。
    </el-alert>

    <!-- 新增提醒 -->
    <el-card class="add-card" shadow="never">
      <div class="add-form">
        <div class="form-item">
          <label>提醒类型：</label>
          <el-select v-model="newReminder.reminderType" placeholder="选择提醒类型" style="width: 180px">
            <el-option label="💊 吃药提醒" value="MEDICATION" />
            <el-option label="🏃 运动提醒" value="EXERCISE" />
            <el-option label="😴 睡眠提醒" value="SLEEP" />
            <el-option label="💧 喝水提醒" value="WATER" />
            <el-option label="📌 自定义" value="CUSTOM" />
          </el-select>
        </div>
        <div class="form-item">
          <label>提醒时间：</label>
          <el-time-picker
            v-model="newReminder.reminderTime"
            format="HH:mm"
            value-format="HH:mm:ss"
            placeholder="选择时间"
            style="width: 180px"
          />
        </div>
        <el-button type="primary" :loading="adding" @click="handleAdd" :icon="Plus">
          添加提醒
        </el-button>
      </div>
    </el-card>

    <!-- 提醒列表 -->
    <el-card class="list-card" shadow="never">
      <div v-loading="loading">
        <el-empty v-if="!loading && reminders.length === 0" description="暂无提醒，点击上方添加" />
        <el-table v-else :data="reminders" stripe style="width: 100%">
          <el-table-column label="提醒类型" width="150">
            <template #default="{ row }">
              {{ reminderTypeLabel(row.reminderType) }}
            </template>
          </el-table-column>
          <el-table-column label="提醒时间" width="140">
            <template #default="{ row }">
              <span class="time-text">{{ row.reminderTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '已开启' : '已关闭' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="200">
            <template #default="{ row }">
              <el-switch
                :model-value="row.status === 1"
                active-text="开"
                inactive-text="关"
                @change="(val: boolean) => handleToggle(row, val)"
                style="margin-right: 12px"
              />
              <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑提醒" width="450px" destroy-on-close>
      <el-form :model="editForm" label-width="80px" v-if="editForm">
        <el-form-item label="提醒类型">
          <el-select v-model="editForm.reminderType" style="width: 100%">
            <el-option label="💊 吃药提醒" value="MEDICATION" />
            <el-option label="🏃 运动提醒" value="EXERCISE" />
            <el-option label="😴 睡眠提醒" value="SLEEP" />
            <el-option label="💧 喝水提醒" value="WATER" />
            <el-option label="📌 自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒时间">
          <el-time-picker
            v-model="editForm.reminderTime"
            format="HH:mm"
            value-format="HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getReminders, createReminder, updateReminder, toggleReminderStatus } from '../api/reminder'
import type { Reminder, ReminderType, ReminderRequest } from '../types'

// ============ 数据 ============
const reminders = ref<Reminder[]>([])
const loading = ref(false)
const adding = ref(false)
const saving = ref(false)

// ============ 新增表单 ============
const newReminder = reactive<{ reminderType: ReminderType | ''; reminderTime: string }>({
  reminderType: '',
  reminderTime: '',
})

async function handleAdd() {
  if (!newReminder.reminderType) { ElMessage.warning('请选择提醒类型'); return }
  if (!newReminder.reminderTime) { ElMessage.warning('请选择提醒时间'); return }
  adding.value = true
  try {
    await createReminder({
      reminderType: newReminder.reminderType as ReminderType,
      reminderTime: newReminder.reminderTime,
      status: 1,
    })
    ElMessage.success('提醒添加成功')
    newReminder.reminderType = ''
    newReminder.reminderTime = ''
    await fetchReminders()
  } catch {
    // handled
  } finally {
    adding.value = false
  }
}

// ============ 编辑 ============
const editVisible = ref(false)
const editForm = ref<ReminderRequest | null>(null)
const editingId = ref<number | null>(null)

function handleEdit(row: Reminder) {
  editingId.value = row.reminderId
  editForm.value = {
    reminderType: row.reminderType,
    reminderTime: row.reminderTime,
    status: row.status,
  }
  editVisible.value = true
}

async function handleSave() {
  if (!editForm.value || editingId.value === null) return
  saving.value = true
  try {
    await updateReminder(editingId.value, editForm.value)
    ElMessage.success('修改成功')
    editVisible.value = false
    await fetchReminders()
  } catch {
    // handled
  } finally {
    saving.value = false
  }
}

// ============ 开关 ============
async function handleToggle(row: Reminder, val: boolean) {
  const newStatus = val ? 1 : 0
  try {
    await toggleReminderStatus(row.reminderId, newStatus)
    row.status = newStatus
    ElMessage.success(val ? '提醒已开启' : '提醒已关闭')
  } catch {
    // handled
  }
}

// ============ 查询 ============
async function fetchReminders() {
  loading.value = true
  try {
    const res = await getReminders()
    reminders.value = res.data.data || []
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

// ============ 工具 ============
const typeLabels: Record<string, string> = {
  MEDICATION: '💊 吃药提醒',
  EXERCISE: '🏃 运动提醒',
  SLEEP: '😴 睡眠提醒',
  WATER: '💧 喝水提醒',
  CUSTOM: '📌 自定义',
}
function reminderTypeLabel(type: ReminderType) {
  return typeLabels[type] || type
}

onMounted(fetchReminders)
</script>

<style scoped>
.reminder-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}
.page-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #303133;
}
.subtitle {
  color: #909399;
  margin: 0;
}

.add-card {
  margin-bottom: 20px;
}
.add-form {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}
.form-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-item label {
  font-size: 13px;
  color: #606266;
}

.time-text {
  font-weight: 600;
  color: #409eff;
  font-size: 15px;
}
</style>
