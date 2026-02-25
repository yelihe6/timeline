<template>
  <div class="cover-settings">
    <button class="cover-trigger" @click="open = !open" title="背景设置">
      <span class="icon">🖼</span>
      <span class="label">背景</span>
    </button>
    <Transition name="dropdown">
      <div v-if="open" class="cover-panel" @click.stop>
        <div class="panel-header">自定义背景</div>
        <div class="panel-section">
          <label>背景图片</label>
          <div class="custom-image-area">
            <div v-if="coverImages.length" class="image-list">
              <div
                v-for="(item, idx) in coverImages"
                :key="idx"
                class="image-thumb-wrap"
              >
                <div class="image-thumb" :style="{ backgroundImage: `url(${item.url})` }"></div>
                <div class="image-thumb-actions">
                  <button type="button" @click="openAdjustModal(item, idx)" class="btn-thumb">调整</button>
                  <button type="button" @click="removeCoverImage(idx)" class="btn-thumb btn-remove">删除</button>
                </div>
              </div>
            </div>
            <div v-else class="image-placeholder">暂无图片</div>
            <div class="image-actions">
              <div class="btn-add-img" @click="triggerFileInput">
                添加图片
              </div>
              <input ref="fileInputRef" type="file" accept="image/*" multiple @change="onFileSelect" class="file-input-hidden" />
              <button v-if="coverImages.length" @click="clearCoverImages" class="btn-remove-img">全部移除</button>
            </div>
          </div>
        </div>
        <div class="panel-section" v-if="coverImages.length > 1">
          <label>轮播间隔 {{ coverCarouselInterval }} 秒（0 为关闭）</label>
          <input
            type="range"
            min="0"
            max="60"
            :value="coverCarouselInterval"
            @input="setCoverCarouselInterval($event.target.value)"
            class="opacity-slider"
          />
        </div>
        <div class="panel-section" v-if="coverImages.length">
          <label>透明度 {{ Math.round(coverOpacity * 100) }}%</label>
          <input
            type="range"
            min="0"
            max="100"
            :value="coverOpacity * 100"
            @input="setCoverOpacity($event.target.value / 100)"
            class="opacity-slider"
          />
        </div>
      </div>
    </Transition>
    <div v-if="open" class="cover-backdrop" @click="open = false"></div>
    <CoverAdjustModal
      :show="adjustModalOpen"
      :image-url="pendingImageUrl"
      :mode="adjustMode"
      :initial-position="pendingPosition"
      :initial-size="pendingSize"
      @close="closeAdjustModal"
      @confirm-add="onAdjustConfirmAdd"
      @confirm-replace="onAdjustConfirmReplace"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useCover } from '../composables/useCover'
import CoverAdjustModal from './CoverAdjustModal.vue'

const open = ref(false)
const fileInputRef = ref(null)
const adjustModalOpen = ref(false)
const pendingImageUrl = ref('')
const pendingIndex = ref(-1)
const pendingPosition = ref({ x: 50, y: 50 })
const pendingSize = ref(100)
const adjustMode = ref('add')
const {
  coverOpacity,
  coverImages,
  coverCarouselInterval,
  setCoverOpacity,
  setCoverCarouselInterval,
  addCoverImage,
  updateCoverImageSettings,
  removeCoverImage,
  clearCoverImages
} = useCover()

function triggerFileInput() {
  fileInputRef.value?.click()
}

function openAdjustModal(itemOrUrl, index = -1) {
  const isReplace = index >= 0
  if (typeof itemOrUrl === 'string') {
    pendingImageUrl.value = itemOrUrl
    pendingPosition.value = { x: 50, y: 50 }
    pendingSize.value = 100
  } else {
    pendingImageUrl.value = itemOrUrl?.url || ''
    pendingPosition.value = { ...(itemOrUrl?.position || { x: 50, y: 50 }) }
    pendingSize.value = itemOrUrl?.size ?? 100
  }
  pendingIndex.value = index
  adjustMode.value = isReplace ? 'replace' : 'add'
  adjustModalOpen.value = true
}

