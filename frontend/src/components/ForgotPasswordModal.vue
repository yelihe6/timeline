<template>
  <Transition name="modal">
    <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
      <!-- 步骤1：邮箱 + 验证码 -->
      <div v-if="!showSetPassword" class="modal-box">
        <h3 class="modal-title">找回密码</h3>
        <p class="modal-desc">请输入注册时使用的邮箱，我们将发送6位数字验证码</p>
        <form @submit.prevent="handleVerify">
          <div class="form-group">
            <label>邮箱</label>
            <div class="input-with-btn">
              <input v-model="form.email" type="text" placeholder="请输入注册邮箱" :disabled="codeSent" />
              <button v-if="!codeSent" type="button" class="btn-send" :disabled="!canSendCode || sending" @click="sendCode">
                {{ sending ? '发送中...' : '发送验证码' }}
              </button>
              <span v-else class="countdown">{{ countdown > 0 ? countdown + 's 后重发' : '已发送' }}</span>
            </div>
            <p v-if="sendError" class="field-error">{{ sendError }}</p>
          </div>
          <div class="form-group">
            <label>验证码</label>
            <input v-model="form.code" type="text" placeholder="请输入6位数字验证码" maxlength="6" :disabled="!codeSent" />
            <p v-if="verifyError" class="field-error">{{ verifyError }}</p>
          </div>
          <div class="modal-actions">
            <button type="button" @click="$emit('close')" class="btn-cancel">取消</button>
            <button type="submit" class="btn-submit" :disabled="!codeSent || !form.code?.trim() || verifying">
              {{ verifying ? '验证中...' : '下一步' }}
            </button>
          </div>
        </form>
      </div>
      <!-- 步骤2：设置新密码（验证通过后弹出） -->
      <div v-else class="modal-box modal-box-password">
        <h3 class="modal-title">设置新密码</h3>
        <p class="modal-desc">请设置您的新密码</p>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>新密码</label>
            <div class="password-wrap">
              <svg class="pwd-lock" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
              <input v-model="form.newPassword" :type="showNewPassword ? 'text' : 'password'" placeholder="8-16位，含字母、数字、特殊字符" />
              <button v-if="form.newPassword" type="button" class="pwd-btn pwd-clear" @click="form.newPassword = ''" title="清除">×</button>
              <button type="button" class="pwd-btn pwd-toggle" @click="showNewPassword = !showNewPassword" :title="showNewPassword ? '隐藏' : '显示'">
                <svg v-if="showNewPassword" class="pwd-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                <svg v-else class="pwd-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>确认新密码</label>
            <div class="password-wrap">
              <svg class="pwd-lock" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
              <input v-model="form.confirmPassword" :type="showConfirmPassword ? 'text' : 'password'" placeholder="请再次输入新密码" />
              <button v-if="form.confirmPassword" type="button" class="pwd-btn pwd-clear" @click="form.confirmPassword = ''" title="清除">×</button>
              <button type="button" class="pwd-btn pwd-toggle" @click="showConfirmPassword = !showConfirmPassword" :title="showConfirmPassword ? '隐藏' : '显示'">
                <svg v-if="showConfirmPassword" class="pwd-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                <svg v-else class="pwd-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              </button>
            </div>
          </div>
          <p v-if="submitError" class="field-error">{{ submitError }}</p>
          <p class="field-hint">8-16位，需包含数字、英文字母和特殊字符</p>
          <div class="modal-actions">
            <button type="button" @click="showSetPassword = false" class="btn-cancel">返回</button>
            <button type="submit" class="btn-submit" :disabled="!canSubmit || submitting">
              {{ submitting ? '提交中...' : '确定' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { authApi } from '../api/request'
import { useToast } from '../composables/useToast'

const EMAIL_REG = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
const PASSWORD_REG = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&._])[A-Za-z\d@$!%*#?&._]{8,16}$/

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['close', 'success'])

const { show: showToast } = useToast()
const form = ref({ email: '', code: '', newPassword: '', confirmPassword: '' })
const sendError = ref('')
const verifyError = ref('')
const submitError = ref('')
const codeSent = ref(false)
const showSetPassword = ref(false)
const sending = ref(false)
const verifying = ref(false)
const submitting = ref(false)
const countdown = ref(0)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
let countdownTimer = null

const canSendCode = computed(() => {
  const e = form.value.email?.trim()
  return e && EMAIL_REG.test(e)
})

