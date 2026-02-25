<template>
  <Transition name="modal">
    <div v-if="show" class="modal-overlay" @click.self="handleCancel">
      <div class="modal-box">
        <h3 class="modal-title">调整背景展示区域</h3>
        <p class="modal-desc">拖动滑块调整图片在背景中的位置与缩放</p>
        <div class="preview-wrap">
          <div
            class="preview-bg"
            :style="previewStyle"
          ></div>
        </div>
        <div class="adjust-section">
          <div class="slider-row">
            <label>水平位置 {{ position.x }}%</label>
            <input
              type="range"
              min="0"
              max="100"
              v-model.number="position.x"
              class="slider"
            />
          </div>
          <div class="slider-row">
            <label>垂直位置 {{ position.y }}%</label>
            <input
              type="range"
              min="0"
              max="100"
              v-model.number="position.y"
              class="slider"
            />
          </div>
          <div class="slider-row">
            <label>缩放 {{ size }}%</label>
            <input
              type="range"
              min="50"
              max="200"
              v-model.number="size"
              class="slider"
            />
          </div>
        </div>
        <div class="modal-actions">
          <button type="button" @click="handleCancel" class="btn-cancel">取消</button>
          <button type="button" @click="handleConfirm" class="btn-confirm">应用</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  imageUrl: { type: String, default: '' },
  mode: { type: String, default: 'add' },
  initialPosition: { type: Object, default: () => ({ x: 50, y: 50 }) },
  initialSize: { type: Number, default: 100 }
})

const emit = defineEmits(['close', 'confirm-add', 'confirm-replace'])

const position = ref({ x: 50, y: 50 })
const size = ref(100)

watch(() => props.show, (visible) => {
  if (visible) {
    position.value = { x: props.initialPosition?.x ?? 50, y: props.initialPosition?.y ?? 50 }
    size.value = props.initialSize ?? 100
  }
}, { immediate: true })

const previewStyle = computed(() => ({
  backgroundImage: props.imageUrl ? `url(${props.imageUrl})` : 'none',
  backgroundPosition: `${position.value.x}% ${position.value.y}%`,
  backgroundSize: size.value === 100 ? 'cover' : `${size.value}%`,
  backgroundRepeat: 'no-repeat'
}))

function handleConfirm() {
  if (!props.imageUrl) return
  const data = {
    imageUrl: props.imageUrl,
    position: { ...position.value },
    size: size.value
  }
  if (props.mode === 'replace') {
    emit('confirm-replace', data)
  } else {
    emit('confirm-add', data)
  }
  emit('close')
}

function handleCancel() {
  emit('close')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1500;
}
.modal-box {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 24px;
  min-width: 360px;
  max-width: 420px;
  border: 1px solid var(--border);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.2);
}
.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 6px;
}
.modal-desc {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 16px;
}
.preview-wrap {
  width: 100%;
  height: 160px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--bg-input);
  border: 1px solid var(--border);
  margin-bottom: 20px;
}
.preview-bg {
  width: 100%;
  height: 100%;
}
.adjust-section {
  margin-bottom: 20px;
}
.slider-row {
  margin-bottom: 12px;
}
.slider-row:last-child {
  margin-bottom: 0;
}
.slider-row label {
  display: block;
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 6px;
}
.slider {
  width: 100%;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: var(--border);
  border-radius: 3px;
  outline: none;
}
.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--accent);
  cursor: pointer;
}
.slider::-moz-range-thumb {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--accent);
  cursor: pointer;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.btn-cancel {
  padding: 10px 20px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text);
  font-size: 15px;
  cursor: pointer;
}
.btn-cancel:hover {
  border-color: var(--accent);
  color: var(--accent);
}
.btn-confirm {
  padding: 10px 20px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--accent);
  color: white;
  font-size: 15px;
  cursor: pointer;
}
.btn-confirm:hover {
  filter: brightness(1.1);
}
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-active .modal-box,
.modal-leave-active .modal-box {
  transition: transform 0.2s ease;
}
.modal-enter-from .modal-box,
.modal-leave-to .modal-box {
  transform: scale(0.95);
}
</style>
