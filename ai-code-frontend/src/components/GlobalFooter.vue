<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p class="copyright">
        <a
          href="https://github.com/iamwsll/AI-code-generate-platform"
          target="_blank"
          rel="noopener noreferrer"
          class="author-link"
        >
          零代码生成平台 by iamwsll
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  text-align: center;
  padding: 20px;
  margin-top: 40px;
  border-top: 1px solid rgba(11, 15, 26, 0.08);
}

.copyright {
  margin: 0;
  color: #5f6575;
  font-size: 14px;
}

.author-link {
  color: inherit;
  text-decoration: none;
}

.author-link:hover {
  color: #ff6b2c;
}
</style>
