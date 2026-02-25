<template>
  <div class="calendar-page">
    <div class="calendar-toolbar">
      <button type="button" @click="prevMonth" class="btn-nav">‹ 上月</button>
      <div v-if="!editingDate" class="calendar-title" @click="startEditDate">
        {{ displayYear }} 年 {{ displayMonth }} 月
      </div>
      <div
        v-else
        ref="editAreaRef"
        class="calendar-title-edit"
        @focusout="onEditAreaFocusOut"
      >
        <input
          ref="yearInputRef"
          v-model.number="editYear"
          type="number"
          min="1"
          max="9999"
          class="date-input-inline"
          @keydown.enter="applyEditDate"
        />
        <span>年</span>
        <input
          v-model.number="editMonth"
          type="number"
          min="1"
          max="12"
          class="date-input-inline month-input"
          @keydown.enter="applyEditDate"
        />
        <span>月</span>
      </div>
      <button type="button" @click="nextMonth" class="btn-nav">下月 ›</button>
    </div>

    <div class="calendar-grid">
      <div class="weekday-header">
        <span v-for="d in weekdays" :key="d" class="weekday">{{ d }}</span>
      </div>
      <div class="calendar-body">
        <div
          v-for="(week, wi) in calendarDays"
          :key="wi"
          class="calendar-row"
        >
          <div
            v-for="(day, di) in week"
            :key="di"
            :class="['day-cell', {
              other: day && day.otherMonth,
              today: day && day.isToday,
              hasEvents: day && getDayEvents(day).length > 0
            }]"
            @click="day && openDayModal(day)"
          >
            <span v-if="day" class="day-num">{{ day.day }}</span>
            <div v-if="day" class="day-events">
              <div
                v-for="(ev, ei) in getVisibleEvents(day)"
                :key="ev.eventId"
                class="day-event-item"
              >
                {{ ev.name }}
              </div>
              <div
                v-if="getDayEvents(day).length > maxVisible"
                class="day-more"
              >
                +{{ getDayEvents(day).length - maxVisible }} 项
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 当日事件弹窗 -->
    <div v-if="selectedDay" class="modal" @click.self="selectedDay = null">
      <div class="modal-content day-modal">
        <h3 class="modal-title">
          {{ selectedDay.year }}-{{ String(selectedDay.month).padStart(2, '0') }}-{{ String(selectedDay.day).padStart(2, '0') }} 的事件
        </h3>
        <div class="day-events-list">
          <div
            v-for="e in getDayEvents(selectedDay)"
            :key="e.eventId"
            :class="['day-event-card', 'event-' + getEventStatus(e)]"
          >
            <div class="event-card-name">{{ e.name }}</div>
            <p v-if="e.description" class="event-card-desc">{{ e.description }}</p>
          </div>
          <p v-if="getDayEvents(selectedDay).length === 0" class="empty-day">
            {{ selectedDay.otherMonth ? '该日期属于其他月份，请切换月份查看' : '当日暂无事件' }}
          </p>
        </div>
        <button type="button" @click="selectedDay = null" class="btn-close-modal">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { eventApi } from '../api/request'

const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const maxVisible = 3

const displayYear = ref(new Date().getFullYear())
const displayMonth = ref(new Date().getMonth() + 1)
const editingDate = ref(false)
const editYear = ref(displayYear.value)
const editMonth = ref(displayMonth.value)
const yearInputRef = ref(null)
const editAreaRef = ref(null)
const monthEvents = ref([])
const selectedDay = ref(null)
const loading = ref(false)

const user = ref(JSON.parse(sessionStorage.getItem('user') || '{}'))
const userId = computed(() => user.value?.userId)

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

const calendarDays = computed(() => {
  const year = displayYear.value
  const month = displayMonth.value
  const first = new Date(year, month - 1, 1)
  const last = new Date(year, month, 0)
  const firstWeekday = (first.getDay() + 6) % 7
  const lastDate = last.getDate()

  const days = []
  for (let i = 0; i < firstWeekday; i++) {
    const prev = new Date(year, month - 1, -firstWeekday + i + 1)
    days.push({
      year: prev.getFullYear(),
      month: prev.getMonth() + 1,
      day: prev.getDate(),
      otherMonth: true,
      isToday: false
    })
  }
  const today = new Date()
  const todayY = today.getFullYear()
  const todayM = today.getMonth() + 1
  const todayD = today.getDate()
  for (let d = 1; d <= lastDate; d++) {
    days.push({
      year,
      month,
      day: d,
      otherMonth: false,
      isToday: year === todayY && month === todayM && d === todayD
    })
  }
  const remaining = 42 - days.length
  for (let i = 0; i < remaining; i++) {
    const next = new Date(year, month, i + 1)
    days.push({
      year: next.getFullYear(),
      month: next.getMonth() + 1,
      day: next.getDate(),
      otherMonth: true,
      isToday: false
    })
  }

  const weeks = []
  for (let i = 0; i < days.length; i += 7) {
    weeks.push(days.slice(i, i + 7))
  }
  return weeks
})

function getDayKey(day) {
  return `${day.year}-${day.month}-${day.day}`
}

function getDayEvents(day) {
  if (!day) return []
  const key = getDayKey(day)
  return monthEvents.value.filter(e =>
    e.year === day.year && e.month === day.month && e.day === day.day
  )
}

function getVisibleEvents(day) {
  const list = getDayEvents(day)
  return list.slice(0, maxVisible)
}

