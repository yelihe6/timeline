import { ref, watch } from 'vue'

const THEME_KEY = 'time-cyber-theme'
const themes = ['dark', 'light']

const theme = ref(localStorage.getItem(THEME_KEY) || 'light')

export function useTheme() {
  function setTheme(val) {
    if (!themes.includes(val)) return
    theme.value = val
    localStorage.setItem(THEME_KEY, val)
    document.documentElement.setAttribute('data-theme', val)
  }

  function toggleTheme() {
    setTheme(theme.value === 'dark' ? 'light' : 'dark')
  }

  // 初始化时应用主题
  if (typeof document !== 'undefined') {
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  return { theme, setTheme, toggleTheme }
}
