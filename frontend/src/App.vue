<template>
  <!-- 路由出口：所有路由对应的页面都会显示在这里 -->
  <router-view />
</template>

<script setup lang="ts">
// 这里不需要写任何代码
</script>

<style>
/* 只保留全局样式重置，其他页面样式都放到对应的.vue文件里 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
}
</style>
