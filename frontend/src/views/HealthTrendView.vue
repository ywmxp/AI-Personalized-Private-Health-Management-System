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
          <el-select v-model="filters.dataType" placeholder="选择数据类型" @change="fetchTrends" style="width: 200px">
            <el-option label="血压" value="BLOOD_PRESSURE" />
            <el-option label="血糖" value="BLOOD_GLUCOSE" />
            <el-option label="体重" value="WEIGHT" />
            <el-option label="运动时长" value="EXERCISE_MINUTES" />
            <el-option label="睡眠时长" value="SLEEP_HOURS" />
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
        <el-button @click="handleDownloadChart" :disabled="!trendData || trendData.points.length === 0" :icon="Download">
          下载图表
        </el-button>
        <el-button @click="handleExportCSV" :disabled="!trendData || trendData.points.length === 0">
          导出 CSV
        </el-button>
        <el-button type="primary" @click="handleExportPDF" :disabled="!trendData || trendData.points.length === 0" :icon="Document">
          导出 PDF 报告
        </el-button>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <el-card class="chart-card" shadow="never" v-loading="loading">
      <template v-if="trendData && trendData.points.length > 0">
        <div class="chart-header">
          <h3>{{ dataTypeLabel }}</h3>
          <div class="chart-legend" v-if="isBP">
            <span class="legend-dot" style="background:#E85D4A"></span>收缩压
            <span class="legend-dot" style="background:#409EFF;margin-left:12px"></span>舒张压
          </div>
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

    <!-- 24h 时间占比环形图（仅睡眠/运动） -->
    <el-card v-if="showDonut && trendData && trendData.points.length > 0" class="donut-card" shadow="never">
      <h4 style="margin: 0 0 16px; color: #303133">📊 {{ donutTitle }}</h4>
      <div style="display:flex;align-items:center;gap:32px;flex-wrap:wrap">
        <div class="donut-container"><canvas ref="donutCanvas" class="donut-canvas"></canvas></div>
        <div class="donut-stats">
          <div class="donut-stat"><span class="donut-dot" style="background:#6366F1"></span><span>睡眠 <strong>{{ donutSleepH.toFixed(1) }}h</strong></span></div>
          <div class="donut-stat"><span class="donut-dot" style="background:#0D7377"></span><span>运动 <strong>{{ donutExerH.toFixed(1) }}h</strong></span></div>
          <div class="donut-stat"><span class="donut-dot" style="background:#E8E3DD"></span><span>其他 <strong>{{ donutOtherH.toFixed(1) }}h</strong></span></div>
          <div class="donut-stat" style="margin-top:6px;font-size:14px;color:#909399">{{ donutSummary }}</div>
        </div>
      </div>
    </el-card>

    <!-- 数据明细表 -->
    <el-card v-if="trendData && trendData.points.length > 0" class="table-card" shadow="never">
      <h4 style="margin: 0 0 16px; color: #303133">📊 数据明细</h4>
      <el-table :data="trendData.points" stripe size="small" max-height="300">
        <el-table-column prop="date" label="日期" width="150" />
        <el-table-column :label="'数值 (' + dataTypeUnit + ')'" width="180">
          <template #default="{ row }">{{ row.value }}</template>
        </el-table-column>
        <el-table-column :label="'与均值差值 (' + dataTypeUnit + ')'" min-width="160">
          <template #default="{ row }">
            <span :style="{ color: diffColor((parseValues(row.value)[0]) - avgNumeric), fontWeight: '600' }">
              {{ isBP ? diffBPText(row.value, avgNumeric) : diffText(parseValues(row.value)[0] - avgNumeric) }}
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
import { Search, Download, TrendCharts, Document } from '@element-plus/icons-vue'
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

const isBP = computed(() => filters.dataType === 'BLOOD_PRESSURE')

