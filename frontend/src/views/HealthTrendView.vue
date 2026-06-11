<template>
  <div class="health-trend-page">
    <div class="page-header">
      <h2>📈 健康趋势图</h2>
      <p class="subtitle">可视化查看您的健康数据变化趋势</p>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <div class="filter-item">
          <label>数据类型：</label>
          <el-select v-model="filters.dataType" placeholder="选择数据类型" @change="fetchTrends">
            <el-option label="🩸 血压" value="BLOOD_PRESSURE" />
            <el-option label="🩸 血糖" value="BLOOD_GLUCOSE" />
            <el-option label="⚖️ 体重" value="WEIGHT" />
            <el-option label="🏃 运动时长" value="EXERCISE_MINUTES" />
            <el-option label="😴 睡眠时长" value="SLEEP_HOURS" />
          </el-select>
        </div>
        <div class="filter-item">
          <label>时间范围：</label>
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="fetchTrends"
          />
        </div>
        <el-button type="primary" @click="fetchTrends" :loading="loading" :icon="Search">
          查询
        </el-button>
        <el-button @click="handleExport" :disabled="!trendData || trendData.points.length === 0" :icon="Download">
          导出报告
        </el-button>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <el-card class="chart-card" shadow="never" v-loading="loading">
      <template v-if="trendData && trendData.points.length > 0">
        <div class="chart-header">
          <h3>{{ dataTypeLabel }}</h3>
          <span class="chart-unit">单位：{{ dataTypeUnit }}</span>
        </div>

        <!-- Canvas 趋势图 -->
        <div class="chart-container">
          <canvas ref="chartCanvas" class="trend-canvas"></canvas>
        </div>

        <!-- 数据统计 -->
        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-label">平均值</span>
            <span class="stat-value">{{ avgValue }}</span>
            <span class="stat-unit">{{ dataTypeUnit }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">最高值</span>
            <span class="stat-value high">{{ maxValue }}</span>
            <span class="stat-unit">{{ dataTypeUnit }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">最低值</span>
            <span class="stat-value low">{{ minValue }}</span>
            <span class="stat-unit">{{ dataTypeUnit }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">数据点</span>
            <span class="stat-value">{{ trendData.points.length }}</span>
            <span class="stat-unit">个</span>
          </div>
        </div>

        <!-- 趋势描述 -->
        <div class="trend-desc">
          <el-icon :size="18"><TrendCharts /></el-icon>
          <span>{{ trendDescription }}</span>
        </div>
      </template>

      <EmptyState
        v-else-if="!loading"
        description="选择数据类型和时间范围，查看健康趋势"
      />
    </el-card>

    <!-- 数据明细表 -->
    <el-card v-if="trendData && trendData.points.length > 0" class="table-card" shadow="never">
      <h4 style="margin: 0 0 16px; color: #303133">📊 数据明细</h4>
      <el-table :data="trendData.points" stripe size="small" max-height="300">
        <el-table-column prop="date" label="日期" width="150" />
        <el-table-column prop="value" label="数值" width="120" />
        <el-table-column label="单位" width="100">
          <template>{{ dataTypeUnit }}</template>
        </el-table-column>
        <el-table-column label="与均值差值" min-width="140">
          <template #default="{ row }">
            <span :style="{ color: diffColor(Number(row.value) - Number(avgNumeric)), fontWeight: '600' }">
              {{ diffText(Number(row.value) - Number(avgNumeric)) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download, TrendCharts } from '@element-plus/icons-vue'
import { getHealthTrends } from '../api/health'
import EmptyState from '../components/common/EmptyState.vue'

// ============ 筛选 ============
const filters = reactive({
  dataType: 'WEIGHT',
  dateRange: null as [string, string] | null,
})

// 默认日期范围（最近30天）
function getDefaultDateRange(): [string, string] {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  const fmt = (d: Date) => d.toISOString().split('T')[0]
  return [fmt(start), fmt(end)]
}

// ============ 趋势数据 ============
const loading = ref(false)
const trendData = ref<{ dataType: string; points: { date: string; value: string }[] } | null>(null)

const dataTypeLabel = computed(() => {
  const labels: Record<string, string> = {
    BLOOD_PRESSURE: '血压趋势',
    BLOOD_GLUCOSE: '血糖趋势',
    WEIGHT: '体重趋势',
    EXERCISE_MINUTES: '运动时长趋势',
    SLEEP_HOURS: '睡眠时长趋势',
  }
  return labels[filters.dataType] || '健康数据趋势'
})

const dataTypeUnit = computed(() => {
  const units: Record<string, string> = {
    BLOOD_PRESSURE: 'mmHg',
    BLOOD_GLUCOSE: 'mmol/L',
    WEIGHT: 'kg',
    EXERCISE_MINUTES: '分钟',
    SLEEP_HOURS: '小时',
  }
  return units[filters.dataType] || ''
})

const avgNumeric = computed(() => {
  if (!trendData.value?.points?.length) return 0
  const nums = trendData.value.points.map((p) => Number(p.value) || 0)
  return nums.reduce((a, b) => a + b, 0) / nums.length
})
const avgValue = computed(() => avgNumeric.value.toFixed(1))
const maxValue = computed(() => {
  if (!trendData.value?.points?.length) return '--'
  return Math.max(...trendData.value.points.map((p) => Number(p.value) || 0)).toFixed(1)
})
const minValue = computed(() => {
  if (!trendData.value?.points?.length) return '--'
  return Math.min(...trendData.value.points.map((p) => Number(p.value) || 0)).toFixed(1)
})

const trendDescription = computed(() => {
  if (!trendData.value?.points || trendData.value.points.length < 2) return '数据点不足，无法分析趋势'
  const points = trendData.value.points
  const first = Number(points[0].value) || 0
  const last = Number(points[points.length - 1].value) || 0
  const diff = last - first
  const pct = first !== 0 ? ((diff / first) * 100).toFixed(1) : '0'

  if (Math.abs(diff) < 0.01) return '在此期间数据基本保持稳定'
  if (diff > 0) return `整体呈上升趋势，增长约 ${pct}%，建议持续关注`
  return `整体呈下降趋势，下降约 ${Math.abs(Number(pct))}%，继续保持！`
})

function diffText(diff: number) {
  if (Math.abs(diff) < 0.01) return '持平'
  return diff > 0 ? `+${diff.toFixed(1)}` : diff.toFixed(1)
}
function diffColor(diff: number) {
  if (Math.abs(diff) < 0.01) return '#909399'
  return diff > 0 ? '#f56c6c' : '#67c23a'
}

// ============ Canvas 图表 ============
const chartCanvas = ref<HTMLCanvasElement | null>(null)

function drawChart() {
  const canvas = chartCanvas.value
  if (!canvas || !trendData.value?.points?.length) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const dpr = window.devicePixelRatio || 1
  const rect = canvas.getBoundingClientRect()
  canvas.width = rect.width * dpr
  canvas.height = rect.height * dpr
  ctx.scale(dpr, dpr)

  const W = rect.width
  const H = rect.height
  const padding = { top: 24, right: 24, bottom: 44, left: 56 }
  const chartW = W - padding.left - padding.right
  const chartH = H - padding.top - padding.bottom

  const points = trendData.value.points
  const values = points.map((p) => Number(p.value) || 0)
  const minVal = Math.min(...values)
  const maxVal = Math.max(...values)
  const valRange = maxVal - minVal || 1

  // 背景
  ctx.fillStyle = '#fff'
  ctx.fillRect(0, 0, W, H)

  // 网格线
  ctx.strokeStyle = '#f0f0f0'
  ctx.lineWidth = 1
  for (let i = 0; i <= 4; i++) {
    const y = padding.top + (chartH / 4) * i
    ctx.beginPath()
    ctx.moveTo(padding.left, y)
    ctx.lineTo(W - padding.right, y)
    ctx.stroke()

    // Y轴标签
    const val = maxVal - (valRange / 4) * i
    ctx.fillStyle = '#909399'
    ctx.font = '12px sans-serif'
    ctx.textAlign = 'right'
    ctx.fillText(val.toFixed(1), padding.left - 8, y + 4)
  }

  // X轴标签
  ctx.textAlign = 'center'
  const xStep = chartW / Math.max(points.length - 1, 1)
  const labelStep = Math.max(1, Math.floor(points.length / 8))
  points.forEach((p, i) => {
    if (i % labelStep === 0 || i === points.length - 1) {
      const x = padding.left + xStep * i
      ctx.fillStyle = '#909399'
      ctx.font = '11px sans-serif'
      ctx.fillText(p.date.slice(5), x, H - padding.bottom + 18)
    }
  })

  // 渐变填充
  const gradient = ctx.createLinearGradient(0, padding.top, 0, padding.top + chartH)
  gradient.addColorStop(0, 'rgba(64, 158, 255, 0.25)')
  gradient.addColorStop(1, 'rgba(64, 158, 255, 0.02)')

  ctx.beginPath()
  points.forEach((p, i) => {
    const x = padding.left + xStep * i
    const y = padding.top + chartH - ((Number(p.value) - minVal) / valRange) * chartH
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  })
  // 闭合填充区域
  const lastX = padding.left + xStep * (points.length - 1)
  ctx.lineTo(lastX, padding.top + chartH)
  ctx.lineTo(padding.left, padding.top + chartH)
  ctx.closePath()
  ctx.fillStyle = gradient
  ctx.fill()

  // 折线
  ctx.beginPath()
  points.forEach((p, i) => {
    const x = padding.left + xStep * i
    const y = padding.top + chartH - ((Number(p.value) - minVal) / valRange) * chartH
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  })
  ctx.strokeStyle = '#409eff'
  ctx.lineWidth = 2.5
  ctx.lineJoin = 'round'
  ctx.stroke()

  // 数据点
  points.forEach((p, i) => {
    const x = padding.left + xStep * i
    const y = padding.top + chartH - ((Number(p.value) - minVal) / valRange) * chartH
    ctx.beginPath()
    ctx.arc(x, y, 4, 0, Math.PI * 2)
    ctx.fillStyle = '#fff'
    ctx.fill()
    ctx.strokeStyle = '#409eff'
    ctx.lineWidth = 2.5
    ctx.stroke()
  })
}

// ============ 数据获取 ============
async function fetchTrends() {
  loading.value = true
  try {
    const [startTime, endTime] = filters.dateRange || getDefaultDateRange()
    const res = await getHealthTrends({
      dataType: filters.dataType,
      startTime: startTime + ' 00:00:00',
      endTime: endTime + ' 23:59:59',
    })
    trendData.value = res.data.data
    await nextTick()
    drawChart()
  } catch {
    trendData.value = null
  } finally {
    loading.value = false
  }
}

// ============ 导出 ============
function handleExport() {
  if (!trendData.value?.points?.length) return

  const headers = ['日期', `数值(${dataTypeUnit.value})`, '与均值差值']
  const rows = trendData.value.points.map((p) => {
    const diff = (Number(p.value) - avgNumeric.value).toFixed(1)
    return [p.date, p.value, diff]
  })

  let csv = '﻿' // BOM for Excel
  csv += headers.join(',') + '\n'
  rows.forEach((r) => (csv += r.join(',') + '\n'))

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `健康趋势_${dataTypeLabel.value}_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('报告导出成功')
}

// ============ Resize ============
let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  if (!filters.dateRange) {
    filters.dateRange = getDefaultDateRange()
  }
  fetchTrends()

  resizeObserver = new ResizeObserver(() => {
    drawChart()
  })
  if (chartCanvas.value) {
    resizeObserver.observe(chartCanvas.value)
  }
})
</script>

<style scoped>
.health-trend-page {
  max-width: 1000px;
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

.filter-card {
  margin-bottom: 20px;
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-item label {
  white-space: nowrap;
  color: #606266;
  font-size: 14px;
}

.chart-card {
  margin-bottom: 20px;
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.chart-header h3 {
  margin: 0;
  color: #303133;
}
.chart-unit {
  color: #909399;
  font-size: 13px;
}
.chart-container {
  width: 100%;
  height: 320px;
}
.trend-canvas {
  width: 100%;
  height: 100%;
}

/* 统计 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}
.stat-item {
  text-align: center;
}
.stat-label {
  display: block;
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}
.stat-value.high { color: #f56c6c; }
.stat-value.low { color: #67c23a; }
.stat-unit {
  font-size: 13px;
  color: #909399;
  margin-left: 2px;
}

.trend-desc {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #606266;
  font-size: 14px;
}

.table-card {
  margin-bottom: 20px;
}
</style>
