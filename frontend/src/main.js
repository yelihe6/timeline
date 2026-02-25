import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { useTheme } from './composables/useTheme'
import { useCover } from './composables/useCover'
import './style.css'

// 初始化主题与背景
useTheme()
useCover()

createApp(App).use(router).mount('#app')
