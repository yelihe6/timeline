<template>
  <Transition name="modal">
    <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-box">
        <h3 class="modal-title">编辑个人信息</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>邮箱</label>
            <input :value="form.email" type="text" disabled class="input-disabled" />
          </div>
          <div class="form-group">
            <label>昵称</label>
            <input v-model="form.nickname" type="text" placeholder="昵称" maxlength="50" />
          </div>
          <div class="form-group">
            <label>电话号码</label>
            <input v-model="form.phone" type="text" placeholder="请输入电话号码" maxlength="20" />
          </div>
          <div class="modal-actions">
            <button type="button" @click="$emit('close')" class="btn-cancel">取消</button>
            <button type="submit" class="btn-submit" :disabled="!form.nickname?.trim()">保存</button>
          </div>
        </form>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  user: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['close', 'saved'])

const form = ref({ nickname: '', email: '', phone: '' })

watch(() => [props.show, props.user], () => {
  if (props.show && props.user) {
    form.value = {
      nickname: props.user.nickname || '',
      email: props.user.email || '',
      phone: props.user.phone || ''
    }
  }
}, { immediate: true })

async function handleSubmit() {
  emit('saved', { nickname: form.value.nickname.trim(), phone: form.value.phone?.trim() || '' })
  emit('close')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
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
  border: 1px solid var(--border);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}
.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 20px;
}
.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 6px;
}
.form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--bg-input);
  color: var(--text);
  font-size: 15px;
}
.form-group input:focus {
  outline: none;
  border-color: var(--accent);
}
.input-disabled {
  background: var(--bg);
  color: var(--text-muted);
  cursor: not-allowed;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
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
.btn-submit {
  padding: 10px 20px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--accent);
  color: white;
  font-size: 15px;
  cursor: pointer;
}
.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.modal-enter-active, .modal-leave-active { transition: opacity 0.2s; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
