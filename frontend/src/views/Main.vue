<template>
  <div class="events-page">
    <div class="toolbar">
      <div class="search-card">
        <div class="search-row search-row-main">
          <div class="search-field">
            <label class="field-label">事件名称</label>
            <input v-model="filters.name" class="search-name" placeholder="输入关键词" />
          </div>
          <div class="search-field">
            <label class="field-label">日期范围</label>
            <div class="date-section">
              <div class="date-range">
                <label class="date-label">开始</label>
                <input
                  v-model="filters.startDate"
                  type="date"
                  class="date-input"
                  :max="filters.endDate || undefined"
                  @change="onStartDateChange"
                />
              </div>
              <div class="date-range">
                <label class="date-label">结束</label>
                <input
                  v-model="filters.endDate"
                  type="date"
                  class="date-input"
                  :min="filters.startDate || undefined"
                  @change="onEndDateChange"
                />
              </div>
            </div>
          </div>
        </div>
        <div class="search-actions">
          <button type="button" @click="clearAllFilters" class="btn-clear-all">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 102.13-9.36L1 10"/></svg>
            重置
          </button>
          <button :disabled="selectedIds.length === 0" @click="batchDelete" class="btn-batch-del">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
            {{ selectedIds.length > 0 ? `批量删除 (${selectedIds.length})` : '批量删除' }}
          </button>
          <button @click="showAdd = true" class="btn-add">+ 添加事件</button>
        </div>
      </div>
    </div>

    <div class="content">
      <div v-if="events.length" class="select-actions">
        <button type="button" @click="selectAll" class="btn-select">全选</button>
        <button type="button" @click="deselectAll" class="btn-select">全不选</button>
      </div>
      <div class="event-list">
        <div v-for="e in events" :key="e.eventId" :class="['event-item', 'event-' + getEventStatus(e)]">
          <input type="checkbox" v-model="selectedIds" :value="e.eventId" class="cyber-checkbox" />
          <div class="event-info">
            <span class="event-name">{{ e.name }}</span>
            <span class="event-countdown">{{ getCountdownText(e) }}</span>
            <span class="event-date">{{ e.year }}-{{ String(e.month).padStart(2,'0') }}-{{ String(e.day).padStart(2,'0') }}</span>
            <p v-if="e.description" class="event-desc">{{ e.description }}</p>
          </div>
          <div class="event-actions">
            <button @click="editEvent(e)" class="btn-edit">编辑</button>
            <button @click="deleteOne(e.eventId)" class="btn-del">删除</button>
          </div>
        </div>
        <p v-if="events.length === 0 && !loading" class="empty">暂无事件</p>
      </div>

      <div class="pagination">
        <div class="page-size">
          <span class="page-size-label">每页</span>
          <select v-model.number="pageSize" @change="onPageSizeChange" class="page-size-select">
            <option :value="8">8</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          <span class="page-size-label">项</span>
        </div>
        <button :disabled="page <= 1" @click="page--; loadEvents()">上一页</button>
        <span class="page-info">{{ page }} / {{ totalPages || 1 }}</span>
        <button :disabled="page >= totalPages" @click="page++; loadEvents()">下一页</button>
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <div v-if="showAdd || editing" class="modal" @click.self="closeModal">
      <div class="modal-content">
        <h3 class="modal-title">{{ editing ? '编辑事件' : '添加事件' }}</h3>
        <form @submit.prevent="submitForm">
          <div class="form-group">
            <label>事件名称</label>
            <input v-model="form.name" required maxlength="50" :class="{ 'input-error': nameExists }" @input="scheduleNameCheck" />
            <p v-if="nameExists" class="form-error">该日期下事件名称已存在</p>
          </div>
          <div class="form-group">
            <label>日期</label>
            <div class="date-picker-wrap">
              <input v-model="formDateStr" type="date" class="date-input-full" required @change="scheduleNameCheck" />
            </div>
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="form.description" rows="3"></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" @click="closeModal" class="btn-cancel">取消</button>
            <button type="submit" class="btn-submit" :disabled="!form.name?.trim() || nameExists">确定</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { eventApi } from '../api/request'
import { useConfirm } from '../composables/useConfirm'
import { useToast } from '../composables/useToast'
import ThemeToggle from '../components/ThemeToggle.vue'
import CoverSettings from '../components/CoverSettings.vue'

const router = useRouter()
const { confirm } = useConfirm()
const { show: showToast } = useToast()
const user = ref(JSON.parse(sessionStorage.getItem('user') || '{}'))
const events = ref([])
const page = ref(1)
const totalPages = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const selectedIds = ref([])
const showAdd = ref(false)
const editing = ref(null)
const nameExists = ref(false)
let nameCheckTimer = null

