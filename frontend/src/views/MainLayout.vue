<template>
  <div class="app-layout" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header" ref="headerRef">
        <div class="user-card" @click="!sidebarCollapsed && (showDropdown = !showDropdown)">
          <span class="user-initial" :class="{ visible: sidebarCollapsed }">{{ (user?.nickname || '用')[0] }}</span>
          <div class="user-card-content" :class="{ visible: contentExpanded }">
            <div class="user-card-name">{{ user?.nickname || '用户' }}</div>
            <div class="user-card-email">{{ user?.email || '' }}</div>
          </div>
          <svg class="dropdown-arrow" :class="{ open: showDropdown, visible: contentExpanded }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
        </div>
        <Transition name="dropdown">
          <div v-if="showDropdown" class="dropdown-menu">
            <button type="button" class="dropdown-item" @click="openEditInfo">编辑信息</button>
            <button type="button" class="dropdown-item" @click="openChangePassword">修改密码</button>
          </div>
        </Transition>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/main/home" class="nav-item" active-class="active" title="首页">
          <span class="nav-icon">🏠</span>
          <span class="nav-text">首页</span>
        </router-link>
        <router-link to="/main/events" class="nav-item" active-class="active" title="事件">
          <span class="nav-icon">📋</span>
          <span class="nav-text">事件</span>
        </router-link>
        <router-link to="/main/calendar" class="nav-item" active-class="active" title="日历">
          <span class="nav-icon">📅</span>
          <span class="nav-text">日历</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <button @click="handleLogout" class="btn-exit" title="退出">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          <span class="btn-exit-text">退出</span>
        </button>
      </div>
      <button type="button" class="btn-toggle" :title="sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'" @click="toggleSidebar">
        <svg class="toggle-icon" :class="{ collapsed: sidebarCollapsed }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
    </aside>
    <main class="main-area">
      <header class="main-header" :class="{ collapsed: headerCollapsed }">
        <h1 class="page-title">时间线</h1>
        <div class="header-right">
          <CoverSettings />
          <button
            type="button"
            class="btn-header-toggle"
            :title="headerCollapsed ? '展开顶栏' : '收起顶栏'"
            @click="toggleHeader"
          >
            <svg class="toggle-icon-vertical" :class="{ collapsed: headerCollapsed }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg>
          </button>
          <ThemeToggle />
        </div>
      </header>
      <div class="main-content">
        <router-view />
      </div>
    </main>
    <EditInfoModal :show="showEditInfo" :user="user" @close="showEditInfo = false" @saved="onInfoSaved" />
    <ChangePasswordModal :show="showChangePassword" @close="showChangePassword = false" @saved="onPasswordSaved" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import ThemeToggle from '../components/ThemeToggle.vue'
import CoverSettings from '../components/CoverSettings.vue'
import EditInfoModal from '../components/EditInfoModal.vue'
import ChangePasswordModal from '../components/ChangePasswordModal.vue'
import { useConfirm } from '../composables/useConfirm'
import { useToast } from '../composables/useToast'
import { authApi } from '../api/request'

const router = useRouter()
const user = ref(JSON.parse(sessionStorage.getItem('user') || '{}'))
const { confirm } = useConfirm()
const { show: showToast } = useToast()
const showDropdown = ref(false)
const showEditInfo = ref(false)
const showChangePassword = ref(false)
const headerRef = ref(null)
const sidebarCollapsed = ref(JSON.parse(localStorage.getItem('sidebarCollapsed') || 'false'))
const contentExpanded = ref(!JSON.parse(localStorage.getItem('sidebarCollapsed') || 'false'))
const headerCollapsed = ref(JSON.parse(localStorage.getItem('headerCollapsed') || 'false'))
let expandTimer = null

function toggleHeader() {
  headerCollapsed.value = !headerCollapsed.value
  localStorage.setItem('headerCollapsed', JSON.stringify(headerCollapsed.value))
}

function toggleSidebar() {
  const willCollapse = !sidebarCollapsed.value
  sidebarCollapsed.value = willCollapse
  localStorage.setItem('sidebarCollapsed', JSON.stringify(sidebarCollapsed.value))

  if (expandTimer) clearTimeout(expandTimer)
  if (willCollapse) {
    contentExpanded.value = false
  } else {
    expandTimer = setTimeout(() => {
      contentExpanded.value = true
      expandTimer = null
    }, 260)
  }
}

watch(sidebarCollapsed, (v) => {
  if (v) {
    showDropdown.value = false
    contentExpanded.value = false
  }
})

function openEditInfo() {
  showDropdown.value = false
  showEditInfo.value = true
}

function openChangePassword() {
  showDropdown.value = false
  showChangePassword.value = true
}

