<script setup lang="ts"></script>
<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p>
        零代码生成平台 by
        <a href="https://iamwsll.cn" target="_blank" rel="noopener noreferrer">iamwsll</a>
      </p>
    </div>
  </a-layout-footer>
</template>

<style scoped>
.footer {
  background: #001529;
  color: rgba(255, 255, 255, 0.65);
  text-align: center;
  padding: 24px 50px;
  position: relative;
  bottom: 0;
  width: 100%;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
}

.footer-content p {
  margin: 0;
  font-size: 14px;
}

.footer-content a {
  color: #1890ff;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-content a:hover {
  color: #40a9ff;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer {
    padding: 20px;
  }

  .footer-content p {
    font-size: 12px;
  }
}
</style>