const filters = ref({ name: '', startDate: '', endDate: '' })
const today = new Date()
const form = ref({ name: '', year: today.getFullYear(), month: today.getMonth() + 1, day: today.getDate(), description: '' })

function toYMD(y, m, d) {
  if (!y || !m || !d) return ''
  return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
}
function parseYMD(s) {
  if (!s || !/^\d{4}-\d{2}-\d{2}$/.test(s)) return null
  const [y, m, d] = s.split('-').map(Number)
  return { year: y, month: m, day: d }
}
function getEventDateNum(e) {
  if (!e?.year || !e?.month || !e?.day) return 0
  return e.year * 10000 + e.month * 100 + e.day
}
function getEventStatus(e) {
  const eventDate = getEventDateNum(e)
  const today = new Date()
  const todayNum = today.getFullYear() * 10000 + (today.getMonth() + 1) * 100 + today.getDate()
  if (eventDate === todayNum) return 'today'
  if (eventDate > todayNum) {
    const eventD = new Date(e.year, e.month - 1, e.day)
    const todayD = new Date(today.getFullYear(), today.getMonth(), today.getDate())
    const daysUntil = Math.round((eventD - todayD) / (24 * 60 * 60 * 1000))
    if (daysUntil <= 3) return 'upcoming-soon'
    return 'upcoming'
  }
  return 'past'
}

function getCountdownText(e) {
  const todayD = new Date()
  todayD.setHours(0, 0, 0, 0)
  const eventDStart = new Date(e.year, e.month - 1, e.day)
  eventDStart.setHours(0, 0, 0, 0)
  const daysDiff = Math.round((eventDStart - todayD) / (24 * 60 * 60 * 1000))
  if (daysDiff === 0) return '今天'
  if (daysDiff > 0) return `剩余${daysDiff}天`
  return `已过期${-daysDiff}天`
}

const formDateStr = computed({
  get: () => toYMD(form.value.year, form.value.month, form.value.day),
  set: (v) => {
    const p = parseYMD(v)
    if (p) {
      form.value.year = p.year
      form.value.month = p.month
      form.value.day = p.day
    }
  }
})

function onStartDateChange() {
  const start = parseYMD(filters.value.startDate)
  const end = parseYMD(filters.value.endDate)
  if (start && end && start.year * 10000 + start.month * 100 + start.day > end.year * 10000 + end.month * 100 + end.day) {
    filters.value.endDate = filters.value.startDate
  }
}

function onEndDateChange() {
  const start = parseYMD(filters.value.startDate)
  const end = parseYMD(filters.value.endDate)
  if (start && end && end.year * 10000 + end.month * 100 + end.day < start.year * 10000 + start.month * 100 + start.day) {
    filters.value.startDate = filters.value.endDate
  }
}

function clearAllFilters() {
  filters.value.startDate = ''
  filters.value.endDate = ''
}

const userId = computed(() => user.value?.userId)

function doSearch() {
  page.value = 1
  loadEvents()
}
function onPageSizeChange() {
  page.value = 1
  loadEvents()
}
function selectAll() {
  selectedIds.value = events.value.map(e => e.eventId)
}
function deselectAll() {
  selectedIds.value = []
}
let searchTimer = null
watch(filters, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(doSearch, 300)
}, { deep: true })

