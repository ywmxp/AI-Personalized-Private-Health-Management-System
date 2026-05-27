<template>
  <div class="spinner-container">
    <el-icon class="spinner" :size="size"><Loading /></el-icon>
    <p v-if="text" class="spinner-text">{{ text }}</p>
  </div>
</template>

<script setup lang="ts">
import { Loading } from '@element-plus/icons-vue'

withDefaults(defineProps<{
  size?: number
  text?: string
}>(), {
  size: 32,
})
</script>

<style scoped>
.spinner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}
.spinner {
  animation: spin 1s linear infinite;
}
.spinner-text {
  margin-top: 12px;
  color: #909399;
  font-size: 14px;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