/** 解析血压值 "120/80" → [120, 80]，非血压返回 [Number(value)] */
function parseValues(v: string): number[] {
  if (v.includes('/')) return v.split('/').map(Number)
  return [Number(v) || 0]
}

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
  const nums = trendData.value.points.map((p) => parseValues(p.value)[0])
  return nums.reduce((a, b) => a + b, 0) / nums.length
})
const avgValue = computed(() => isBP.value
  ? trendData.value!.points.map(p => parseValues(p.value)).reduce((s, [sy, di]) => [s[0]+sy, s[1]+di], [0,0]).map((n: number) => (n / trendData.value!.points.length).toFixed(0)).join('/')
  : avgNumeric.value.toFixed(1)
)
const maxValue = computed(() => {
  if (!trendData.value?.points?.length) return '--'
  if (isBP.value) {
    const allSys = trendData.value.points.map(p => parseValues(p.value)[0])
    const allDia = trendData.value.points.map(p => parseValues(p.value)[1])
    return `${Math.max(...allSys).toFixed(0)}/${Math.max(...allDia).toFixed(0)}`
  }
  return Math.max(...trendData.value.points.map((p) => Number(p.value) || 0)).toFixed(1)
})
const minValue = computed(() => {
  if (!trendData.value?.points?.length) return '--'
  if (isBP.value) {
    const allSys = trendData.value.points.map(p => parseValues(p.value)[0])
    const allDia = trendData.value.points.map(p => parseValues(p.value)[1])
    return `${Math.min(...allSys).toFixed(0)}/${Math.min(...allDia).toFixed(0)}`
  }
  return Math.min(...trendData.value.points.map((p) => Number(p.value) || 0)).toFixed(1)
})

const trendDescription = computed(() => {
  if (!trendData.value?.points || trendData.value.points.length < 2) return '数据点不足，无法分析趋势'
  const points = trendData.value.points
  const label = dataTypeLabel.value

  if (isBP.value) {
    const firstSy = parseValues(points[0].value)[0]
    const firstDi = parseValues(points[0].value)[1]
    const lastSy = parseValues(points[points.length - 1].value)[0]
    const lastDi = parseValues(points[points.length - 1].value)[1]
    const allSys = points.map(p => parseValues(p.value)[0])
    const allDia = points.map(p => parseValues(p.value)[1])
    const avgSy = (allSys.reduce((a, b) => a + b, 0) / allSys.length).toFixed(0)
    const avgDi = (allDia.reduce((a, b) => a + b, 0) / allDia.length).toFixed(0)

    const syTrend = lastSy - firstSy > 1 ? '上升' : lastSy - firstSy < -1 ? '下降' : '稳定'
    const diTrend = lastDi - firstDi > 1 ? '上升' : lastDi - firstDi < -1 ? '下降' : '稳定'
    const syVerdict = avgSy >= 140 ? '偏高' : avgSy >= 120 ? '正常高值' : avgSy >= 90 ? '理想' : '偏低'
    const diVerdict = avgDi >= 90 ? '偏高' : avgDi >= 80 ? '正常高值' : avgDi >= 60 ? '理想' : '偏低'

    const parts = [
      `近${points.length}次测量，收缩压均值 ${avgSy} mmHg（${syVerdict}）、舒张压均值 ${avgDi} mmHg（${diVerdict}）`,
      `收缩压呈${syTrend}趋势${syTrend !== '稳定' ? '，舒张压呈' + diTrend + '趋势' : '，舒张压同步' + diTrend}`,
    ]
    if (syVerdict.includes('偏高') || diVerdict.includes('偏高')) {
      parts.push('建议减少高盐饮食、规律监测血压，若持续偏高请咨询医生')
    } else if (syVerdict === '偏低' || diVerdict === '偏低') {
      parts.push('血压偏低，注意营养均衡，避免突然起身')
    } else {
      parts.push('血压处于健康范围，继续保持当前生活习惯')
    }
    return parts.join('。')
  }

  // Non-BP
  const first = parseValues(points[0].value)[0]
  const last = parseValues(points[points.length - 1].value)[0]
  const diff = last - first
  const pct = first !== 0 ? ((diff / first) * 100).toFixed(1) : '0'
  const allVals = points.map(p => parseValues(p.value)[0])
  const avg = (allVals.reduce((a, b) => a + b, 0) / allVals.length).toFixed(1)

  let desc = `近${points.length}次记录的${label}均值为 ${avg}，`
  if (Math.abs(diff) < 0.01) {
    desc += '整体趋势平稳，数据波动较小'
  } else if (diff > 0) {
    desc += `整体上升约 ${pct}%，建议留意变化趋势`
  } else {
    desc += `整体下降约 ${Math.abs(Number(pct))}%，继续保持`
  }

  // Add data-type-specific advice
  const advice: Record<string, string> = {
    '体重趋势': '建议维持均衡饮食与规律运动，每周至少150分钟中等强度有氧运动',
    '血糖趋势': '注意控制碳水化合物摄入，少量多餐，定期监测空腹和餐后血糖',
    '运动时长趋势': '建议每周运动不少于150分钟，结合有氧与力量训练',
    '睡眠时长趋势': '成人理想睡眠时长为7-8小时，保持固定作息有助于提升睡眠质量',
  }
  if (advice[label]) desc += '。' + advice[label]

  return desc + '。'
})