function onClickOutside(e) {
  if (headerRef.value && !headerRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}

async function onInfoSaved(data) {
  try {
    await authApi.updateUserInfo(user.value.userId, data)
    user.value = { ...user.value, nickname: data.nickname, phone: data.phone }
    sessionStorage.setItem('user', JSON.stringify(user.value))
    showToast('修改成功')
  } catch (e) {
    showToast(e.message || '修改失败')
  }
}

async function onPasswordSaved(data) {
  try {
    await authApi.changePassword(user.value.userId, data)
    showToast('密码修改成功')
  } catch (e) {
    showToast(e.message || '修改失败')
  }
}

async function handleLogout() {
  const ok = await confirm('是否要退出？', '确定')
  if (!ok) return
  sessionStorage.removeItem('user')
  showToast('退出成功')
  setTimeout(() => router.push('/login'), 800)
}

onMounted(() => {
  if (!user.value?.userId) router.push('/login')
  document.addEventListener('click', onClickOutside)
})
onUnmounted(() => {
  document.removeEventListener('click', onClickOutside)
  if (expandTimer) clearTimeout(expandTimer)
})
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 260px;
  height: 100vh;
  background: var(--bg-card);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow);
  z-index: 100;
  transition: width 0.25s ease;
}
.sidebar.collapsed {
  width: 72px;
}
.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid var(--border);
  position: relative;
  flex-shrink: 0;
  min-width: 0;
}
.sidebar.collapsed .sidebar-header {
  padding: 12px;
}
.user-card {
  position: relative;
  padding: 12px 36px 12px 12px;
  background: var(--bg-input);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.2s;
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
}
.sidebar:not(.collapsed) .user-card {
  justify-content: flex-start;
  padding: 12px 36px 12px 12px;
}
.sidebar.collapsed .user-card {
  padding: 12px;
}
.user-card:hover {
  background: var(--bg);
}
.user-initial {
  position: absolute;
  width: 0;
  overflow: hidden;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
}
.user-initial.visible {
  position: static;
  width: auto;
  overflow: visible;
  opacity: 1;
  pointer-events: auto;
}
.user-card-content {
  flex: 1;
  min-width: 0;
  opacity: 0;
  transition: opacity 0.2s ease;
}
.user-card-content.visible {
  opacity: 1;
  overflow: visible;
}
.user-card-content:not(.visible) {
  position: absolute;
  width: 0;
  overflow: hidden;
  pointer-events: none;
  flex: 0;
}
.user-card-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 4px;
}
.user-card-email {
  font-size: 13px;
  color: var(--text-muted);
  word-break: break-all;
}
.user-initial.visible {
  font-size: 18px;
  font-weight: 600;
  color: var(--text);
  width: 32px;
  height: 32px;
  min-width: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  border-radius: 50%;
}
.dropdown-arrow {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: var(--text-muted);
  transition: transform 0.2s, opacity 0.2s ease;
  opacity: 0;
  pointer-events: none;
}
.dropdown-arrow.visible {
  opacity: 1;
  pointer-events: auto;
}
.dropdown-arrow.open {
  transform: translateY(-50%) rotate(180deg);
}
.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  z-index: 10;
}
.dropdown-item {
  width: 100%;
  padding: 12px 20px;
  border: none;
  background: transparent;
  color: var(--text);
  font-size: 14px;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s;
}
.dropdown-item:hover {
  background: var(--bg-input);
}
.dropdown-enter-active, .dropdown-leave-active { transition: all 0.15s; }
.dropdown-enter-from, .dropdown-leave-to { opacity: 0; transform: translateY(-4px); }
.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.sidebar.collapsed .sidebar-nav {
  padding: 12px 8px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  text-decoration: none;
  font-size: 15px;
  transition: all 0.2s ease;
}
.sidebar.collapsed .nav-item {
  justify-content: center;
  padding: 12px;
}
.nav-item:hover {
  background: var(--bg-input);
  color: var(--text);
}
.nav-item.active {
  background: rgba(59, 130, 246, 0.1);
  color: var(--accent);
  font-weight: 500;
}
.nav-icon {
  font-size: 18px;
  flex-shrink: 0;
}
.nav-text {
  white-space: nowrap;
  overflow: hidden;
}
.sidebar.collapsed .nav-text {
  display: none;
}
.sidebar-footer {
  padding: 20px;
  border-top: 1px solid var(--border);
  position: relative;
}
.sidebar.collapsed .sidebar-footer {
  padding: 12px;
}
.btn-exit {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  background: transparent;
  border: 1px solid var(--danger);
  border-radius: var(--radius-sm);
  color: var(--danger);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-exit:hover {
  background: rgba(239, 68, 68, 0.1);
}
.sidebar.collapsed .btn-exit {
  padding: 10px;
  justify-content: center;
}
.btn-exit .btn-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}
.btn-exit-text {
  white-space: nowrap;
}
.sidebar.collapsed .btn-exit-text {
  display: none;
}
.btn-toggle {
  position: absolute;
  right: -14px;
  top: 50%;
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 50%;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: var(--shadow);
  z-index: 101;
}
.btn-toggle:hover {
  background: var(--bg-input);
  color: var(--accent);
  border-color: var(--accent);
}
.toggle-icon {
  width: 14px;
  height: 14px;
  transition: transform 0.25s ease;
}
.toggle-icon.collapsed {
  transform: rotate(180deg);
}
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  margin-left: 260px;
  transition: margin-left 0.25s ease;
}
.app-layout.sidebar-collapsed .main-area {
  margin-left: 72px;
}
.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 28px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
  transition: padding 0.25s ease, min-height 0.25s ease;
}
.main-header.collapsed {
  padding: 10px 28px;
  min-height: 48px;
}
.main-header .page-title,
.main-header .header-right > * {
  transition: opacity 0.25s ease;
}
.main-header.collapsed .page-title {
  opacity: 0;
  pointer-events: none;
  position: absolute;
  width: 0;
  overflow: hidden;
}
.main-header.collapsed .header-right > :not(.btn-header-toggle) {
  opacity: 0;
  pointer-events: none;
  position: absolute;
  width: 0;
  overflow: hidden;
}
.main-header.collapsed .header-right {
  margin-left: auto;
}
.btn-header-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  background: var(--bg-input);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-header-toggle:hover {
  border-color: var(--accent);
  color: var(--accent);
}
.toggle-icon-vertical {
  width: 18px;
  height: 18px;
  transition: transform 0.25s ease;
}
.toggle-icon-vertical.collapsed {
  transform: rotate(180deg);
}
.page-title {
  font-size: 24px;
  color: var(--text);
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.main-content {
  flex: 1;
  padding: 28px;
  overflow: auto;
}
</style>
