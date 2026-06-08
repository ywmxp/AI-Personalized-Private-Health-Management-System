<template>
  <el-menu
    :default-active="route.path"
    router
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409eff"
  >
    <el-menu-item index="/home">
      <el-icon><House /></el-icon>
      <span>首页</span>
    </el-menu-item>

    <el-menu-item index="/health-data">
      <el-icon><Monitor /></el-icon>
      <span>健康数据</span>
    </el-menu-item>

    <el-menu-item index="/health-trends">
      <el-icon><TrendCharts /></el-icon>
      <span>健康趋势</span>
    </el-menu-item>

    <el-sub-menu index="ai-group">
      <template #title>
        <el-icon><MagicStick /></el-icon>
        <span>AI 健康分析</span>
      </template>
      <el-menu-item index="/ai-profile">
        <el-icon><UserFilled /></el-icon>
        <span>健康画像</span>
      </el-menu-item>
      <el-menu-item index="/ai-plan">
        <el-icon><Document /></el-icon>
        <span>健康计划</span>
      </el-menu-item>
      <el-menu-item index="/knowledge">
        <el-icon><Reading /></el-icon>
        <span>健康知识</span>
      </el-menu-item>
    </el-sub-menu>

    <el-menu-item index="/reminders">
      <el-icon><AlarmClock /></el-icon>
      <span>健康提醒</span>
    </el-menu-item>

    <el-menu-item index="/profile">
      <el-icon><User /></el-icon>
      <span>个人中心</span>
    </el-menu-item>

    <!-- 管理员菜单 -->
    <template v-if="isAdmin">
      <div class="menu-divider">
        <span>管理员</span>
      </div>
      <el-menu-item index="/admin/users">
        <el-icon><Avatar /></el-icon>
        <span>用户管理</span>
      </el-menu-item>
      <el-menu-item index="/admin/statistics">
        <el-icon><PieChart /></el-icon>
        <span>数据统计</span>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  House, Monitor, TrendCharts, MagicStick, UserFilled,
  Document, Reading, AlarmClock, User, Avatar, PieChart,
} from '@element-plus/icons-vue'

const route = useRoute()

const isAdmin = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  return userInfo?.role === 'admin'
})
</script>

<style scoped>
.menu-divider {
  padding: 12px 20px 4px;
  color: #6b7b8d;
  font-size: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  margin-top: 8px;
}
.menu-divider span {
  letter-spacing: 2px;
}
</style>