function diffText(diff: number) {
  if (Math.abs(diff) < 0.01) return '持平'
  return diff > 0 ? `+${diff.toFixed(1)}` : diff.toFixed(1)
}
function diffBPText(raw: string, avg: number) {
  const [sy, di] = parseValues(raw)
  const d1 = sy - avg; const d2 = di - (avg * 0.7) // diastolic ~70% of systolic avg ratio
  const s1 = diffText(d1); const s2 = diffText(d2)
  return `收缩 ${s1} / 舒张 ${s2}`
}
function diffColor(diff: number) {
  if (Math.abs(diff) < 0.01) return '#909399'
  return diff > 0 ? '#f56c6c' : '#67c23a'
}

// ============ Donut chart (24h time distribution) ============
const donutCanvas = ref<HTMLCanvasElement | null>(null)
const otherAvgHours = ref(0) // avg hours of the complementary type (sleep ↔ exercise)

const donutDateRange = computed(() => {
  const [s, e] = filters.dateRange || getDefaultDateRange()
  return `${s} 至 ${e}`
})

const showDonut = computed(() => filters.dataType === 'SLEEP_HOURS' || filters.dataType === 'EXERCISE_MINUTES')
const donutTitle = computed(() => `日均时间分布（${donutDateRange.value}）`)
const donutSummary = computed(() => {
  const pct = Math.round(((donutSleepH.value + donutExerH.value) / 24) * 100)
  return `统计周期内睡眠+运动日均共占全天 ${pct}%`
})
const otherType = computed(() => filters.dataType === 'SLEEP_HOURS' ? 'EXERCISE_MINUTES' : 'SLEEP_HOURS')

const donutSleepH = computed(() => filters.dataType === 'SLEEP_HOURS' ? donutCurrentAvg.value : otherAvgHours.value)
const donutExerH = computed(() => filters.dataType === 'EXERCISE_MINUTES' ? donutCurrentAvg.value : otherAvgHours.value)
const donutOtherH = computed(() => Math.max(0, 24 - donutSleepH.value - donutExerH.value))

const donutCurrentAvg = computed(() => {
  if (!trendData.value?.points?.length) return 0
  const vals = trendData.value.points.map(p => {
    const v = Number(p.value) || 0
    return filters.dataType === 'EXERCISE_MINUTES' ? v / 60 : v
  })
  return vals.reduce((a, b) => a + b, 0) / vals.length
})

