<template>
  <div class="knowledge-page">
    <div class="page-header">
      <h2>📚 健康知识</h2>
      <p class="subtitle">AI 根据您的健康状况推送个性化健康知识</p>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 知识库 Tab -->
      <el-tab-pane label="知识库" name="library">
        <el-card class="filter-card" shadow="never">
          <el-form :inline="true">
            <el-form-item label="关键词">
              <el-input
                v-model="filterForm.keyword"
                placeholder="搜索标题或内容"
                clearable
                @keyup.enter="fetchKnowledgeList"
                @clear="fetchKnowledgeList"
              />
            </el-form-item>
            <el-form-item label="标签">
              <el-input
                v-model="filterForm.tag"
                placeholder="按标签筛选"
                clearable
                @keyup.enter="fetchKnowledgeList"
                @clear="fetchKnowledgeList"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchKnowledgeList">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <div v-loading="knowledgeLoading" class="knowledge-list">
          <el-empty v-if="!knowledgeLoading && knowledgeList.length === 0" description="暂无健康知识" />
          <el-card
            v-for="item in knowledgeList"
            :key="item.knowledgeId"
            class="knowledge-item"
            shadow="hover"
          >
            <div class="knowledge-header">
              <h4>{{ item.title }}</h4>
              <el-tag size="small">{{ item.relateTag }}</el-tag>
            </div>
            <p class="knowledge-content">{{ item.content }}</p>
            <div class="knowledge-footer">
              <span class="source">来源：{{ item.source }}</span>
              <span class="time">{{ item.createTime }}</span>
            </div>
          </el-card>

          <!-- 分页 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="knowledgePagination.page"
              v-model:page-size="knowledgePagination.size"
              :total="knowledgePagination.total"
              :page-sizes="[10, 20]"
              layout="total, prev, pager, next"
              @change="fetchKnowledgeList"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 我的推送 Tab -->
      <el-tab-pane label="我的推送" name="pushes">
        <!-- 生成推送按钮 -->
        <div class="push-action">
          <el-button type="primary" :loading="pushing" @click="handleGeneratePush" :icon="MagicStick">
            AI 生成知识推送
          </el-button>
          <span class="push-hint" v-if="!pushList.length && !pushLoading">点击按钮，AI 将根据您的健康画像推荐知识</span>
        </div>

        <div v-loading="pushLoading" class="push-list">
          <el-empty v-if="!pushLoading && pushList.length === 0" description="暂无推送内容" />
          <div
            v-for="item in pushList"
            :key="item.pushId"
            class="push-item"
            :class="{ unread: item.isRead === 0 }"
            @click="handleReadPush(item)"
          >
            <div class="push-dot" v-if="item.isRead === 0"></div>
            <div class="push-info">
              <h5>{{ item.title }}</h5>
              <div class="push-meta">
                <el-tag size="small">{{ item.relateTag }}</el-tag>
                <span class="push-time">{{ item.pushTime }}</span>
              </div>
            </div>
            <el-tag v-if="item.isRead === 0" type="danger" size="small">未读</el-tag>
            <el-tag v-else type="info" size="small">已读</el-tag>
          </div>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="pushPagination.page"
              v-model:page-size="pushPagination.size"
              :total="pushPagination.total"
              :page-sizes="[10, 20]"
              layout="total, prev, pager, next"
              @change="fetchPushList"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 知识详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="selectedKnowledge?.title"
      width="600px"
      destroy-on-close
    >
      <div v-if="selectedKnowledge" class="detail-body">
        <div class="detail-meta">
          <el-tag size="small">{{ selectedKnowledge.relateTag }}</el-tag>
          <span>来源：{{ selectedKnowledge.source }}</span>
        </div>
        <div class="detail-content">{{ selectedKnowledge.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, MagicStick } from '@element-plus/icons-vue'
import { getKnowledgeList, getMyPushes, markPushRead } from '../api/knowledge'
import { pushKnowledge } from '../api/ai'
import type { KnowledgeItem, KnowledgePush } from '../types'

// ============ Tab 状态 ============
const activeTab = ref('library')

// ============ 知识库 ============
const filterForm = reactive({ keyword: '', tag: '' })
const knowledgeLoading = ref(false)
const knowledgeList = ref<KnowledgeItem[]>([])
const knowledgePagination = reactive({ page: 1, size: 10, total: 0 })

async function fetchKnowledgeList() {
  knowledgeLoading.value = true
  try {
    const res = await getKnowledgeList({
      keyword: filterForm.keyword || undefined,
      tag: filterForm.tag || undefined,
      page: knowledgePagination.page,
      size: knowledgePagination.size,
    })
    const data = res.data.data
    knowledgeList.value = data.items || []
    knowledgePagination.total = data.total || 0
  } catch {
    // handled
  } finally {
    knowledgeLoading.value = false
  }
}

// ============ 推送 ============
const pushing = ref(false)
const pushLoading = ref(false)
const pushList = ref<KnowledgePush[]>([])
const pushPagination = reactive({ page: 1, size: 10, total: 0 })

async function handleGeneratePush() {
  pushing.value = true
  try {
    const res = await pushKnowledge({ profileId: 0 }) // 0 表示后端自动匹配最新画像
    const pushIds = res.data.data?.pushIds || []
    ElMessage.success(`成功生成 ${pushIds.length} 条推送！`)
    await fetchPushList()
  } catch {
    // handled
  } finally {
    pushing.value = false
  }
}

async function fetchPushList() {
  pushLoading.value = true
  try {
    const res = await getMyPushes({ page: pushPagination.page, size: pushPagination.size })
    const data = res.data.data
    pushList.value = data.items || []
    pushPagination.total = data.total || 0
  } catch {
    // handled
  } finally {
    pushLoading.value = false
  }
}

async function handleReadPush(item: KnowledgePush) {
  if (item.isRead === 1) return
  try {
    await markPushRead(item.pushId)
    item.isRead = 1
    ElMessage.success('已标记为已读')
  } catch {
    // handled
  }
}

// ============ 知识详情弹窗 ============
const detailVisible = ref(false)
const selectedKnowledge = ref<KnowledgeItem | null>(null)

function handleTabChange(tab: string) {
  if (tab === 'pushes') fetchPushList()
}

onMounted(() => {
  fetchKnowledgeList()
})
</script>

<style scoped>
.knowledge-page {
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

/* 知识列表 */
.knowledge-list {
  min-height: 300px;
}
.knowledge-item {
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.knowledge-item:hover {
  border-color: #409eff;
}
.knowledge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.knowledge-header h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}
.knowledge-content {
  color: #606266;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0 0 10px;
}
.knowledge-footer {
  display: flex;
  justify-content: space-between;
  color: #c0c4cc;
  font-size: 12px;
}

/* 推送区 */
.push-action {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f0f9ff;
  border-radius: 8px;
}
.push-hint {
  color: #909399;
  font-size: 13px;
}
.push-list {
  min-height: 300px;
}
.push-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #ebeef5;
}
.push-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.push-item.unread {
  background: #f0f9ff;
}
.push-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
  flex-shrink: 0;
}
.push-info {
  flex: 1;
  min-width: 0;
}
.push-info h5 {
  margin: 0 0 6px;
  font-size: 15px;
  color: #303133;
}
.push-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}
.push-time {
  color: #c0c4cc;
  font-size: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 弹窗 */
.detail-body {
  line-height: 1.8;
}
.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  color: #909399;
  font-size: 13px;
}
.detail-content {
  color: #303133;
  font-size: 15px;
}
</style>