const canSubmit = computed(() => {
  const { email, code, newPassword, confirmPassword } = form.value
  if (!email?.trim() || !code?.trim() || !newPassword || !confirmPassword) return false
  if (newPassword.length < 8 || newPassword.length > 16) return false
  if (!PASSWORD_REG.test(newPassword)) return false
  if (newPassword !== confirmPassword) return false
  return true
})

watch(() => props.show, (v) => {
  if (!v) {
    form.value = { email: '', code: '', newPassword: '', confirmPassword: '' }
    sendError.value = ''
    verifyError.value = ''
    submitError.value = ''
    codeSent.value = false
    showSetPassword.value = false
    showNewPassword.value = false
    showConfirmPassword.value = false
    if (countdownTimer) clearInterval(countdownTimer)
    countdown.value = 0
  }
})

async function sendCode() {
  if (!canSendCode.value || sending.value) return
  sendError.value = ''
  sending.value = true
  try {
    const res = await authApi.sendResetCode({ email: form.value.email.trim() })
    if (res.code === 200) {
      codeSent.value = true
      countdown.value = 60
      showToast('验证码已发送')
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) clearInterval(countdownTimer)
      }, 1000)
    } else {
      sendError.value = res.message || '发送失败'
    }
  } catch (e) {
    sendError.value = e.message || '发送失败'
  } finally {
    sending.value = false
  }
}

async function handleVerify() {
  if (!form.value.code?.trim() || verifying.value) return
  verifyError.value = ''
  verifying.value = true
  try {
    const res = await authApi.verifyResetCode({
      email: form.value.email.trim(),
      code: form.value.code.trim()
    })
    if (res.code === 200) {
      showSetPassword.value = true
    } else {
      verifyError.value = res.message || '验证失败'
    }
  } catch (e) {
    verifyError.value = e.message || '验证失败'
  } finally {
    verifying.value = false
  }
}

async function handleSubmit() {
  if (!canSubmit.value || submitting.value) return
  submitError.value = ''
  submitting.value = true
  try {
    const res = await authApi.resetPassword({
      email: form.value.email.trim(),
      code: form.value.code.trim(),
      newPassword: form.value.newPassword
    })
    if (res.code === 200) {
      showToast('密码重置成功')
      emit('success')
      emit('close')
    } else {
      submitError.value = res.message || '重置失败'
    }
  } catch (e) {
    submitError.value = e.message || '重置失败'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 24px;
  width: 100%;
  max-width: 400px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border);
}
.modal-title {
  font-size: 20px;
  margin-bottom: 8px;
  color: var(--text);
}
.modal-desc {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 20px;
}
.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  font-size: 14px;
  margin-bottom: 6px;
  color: var(--text);
}
.form-group input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--bg-input);
  color: var(--text);
  font-size: 15px;
}
.form-group input:disabled {
  opacity: 0.7;
}
.password-wrap {
  position: relative;
}
.password-wrap input {
  padding-left: 40px;
  padding-right: 72px;
}
.pwd-lock {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: var(--text-muted);
  pointer-events: none;
}
.pwd-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 20px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}
.pwd-btn:hover {
  color: var(--text);
}
.pwd-clear {
  right: 40px;
}
.pwd-toggle {
  right: 8px;
}
.pwd-icon {
  width: 18px;
  height: 18px;
}
.input-with-btn {
  display: flex;
  gap: 8px;
  align-items: center;
}
.input-with-btn input {
  flex: 1;
}
.btn-send {
  padding: 12px 16px;
  background: var(--accent);
  border: none;
  border-radius: var(--radius-sm);
  color: white;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
}
.btn-send:hover:not(:disabled) {
  background: var(--accent-hover);
}
.btn-send:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.countdown {
  font-size: 13px;
  color: var(--text-muted);
  white-space: nowrap;
}
.field-error {
  color: var(--danger);
  font-size: 13px;
  margin-top: 6px;
}
.field-hint {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 12px;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
.btn-cancel {
  padding: 10px 20px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  cursor: pointer;
}
.btn-cancel:hover {
  border-color: var(--text);
  color: var(--text);
}
.btn-submit {
  padding: 10px 20px;
  background: var(--accent);
  border: none;
  border-radius: var(--radius-sm);
  color: white;
  cursor: pointer;
}
.btn-submit:hover:not(:disabled) {
  background: var(--accent-hover);
}
.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.modal-enter-active, .modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
