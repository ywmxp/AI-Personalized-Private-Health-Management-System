<template>
<<<<<<< HEAD
  <div class="health-data-page">
    <div class="page-header">
      <h2>💪 健康数据管理</h2>
      <p class="subtitle">记录和管理您的日常健康指标</p>
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
            <el-input-number v-model="form.dataValue" :min="0" :precision="1" style="width: 160px" />
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
            <el-tag size="small">{{ dataTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数值" width="120">
          <template #default="{ row }">
            <span class="value-text">{{ row.value }} {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="记录时间" min-width="160">
          <template #default="{ row }">
            {{ row.recordedAt }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该条记录？" @confirm="handleDelete(row.id)">
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
=======
  <div class="page">
    <h2>健康数据</h2>

    <el-tabs v-model="activeTab">
      <!-- ========== Tab 1: 数据管理 ========== -->
      <el-tab-pane label="数据管理" name="manage">
        <!-- 录入表单 -->
        <el-card class="section-card">
          <template #header><span>录入健康数据</span></template>
          <el-form :model="form" :rules="rules" ref="formRef" inline class="entry-form">
            <el-form-item label="数据类型" prop="dataType">
              <el-select v-model="form.dataType" placeholder="请选择" style="width: 160px">
                <el-option
                  v-for="item in DATA_TYPES"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="数值" prop="dataValue">
              <el-input v-model="form.dataValue" placeholder="如 70.5 或 120/80" style="width: 180px" />
            </el-form-item>
            <el-form-item label="记录时间" prop="recordTime">
              <el-date-picker
                v-model="form.recordTime"
                type="datetime"
                placeholder="选择时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 200px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleCreate">
                提交
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 查询 + 列表 -->
        <el-card class="section-card">
          <template #header><span>数据记录</span></template>
          <div class="filter-bar">
            <el-select v-model="query.dataType" placeholder="数据类型" clearable style="width: 160px">
              <el-option
                v-for="item in DATA_TYPES"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-date-picker
              v-model="query.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 360px"
            />
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </div>

          <el-table :data="tableData" v-loading="loading" stripe class="data-table">
            <el-table-column prop="recordTime" label="记录时间" width="180" />
            <el-table-column prop="dataType" label="数据类型" width="120">
              <template #default="{ row }">{{ typeLabel(row.dataType) }}</template>
            </el-table-column>
            <el-table-column prop="dataValue" label="数值" min-width="120" />
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button type="danger" size="small" text @click="handleDelete(row.dataId)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="pagination.total > 0"
            class="pagination"
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @change="fetchRecords"
          />
        </el-card>
      </el-tab-pane>

      <!-- ========== Tab 2: 批量导入 ========== -->
      <el-tab-pane label="批量导入" name="import">
        <el-card class="section-card">
          <template #header><span>CSV 批量导入健康数据</span></template>

          <el-alert type="info" :closable="false" show-icon class="import-alert">
            <template #title>
              请先下载 CSV 模板，按模板格式填写健康数据后上传
            </template>
          </el-alert>

          <el-steps :active="importStep" align-center class="import-steps">
            <el-step title="下载模板" description="获取 CSV 模板文件" />
            <el-step title="选择文件" description="选择填写好的 CSV" />
            <el-step title="开始导入" description="上传并解析数据" />
          </el-steps>

          <div class="import-actions">
            <el-button type="primary" plain @click="downloadTemplate">下载 CSV 模板</el-button>
            <el-upload
              :auto-upload="false"
              accept=".csv"
              :limit="1"
              :on-change="handleFileChange"
              :file-list="fileList"
              class="import-upload"
            >
              <el-button type="primary" plain>选择 CSV 文件</el-button>
            </el-upload>
            <el-button type="primary" :loading="importing" :disabled="!selectedFile" @click="handleImport">
              开始导入
            </el-button>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
>>>>>>> dev
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
<<<<<<< HEAD
import { ElMessage } from 'element-plus'
import { Upload, Delete } from '@element-plus/icons-vue'
import { getHealthRecords, uploadHealthData, deleteHealthData, importHealthData } from '../api/health'
import type { HealthRecord } from '../types'

// ============ 表单 ============
const now = () => {
  const d = new Date()
  return d.toISOString().replace('T', ' ').slice(0, 19)
}

const form = reactive({
  dataType: 'WEIGHT',
  dataValue: 70,
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

const submitting = ref(false)

async function handleSubmit() {
  if (!form.dataType || form.dataValue <= 0) {
    ElMessage.warning('请完善数据信息')
    return
  }
  submitting.value = true
  try {
    await uploadHealthData({
      type: form.dataType,
      value: form.dataValue,
      unit: dataUnit.value,
    })
    ElMessage.success('数据录入成功')
    form.dataValue = 70
    form.recordTime = now()
    await fetchRecords()
  } catch {
    // handled
=======
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, UploadFile } from 'element-plus'
import { createRecord, queryRecords, deleteRecord, importCsv } from '../api/health'
import type { HealthRecord, HealthDataCreateRequest, PaginatedData } from '../types'

// ---- 常量 ----
const DATA_TYPES = [
  { label: '血压', value: 'BLOOD_PRESSURE' },
  { label: '血糖', value: 'BLOOD_GLUCOSE' },
  { label: '体重', value: 'WEIGHT' },
  { label: '运动时长', value: 'EXERCISE_MINUTES' },
  { label: '睡眠时长', value: 'SLEEP_HOURS' },
] as const

function typeLabel(v: string) {
  return DATA_TYPES.find((d) => d.value === v)?.label || v
}

// ---- Tab 状态 ----
const activeTab = ref('manage')

// ---- 录入表单 ----
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<HealthDataCreateRequest>({
  dataType: '',
  dataValue: '',
  recordTime: '',
})

const rules = {
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  dataValue: [{ required: true, message: '请输入数值', trigger: 'blur' }],
  recordTime: [{ required: true, message: '请选择记录时间', trigger: 'change' }],
}

async function handleCreate() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createRecord({ ...form })
    ElMessage.success('录入成功')
    Object.assign(form, { dataType: '', dataValue: '', recordTime: '' })
    fetchRecords()
  } catch {
    // 错误已在拦截器中处理
>>>>>>> dev
  } finally {
    submitting.value = false
  }
}

<<<<<<< HEAD
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
=======
// ---- 查询 ----
const loading = ref(false)
const tableData = ref<HealthRecord[]>([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const query = reactive({
  dataType: '',
  dateRange: null as [string, string] | null,
})

async function fetchRecords() {
  loading.value = true
  try {
    const params: Record<string, string | number> = {
      page: pagination.page,
      size: pagination.size,
    }
    if (query.dataType) params.dataType = query.dataType
    if (query.dateRange) {
      params.startTime = query.dateRange[0]
      params.endTime = query.dateRange[1]
    }
    const res = await queryRecords(params)
    const data: PaginatedData<HealthRecord> = res.data.data
    tableData.value = data.items
    pagination.total = data.total
  } catch {
    // 错误已在拦截器中处理
>>>>>>> dev
  } finally {
    loading.value = false
  }
}

<<<<<<< HEAD
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
.health-data-page {
  max-width: 960px;
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

.input-card {
  margin-bottom: 20px;
}
.input-card h4 {
  margin: 0 0 16px;
  color: #303133;
}
.form-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 16px;
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
.form-actions {
  display: flex;
  gap: 12px;
}

.list-card h4 {
  margin: 0 0 16px;
  color: #303133;
}
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.list-header h4 {
  margin: 0;
}
.value-text {
  font-weight: 600;
  color: #409eff;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
=======
function handleQuery() {
  pagination.page = 1
  fetchRecords()
}

// ---- 删除 ----
async function handleDelete(dataId: number) {
  try {
    await ElMessageBox.confirm('确定删除该条记录？', '确认删除', { type: 'warning' })
    await deleteRecord(dataId)
    ElMessage.success('删除成功')
    fetchRecords()
  } catch {
    // 用户取消或出错
  }
}

// ---- 批量导入 ----
const selectedFile = ref<File | null>(null)
const fileList = ref<UploadFile[]>([])
const importing = ref(false)

const importStep = computed(() => {
  if (importing.value) return 2
  if (selectedFile.value) return 1
  return 0
})

function handleFileChange(file: UploadFile) {
  selectedFile.value = file.raw || null
}

function downloadTemplate() {
  const csv = 'dataType,dataValue,recordTime\nWEIGHT,70.5,2026-05-24 08:30:00\n'
  const blob = new Blob([csv], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'health_data_template.csv'
  a.click()
  URL.revokeObjectURL(url)
}

async function handleImport() {
  if (!selectedFile.value) return
  importing.value = true
  try {
    await importCsv(selectedFile.value)
    ElMessage.success('导入成功')
    selectedFile.value = null
    fileList.value = []
  } catch {
    // 错误已在拦截器中处理
  } finally {
    importing.value = false
  }
}

// ---- 生命周期 ----
onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.section-card {
  margin-bottom: 20px;
}

.entry-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.data-table {
  width: 100%;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.import-alert {
  margin-bottom: 24px;
}

.import-steps {
  margin-bottom: 24px;
}

.import-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.import-upload {
  display: inline-flex;
>>>>>>> dev
}
</style>
