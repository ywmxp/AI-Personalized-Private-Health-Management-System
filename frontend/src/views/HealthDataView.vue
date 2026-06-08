<template>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
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
}
</style>
