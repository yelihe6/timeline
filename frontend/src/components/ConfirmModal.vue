<template>
  <Transition name="modal">
    <div v-if="confirmState.show" class="confirm-overlay" @click.self="handleConfirm(false)">
      <div class="confirm-box">
        <p class="confirm-message">{{ confirmState.message }}</p>
        <div class="confirm-actions">
          <button type="button" @click="handleConfirm(false)" class="btn-cancel">取消</button>
          <button type="button" @click="handleConfirm(true)" class="btn-confirm">{{ confirmState.confirmText }}</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { useConfirm } from '../composables/useConfirm'

const { confirmState, handleConfirm } = useConfirm()
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1500;
}
.confirm-box {
  background: var(--surface);
  border-radius: var(--radius);
  padding: 28px;
  min-width: 320px;
  border: 1px solid var(--border);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}
.confirm-message {
  font-size: 16px;
  color: var(--text);
  margin-bottom: 24px;
  line-height: 1.5;
}
.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.btn-cancel {
  padding: 10px 20px;
  background: var(--bg-input);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text);
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-cancel:hover {
  border-color: var(--accent);
  color: var(--accent);
}
.btn-confirm {
  padding: 10px 20px;
  background: var(--danger);
  border: none;
  border-radius: var(--radius-sm);
  color: white;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s ease;
}
.btn-confirm:hover {
  background: #dc2626;
}
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-active .confirm-box,
.modal-leave-active .confirm-box {
  transition: transform 0.2s ease;
}
.modal-enter-from .confirm-box,
.modal-leave-to .confirm-box {
  transform: scale(0.95);
}
</style>
