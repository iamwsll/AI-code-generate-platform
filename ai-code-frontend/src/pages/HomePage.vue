<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listGoodAppVoByPage, listMyAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 优化提示词功能已移除

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <!-- 网站标题和描述 -->
      <div class="hero-section">
        <div class="hero-badge">Beta · 一句话生成网站</div>
        <h1 class="hero-title">零代码生成平台</h1>
        <p class="hero-description">把你的想法，直接变成可运行的网页应用</p>
        <div class="hero-metrics">
          <div class="metric">
            <span class="metric-value">自然语言</span>
            <span class="metric-label">描述即可生成</span>
          </div>
          <div class="metric">
            <span class="metric-value">可视作品</span>
            <span class="metric-label">一键打开链接</span>
          </div>
          <div class="metric">
            <span class="metric-value">可持续迭代</span>
            <span class="metric-label">继续对话改版</span>
          </div>
        </div>
      </div>

      <!-- 用户提示词输入框 -->
      <div class="input-section">
        <a-textarea
          v-model:value="userPrompt"
          placeholder="帮我创建个人博客网站"
          :rows="4"
          :maxlength="1000"
          class="prompt-input"
        />
        <div class="prompt-hint">提示：描述风格、页面结构、功能模块越具体越好</div>
        <div class="input-actions">
          <a-button type="primary" size="large" @click="createApp" :loading="creating">
            <template #icon>
              <span>↑</span>
            </template>
          </a-button>
        </div>
      </div>

      <!-- 快捷按钮 -->
      <div class="quick-actions">
        <a-button
          type="default"
          @click="
            setPrompt(
              '创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能、评论系统和个人简介页面。采用简洁的设计风格，支持响应式布局，文章支持Markdown格式，首页展示最新文章和热门推荐。',
            )
          "
          >个人博客网站</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。',
            )
          "
          >企业官网</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '构建一个功能完整的在线商城，包含商品展示、购物车、用户注册登录、订单管理、支付结算等功能。设计现代化的商品卡片布局，支持商品搜索筛选、用户评价、优惠券系统和会员积分功能。',
            )
          "
          >在线商城</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '制作一个精美的作品展示网站，适合设计师、摄影师、艺术家等创作者。包含作品画廊、项目详情页、个人简历、联系方式等模块。采用瀑布流或网格布局展示作品，支持图片放大预览和作品分类筛选。',
            )
          "
          >作品展示网站</a-button
        >
        <a-button type="default" @click="setPrompt('创建一个简单的个人博客网站，不超过20行')"
          >简单博客网站</a-button
        >
      </div>

      <!-- 我的作品 -->
      <div class="section">
        <h2 class="section-title">我的作品</h2>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <!-- 精选案例 -->
      <div class="section">
        <h2 class="section-title">精选案例</h2>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个案例`"
            @change="loadFeaturedApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background:
    radial-gradient(1200px 600px at 10% -10%, rgba(255, 107, 44, 0.25), transparent 60%),
    radial-gradient(900px 500px at 90% 10%, rgba(43, 124, 255, 0.22), transparent 55%),
    linear-gradient(180deg, #fff7ee 0%, #f6f1e9 45%, #eef1f6 100%);
  position: relative;
  overflow: hidden;
}

/* 细线条纹理 */
#homePage::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    repeating-linear-gradient(
      120deg,
      rgba(11, 15, 26, 0.04) 0,
      rgba(11, 15, 26, 0.04) 1px,
      transparent 1px,
      transparent 40px
    ),
    repeating-linear-gradient(
      300deg,
      rgba(11, 15, 26, 0.03) 0,
      rgba(11, 15, 26, 0.03) 1px,
      transparent 1px,
      transparent 48px
    );
  pointer-events: none;
  opacity: 0.6;
}

/* 浮动块 */
#homePage::after {
  content: '';
  position: absolute;
  inset: -10% 0 0 -10%;
  background:
    radial-gradient(200px 160px at 12% 25%, rgba(255, 107, 44, 0.35), transparent 70%),
    radial-gradient(240px 190px at 85% 15%, rgba(43, 124, 255, 0.25), transparent 70%),
    radial-gradient(180px 140px at 70% 70%, rgba(24, 184, 146, 0.2), transparent 70%);
  pointer-events: none;
  filter: blur(10px);
  animation: floatGlow 16s ease-in-out infinite;
}

@keyframes floatGlow {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
    opacity: 0.7;
  }
  50% {
    transform: translate3d(16px, -12px, 0) scale(1.03);
    opacity: 0.95;
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

/* 英雄区域 */
.hero-section {
  text-align: left;
  padding: 96px 0 48px;
  margin-bottom: 24px;
  color: var(--ink-1);
  position: relative;
  overflow: hidden;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid rgba(11, 15, 26, 0.15);
  background: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  letter-spacing: 0.3px;
  color: var(--ink-2);
  margin-bottom: 16px;
  box-shadow: var(--shadow-2);
}

.hero-title {
  font-size: 60px;
  font-weight: 700;
  margin: 0 0 20px;
  line-height: 1.2;
  letter-spacing: -1.5px;
  position: relative;
  z-index: 2;
  text-shadow: 6px 6px 0 rgba(255, 107, 44, 0.08);
}

.hero-description {
  font-size: 18px;
  margin: 0;
  color: var(--muted-1);
  position: relative;
  z-index: 2;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 24px;
  max-width: 820px;
}

.metric {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--stroke-1);
  border-radius: 14px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  box-shadow: var(--shadow-2);
}

.metric-value {
  font-weight: 600;
  color: var(--ink-1);
  font-size: 16px;
}

.metric-label {
  font-size: 13px;
  color: var(--muted-1);
}

/* 输入区域 */
.input-section {
  position: relative;
  margin: 0 auto 24px;
  max-width: 820px;
}

.prompt-input {
  border-radius: 18px;
  border: 2px solid rgba(11, 15, 26, 0.12);
  font-size: 16px;
  padding: 22px 70px 22px 20px;
  background: #ffffff;
  box-shadow: var(--shadow-1);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.prompt-input:focus {
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.2);
  transform: translate3d(0, -2px, 0);
}

.input-actions {
  position: absolute;
  bottom: 14px;
  right: 14px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.prompt-hint {
  margin-top: 10px;
  font-size: 13px;
  color: var(--muted-1);
  text-align: left;
}

.input-actions :deep(.ant-btn-primary) {
  border-radius: 12px;
  background: var(--ink-1);
  border: 2px solid var(--ink-1);
  box-shadow: 6px 6px 0 rgba(255, 107, 44, 0.2);
}

/* 快捷按钮 */
.quick-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-start;
  margin: 20px 0 60px;
  flex-wrap: wrap;
}

.quick-actions .ant-btn {
  border-radius: 999px;
  padding: 8px 18px;
  height: auto;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(11, 15, 26, 0.18);
  color: var(--ink-2);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border 0.2s ease;
}

.quick-actions .ant-btn:hover {
  border-color: rgba(11, 15, 26, 0.45);
  transform: translate3d(0, -2px, 0);
  box-shadow: 8px 8px 0 rgba(43, 124, 255, 0.12);
}

/* 区域标题 */
.section {
  margin-bottom: 60px;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 28px;
  color: var(--ink-1);
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.section-title::after {
  content: '';
  width: 48px;
  height: 3px;
  background: linear-gradient(90deg, var(--accent-1), transparent);
  border-radius: 999px;
}

/* 我的作品网格 */
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

/* 精选案例网格 */
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-section {
    padding: 70px 0 32px;
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-description {
    font-size: 16px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
  }

  .quick-actions {
    justify-content: flex-start;
  }
}
</style>