async function loadMonthEvents() {
  if (!userId.value) return
  loading.value = true
  try {
    const year = displayYear.value
    const month = displayMonth.value
    const first = new Date(year, month - 1, 1)
    const last = new Date(year, month, 0)
    const firstWeekday = (first.getDay() + 6) % 7
    const lastDate = last.getDate()
    const firstVisible = new Date(year, month - 1, 1 - firstWeekday)
    const remaining = 42 - firstWeekday - lastDate
    const lastVisible = new Date(year, month, remaining)

    const res = await eventApi.list({
      userId: userId.value,
      page: 1,
      size: 100,
      startYear: firstVisible.getFullYear(),
      startMonth: firstVisible.getMonth() + 1,
      startDay: firstVisible.getDate(),
      endYear: lastVisible.getFullYear(),
      endMonth: lastVisible.getMonth() + 1,
      endDay: lastVisible.getDate()
    })
    if (res.code === 200 && res.data) {
      monthEvents.value = res.data.list || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function startEditDate() {
  editYear.value = displayYear.value
  editMonth.value = displayMonth.value
  editingDate.value = true
  nextTick(() => yearInputRef.value?.focus())
}
function applyEditDate() {
  let y = Math.floor(Number(editYear.value)) || displayYear.value
  let m = Math.floor(Number(editMonth.value)) || displayMonth.value
  y = Math.max(1, Math.min(9999, y))
  m = Math.max(1, Math.min(12, m))
  displayYear.value = y
  displayMonth.value = m
  editingDate.value = false
}
function onEditAreaFocusOut(e) {
  if (e.relatedTarget && editAreaRef.value?.contains(e.relatedTarget)) return
  applyEditDate()
}
function prevMonth() {
  if (displayMonth.value === 1) {
    displayYear.value--
    displayMonth.value = 12
  } else {
    displayMonth.value--
  }
}

function nextMonth() {
  if (displayMonth.value === 12) {
    displayYear.value++
    displayMonth.value = 1
  } else {
    displayMonth.value++
  }
}

function openDayModal(day) {
  selectedDay.value = day
}

watch([displayYear, displayMonth], loadMonthEvents)

onMounted(() => {
  loadMonthEvents()
})
</script>

<style scoped>
.calendar-page {
  max-width: 1000px;
  margin: 0 auto;
}
.calendar-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.btn-nav {
  padding: 8px 16px;
  background: var(--bg-input);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-nav:hover {
  border-color: var(--accent);
  color: var(--accent);
}
.calendar-title {
  font-size: 20px;
  color: var(--text);
  font-weight: 600;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  transition: background 0.2s ease;
}
.calendar-title:hover {
  background: rgba(59, 130, 246, 0.12);
}
.calendar-title-edit {
  display: flex;
  align-items: center;
  gap: 6px;
}
.date-input-inline {
  width: 72px;
  padding: 6px 10px;
  font-size: 18px;
  font-weight: 600;
  text-align: center;
  border: 1px solid var(--accent);
  border-radius: var(--radius-sm);
  background: var(--bg-input);
  color: var(--text);
}
.date-input-inline.month-input {
  width: 48px;
}
.date-input-inline:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
}
.calendar-grid {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
}
.weekday-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: var(--bg-input);
  border-bottom: 1px solid var(--border);
}
.weekday {
  padding: 12px;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-muted);
}
.calendar-body {
  display: flex;
  flex-direction: column;
}
.calendar-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  border-bottom: 1px solid var(--border);
}
.calendar-row:last-child {
  border-bottom: none;
}
.day-cell {
  min-height: 100px;
  padding: 8px;
  border-right: 1px solid var(--border);
  cursor: pointer;
  transition: background 0.2s ease;
}
.day-cell:last-child {
  border-right: none;
}
.day-cell:hover {
  background: var(--bg-input);
}
.day-cell.other .day-num {
  color: var(--text-muted);
  opacity: 0.6;
}
.day-cell.today {
  background: rgba(59, 130, 246, 0.08);
}
.day-cell.today .day-num {
  color: var(--accent);
  font-weight: 700;
}
.day-num {
  font-size: 14px;
  color: var(--text);
  margin-bottom: 6px;
  display: block;
}
.day-events {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.day-event-item {
  font-size: 12px;
  color: var(--text);
  padding: 4px 6px;
  background: rgba(59, 130, 246, 0.12);
  border-radius: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.day-more {
  font-size: 11px;
  color: var(--accent);
  padding: 2px 6px;
  cursor: default;
}
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content.day-modal {
  background: var(--surface);
  border-radius: var(--radius);
  padding: 32px;
  width: 90%;
  max-width: 480px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.modal-title {
  font-size: 18px;
  color: var(--text);
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}
.day-events-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.day-event-card {
  padding: 14px 16px;
  background: var(--bg-input);
  border-radius: var(--radius-sm);
  border-left: 4px solid var(--border);
}
.day-event-card.event-upcoming {
  border-left-color: #22c55e;
}
.day-event-card.event-upcoming-soon {
  border-left: 5px solid #22c55e;
  background: linear-gradient(90deg, rgba(34, 197, 94, 0.12) 0%, var(--bg-input) 100%);
  box-shadow: 0 0 0 1px rgba(34, 197, 94, 0.2);
}
.day-event-card.event-today {
  border-left-color: #3b82f6;
}
.day-event-card.event-past {
  border-left-color: #94a3b8;
}
.event-card-name {
  font-weight: 600;
  font-size: 15px;
  color: var(--text);
}
.event-card-desc {
  margin-top: 8px;
  font-size: 14px;
  color: var(--text-muted);
  line-height: 1.5;
}
.empty-day {
  text-align: center;
  color: var(--text-muted);
  padding: 40px 20px;
}
.btn-close-modal {
  padding: 10px 20px;
  background: var(--accent);
  border: none;
  border-radius: var(--radius-sm);
  color: white;
  font-size: 15px;
  cursor: pointer;
  align-self: flex-start;
}
.btn-close-modal:hover {
  background: var(--accent-hover);
}
</style>