function drawDonut() {
  const canvas = donutCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d'); if (!ctx) return

  const dpr = window.devicePixelRatio || 1
  const size = 180
  canvas.width = size * dpr; canvas.height = size * dpr
  canvas.style.width = size + 'px'; canvas.style.height = size + 'px'
  ctx.scale(dpr, dpr)

  const cx = size / 2, cy = size / 2, r = 66, sw = 18
  const sleep = donutSleepH.value, exer = donutExerH.value, other = donutOtherH.value

  // Segments: sleep (purple), exercise (teal), other (light gray)
  const segs = [
    { v: sleep, color: '#6366F1', label: '睡眠' },
    { v: exer, color: '#0D7377', label: '运动' },
  ]
  if (other > 0.05) segs.push({ v: other, color: '#E8E3DD', label: '其他' })

  const total = sleep + exer + other || 1
  let angle = -Math.PI / 2
  segs.forEach(seg => {
    const sweep = (seg.v / total) * Math.PI * 2
    ctx.beginPath(); ctx.arc(cx, cy, r, angle, angle + sweep)
    ctx.strokeStyle = seg.color; ctx.lineWidth = sw; ctx.lineCap = 'butt'; ctx.stroke()
    angle += sweep
  })

  // Center text
  const pct = Math.round(((sleep + exer) / 24) * 100)
  ctx.fillStyle = '#303133'; ctx.font = 'bold 22px -apple-system, sans-serif'
  ctx.textAlign = 'center'; ctx.textBaseline = 'middle'
  ctx.fillText(pct + '%', cx, cy - 2)
}

