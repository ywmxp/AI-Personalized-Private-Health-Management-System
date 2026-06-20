<template>
  <div class="empty-container">
    <el-icon :size="48" color="#c0c4cc"><Document /></el-icon>
    <p>{{ description }}</p>
  </div>
</template>

<script setup lang="ts">
import { Document } from '@element-plus/icons-vue'

withDefaults(defineProps<{
  description?: string
}>(), {
  description: '暂无数据',
})
</script>

<style scoped>
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  color: #909399;
  font-size: 14px;
}
.empty-container p {
  margin-top: 12px;
}
</style>
