<template>
  <div class="home-page">
    <div class="welcome-card">
      <h2>✨ 欢迎使用时间线 ⏱️</h2>
      <p>从左侧导航选择「📋 事件」管理您的事件，或使用「📅 日历」查看日程。</p>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  max-width: 800px;
}
.welcome-card {
  padding: 40px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
[data-theme="dark"] .welcome-card {
  background: rgba(30, 41, 59, 0.75);
}
.welcome-card h2 {
  font-size: 22px;
  color: var(--text);
  margin-bottom: 16px;
}
.welcome-card p {
  color: var(--text-muted);
  font-size: 16px;
  line-height: 1.6;
}
</style>
