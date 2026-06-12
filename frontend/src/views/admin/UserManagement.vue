<template>
<<<<<<< HEAD
  <div class="user-management-page">
    <div class="page-header">
      <h2>👥 用户管理</h2>
      <p class="subtitle">管理员可查看和管理平台用户账号及登录日志</p>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- ==================== 用户列表 Tab ==================== -->
      <el-tab-pane label="用户列表" name="users">
        <!-- 筛选 -->
        <el-card class="filter-card" shadow="never">
          <el-form :inline="true">
            <el-form-item label="用户名">
              <el-input
                v-model="filterForm.username"
                placeholder="请输入用户名"
                clearable
                style="width: 150px"
                @clear="handleUserSearch"
                @keyup.enter="handleUserSearch"
              />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input
                v-model="filterForm.phone"
                placeholder="请输入手机号"
                clearable
                style="width: 150px"
                @clear="handleUserSearch"
                @keyup.enter="handleUserSearch"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select
                v-model="filterForm.status"
                placeholder="全部"
                clearable
                style="width: 110px"
                @change="handleUserSearch"
              >
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUserSearch" :icon="Search">查询</el-button>
              <el-button @click="handleUserReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 表格 -->
        <el-card class="table-card" shadow="never">
          <el-table
            v-loading="userLoading"
            :data="userList"
            border
            stripe
            style="width: 100%"
          >
            <el-table-column prop="userId" label="ID" width="70" align="center" />
            <el-table-column prop="username" label="用户名" min-width="110" show-overflow-tooltip />
            <el-table-column prop="phone" label="手机号" width="140" show-overflow-tooltip />
            <el-table-column label="角色" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.role === 'admin' ? 'warning' : 'info'" size="small">
                  {{ row.role === 'admin' ? '管理员' : '用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
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
            <el-table-column label="操作" width="140" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="handleViewDetail(row)">
                  详情
                </el-button>
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
              v-model:current-page="userPagination.pageNum"
              v-model:page-size="userPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="userPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleUserSearch"
              @current-change="handleUserSearch"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- ==================== 登录日志 Tab ==================== -->
      <el-tab-pane label="登录日志" name="logs">
        <el-card class="filter-card" shadow="never">
          <el-form :inline="true">
            <el-form-item label="用户ID">
              <el-input-number
                v-model="logFilter.userId"
                :min="1"
                placeholder="用户ID"
                style="width: 140px"
                @keyup.enter="handleLogSearch"
              />
            </el-form-item>
            <el-form-item label="结果">
              <el-select
                v-model="logFilter.result"
                placeholder="全部"
                clearable
                style="width: 110px"
                @change="handleLogSearch"
              >
                <el-option label="成功" :value="1" />
                <el-option label="失败" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogSearch" :icon="Search">查询</el-button>
              <el-button @click="handleLogReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="table-card" shadow="never">
          <el-table
            v-loading="logLoading"
            :data="logList"
            border
            stripe
            style="width: 100%"
          >
            <el-table-column prop="logId" label="日志ID" width="80" align="center" />
            <el-table-column prop="userId" label="用户ID" width="80" align="center" />
            <el-table-column prop="phone" label="手机号" width="140" show-overflow-tooltip />
            <el-table-column prop="loginIp" label="登录IP" min-width="150" show-overflow-tooltip />
            <el-table-column label="登录结果" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.loginResult === 1 ? 'success' : 'danger'" size="small">
                  {{ row.loginResult === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="登录时间" width="170">
              <template #default="{ row }">
                {{ formatDate(row.loginTime) }}
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="logPagination.pageNum"
              v-model:page-size="logPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="logPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleLogSearch"
              @current-change="handleLogSearch"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- ==================== 用户详情弹窗 ==================== -->
    <el-dialog v-model="detailVisible" title="用户详情" width="520px" destroy-on-close>
      <el-descriptions v-if="detailUser" :column="2" border>
        <el-descriptions-item label="用户ID">{{ detailUser.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailUser.username }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="detailUser.role === 'admin' ? 'warning' : 'info'" size="small">
            {{ detailUser.role === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailUser.status === 1 ? 'success' : 'danger'" size="small">
            {{ detailUser.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ detailUser.birthDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ genderLabel(detailUser.gender) }}</el-descriptions-item>
        <el-descriptions-item label="身高">{{ detailUser.height ? detailUser.height + ' cm' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDate(detailUser.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
=======
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
>>>>>>> dev
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
<<<<<<< HEAD
import { getAdminUsers, toggleUserStatus, getLoginLogs } from '../../api/admin'
import type { AdminUserItem, LoginLog } from '../../types'

// ============ Tab ============
const activeTab = ref('users')

// ============ 用户列表 ============
const filterForm = reactive({ username: '', phone: '', status: null as number | null })
const userLoading = ref(false)
const userList = ref<AdminUserItem[]>([])
const userPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

async function fetchUsers() {
  userLoading.value = true
=======
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
>>>>>>> dev
  try {
    const res = await getAdminUsers({
      username: filterForm.username || undefined,
      phone: filterForm.phone || undefined,
      status: filterForm.status,
<<<<<<< HEAD
      pageNum: userPagination.pageNum,
      pageSize: userPagination.pageSize,
    })
    const d = res.data.data
    userList.value = d.items || []
    userPagination.total = d.total || 0
  } catch { /* handled */ } finally { userLoading.value = false }
}

function handleUserSearch() { userPagination.pageNum = 1; fetchUsers() }
function handleUserReset() {
  filterForm.username = ''; filterForm.phone = ''; filterForm.status = null
  userPagination.pageNum = 1; fetchUsers()
}

// ============ 启用/禁用 ============
async function handleToggleStatus(row: AdminUserItem) {
  const actionText = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}用户「${row.username}」吗？`,
      `${actionText}账号`,
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' },
    )
=======
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
>>>>>>> dev
    const newStatus = row.status === 1 ? 0 : 1
    await toggleUserStatus(row.userId, newStatus)
    ElMessage.success(`${actionText}成功`)
    row.status = newStatus
  } catch (err) {
    if (err === 'cancel' || err === 'close') return
  }
}

<<<<<<< HEAD
// ============ 用户详情 ============
const detailVisible = ref(false)
const detailUser = ref<AdminUserItem | null>(null)
function handleViewDetail(row: AdminUserItem) {
  detailUser.value = row
  detailVisible.value = true
}
function genderLabel(g?: string) {
  if (g === 'MALE') return '男'
  if (g === 'FEMALE') return '女'
  if (g === 'UNKNOWN') return '未知'
  return g || '-'
}

// ============ 登录日志 ============
const logFilter = reactive({ userId: null as number | null, result: null as number | null })
const logLoading = ref(false)
const logList = ref<LoginLog[]>([])
const logPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

async function fetchLogs() {
  logLoading.value = true
  try {
    const res = await getLoginLogs({
      userId: logFilter.userId || undefined,
      result: logFilter.result || undefined,
      pageNum: logPagination.pageNum,
      pageSize: logPagination.pageSize,
    })
    const d = res.data.data
    logList.value = d.items || []
    logPagination.total = d.total || 0
  } catch { /* handled */ } finally { logLoading.value = false }
}

function handleLogSearch() { logPagination.pageNum = 1; fetchLogs() }
function handleLogReset() {
  logFilter.userId = null; logFilter.result = null
  logPagination.pageNum = 1; fetchLogs()
}

// ============ Tab 切换 ============
function handleTabChange(name: string) {
  if (name === 'logs') fetchLogs()
}

// ============ 工具 ============
=======
>>>>>>> dev
function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN', { hour12: false })
}

<<<<<<< HEAD
onMounted(fetchUsers)
</script>

<style scoped>
.user-management-page {
=======
onMounted(fetchData)
</script>

<style scoped>
.user-management-container {
>>>>>>> dev
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
<<<<<<< HEAD
.filter-card { margin-bottom: 16px; }
.table-card { margin-bottom: 20px; }
=======
.filter-card { margin-bottom: 20px; }
>>>>>>> dev
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
