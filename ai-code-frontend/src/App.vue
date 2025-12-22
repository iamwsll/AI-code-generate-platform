<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <BasicLayout />
</template>

<style>
/* 全局样式优化 */
* {
  box-sizing: border-box;
}

:root {
  color-scheme: light;
  --ink-1: #0b0f1a;
  --ink-2: #2b3345;
  --muted-1: #5f6575;
  --accent-1: #ff6b2c;
  --accent-2: #2b7cff;
  --accent-3: #18b892;
  --surface-1: #fff7ee;
  --surface-2: #f3efe9;
  --stroke-1: rgba(11, 15, 26, 0.08);
  --shadow-1: 0 14px 40px rgba(15, 23, 42, 0.16);
  --shadow-2: 8px 10px 0 rgba(11, 15, 26, 0.12);
}

body {
  margin: 0;
  padding: 0;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell',
    'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  color: var(--ink-1);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
  background: var(--surface-2);
}

html {
  overflow-x: hidden;
}
</style>
