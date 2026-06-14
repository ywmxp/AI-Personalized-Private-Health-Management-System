<template>
  <div class="ai-plan-page">
    <div class="page-header">
      <h2>📋 AI 个性化健康计划</h2>
      <p class="subtitle">根据您的健康需求，AI 为您定制专属健康计划</p>
    </div>

    <!-- 输入区 -->
    <el-card class="input-card" shadow="never">
      <div class="input-row">
        <div class="need-input">
          <label>您的健康需求：</label>
          <el-input
            v-model="healthNeed"
            placeholder="例如：改善睡眠、减重、控制血压..."
            clearable
            style="width: 360px"
            @keyup.enter="handleGenerate"
          />
        </div>
        <div class="quick-tags">
          <span class="quick-label">快捷选择：</span>
          <el-tag
            v-for="tag in quickNeeds"
            :key="tag"
            :type="healthNeed === tag ? 'primary' : 'info'"
            class="quick-tag"
            @click="healthNeed = tag"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
      <el-button type="primary" :loading="generating" @click="handleGenerate" :icon="MagicStick" style="margin-top: 16px">
        生成健康计划
      </el-button>
    </el-card>

    <!-- 加载 -->
    <LoadingSpinner v-if="generating" text="AI 正在为您定制健康计划..." />

    <!-- 计划结果 -->
    <div v-if="planData && !generating" class="plan-result">
      <el-card class="plan-name-card" shadow="hover">
        <div class="plan-name-header">
          <h3>{{ planData.planName }}</h3>
          <el-tag type="success" size="large">AI 生成</el-tag>
        </div>
      </el-card>

      <div class="plan-cards">
        <!-- 饮食建议 -->
        <el-card class="suggest-card diet-card" shadow="hover">
          <div class="suggest-header">
            <span class="suggest-icon">🥗</span>
            <h4>饮食建议</h4>
          </div>
          <div class="suggest-content">
            <p>{{ planData.dietSuggest }}</p>
          </div>
        </el-card>

        <!-- 运动建议 -->
        <el-card class="suggest-card sport-card" shadow="hover">
          <div class="suggest-header">
            <span class="suggest-icon">🏃</span>
            <h4>运动建议</h4>
          </div>
          <div class="suggest-content">
            <p>{{ planData.sportSuggest }}</p>
          </div>
        </el-card>

        <!-- 睡眠建议 -->
        <el-card class="suggest-card sleep-card" shadow="hover">
          <div class="suggest-header">
            <span class="suggest-icon">😴</span>
            <h4>睡眠建议</h4>
          </div>
          <div class="suggest-content">
            <p>{{ planData.sleepSuggest }}</p>
          </div>
        </el-card>
      </div>

      <!-- 免责声明 -->
      <div class="disclaimer">
        <el-icon><WarningFilled /></el-icon>
        <span>以上计划仅供参考，请根据自身实际情况调整。如有健康问题，请咨询专业医生。</span>
      </div>
    </div>

    <EmptyState v-if="!planData && !generating" description="输入您的健康需求，点击「生成健康计划」按钮" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, WarningFilled } from '@element-plus/icons-vue'
import { generatePlan } from '../api/ai'
import LoadingSpinner from '../components/common/LoadingSpinner.vue'
import EmptyState from '../components/common/EmptyState.vue'
import type { AiPlanData } from '../types'

const quickNeeds = ['改善睡眠', '减重管理', '控制血压', '控制血糖', '增强体质', '缓解压力']
const healthNeed = ref('')
const generating = ref(false)
const planData = ref<AiPlanData | null>(null)

async function handleGenerate() {
  if (!healthNeed.value.trim()) {
    ElMessage.warning('请输入或选择健康需求')
    return
  }
  generating.value = true
  planData.value = null
  try {
    const res = await generatePlan({ healthNeed: healthNeed.value.trim() })
    planData.value = res.data.data
    ElMessage.success('健康计划生成成功！')
  } catch {
    // handled in interceptor
  } finally {
    generating.value = false
  }
}
</script>

<style scoped>
.ai-plan-page {
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
  margin-bottom: 24px;
}
.input-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}
.need-input {
  display: flex;
  align-items: center;
  gap: 8px;
}
.need-input label {
  white-space: nowrap;
  color: #606266;
  font-weight: 500;
}
.quick-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.quick-label {
  color: #909399;
  font-size: 13px;
}
.quick-tag {
  cursor: pointer;
  transition: all 0.2s;
}
.quick-tag:hover {
  transform: translateY(-1px);
}

/* 计划名称 */
.plan-name-card {
  margin-bottom: 20px;
}
.plan-name-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.plan-name-header h3 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

/* 建议卡片 */
.plan-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}
.suggest-card {
  border-top: 4px solid;
  transition: transform 0.2s;
}
.suggest-card:hover {
  transform: translateY(-2px);
}
.diet-card {
  border-top-color: #67c23a;
}
.sport-card {
  border-top-color: #409eff;
}
.sleep-card {
  border-top-color: #9b6bff;
}
.suggest-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.suggest-icon {
  font-size: 24px;
}
.suggest-header h4 {
  margin: 0;
  font-size: 17px;
  color: #303133;
}
.suggest-content {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  line-height: 1.8;
  color: #606266;
  font-size: 14px;
  min-height: 100px;
}

.disclaimer {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: #fdf6ec;
  border-radius: 6px;
  color: #b88230;
  font-size: 13px;
}
</style>