async function loadEvents() {
  if (!userId.value) return
  loading.value = true
  try {
    const params = { userId: userId.value, page: page.value, size: pageSize.value }
    if (filters.value.name?.trim()) params.name = filters.value.name.trim()
    const start = parseYMD(filters.value.startDate)
    if (start) {
      params.startYear = start.year
      params.startMonth = start.month
      params.startDay = start.day
    }
    const end = parseYMD(filters.value.endDate)
    if (end) {
      params.endYear = end.year
      params.endMonth = end.month
      params.endDay = end.day
    }
    const res = await eventApi.list(params)
    if (res.code === 200 && res.data) {
      events.value = res.data.list || []
      totalPages.value = res.data.totalPages || 1
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function editEvent(e) {
  editing.value = e
  form.value = { name: e.name, year: e.year, month: e.month, day: e.day, description: e.description || '' }
}

function closeModal() {
  showAdd.value = false
  editing.value = null
  nameExists.value = false
  if (nameCheckTimer) clearTimeout(nameCheckTimer)
  const today = new Date()
  form.value = { name: '', year: today.getFullYear(), month: today.getMonth() + 1, day: today.getDate(), description: '' }
}

async function checkNameExists() {
  const name = form.value.name?.trim()
  const { year, month, day } = form.value
  if (!name || !userId.value || !year || !month || !day) {
    nameExists.value = false
    return
  }
  try {
    const params = { userId: userId.value, name, year, month, day }
    if (editing.value?.eventId) params.excludeEventId = editing.value.eventId
    const res = await eventApi.checkName(params)
    nameExists.value = res?.data?.exists ?? false
  } catch {
    nameExists.value = false
  }
}

function scheduleNameCheck() {
  if (nameCheckTimer) clearTimeout(nameCheckTimer)
  const name = form.value.name?.trim()
  const { year, month, day } = form.value
  if (!name || !year || !month || !day) {
    nameExists.value = false
    return
  }
  nameCheckTimer = setTimeout(checkNameExists, 400)
}

async function submitForm() {
  try {
    if (editing.value) {
      const res = await eventApi.update(editing.value.eventId, form.value)
      if (res.code === 200) {
        closeModal()
        loadEvents()
        showToast('编辑成功')
      } else alert(res.message)
    } else {
      const res = await eventApi.add({ userId: userId.value, ...form.value })
      if (res.code === 200) {
        user.value.operationCount = (user.value.operationCount || 0) + 1
        sessionStorage.setItem('user', JSON.stringify(user.value))
        closeModal()
        loadEvents()
        showToast('添加成功')
      } else alert(res.message)
    }
  } catch (e) {
    alert(e.message)
  }
}

async function deleteOne(id) {
  const ok = await confirm('确定要删除该事件吗？')
  if (!ok) return
  try {
    const res = await eventApi.delete(id, { userId: userId.value, opCount: user.value.operationCount || 0 })
    if (res.code === 200) {
      selectedIds.value = selectedIds.value.filter(x => x !== id)
      user.value.operationCount = Math.max(0, (user.value.operationCount || 0) - 1)
      sessionStorage.setItem('user', JSON.stringify(user.value))
      loadEvents()
      showToast('删除成功')
    } else alert(res.message)
  } catch (e) {
    alert(e.message)
  }
}

async function batchDelete() {
  if (selectedIds.value.length === 0) return
  const ok = await confirm(`确定要删除选中的 ${selectedIds.value.length} 条事件吗？`)
  if (!ok) return
  try {
    const res = await eventApi.batchDelete({
      eventIds: selectedIds.value,
      userId: userId.value,
      opCount: user.value.operationCount || 0
    })
    if (res.code === 200) {
      user.value.operationCount = Math.max(0, (user.value.operationCount || 0) - selectedIds.value.length)
      sessionStorage.setItem('user', JSON.stringify(user.value))
      selectedIds.value = []
      loadEvents()
      showToast('删除成功')
    } else alert(res.message)
  } catch (e) {
    alert(e.message)
  }
}

onMounted(() => {
  if (!userId.value) router.push('/login')
  else loadEvents()
})
</script>

<style scoped>
.events-page {
  max-width: 1200px;
  margin: 0 auto;
}
.toolbar {
  margin-bottom: 24px;
}
.search-card {
  padding: 20px 24px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}
.search-field {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px;
}
.field-label {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
  white-space: nowrap;
}
.search-name {
  width: 200px;
  height: 46px;
  padding: 0 16px;
  box-sizing: border-box;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
  background: var(--bg-input);
  color: var(--text);
  font-size: 14px;
  transition: border-color 0.2s ease;
}
.search-name:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15);
}
.search-name::placeholder {
  color: var(--text-muted);
}
.date-section {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.date-range {
  display: flex;
  align-items: center;
  gap: 10px;
}
.date-label {
  font-size: 12px;
  color: var(--text-muted);
  white-space: nowrap;
  min-width: 32px;
}
.date-input {
  height: 46px;
  padding: 0 14px;
  box-sizing: border-box;
  min-width: 150px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--bg-input);
  color: var(--text);
  font-size: 13px;
  font-family: inherit;
  transition: border-color 0.2s ease;
}
.date-input:hover {
  border-color: var(--border-hover);
}
.date-input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15);
}
.date-picker-wrap {
  max-width: 220px;
}
.date-input-full {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--bg-input);
  color: var(--text);
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.2s ease;
}
.date-input-full:hover {
  border-color: var(--border-hover);
}
.date-input-full:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15);
}
.search-actions {
  display: flex;
  flex-direction: row;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.search-actions button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.btn-icon {
  width: 18px;
  height: 18px;
  margin-right: 8px;
  flex-shrink: 0;
}
.btn-clear-all {
  padding: 10px 20px;
  background: var(--bg-input);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-clear-all:hover {
  border-color: var(--accent);
  color: var(--accent);
}
.btn-batch-del {
  padding: 10px 20px;
  background: var(--danger);
  border: none;
  border-radius: var(--radius-sm);
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-batch-del:hover:not(:disabled) {
  background: #dc2626;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.35);
}
.btn-batch-del:disabled {
  background: var(--bg-input);
  color: var(--text-muted);
  border: 1px solid var(--border);
  cursor: not-allowed;
  opacity: 0.8;
}
.btn-add {
  padding: 10px 24px;
  background: var(--accent);
  border: none;
  border-radius: var(--radius-sm);
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s ease;
}
.btn-add:hover {
  background: var(--accent-hover);
}
.select-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.btn-select {
  padding: 8px 16px;
  font-size: 13px;
  background: var(--bg-input);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}
.btn-select:hover:not(:disabled) {
  border-color: var(--accent);
  color: var(--accent);
}
.btn-select:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.event-list {
  margin-bottom: 24px;
}
.event-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px 24px;
  background: var(--bg-card);
  border-radius: var(--radius);
  margin-bottom: 12px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  transition: all 0.2s ease;
}
.event-item:hover {
  box-shadow: var(--shadow-lg);
}
.event-upcoming {
  border-left: 4px solid #22c55e;
}
.event-upcoming-soon {
  border-left: 5px solid #22c55e;
  background: linear-gradient(90deg, rgba(34, 197, 94, 0.12) 0%, transparent 100%), var(--bg-card);
  box-shadow: 0 0 0 1px rgba(34, 197, 94, 0.2);
}
.event-today {
  border-left: 5px solid #3b82f6;
  background: var(--bg-card);
  box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.2);
}
.event-past {
  border-left: 4px solid #94a3b8;
}
.cyber-checkbox {
  accent-color: var(--accent);
  margin-top: 4px;
  width: 22px;
  height: 22px;
  min-width: 22px;
  min-height: 22px;
  cursor: pointer;
}
.cyber-checkbox:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
.event-info {
  flex: 1;
}
.event-name {
  font-weight: 600;
  font-size: 15px;
  display: block;
  margin-bottom: 6px;
  color: var(--text);
}
.event-countdown {
  display: block;
  font-size: 17px;
  font-weight: 700;
  margin-bottom: 4px;
}
.event-item.event-today .event-countdown {
  color: #3b82f6;
}
.event-item.event-upcoming .event-countdown,
.event-item.event-upcoming-soon .event-countdown {
  color: #22c55e;
}
.event-item.event-past .event-countdown {
  color: var(--text-muted);
  font-weight: 500;
}
.event-date {
  color: var(--text-muted);
  font-size: 12px;
}
.event-desc {
  margin-top: 10px;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.5;
}
.event-actions {
  display: flex;
  gap: 8px;
}
.btn-edit,
.btn-del {
  padding: 8px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid;
  cursor: pointer;
  font-size: 13px;
  background: transparent;
  transition: all 0.2s ease;
}
.btn-edit {
  border-color: var(--accent);
  color: var(--accent);
}
.btn-edit:hover {
  background: rgba(59, 130, 246, 0.1);
}
.btn-del {
  border-color: var(--danger);
  color: var(--danger);
}
.btn-del:hover {
  background: rgba(239, 68, 68, 0.1);
}
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.page-size {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-size-label {
  color: var(--text-muted);
  font-size: 13px;
}
.page-size-select {
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--bg-input);
  color: var(--text);
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
}
.page-size-select:focus {
  outline: none;
  border-color: var(--accent);
}
.pagination button {
  padding: 10px 20px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text);
  cursor: pointer;
  transition: all 0.2s ease;
}
.pagination button:hover:not(:disabled) {
  border-color: var(--accent);
  color: var(--accent);
}
.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.page-info {
  color: var(--text-muted);
  font-size: 14px;
}
.empty {
  text-align: center;
  color: var(--text-muted);
  padding: 60px 20px;
  font-size: 14px;
}
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  background: var(--surface);
  border-radius: var(--radius);
  padding: 32px;
  width: 90%;
  max-width: 520px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-lg);
}
.modal-title {
  margin-bottom: 24px;
  color: var(--text);
  font-size: 18px;
}
.form-group {
  margin-bottom: 18px;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-muted);
  font-size: 13px;
}
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
  background: var(--bg-input);
  color: var(--text);
  font-family: 'Share Tech Mono', monospace;
  font-size: 14px;
  transition: border-color 0.3s ease;
}
.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--accent);
}
.form-group input[type='number'] {
  width: 80px;
  margin-right: 8px;
}
.form-group input.input-error {
  border-color: var(--danger);
}
.form-group input.input-error:focus {
  border-color: var(--danger);
  box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.15);
}
.form-error {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--danger);
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
}
.modal-actions button {
  padding: 12px 24px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}
.btn-cancel {
  background: transparent;
  border: 1px solid var(--border);
  color: var(--text-muted);
}
.btn-cancel:hover {
  border-color: var(--text);
  color: var(--text);
}
.btn-submit {
  background: var(--accent);
  border: none;
  color: white;
}
.btn-submit:hover:not(:disabled) {
  background: var(--accent-hover);
}
.btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