function closeAdjustModal() {
  adjustModalOpen.value = false
  pendingImageUrl.value = ''
  pendingIndex.value = -1
}

async function onAdjustConfirmAdd(data) {
  await addCoverImage(data.imageUrl, data.position, data.size)
  closeAdjustModal()
}

function onAdjustConfirmReplace(data) {
  const idx = pendingIndex.value
  if (idx >= 0 && idx < coverImages.value.length) {
    updateCoverImageSettings(idx, data.position, data.size)
  }
  closeAdjustModal()
}

function onFileSelect(e) {
  const files = e.target.files
  if (!files?.length) return
  let processed = 0
  const toAdd = []
  const handleFile = (file, idx) => {
    if (!file?.type?.startsWith('image/')) {
      tryNext(idx)
      return
    }
    const reader = new FileReader()
    reader.onload = (ev) => {
      const dataUrl = ev.target?.result
      if (dataUrl) toAdd.push(dataUrl)
      tryNext(idx)
    }
    reader.readAsDataURL(file)
  }
  async function tryNext(idx) {
    processed++
    if (idx + 1 < files.length) {
      handleFile(files[idx + 1], idx + 1)
    } else if (toAdd.length === 1) {
      openAdjustModal(toAdd[0], -1)
    } else if (toAdd.length > 1) {
      for (const url of toAdd) {
        await addCoverImage(url, { x: 50, y: 50 }, 100)
      }
    }
  }
  handleFile(files[0], 0)
  e.target.value = ''
}
</script>

<style scoped>
.cover-settings {
  position: relative;
}
.cover-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: var(--shadow);
}
.cover-trigger:hover {
  border-color: var(--accent);
  color: var(--accent);
}
.icon {
  font-size: 16px;
}
.cover-backdrop {
  position: fixed;
  inset: 0;
  z-index: 99;
}
.cover-panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 280px;
  max-width: 360px;
  padding: 20px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-lg);
  z-index: 100;
}
.panel-header {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 16px;
}
.panel-section {
  margin-bottom: 16px;
}
.panel-section:last-child {
  margin-bottom: 0;
}
.panel-section label {
  display: block;
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 8px;
}
.custom-image-area {
  border: 1px dashed var(--border);
  border-radius: var(--radius-sm);
  padding: 12px;
  background: var(--bg-input);
}
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
  max-height: 140px;
  overflow-y: auto;
}
.image-thumb-wrap {
  flex: 0 0 70px;
}
.image-thumb {
  width: 70px;
  height: 52px;
  border-radius: var(--radius-sm);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  border: 1px solid var(--border);
}
.image-thumb-actions {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}
.btn-thumb {
  flex: 1;
  padding: 4px 6px;
  font-size: 11px;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
}
.btn-thumb:hover {
  border-color: var(--accent);
  color: var(--accent);
}
.btn-thumb.btn-remove:hover {
  border-color: var(--danger);
  color: var(--danger);
}
.image-placeholder {
  width: 100%;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 13px;
  border: 1px dashed var(--border);
  border-radius: var(--radius-sm);
  margin-bottom: 10px;
}
.image-actions {
  display: flex;
  gap: 8px;
  align-items: stretch;
}
.btn-add-img {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  padding: 0 14px;
  border: 1px solid var(--accent);
  border-radius: var(--radius-sm);
  color: var(--accent);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: transparent;
  box-sizing: border-box;
}
.file-input-hidden {
  display: none;
}
.btn-remove-img {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  padding: 0 14px;
  border: 1px solid var(--danger);
  border-radius: var(--radius-sm);
  color: var(--danger);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: transparent;
  box-sizing: border-box;
  line-height: 1;
}
.btn-add-img:hover,
.btn-remove-img:hover {
  background: rgba(59, 130, 246, 0.1);
}
.btn-remove-img:hover {
  background: rgba(239, 68, 68, 0.1);
}
.opacity-slider {
  width: 100%;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: var(--border);
  border-radius: 3px;
  outline: none;
}
.opacity-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--accent);
  cursor: pointer;
}
.opacity-slider::-moz-range-thumb {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--accent);
  cursor: pointer;
}
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
