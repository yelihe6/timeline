<template>
  <button class="theme-toggle" @click="toggleTheme" :title="theme === 'dark' ? '切换亮色模式' : '切换暗色模式'">
    <span class="icon">{{ theme === 'dark' ? '☀' : '🌙' }}</span>
  </button>
</template>

<script setup>
import { useTheme } from '../composables/useTheme'

const { theme, toggleTheme } = useTheme()
</script>

<style scoped>
.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text);
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: var(--shadow);
}
.theme-toggle:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--bg-input);
}
.icon {
  font-size: 18px;
  line-height: 1;
}
</style>
