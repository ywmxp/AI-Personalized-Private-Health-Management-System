<template>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
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
  } finally {
    submitting.value = false
  }
}

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
  } finally {
    loading.value = false
  }
}

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
}
</style>
