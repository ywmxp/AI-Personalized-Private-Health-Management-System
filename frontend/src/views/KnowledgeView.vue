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
            @click="handleViewKnowledge(item)"
          >
            <div class="knowledge-header">
              <h4 class="knowledge-title">{{ item.title }}</h4>
              <el-tag size="small">{{ item.relateTag }}</el-tag>
            </div>
            <p class="knowledge-content">{{ item.content }}</p>
            <div class="knowledge-footer">
              <span class="source">来源：{{ item.source }}</span>
              <span class="time">{{ item.createTime }}</span>
            </div>
          </el-card>

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
            @click="handleViewPush(item)"
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

    <!-- 知识详情弹窗（知识库 / 推送共用） -->
    <el-dialog
      v-model="detailVisible"
      :title="detailTitle"
      width="600px"
      destroy-on-close
    >
      <div v-if="detailContent" class="detail-body">
        <div class="detail-meta">
          <el-tag size="small">{{ detailTag }}</el-tag>
          <span v-if="detailSource">来源：{{ detailSource }}</span>
        </div>
        <div class="detail-content">{{ detailContent }}</div>
      </div>
      <div v-else class="detail-body">
        <div class="detail-meta">
          <el-tag size="small">{{ detailTag }}</el-tag>
        </div>
        <el-empty description="文章内容加载中..." :image-size="60" />
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

// ===== localStorage 键名：持久化已读推送 ID =====
const READ_PUSHES_KEY = 'aihealth_read_pushes'

function getReadPushIds(): Set<number> {
  try {
    return new Set(JSON.parse(localStorage.getItem(READ_PUSHES_KEY) || '[]'))
  } catch {
    return new Set()
  }
}

function saveReadPushId(pushId: number) {
  const ids = getReadPushIds()
  ids.add(pushId)
  localStorage.setItem(READ_PUSHES_KEY, JSON.stringify([...ids]))
}

// ===== Tab 状态 =====
const activeTab = ref('library')

// ===== 知识库 =====
const filterForm = reactive({ keyword: '', tag: '' })
const knowledgeLoading = ref(false)
const knowledgeList = ref<KnowledgeItem[]>([])
const knowledgePagination = reactive({ page: 1, size: 10, total: 0 })

// 知识缓存（按 knowledgeId 索引，供推送详情用）
const knowledgeCache = ref<Map<number, KnowledgeItem>>(new Map())

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
    // 更新缓存
    for (const item of knowledgeList.value) {
      knowledgeCache.value.set(item.knowledgeId, item)
    }
  } catch {
    // handled
  } finally {
    knowledgeLoading.value = false
  }
}

// ===== 推送 =====
const pushing = ref(false)
const pushLoading = ref(false)
const pushList = ref<KnowledgePush[]>([])
const pushPagination = reactive({ page: 1, size: 10, total: 0 })

async function handleGeneratePush() {
  pushing.value = true
  try {
    const res = await pushKnowledge({ profileId: 0 })
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
    const items: KnowledgePush[] = data.items || []
    pushPagination.total = data.total || 0
    // 合并 localStorage 中的已读状态
    const readIds = getReadPushIds()
    for (const item of items) {
      if (readIds.has(item.pushId)) {
        item.isRead = 1
      }
    }
    pushList.value = items
  } catch {
    // handled
  } finally {
    pushLoading.value = false
  }
}

// 标记单个推送为已读（调接口 + 存 localStorage）
async function markAsRead(item: KnowledgePush) {
  if (item.isRead === 1) return
  item.isRead = 1
  saveReadPushId(item.pushId)
  try {
    await markPushRead(item.pushId)
  } catch {
    // 接口失败不影响前端已读状态的展示
  }
}

// ===== 详情弹窗（共用） =====
const detailVisible = ref(false)
const detailTitle = ref('')
const detailContent = ref('')
const detailTag = ref('')
const detailSource = ref('')

// 知识库：点击查看文章详情
function handleViewKnowledge(item: KnowledgeItem) {
  detailTitle.value = item.title
  detailContent.value = item.content
  detailTag.value = item.relateTag
  detailSource.value = item.source
  detailVisible.value = true
}

// 推送：点击查看详情 + 标记已读
async function handleViewPush(item: KnowledgePush) {
  // 尝试从缓存中获取完整内容
  const cached = knowledgeCache.value.get(item.knowledgeId)
  let content = cached?.content || ''
  let source = cached?.source || ''

  // 缓存未命中时异步拉取
  if (!cached) {
    try {
      const res = await getKnowledgeList({ page: 1, size: 1 })
      const list = res.data.data?.items || []
      const matched = list.find((k) => k.knowledgeId === item.knowledgeId)
      if (matched) {
        content = matched.content
        source = matched.source
        knowledgeCache.value.set(matched.knowledgeId, matched)
      }
    } catch {
      // 拉取失败则展示 title 作为内容
    }
  }

  detailTitle.value = item.title
  detailContent.value = content || item.title
  detailTag.value = item.relateTag
  detailSource.value = source
  detailVisible.value = true

  // 标记已读
  await markAsRead(item)
}

// ===== Tab 切换 =====
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
.knowledge-title {
  margin: 0;
  font-size: 16px;
  color: #409eff;
  cursor: pointer;
}
.knowledge-item:hover .knowledge-title {
  text-decoration: underline;
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
.push-item:hover .push-info h5 {
  color: #409eff;
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
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
