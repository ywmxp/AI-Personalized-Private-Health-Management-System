<template>
  <div class="statistics-page">
    <div class="page-header">
      <h2>📊 健康数据统计</h2>
      <p class="subtitle">平台数据概览与统计分析</p>
    </div>

    <!-- 时间筛选 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <div class="filter-item">
          <label>统计区间：</label>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </div>
        <el-button type="primary" @click="fetchStatistics" :loading="loading" :icon="Search">
          查询
        </el-button>
      </div>
    </el-card>

    <!-- 概览卡片 -->
    <div class="overview-cards" v-loading="loading">
      <el-card class="overview-card total-users" shadow="hover">
        <div class="ov-icon">👥</div>
        <div class="ov-info">
          <span class="ov-value">{{ stats?.totalUsers ?? '--' }}</span>
          <span class="ov-label">用户总数</span>
        </div>
      </el-card>
      <el-card class="overview-card total-data" shadow="hover">
        <div class="ov-icon">📋</div>
        <div class="ov-info">
          <span class="ov-value">{{ stats?.totalHealthData ?? '--' }}</span>
          <span class="ov-label">健康数据总数</span>
        </div>
      </el-card>
      <el-card class="overview-card risk-low" shadow="hover">
        <div class="ov-icon">✅</div>
        <div class="ov-info">
          <span class="ov-value">{{ stats?.riskDistribution?.low ?? '--' }}</span>
          <span class="ov-label">低风险人数</span>
        </div>
      </el-card>
      <el-card class="overview-card risk-medium" shadow="hover">
        <div class="ov-icon">⚠️</div>
        <div class="ov-info">
          <span class="ov-value">{{ stats?.riskDistribution?.medium ?? '--' }}</span>
          <span class="ov-label">中风险人数</span>
        </div>
      </el-card>
      <el-card class="overview-card risk-high" shadow="hover">
        <div class="ov-icon">🔴</div>
        <div class="ov-info">
          <span class="ov-value">{{ stats?.riskDistribution?.high ?? '--' }}</span>
          <span class="ov-label">高风险人数</span>
        </div>
      </el-card>
    </div>

    <!-- 风险分布图表 -->
    <el-card v-if="stats" class="chart-card" shadow="never">
      <h3>🎯 风险等级分布</h3>
      <div class="risk-chart-container">
        <div class="risk-bars">
          <div class="risk-bar-item">
            <div class="bar-label">低风险</div>
            <div class="bar-track">
              <div
                class="bar-fill low"
                :style="{ width: barPercent(stats.riskDistribution.low) + '%' }"
              ></div>
            </div>
            <span class="bar-count">{{ stats.riskDistribution.low }}</span>
          </div>
          <div class="risk-bar-item">
            <div class="bar-label">中风险</div>
            <div class="bar-track">
              <div
                class="bar-fill medium"
                :style="{ width: barPercent(stats.riskDistribution.medium) + '%' }"
              ></div>
            </div>
            <span class="bar-count">{{ stats.riskDistribution.medium }}</span>
          </div>
          <div class="risk-bar-item">
            <div class="bar-label">高风险</div>
            <div class="bar-track">
              <div
                class="bar-fill high"
                :style="{ width: barPercent(stats.riskDistribution.high) + '%' }"
              ></div>
            </div>
            <span class="bar-count">{{ stats.riskDistribution.high }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 每日数据录入量 -->
    <el-card v-if="stats && stats.dailyDataCount?.length" class="chart-card" shadow="never">
      <h3>📅 每日数据录入量</h3>
      <div class="daily-chart-container">
        <canvas ref="dailyCanvas" class="daily-canvas"></canvas>
      </div>
    </el-card>

    <!-- 空状态 -->
    <EmptyState v-if="!stats && !loading" description="选择时间范围，查询平台统计数据" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getPlatformStatistics } from '../../api/admin'
import EmptyState from '../../components/common/EmptyState.vue'
import type { PlatformStatistics } from '../../types'

const loading = ref(false)
const stats = ref<PlatformStatistics | null>(null)
const dateRange = ref<[string, string] | null>(null)
const dailyCanvas = ref<HTMLCanvasElement | null>(null)

function getDefaultRange(): [string, string] {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  const fmt = (d: Date) => d.toISOString().split('T')[0]
  return [fmt(start), fmt(end)]
}

const maxRiskCount = computed(() => {
  if (!stats.value) return 1
  const { low, medium, high } = stats.value.riskDistribution
  return Math.max(low, medium, high, 1)
})

