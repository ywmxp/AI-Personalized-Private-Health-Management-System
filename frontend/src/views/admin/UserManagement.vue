<template>
  <div class="user-management-container">
    <div class="page-header">
      <h2>👥 用户管理</h2>
      <p class="subtitle">管理员可查看和管理平台用户账号</p>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="filterForm" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="filterForm.username"
            placeholder="请输入用户名"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="filterForm.phone"
            placeholder="请输入手机号"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filterForm.status"
            placeholder="全部"
            clearable
            @change="handleSearch"
            style="width: 120px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="userId" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="140" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              size="small"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAdminUsers, toggleUserStatus } from '../../api/admin'
import type { AdminUserItem } from '../../types'

const filterForm = reactive({
  username: '',
  phone: '',
  status: null as number | null,
})

const loading = ref(false)
const tableData = ref<AdminUserItem[]>([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

async function fetchData() {
  loading.value = true
  try {
    const res = await getAdminUsers({
      username: filterForm.username || undefined,
      phone: filterForm.phone || undefined,
      status: filterForm.status,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    const data = res.data.data
    tableData.value = data.items || []
    pagination.total = data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  fetchData()
}

function handleReset() {
  filterForm.username = ''
  filterForm.phone = ''
  filterForm.status = null
  pagination.pageNum = 1
  fetchData()
}

async function handleToggleStatus(row: AdminUserItem) {
  const actionText = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${actionText}用户「${row.username}」吗？`, `${actionText}账号`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const newStatus = row.status === 1 ? 0 : 1
    await toggleUserStatus(row.userId, newStatus)
    ElMessage.success(`${actionText}成功`)
    row.status = newStatus
  } catch (err) {
    if (err === 'cancel' || err === 'close') return
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN', { hour12: false })
}

onMounted(fetchData)
</script>

<style scoped>
.user-management-container {
  padding: 4px;
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
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
