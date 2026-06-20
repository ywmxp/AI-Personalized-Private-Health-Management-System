<template>
  <div class="header">
    <span class="title">AI 健康管理系统</span>
    <div class="user-area">
      <span class="username">{{ userStore.userInfo?.username || '用户' }}</span>
      <el-button type="danger" text @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}
.title {
  font-size: 16px;
  font-weight: 700;
  color: var(--c-text);
  letter-spacing: -0.005em;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 14px;
}
.username {
  font-size: 13px;
  color: var(--c-text-secondary);
}
</style>
