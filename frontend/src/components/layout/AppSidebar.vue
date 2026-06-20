<template>
  <div class="sidebar">
    <div class="sidebar-logo">
      <div class="logo-icon">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
        </svg>
      </div>
      <span class="logo-text">AI 健康管理</span>
    </div>

    <el-menu
      :default-active="route.path"
      router
      background-color="transparent"
      text-color="var(--c-sidebar-text)"
      active-text-color="#fff"
      class="sidebar-menu"
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
      <el-sub-menu index="ai-group" popper-class="sidebar-popper">
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
      <template v-if="isAdmin">
        <div class="sidebar-divider">
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
  </div>
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
  return (userInfo?.role || '').toUpperCase() === 'ADMIN'
})
</script>

<style scoped>
.sidebar {
  display: flex; flex-direction: column; height: 100%;
  background: var(--c-sidebar);
  position: relative; overflow: hidden;
}
.sidebar::after {
  content: ''; position: absolute; inset: 0; pointer-events: none;
  background-image: var(--noise); opacity: 0.4; z-index: 0;
}
.sidebar-logo {
  display: flex; align-items: center; gap: 10px;
  padding: 22px 20px 18px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  position: relative; z-index: 1;
}
.logo-icon {
  width: 36px; height: 36px;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, var(--c-primary), #14919B);
  border-radius: var(--r-md); color: #fff; flex-shrink: 0;
}
.logo-text {
  font-size: 15px; font-weight: 600; color: #fff;
  font-family: var(--font-display); letter-spacing: 0.03em;
}
.sidebar-menu {
  border-right: none !important; flex: 1; padding: 10px 0;
  position: relative; z-index: 1;
}
.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  margin: 2px 10px; border-radius: var(--r-md);
  font-size: 13px; height: 42px; line-height: 42px;
  transition: all var(--t-fast);
}
.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: var(--c-sidebar-hover);
}
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: var(--c-sidebar-active); color: #fff;
  font-weight: 600; box-shadow: 0 2px 12px rgba(13,115,119,0.3);
}
.sidebar-menu :deep(.el-sub-menu .el-menu) {
  background: rgba(0,0,0,0.14); margin: 0 10px; border-radius: var(--r-md);
}
.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important; font-size: 12px; height: 36px; line-height: 36px;
}
.sidebar-divider {
  padding: 16px 20px 6px; color: rgba(140,165,163,0.4);
  font-size: 10px; text-transform: uppercase; letter-spacing: 0.14em;
  font-family: var(--font-body); margin-top: 6px;
  position: relative; z-index: 1;
}
</style>

<style>
.sidebar-popper {
  background: #1F3634 !important;
  border: 1px solid rgba(255,255,255,0.08) !important;
  border-radius: var(--r-md) !important; padding: 4px 0 !important;
}
.sidebar-popper .el-menu-item {
  color: var(--c-sidebar-text) !important; font-size: 12px !important;
}
.sidebar-popper .el-menu-item:hover {
  background: rgba(255,255,255,0.05) !important; color: #fff !important;
}
</style>
