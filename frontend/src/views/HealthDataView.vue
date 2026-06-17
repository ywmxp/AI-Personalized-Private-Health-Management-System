<template>
  <div class="health-data-page">
    <div class="page-header">
      <h2>💪 健康数据管理</h2>
      <p class="subtitle">记录和管理您的日常健康指标</p>
      <p class="page-motto">每个数字，都是对自己的一份承诺</p>
    </div>

    <!-- 数据录入卡片 -->
    <el-card class="input-card" shadow="never">
      <h4>📝 录入健康数据</h4>
      <div class="input-form">
        <div class="form-row">
          <div class="form-item">
            <label>数据类型</label>
            <el-select v-model="form.dataType" placeholder="选择类型" style="width: 180px">
              <el-option label="🩸 血压" value="BLOOD_PRESSURE" />
              <el-option label="🩸 血糖" value="BLOOD_GLUCOSE" />
              <el-option label="⚖️ 体重" value="WEIGHT" />
              <el-option label="🏃 运动时长" value="EXERCISE_MINUTES" />
              <el-option label="😴 睡眠时长" value="SLEEP_HOURS" />
            </el-select>
          </div>
          <div class="form-item">
            <label>数值</label>
            <!-- 血压：双输入框 -->
            <template v-if="form.dataType === 'BLOOD_PRESSURE'">
              <div class="bp-inputs">
                <el-input v-model="bpSystolic" placeholder="收缩压" style="width: 80px" />
                <span class="bp-slash">/</span>
                <el-input v-model="bpDiastolic" placeholder="舒张压" style="width: 80px" />
              </div>
            </template>
            <el-input v-else v-model="form.dataValue" placeholder="如 70.5" style="width: 160px" />
          </div>
          <div class="form-item">
            <label>单位</label>
            <span class="unit-text">{{ dataUnit }}</span>
          </div>
          <div class="form-item">
            <label>记录时间</label>
            <el-date-picker
              v-model="form.recordTime"
              type="datetime"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择时间"
              style="width: 200px"
            />
          </div>
        </div>
        <div class="form-actions">
          <el-button type="primary" :loading="submitting" @click="handleSubmit" :icon="Upload">
            提交数据
          </el-button>
          <el-button @click="handleImport">
            <el-icon><Upload /></el-icon> 批量导入 (CSV)
          </el-button>
          <el-link href="/health_data_template.csv" target="_blank" :underline="false" style="margin-left: 8px">
            <el-icon><Download /></el-icon> 下载模板
          </el-link>
          <input
            ref="fileInput"
            type="file"
            accept=".csv"
            style="display: none"
            @change="handleFileChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 数据列表 -->
    <el-card class="list-card" shadow="never">
      <div class="list-header">
        <h4>📋 历史记录</h4>
        <div class="list-filters">
          <el-select v-model="filterType" placeholder="全部类型" clearable @change="fetchRecords" style="width: 140px">
            <el-option label="血压" value="BLOOD_PRESSURE" />
            <el-option label="血糖" value="BLOOD_GLUCOSE" />
            <el-option label="体重" value="WEIGHT" />
            <el-option label="运动时长" value="EXERCISE_MINUTES" />
            <el-option label="睡眠时长" value="SLEEP_HOURS" />
          </el-select>
        </div>
      </div>

      <el-table v-loading="loading" :data="records" stripe style="width: 100%">
        <el-table-column label="数据类型" width="130">
          <template #default="{ row }">
            <el-tag size="small">{{ dataTypeLabel(row.dataType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数值" width="120">
          <template #default="{ row }">
            <span class="value-text">{{ row.dataValue }} {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="记录时间" min-width="160">
          <template #default="{ row }">
            {{ row.recordTime }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该条记录？" @confirm="handleDelete(row.dataId)">
              <template #reference>
                <el-button type="danger" size="small" link :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && records.length === 0" description="暂无健康数据，请录入" />

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, prev, pager, next"
          @change="fetchRecords"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Delete, Download } from '@element-plus/icons-vue'
import { getHealthRecords, uploadHealthData, deleteHealthData, importHealthData } from '../api/health'
import type { HealthRecord } from '../types'

// ============ 表单 ============
const now = () => {
  const d = new Date()
  return d.toISOString().replace('T', ' ').slice(0, 19)
}

const form = reactive({
  dataType: 'WEIGHT',
  dataValue: '70.5',
  recordTime: now(),
})

const dataUnits: Record<string, string> = {
  BLOOD_PRESSURE: 'mmHg',
  BLOOD_GLUCOSE: 'mmol/L',
  WEIGHT: 'kg',
  EXERCISE_MINUTES: '分钟',
  SLEEP_HOURS: '小时',
}
const dataUnit = computed(() => dataUnits[form.dataType] || '')

// 血压双输入框
const bpSystolic = ref('120')
const bpDiastolic = ref('80')
// 切换血压时从 form.dataValue 拆分，否则组合
function syncBpFromForm() {
  if (form.dataType === 'BLOOD_PRESSURE' && form.dataValue.includes('/')) {
    const parts = form.dataValue.split('/')
    bpSystolic.value = parts[0]
    bpDiastolic.value = parts[1]
  }
}
function syncBpToForm() {
  if (form.dataType === 'BLOOD_PRESSURE') {
    form.dataValue = bpSystolic.value + '/' + bpDiastolic.value
  }
}

const submitting = ref(false)

async function handleSubmit() {
  syncBpToForm()
  if (!form.dataType || !form.dataValue.trim()) {
    ElMessage.warning('请完善数据信息')
    return
  }
  // 非血压类型不允许负数
  if (form.dataType !== 'BLOOD_PRESSURE') {
    const val = parseFloat(form.dataValue)
    if (isNaN(val) || val < 0) {
      ElMessage.warning('请输入有效的非负数值')
      return
    }
  }
  submitting.value = true
  try {
    await uploadHealthData({
      dataType: form.dataType,
      dataValue: String(form.dataValue),
      unit: dataUnit.value,
      recordTime: form.recordTime,
    })
    ElMessage.success('数据录入成功')
    form.dataValue = ''
    form.recordTime = now()
    await fetchRecords()
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

// ============ 批量导入 ============
const fileInput = ref<HTMLInputElement | null>(null)

function handleImport() {
  fileInput.value?.click()
}

async function handleFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    const res = await importHealthData(file)
    const count = res.data.data?.importedCount || 0
    ElMessage.success(`成功导入 ${count} 条数据`)
    await fetchRecords()
  } catch {
    // handled
  } finally {
    input.value = ''
  }
}

// ============ 记录列表 ============
const loading = ref(false)
const records = ref<HealthRecord[]>([])
const filterType = ref<string | null>(null)
const pagination = reactive({ page: 1, size: 10, total: 0 })

async function fetchRecords() {
  loading.value = true
  try {
    const res = await getHealthRecords({
      dataType: filterType.value || undefined,
      page: pagination.page,
      size: pagination.size,
    })
    const data = res.data.data
    records.value = data.items || []
    pagination.total = data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await deleteHealthData(id)
    ElMessage.success('删除成功')
    await fetchRecords()
  } catch {
    // handled
  }
}

const typeLabels: Record<string, string> = {
  BLOOD_PRESSURE: '🩸 血压',
  BLOOD_GLUCOSE: '🩸 血糖',
  WEIGHT: '⚖️ 体重',
  EXERCISE_MINUTES: '🏃 运动时长',
  SLEEP_HOURS: '😴 睡眠时长',
}
function dataTypeLabel(type: string) {
  return typeLabels[type] || type
}

onMounted(fetchRecords)
</script>

<style scoped>
.health-data-page { max-width: 960px; margin: 0 auto; }

.page-header { margin-bottom: 24px; }
.page-header h2 {
  margin: 0 0 6px; font-size: 24px; color: var(--c-text);
  font-family: var(--font-display);
}
.subtitle { color: var(--c-text-muted); margin: 0; font-size: 13px; }
.page-motto {
  margin-top: 8px; font-size: 13px; font-style: italic;
  color: var(--c-text-muted); opacity: 0.7;
  font-family: var(--font-display);
}

.input-card, .list-card {
  border-radius: var(--r-lg); border: 1px solid var(--c-border-light);
  margin-bottom: 20px;
}
.input-card :deep(.el-card__body),
.list-card :deep(.el-card__body) { padding: 22px 24px; }

.input-card h4, .list-header h4 {
  margin: 0 0 14px; color: var(--c-text); font-size: 15px; font-weight: 600;
}
.form-row { display: flex; align-items: flex-end; gap: 16px; flex-wrap: wrap; margin-bottom: 16px; }
.form-item { display: flex; flex-direction: column; gap: 5px; }
.form-item label { font-size: 12px; color: var(--c-text-secondary); font-weight: 500; }
.unit-text { padding-top: 6px; font-size: 14px; color: var(--c-text-muted); display: inline-block; min-width: 50px; }
.form-actions { display: flex; gap: 12px; }

/* Input overrides */
:deep(.el-select .el-input__wrapper),
:deep(.el-input-number .el-input__wrapper),
:deep(.el-date-picker .el-input__wrapper) {
  border-radius: var(--r-md);
  box-shadow: 0 0 0 1px var(--c-border) inset !important;
  background: var(--c-bg);
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
.unit-text {
  padding-top: 6px;
  font-size: 14px;
  color: #909399;
  display: inline-block;
  min-width: 50px;
}
.bp-inputs {
  display: flex;
  align-items: center;
  gap: 4px;
}
.bp-slash {
  font-size: 18px;
  font-weight: 600;
  color: #606266;
}
.form-actions {
  display: flex;
  gap: 12px;
}
:deep(.el-select .el-input__wrapper:hover),
:deep(.el-input-number .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--c-primary) inset !important;
}

.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.list-header h4 { margin: 0; }

/* Table polish */
:deep(.el-table) { border-radius: var(--r-md); overflow: hidden; }
:deep(.el-table th.el-table__cell) {
  background: var(--c-bg); color: var(--c-text-secondary);
  font-weight: 600; font-size: 12px; letter-spacing: 0.02em;
  border-bottom: 2px solid var(--c-border);
}
:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid var(--c-border-light);
}
:deep(.el-table .el-table__row:hover > td) {
  background: var(--c-primary-light);
}
:deep(.el-table__body tr:last-child td) {
  border-bottom: none;
}

.value-text { font-weight: 600; color: var(--c-primary); font-size: 15px; font-family: var(--font-display); }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 18px; }

/* Data type tag colors */
:deep(.el-tag) { border-radius: var(--r-sm); font-weight: 500; }
</style>
