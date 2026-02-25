<template>
  <Transition name="toast">
    <div v-if="toast.show" class="toast">
      {{ toast.message }}
    </div>
  </Transition>
</template>

<script setup>
import { useToast } from '../composables/useToast'

const { toast } = useToast()
</script>

<style scoped>
.toast {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 14px 28px;
  background: var(--success);
  color: white;
  font-size: 15px;
  font-weight: 500;
  border-radius: var(--radius-sm);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 2000;
}
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}
</style>
