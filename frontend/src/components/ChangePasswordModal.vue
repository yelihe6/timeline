<template>
  <Transition name="modal">
    <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-box">
        <h3 class="modal-title">修改密码</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>原密码</label>
            <input v-model="form.oldPassword" type="password" placeholder="请输入原密码" />
          </div>
          <div class="form-group">
            <label>新密码</label>
            <input v-model="form.newPassword" type="password" placeholder="8-16位，含字母、数字、特殊字符" />
          </div>
          <div class="form-group">
            <label>确认新密码</label>
            <input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" />
          </div>
          <p v-if="error" class="field-error">{{ error }}</p>
          <p v-else class="field-hint">8-16位，需包含数字、英文字母和特殊字符</p>
          <div class="modal-actions">
            <button type="button" @click="$emit('close')" class="btn-cancel">取消</button>
            <button type="submit" class="btn-submit" :disabled="!canSubmit">确定</button>
          </div>
        </form>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const PASSWORD_REG = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&._])[A-Za-z\d@$!%*#?&._]{8,16}$/

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['close', 'saved'])

const form = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const error = ref('')

const canSubmit = computed(() => {
  return form.value.oldPassword && form.value.newPassword && form.value.confirmPassword && !error.value
})

watch(() => props.show, (v) => {
  if (!v) {
    form.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    error.value = ''
  }
})

watch(() => [form.value.newPassword, form.value.confirmPassword], () => {
  if (!form.value.newPassword) {
    error.value = ''
    return
  }
  if (form.value.newPassword.length < 8 || form.value.newPassword.length > 16) {
    error.value = '密码长度为8-16位'
    return
  }
  if (!PASSWORD_REG.test(form.value.newPassword)) {
    error.value = '需包含数字、英文字母和特殊字符'
    return
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return
  }
  error.value = ''
})

async function handleSubmit() {
  if (!canSubmit.value) return
  emit('saved', {
    oldPassword: form.value.oldPassword,
    newPassword: form.value.newPassword
  })
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
.field-error { color: var(--danger); font-size: 13px; margin-bottom: 8px; }
.field-hint { color: var(--text-muted); font-size: 13px; margin-bottom: 8px; }
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
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
