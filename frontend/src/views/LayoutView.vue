<template>
  <el-container class="layout">
    <el-aside width="220px">
      <AppSidebar />
    </el-aside>
    <el-container>
      <el-header height="60px">
        <AppHeader />
      </el-header>
      <el-main>
        <RouterView />
      </el-main>
    </el-container>
  </el-container>

  <!-- 提醒推送弹窗 (3.4.2) -->
  <ReminderPopup />
</template>

<script setup lang="ts">
import AppHeader from '../components/layout/AppHeader.vue'
import AppSidebar from '../components/layout/AppSidebar.vue'
import ReminderPopup from '../components/reminder/ReminderPopup.vue'
</script>

<style scoped>
.layout {
  height: 100vh;
}
.el-aside {
  background: var(--c-sidebar);
  width: 240px !important;
  overflow: hidden;
}
.el-header {
  background: rgba(255,255,255,0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--c-border-light);
  padding: 0 28px;
}
.el-main {
  background: var(--c-bg);
  padding: 26px 30px;
}
</style>
