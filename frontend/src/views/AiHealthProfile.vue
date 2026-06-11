<template>
  <div class="ai-profile-page">
    <div class="page-header">
      <h2>🧬 AI 健康画像</h2>
      <p class="subtitle">基于您的健康数据，AI 为您生成个性化健康画像</p>
    </div>

    <!-- 操作区 -->
    <el-card class="action-card" shadow="never">
      <div class="action-row">
        <div class="time-range-select">
          <label>选择时间范围：</label>
          <el-select v-model="timeRange" placeholder="请选择时间范围" style="width: 200px">
            <el-option label="最近7天" value="LAST_7_DAYS" />
            <el-option label="最近30天" value="LAST_30_DAYS" />
            <el-option label="最近90天" value="LAST_90_DAYS" />
          </el-select>
        </div>
        <el-button type="primary" :loading="generating" @click="handleGenerate" :icon="MagicStick">
          生成健康画像
        </el-button>
      </div>
    </el-card>

    <!-- 加载状态 -->
    <LoadingSpinner v-if="generating" text="AI 正在分析您的健康数据..." />

    <!-- 画像结果 -->
    <div v-if="profileData && !generating" class="profile-result">
      <!-- 风险等级卡片 -->
      <el-card class="risk-card" shadow="hover">
        <div class="risk-header">
          <span class="risk-label">健康风险等级</span>
          <el-tag
            :type="riskTagType"
            size="large"
            effect="dark"
            class="risk-tag"
          >
            {{ riskLabel }}
          </el-tag>
        </div>
        <div class="risk-bar-wrapper">
          <div class="risk-bar">
            <div
              class="risk-indicator"
              :style="{ left: riskPosition + '%' }"
            >
              <span class="indicator-dot"></span>
            </div>
            <div class="risk-segments">
              <div class="segment low" :class="{ active: profileData.riskLevel === 'LOW' }">低风险</div>
              <div class="segment medium" :class="{ active: profileData.riskLevel === 'MEDIUM' }">中风险</div>
              <div class="segment high" :class="{ active: profileData.riskLevel === 'HIGH' }">高风险</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 健康标签 -->
      <el-card class="tags-card" shadow="hover">
        <h3>🏷️ 健康标签</h3>
        <div class="tags-wrapper">
          <el-tag
            v-for="(tag, idx) in profileData.healthTags"
            :key="idx"
            :type="tagType(tag)"
            size="large"
            class="health-tag"
          >
            {{ tag }}
          </el-tag>
          <EmptyState v-if="!profileData.healthTags?.length" description="暂无健康标签" />
        </div>
      </el-card>

      <!-- 分析报告 -->
      <el-card class="analysis-card" shadow="hover">
        <h3>📋 综合分析</h3>
        <div class="analysis-content">
          <p>{{ profileData.analysis }}</p>
        </div>
        <div class="disclaimer">
          <el-icon><WarningFilled /></el-icon>
          <span>以上分析仅供参考，不构成医疗诊断建议。如有健康问题，请及时就医。</span>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <EmptyState v-if="!profileData && !generating" description="点击「生成健康画像」按钮，AI 将分析您的健康数据" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, WarningFilled } from '@element-plus/icons-vue'
import { generateProfile } from '../api/ai'
import LoadingSpinner from '../components/common/LoadingSpinner.vue'
import EmptyState from '../components/common/EmptyState.vue'
import type { AiProfileData } from '../types'

const timeRange = ref('LAST_7_DAYS')
const generating = ref(false)
const profileData = ref<AiProfileData | null>(null)

const riskTagType = computed(() => {
  const map: Record<string, string> = { LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }
  return map[profileData.value?.riskLevel || ''] || 'info'
})

const riskLabel = computed(() => {
  const map: Record<string, string> = { LOW: '低风险', MEDIUM: '中等风险', HIGH: '高风险' }
  return map[profileData.value?.riskLevel || ''] || '未知'
})

const riskPosition = computed(() => {
  const map: Record<string, number> = { LOW: 16, MEDIUM: 50, HIGH: 83 }
  return map[profileData.value?.riskLevel || ''] || 0
})

function tagType(tag: string): 'success' | 'warning' | 'danger' | 'info' {
  const positive = ['正常', '达标', '良好', '健康', '均衡']
  const negative = ['不足', '偏高', '偏低', '过高', '过低', '超标', '不良']
  if (positive.some((k) => tag.includes(k))) return 'success'
  if (negative.some((k) => tag.includes(k))) return 'danger'
  return 'info'
}

async function handleGenerate() {
  generating.value = true
  profileData.value = null
  try {
    const res = await generateProfile({ timeRange: timeRange.value })
    profileData.value = res.data.data
    ElMessage.success('健康画像生成成功！')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    generating.value = false
  }
}
</script>

<style scoped>
.ai-profile-page {
  max-width: 900px;
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

.action-card {
  margin-bottom: 24px;
}
.action-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.time-range-select {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.profile-result {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 风险卡片 */
.risk-card {
  text-align: center;
}
.risk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.risk-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.risk-tag {
  font-size: 16px;
  padding: 8px 24px;
}
.risk-bar-wrapper {
  padding: 0 20px;
}
.risk-bar {
  position: relative;
  padding-top: 20px;
}
.risk-indicator {
  position: absolute;
  top: 0;
  transform: translateX(-50%);
  z-index: 2;
  transition: left 0.6s ease;
}
.indicator-dot {
  display: block;
  width: 14px;
  height: 14px;
  background: #409eff;
  border: 3px solid #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.5);
}
.risk-segments {
  display: flex;
  height: 44px;
  border-radius: 8px;
  overflow: hidden;
}
.segment {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 500;
  color: #fff;
  transition: all 0.3s;
}
.segment.low {
  background: #c0e0c0;
  color: #2d6a2d;
}
.segment.medium {
  background: #ffe0a0;
  color: #8a6d14;
}
.segment.high {
  background: #f5c0c0;
  color: #8a2d2d;
}
.segment.active {
  transform: scaleY(1.15);
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

/* 健康标签 */
.tags-card h3 {
  margin: 0 0 16px;
  color: #303133;
}
.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.health-tag {
  font-size: 14px;
  padding: 8px 16px;
}

/* 分析卡片 */
.analysis-card h3 {
  margin: 0 0 16px;
  color: #303133;
}
.analysis-content {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  line-height: 1.8;
  color: #606266;
  font-size: 15px;
}
.disclaimer {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 16px;
  padding: 10px 14px;
  background: #fdf6ec;
  border-radius: 6px;
  color: #b88230;
  font-size: 13px;
}
</style>
