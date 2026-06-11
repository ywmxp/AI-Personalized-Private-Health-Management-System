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
  background: #304156;
}
.el-header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}
.el-main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