function barPercent(val: number) {
  return Math.round((val / maxRiskCount.value) * 100)
}

// ============ 获取数据 ============
async function fetchStatistics() {
  loading.value = true
  try {
    const [startDate, endDate] = dateRange.value || getDefaultRange()
    const res = await getPlatformStatistics({ startDate, endDate })
    stats.value = res.data.data
    await nextTick()
    drawDailyChart()
  } catch {
    stats.value = null
  } finally {
    loading.value = false
  }
}

// ============ 每日数据图表 ============
function drawDailyChart() {
  const canvas = dailyCanvas.value
  if (!canvas || !stats.value?.dailyDataCount?.length) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const dpr = window.devicePixelRatio || 1
  const rect = canvas.getBoundingClientRect()
  canvas.width = rect.width * dpr
  canvas.height = rect.height * dpr
  ctx.scale(dpr, dpr)

  const W = rect.width
  const H = rect.height
  const pad = { top: 16, right: 16, bottom: 36, left: 40 }
  const chartW = W - pad.left - pad.right
  const chartH = H - pad.top - pad.bottom

  const points = stats.value.dailyDataCount
  const maxVal = Math.max(...points.map((p) => p.count), 1)

  ctx.fillStyle = '#fff'
  ctx.fillRect(0, 0, W, H)

  // 网格
  ctx.strokeStyle = '#f0f0f0'
  ctx.lineWidth = 1
  for (let i = 0; i <= 4; i++) {
    const y = pad.top + (chartH / 4) * i
    ctx.beginPath()
    ctx.moveTo(pad.left, y)
    ctx.lineTo(W - pad.right, y)
    ctx.stroke()
    ctx.fillStyle = '#909399'
    ctx.font = '11px sans-serif'
    ctx.textAlign = 'right'
    ctx.fillText(Math.round(maxVal - (maxVal / 4) * i) + '', pad.left - 8, y + 4)
  }

  const barW = Math.max(4, chartW / points.length - 4)
  points.forEach((p, i) => {
    const x = pad.left + (chartW / points.length) * i + 2
    const h = (p.count / maxVal) * chartH
    const y = pad.top + chartH - h

    ctx.fillStyle = '#409eff'
    ctx.fillRect(x, y, barW, h)

    // 标签
    if (i % Math.max(1, Math.floor(points.length / 8)) === 0) {
      ctx.fillStyle = '#909399'
      ctx.font = '10px sans-serif'
      ctx.textAlign = 'center'
      ctx.fillText(p.date.slice(5), x + barW / 2, H - pad.bottom + 16)
    }
  })
}

onMounted(() => {
  if (!dateRange.value) {
    dateRange.value = getDefaultRange()
  }
  fetchStatistics()
})
</script>

<style scoped>
.statistics-page {
  max-width: 1100px;
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

.filter-card { margin-bottom: 20px; }
.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-item label {
  white-space: nowrap;
  color: #606266;
}

/* 概览卡片 */
.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
.overview-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 0;
}
.overview-card .ov-icon {
  font-size: 36px;
}
.ov-info {
  display: flex;
  flex-direction: column;
}
.ov-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}
.ov-label {
  font-size: 13px;
  color: #909399;
}
.total-users { border-left: 4px solid #409eff; }
.total-data { border-left: 4px solid #67c23a; }
.risk-low { border-left: 4px solid #67c23a; }
.risk-medium { border-left: 4px solid #e6a23c; }
.risk-high { border-left: 4px solid #f56c6c; }

/* 风险分布 */
.chart-card {
  margin-bottom: 20px;
}
.chart-card h3 {
  margin: 0 0 20px;
  color: #303133;
}
.risk-chart-container {
  max-width: 600px;
  margin: 0 auto;
}
.risk-bars {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.risk-bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.bar-label {
  width: 70px;
  font-size: 14px;
  color: #606266;
  text-align: right;
}
.bar-track {
  flex: 1;
  height: 28px;
  background: #f5f7fa;
  border-radius: 6px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.6s ease;
}
.bar-fill.low { background: linear-gradient(90deg, #67c23a, #85ce61); }
.bar-fill.medium { background: linear-gradient(90deg, #e6a23c, #ebb563); }
.bar-fill.high { background: linear-gradient(90deg, #f56c6c, #f78989); }
.bar-count {
  width: 40px;
  font-weight: 700;
  color: #303133;
}

/* 每日图表 */
.daily-chart-container {
  width: 100%;
  height: 240px;
}
.daily-canvas {
  width: 100%;
  height: 100%;
}
</style>
