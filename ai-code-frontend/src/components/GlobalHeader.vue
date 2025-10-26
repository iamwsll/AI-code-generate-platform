<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()

// 当前选中菜单
const selectedKeys = ref<string[]>([router.currentRoute.value.path])

// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const menuItems = ref<MenuProps['items']>([
  {
    key: '/',
    label: '首页',
  },
  {
    key: '/about',
    label: '关于',
  },
])

const handleMenuClick = (e: { key: string }) => {
  router.push(e.key)
}
</script>

<template>
  <a-layout-header class="header">
    <div class="header-content">
      <div class="logo-section">
        <img class="logo" src="@/assets/logo.png" alt="Logo" />
        <span class="title">零代码生成平台</span>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        class="menu"
        :items="menuItems"
        @click="handleMenuClick"
      />
      <div class="user-section">
        <a-button type="primary">登录</a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<style scoped>
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 50px;
  position: sticky;
  top: 0;
  z-index: 999;
}

.header-content {
  display: flex;
  align-items: center;
  height: 64px;
  max-width: 1400px;
  margin: 0 auto;
}

.logo-section {
  display: flex;
  align-items: center;
  margin-right: 40px;
  flex-shrink: 0;
}

.logo {
  height: 40px;
  width: 40px;
  margin-right: 12px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}

.menu {
  flex: 1;
  border: none;
  line-height: 64px;
}

.user-section {
  margin-left: auto;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 0 20px;
  }

  .logo-section {
    margin-right: 20px;
  }

  .logo {
    height: 32px;
    width: 32px;
    margin-right: 8px;
  }

  .title {
    font-size: 16px;
  }

  .menu {
    display: none;
  }
}

@media (max-width: 576px) {
  .header {
    padding: 0 16px;
  }

  .logo-section {
    margin-right: 10px;
  }

  .title {
    font-size: 14px;
  }
}
</style>
