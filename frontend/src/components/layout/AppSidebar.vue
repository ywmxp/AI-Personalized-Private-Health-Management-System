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
    <el-menu-item index="/ai-advice">
      <el-icon><MagicStick /></el-icon>
      <span>AI 建议</span>
    </el-menu-item>
    <el-menu-item index="/reminders">
      <el-icon><AlarmClock /></el-icon>
      <span>健康提醒</span>
    </el-menu-item>
    <el-menu-item index="/profile">
      <el-icon><User /></el-icon>
      <span>个人中心</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { House, Monitor, MagicStick, AlarmClock, User } from '@element-plus/icons-vue'
const route = useRoute()
</script>
