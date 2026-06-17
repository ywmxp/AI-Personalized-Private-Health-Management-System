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
            <el-input v-else v-model="form.dataValue" placeholder="输入数值" style="width: 160px" />
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
            {{ formatDateTime(row.recordTime) }}
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

    <el-dialog
      v-model="overwriteDialogVisible"
      title="发现重复记录"
      width="720px"
      destroy-on-close
    >
      <div v-if="pendingConflict" class="compare-dialog">
        <p class="compare-tip">该时间已存在同类型数据，请核对后决定是否覆盖。</p>
        <div class="compare-grid">
          <div class="compare-card">
            <h5>已有数据</h5>
            <div class="compare-row"><span>类型</span><strong>{{ dataTypeLabel(pendingConflict.existingRecord.dataType) }}</strong></div>
            <div class="compare-row"><span>数值</span><strong>{{ pendingConflict.existingRecord.dataValue }}</strong></div>
            <div class="compare-row"><span>单位</span><strong>{{ pendingConflict.existingRecord.unit }}</strong></div>
            <div class="compare-row"><span>时间</span><strong>{{ formatDateTime(pendingConflict.existingRecord.recordTime) }}</strong></div>
          </div>
          <div class="compare-card compare-card--incoming">
            <h5>当前录入</h5>
            <div class="compare-row"><span>类型</span><strong>{{ dataTypeLabel(pendingConflict.incomingRecord.dataType) }}</strong></div>
            <div class="compare-row"><span>数值</span><strong>{{ pendingConflict.incomingRecord.dataValue }}</strong></div>
            <div class="compare-row"><span>单位</span><strong>{{ pendingConflict.incomingRecord.unit }}</strong></div>
            <div class="compare-row"><span>时间</span><strong>{{ formatDateTime(pendingConflict.incomingRecord.recordTime) }}</strong></div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="closeOverwriteDialog">取消</el-button>
        <el-button type="primary" :loading="overwriteSubmitting" @click="confirmOverwrite">
          确认覆盖
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="importConflictDialogVisible"
      title="CSV 中发现重复记录"
      width="880px"
      destroy-on-close
    >
      <div v-if="importConflict" class="import-dialog">
        <p class="compare-tip">
          共发现 {{ importConflict.duplicateCount }} 条重复记录。可以选择覆盖所有重复项，或跳过重复项仅导入不冲突的数据。
        </p>
        <el-table :data="importConflictPreview" size="small" border max-height="320">
          <el-table-column label="已有数据" min-width="300">
            <template #default="{ row }">
              <div class="import-preview-cell">
                <strong>{{ dataTypeLabel(row.existingRecord.dataType) }}</strong>
                <span>{{ row.existingRecord.dataValue }} {{ row.existingRecord.unit }}</span>
                <span>{{ formatDateTime(row.existingRecord.recordTime) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="CSV 数据" min-width="300">
            <template #default="{ row }">
              <div class="import-preview-cell">
                <strong>{{ dataTypeLabel(row.incomingRecord.dataType) }}</strong>
                <span>{{ row.incomingRecord.dataValue }} {{ row.incomingRecord.unit }}</span>
                <span>{{ formatDateTime(row.incomingRecord.recordTime) }}</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <p v-if="importConflict.duplicates.length > importConflictPreview.length" class="import-note">
          仅展示前 {{ importConflictPreview.length }} 条重复记录。
        </p>
      </div>
      <template #footer>
        <el-button @click="closeImportConflictDialog">取消</el-button>
        <el-button :loading="importDecisionSubmitting" @click="resolveImportConflict('SKIP')">
          跳过重复
        </el-button>
        <el-button type="primary" :loading="importDecisionSubmitting" @click="resolveImportConflict('OVERWRITE')">
          覆盖全部
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Delete, Download } from '@element-plus/icons-vue'
import { getHealthRecords, uploadHealthData, deleteHealthData, importHealthData } from '../api/health'
import type {
  HealthDataConflictResponse,
  HealthDataCreateRequest,
  HealthDataImportConflictItem,
  HealthDataImportConflictResponse,
  HealthRecord,
} from '../types'

// ============ 表单 ============
const now = () => {
  const d = new Date()
  d.setSeconds(0, 0)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:00`
}

const form = reactive({
  dataType: 'WEIGHT',
  dataValue: '',
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
const bpSystolic = ref('')
const bpDiastolic = ref('')

watch(() => form.dataType, (nextType, prevType) => {
  if (nextType === prevType) {
    return
  }
  form.dataValue = ''
  if (nextType === 'BLOOD_PRESSURE') {
    bpSystolic.value = ''
    bpDiastolic.value = ''
  }
})

const submitting = ref(false)
const overwriteSubmitting = ref(false)
const overwriteDialogVisible = ref(false)
const pendingConflict = ref<HealthDataConflictResponse | null>(null)

async function handleSubmit() {
  const payload = buildSubmitPayload()
  if (!payload) {
    return
  }

  submitting.value = true
  try {
    await submitHealthData(payload)
  } finally {
    submitting.value = false
  }
}

function buildSubmitPayload(overwrite = false): HealthDataCreateRequest | null {
  let dataValue = form.dataValue.trim()

  if (form.dataType === 'BLOOD_PRESSURE') {
    const systolic = bpSystolic.value.trim()
    const diastolic = bpDiastolic.value.trim()
    if (!systolic || !diastolic) {
      ElMessage.warning('请完善血压数值')
      return null
    }
    dataValue = `${systolic}/${diastolic}`
  }

  if (!form.dataType || !dataValue) {
    ElMessage.warning('请完善数据信息')
    return null
  }
  // 非血压类型不允许负数
  if (form.dataType !== 'BLOOD_PRESSURE') {
    const val = parseFloat(dataValue)
    if (isNaN(val) || val < 0) {
      ElMessage.warning('请输入有效的非负数值')
      return null
    }
  }
  form.dataValue = dataValue
  return {
    dataType: form.dataType,
    dataValue,
    unit: dataUnit.value,
    recordTime: form.recordTime,
    overwrite,
  }
}

async function submitHealthData(payload: HealthDataCreateRequest): Promise<boolean> {
  try {
    await uploadHealthData(payload)
    ElMessage.success(payload.overwrite ? '数据已覆盖' : '数据录入成功')
    await fetchRecords()
    return true
  } catch (error) {
    const err = error as Error & { code?: number; data?: unknown }
    if (err.code === 40901) {
      pendingConflict.value = err.data as HealthDataConflictResponse
      overwriteDialogVisible.value = true
      return false
    }
    return false
  }
}

function closeOverwriteDialog() {
  overwriteDialogVisible.value = false
  pendingConflict.value = null
}

async function confirmOverwrite() {
  const payload = buildSubmitPayload(true)
  if (!payload) {
    return
  }
  overwriteSubmitting.value = true
  try {
    const success = await submitHealthData(payload)
    if (success) {
      closeOverwriteDialog()
    }
  } finally {
    overwriteSubmitting.value = false
  }
}

// ============ 批量导入 ============
const fileInput = ref<HTMLInputElement | null>(null)
const pendingImportFile = ref<File | null>(null)
const importConflictDialogVisible = ref(false)
const importConflict = ref<HealthDataImportConflictResponse | null>(null)
const importDecisionSubmitting = ref(false)
const importConflictPreview = computed<HealthDataImportConflictItem[]>(() =>
  importConflict.value?.duplicates.slice(0, 5) || []
)

function handleImport() {
  fileInput.value?.click()
}

async function handleFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    pendingImportFile.value = file
    await submitImportFile()
  } finally {
    input.value = ''
  }
}

async function submitImportFile(action?: 'OVERWRITE' | 'SKIP') {
  if (!pendingImportFile.value) return
  try {
    const res = await importHealthData(pendingImportFile.value, action)
    const result = res.data.data
    const parts = [`新增 ${result.importedCount} 条`]
    if (result.overwrittenCount > 0) {
      parts.push(`覆盖 ${result.overwrittenCount} 条`)
    }
    if (result.skippedDuplicateCount > 0) {
      parts.push(`跳过 ${result.skippedDuplicateCount} 条重复`)
    }
    ElMessage.success(`导入完成：${parts.join('，')}`)
    closeImportConflictDialog()
    pendingImportFile.value = null
    await fetchRecords()
  } catch (error) {
    const err = error as Error & { code?: number; data?: unknown }
    if (err.code === 40902) {
      importConflict.value = err.data as HealthDataImportConflictResponse
      importConflictDialogVisible.value = true
      return
    }
  }
}

function closeImportConflictDialog() {
  importConflictDialogVisible.value = false
  importConflict.value = null
  pendingImportFile.value = null
}

async function resolveImportConflict(action: 'OVERWRITE' | 'SKIP') {
  importDecisionSubmitting.value = true
  try {
    await submitImportFile(action)
  } finally {
    importDecisionSubmitting.value = false
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

function formatDateTime(value?: string) {
  return value?.replace('T', ' ') || ''
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

.compare-tip {
  margin: 0 0 16px;
  color: var(--c-text-secondary);
  line-height: 1.6;
}

.compare-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.compare-card {
  padding: 16px;
  border: 1px solid var(--c-border);
  border-radius: var(--r-md);
  background: var(--c-bg);
}

.compare-card--incoming {
  background: var(--c-primary-light);
}

.compare-card h5 {
  margin: 0 0 12px;
  font-size: 15px;
  color: var(--c-text);
}

.compare-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 8px 0;
  border-bottom: 1px solid var(--c-border-light);
}

.compare-row:last-child {
  border-bottom: none;
}

.compare-row span {
  color: var(--c-text-secondary);
}

.compare-row strong {
  color: var(--c-text);
  text-align: right;
}

.import-preview-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.5;
}

.import-note {
  margin: 12px 0 0;
  color: var(--c-text-muted);
  font-size: 12px;
}

@media (max-width: 720px) {
  .compare-grid {
    grid-template-columns: 1fr;
  }
}
</style>