async function fetchOtherAvg() {
  const [startTime, endTime] = filters.dateRange || getDefaultDateRange()
  try {
    const res = await getHealthTrends({ dataType: otherType.value, startTime: startTime + ' 00:00:00', endTime: endTime + ' 23:59:59' })
    const pts = res.data.data?.points
    if (pts?.length) {
      const vals = pts.map((p: { value: string }) => {
        const v = Number(p.value) || 0
        return otherType.value === 'EXERCISE_MINUTES' ? v / 60 : v
      })
      otherAvgHours.value = vals.reduce((a: number, b: number) => a + b, 0) / vals.length
    }
  } catch { otherAvgHours.value = 0 }
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

  // For BP: two series (systolic + diastolic); for others: single series
  const seriesList = isBP.value
    ? [
        { key: 'systolic', color: '#E85D4A', label: '收缩压' },
        { key: 'diastolic', color: '#409EFF', label: '舒张压' },
      ]
    : [{ key: 'single', color: '#409EFF', label: '' }]

  // Gather all values for range
  const allValues: number[] = []
  const seriesData = seriesList.map((s, si) => {
    const vals = points.map(p => {
      const parsed = parseValues(p.value)
      return si === 0 ? parsed[0] : (parsed[1] ?? parsed[0])
    })
    allValues.push(...vals)
    return vals
  })

  const minVal = Math.min(...allValues)
  const maxVal = Math.max(...allValues)
  const valRange = maxVal - minVal || 1

  // 背景
  ctx.fillStyle = '#fff'
  ctx.fillRect(0, 0, W, H)

  // 网格线 + Y轴
  ctx.strokeStyle = '#f0f0f0'
  ctx.lineWidth = 1
  for (let i = 0; i <= 4; i++) {
    const y = padding.top + (chartH / 4) * i
    ctx.beginPath()
    ctx.moveTo(padding.left, y)
    ctx.lineTo(W - padding.right, y)
    ctx.stroke()
    const val = maxVal - (valRange / 4) * i
    ctx.fillStyle = '#909399'
    ctx.font = '12px sans-serif'
    ctx.textAlign = 'right'
    ctx.fillText(val.toFixed(isBP.value ? 0 : 1), padding.left - 8, y + 4)
  }

  // X轴
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

  // Draw each series
  seriesData.forEach((values, si) => {
    const { color } = seriesList[si]

    // Fill (single series only)
    if (seriesList.length === 1) {
      const gradient = ctx.createLinearGradient(0, padding.top, 0, padding.top + chartH)
      gradient.addColorStop(0, 'rgba(64, 158, 255, 0.25)')
      gradient.addColorStop(1, 'rgba(64, 158, 255, 0.02)')
      ctx.beginPath()
      values.forEach((v, i) => {
        const x = padding.left + xStep * i
        const y = padding.top + chartH - ((v - minVal) / valRange) * chartH
        if (i === 0) ctx.moveTo(x, y)
        else ctx.lineTo(x, y)
      })
      const lastX = padding.left + xStep * (values.length - 1)
      ctx.lineTo(lastX, padding.top + chartH)
      ctx.lineTo(padding.left, padding.top + chartH)
      ctx.closePath()
      ctx.fillStyle = gradient
      ctx.fill()
    }

    // Line
    ctx.beginPath()
    values.forEach((v, i) => {
      const x = padding.left + xStep * i
      const y = padding.top + chartH - ((v - minVal) / valRange) * chartH
      if (i === 0) ctx.moveTo(x, y)
      else ctx.lineTo(x, y)
    })
    ctx.strokeStyle = color
    ctx.lineWidth = 2.5
    ctx.lineJoin = 'round'
    ctx.stroke()

    // Data points
    values.forEach((v, i) => {
      const x = padding.left + xStep * i
      const y = padding.top + chartH - ((v - minVal) / valRange) * chartH
      ctx.beginPath()
      ctx.arc(x, y, seriesList.length === 1 ? 4 : 3.5, 0, Math.PI * 2)
      ctx.fillStyle = '#fff'
      ctx.fill()
      ctx.strokeStyle = color
      ctx.lineWidth = 2.5
      ctx.stroke()
    })
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
    if (showDonut.value) await fetchOtherAvg()
    await nextTick()
    drawChart()
    if (showDonut.value) drawDonut()
  } catch {
    trendData.value = null
  } finally {
    loading.value = false
  }
}

// ============ 导出 ============
// ============ CSV 导出 ============
function handleExportCSV() {
  if (!trendData.value?.points?.length) return

  const headers = ['日期', `数值 (${dataTypeUnit.value})`, '与均值差值']
  const rows = trendData.value.points.map((p) => {
    const diff = (Number(p.value) - avgNumeric.value).toFixed(1)
    return [p.date, p.value + ' ' + dataTypeUnit.value, diff]
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
  ElMessage.success('CSV 导出成功')
}

// ============ 图表 PNG 下载 ============
function handleDownloadChart() {
  const canvas = chartCanvas.value
  if (!canvas) return
  const link = document.createElement('a')
  link.download = `${dataTypeLabel.value}_${new Date().toISOString().slice(0, 10)}.png`
  link.href = canvas.toDataURL('image/png')
  link.click()
  ElMessage.success('图表已下载')
}

// ============ PDF 报告导出 ============
function handleExportPDF() {
  if (!trendData.value) return

  const canvas = chartCanvas.value
  const chartImg = canvas ? canvas.toDataURL('image/png') : ''

  const rows = trendData.value.points.map(p => {
    const diff = (parseValues(p.value)[0] - avgNumeric.value).toFixed(1)
    return `<tr><td>${p.date}</td><td>${p.value} ${dataTypeUnit.value}</td><td>${diffText(Number(diff))}</td></tr>`
  }).join('')

  const reportHTML = `<!DOCTYPE html><html><head><meta charset="UTF-8"><title>健康报告</title>
<style>
  body { font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif; padding: 32px 40px; color: #1D2420; max-width: 800px; margin: 0 auto; }
  h1 { font-size: 22px; margin-bottom: 4px; }
  .meta { color: #909399; font-size: 13px; margin-bottom: 20px; }
  .section { margin: 20px 0; }
  .section h3 { font-size: 16px; border-bottom: 1px solid #e8e3dd; padding-bottom: 6px; margin-bottom: 10px; }
  .chart-img { width: 100%; max-height: 300px; object-fit: contain; border: 1px solid #e8e3dd; border-radius: 8px; }
  table { width: 100%; border-collapse: collapse; font-size: 13px; margin: 10px 0; }
  th { background: #f5f0eb; padding: 8px 10px; text-align: left; border-bottom: 2px solid #e8e3dd; }
  td { padding: 8px 10px; border-bottom: 1px solid #f0ece7; }
  .stats { display: flex; gap: 24px; margin: 12px 0; }
  .stat { text-align: center; } .stat-val { font-size: 22px; font-weight: 700; } .stat-lbl { font-size: 12px; color: #909399; }
  .analysis { background: #faf8f5; padding: 14px 18px; border-radius: 8px; line-height: 1.7; font-size: 14px; }
  .donut-img { width: 180px; height: 180px; }
  .footer { margin-top: 24px; padding-top: 12px; border-top: 1px solid #e8e3dd; font-size: 11px; color: #c0c4cc; text-align: center; }
</style></head><body>
  <h1>${dataTypeLabel.value} 健康报告</h1>
  <p class="meta">时间范围：${(filters.dateRange || getDefaultDateRange()).join(' 至 ')} ｜ 生成时间：${new Date().toLocaleString('zh-CN')}</p>

  <div class="section">
    <h3>趋势图表</h3>
    ${chartImg ? `<img src="${chartImg}" class="chart-img" alt="趋势图">` : '<p>图表数据不可用</p>'}
    <div class="stats">
      <div class="stat"><span class="stat-val">${avgValue.value}</span><span class="stat-lbl"> 平均值</span></div>
      <div class="stat"><span class="stat-val">${maxValue.value}</span><span class="stat-lbl"> 最高值</span></div>
      <div class="stat"><span class="stat-val">${minValue.value}</span><span class="stat-lbl"> 最低值</span></div>
      <div class="stat"><span class="stat-val">${trendData.value.points.length}</span><span class="stat-lbl"> 数据点</span></div>
    </div>
  </div>

  <div class="section">
    <h3>趋势分析</h3>
    <div class="analysis">${trendDescription.value}</div>
  </div>

  ${showDonut.value ? `<div class="section"><h3>时间分布</h3><p>${donutSummary.value}</p></div>` : ''}

  <div class="section">
    <h3>数据明细</h3>
    <table><thead><tr><th>日期</th><th>数值 (${dataTypeUnit.value})</th><th>与均值差值 (${dataTypeUnit.value})</th></tr></thead><tbody>${rows}</tbody></table>
  </div>

  <div class="footer">AI 个性化私人健康管理系统 · 自动生成报告</div>
</body></html>`

  const w = window.open('', '_blank', 'width=900,height=700')
  if (!w) { ElMessage.warning('请允许弹出窗口以导出报告'); return }
  w.document.write(reportHTML)
  w.document.close()
  setTimeout(() => w.print(), 600)
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
    if (showDonut.value) drawDonut()
  })
  if (chartCanvas.value) resizeObserver.observe(chartCanvas.value)
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
.chart-unit { color: #909399; font-size: 13px; }
.chart-legend { display: flex; align-items: center; font-size: 12px; color: #606266; }
.legend-dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 4px; }
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

.table-card { margin-bottom: 20px; }
.donut-card { margin-bottom: 20px; }
.donut-container { width: 160px; height: 160px; flex-shrink: 0; }
.donut-canvas { width: 160px; height: 160px; }
.donut-stats { display: flex; flex-direction: column; gap: 10px; }
.donut-stat { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #606266; }
.donut-dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.donut-text strong { color: #303133; margin-left: 2px; }

/* ===== PDF 导出打印样式 ===== */
@media print {
  .el-aside, .el-header, .el-menu, .sidebar, .filter-card, .pagination-wrapper,
  .page-motto, .subtitle, button, .el-button, .el-pagination { display: none !important; }
  .el-main { padding: 0 !important; margin: 0 !important; }
  .health-trend-page { max-width: 100% !important; }
  .el-card { box-shadow: none !important; border: 1px solid #ddd !important; break-inside: avoid; }
  .chart-container { max-height: 280px; }
  canvas { max-width: 100%; height: auto !important; }
  h2 { font-size: 20px !important; }
}
</style>
