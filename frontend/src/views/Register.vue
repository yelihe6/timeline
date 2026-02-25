<template>
  <div class="register-page">
    <div class="top-actions">
      <CoverSettings />
      <ThemeToggle />
    </div>
    <div class="register-card">
      <h1 class="page-title">注册账号</h1>
      <p class="subtitle">创建您的时间线账号</p>
      <p class="email-notice">邮箱务必是可使用的邮箱</p>
      <form @submit.prevent="handleRegister" novalidate>
        <div class="form-group">
          <label class="field-label">邮箱</label>
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
            <input v-model="email" type="text" placeholder="请输入邮箱" autocomplete="email" :class="{ invalid: emailError }" />
          </div>
          <p v-if="emailError" class="field-error">{{ emailError }}</p>
          <p v-else-if="emailChecking" class="field-hint">正在检测...</p>
          <p v-else-if="email && emailAvailable === false" class="field-error">该邮箱已被注册</p>
          <p v-else-if="email && emailAvailable === true && !emailError" class="field-ok">邮箱可用</p>
        </div>
        <div class="form-group">
          <label class="field-label">密码</label>
          <div class="password-wrap">
            <svg class="pwd-lock" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
            <input v-model="password" :type="showPassword ? 'text' : 'password'" placeholder="至少8位字符" :class="{ invalid: passwordError }" />
            <button v-if="password" type="button" class="pwd-btn pwd-clear" @click="password = ''" title="清除">×</button>
            <button type="button" class="pwd-btn pwd-toggle" @click="showPassword = !showPassword" :title="showPassword ? '隐藏' : '显示'">
              <svg v-if="showPassword" class="pwd-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              <svg v-else class="pwd-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            </button>
          </div>
          <ul class="pwd-rules">
            <li :class="{ met: pwdRules.lengthMet }"><span class="rule-dot"></span>8-16位</li>
            <li :class="{ met: pwdRules.hasNumber }"><span class="rule-dot"></span>包含数字</li>
            <li :class="{ met: pwdRules.hasLetter }"><span class="rule-dot"></span>包含英文字母</li>
            <li :class="{ met: pwdRules.hasSpecial }"><span class="rule-dot"></span>包含特殊字符</li>
          </ul>
        </div>
        <div class="form-group">
          <label class="field-label">确认密码</label>
          <div class="password-wrap">
            <svg class="pwd-lock" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
            <input v-model="passwordConfirm" :type="showPasswordConfirm ? 'text' : 'password'" placeholder="请再次输入密码" :class="{ invalid: passwordMatchError }" />
            <button v-if="passwordConfirm" type="button" class="pwd-btn pwd-clear" @click="passwordConfirm = ''" title="清除">×</button>
            <button type="button" class="pwd-btn pwd-toggle" @click="showPasswordConfirm = !showPasswordConfirm" :title="showPasswordConfirm ? '隐藏' : '显示'">
              <svg v-if="showPasswordConfirm" class="pwd-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              <svg v-else class="pwd-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            </button>
          </div>
          <p v-if="passwordMatchError" class="field-error">{{ passwordMatchError }}</p>
        </div>
        <div class="form-group captcha-row">
          <input v-model="verifyCode" type="text" placeholder="验证码" maxlength="4" required />
          <img :src="captchaSrc" alt="验证码" class="captcha-img" @click="refreshCaptcha" />
        </div>
        <p v-if="message" :class="messageType">{{ message }}</p>
        <button type="submit" class="btn-primary" :disabled="!canSubmit">注 册</button>
      </form>
      <div class="links">
        <router-link to="/login" class="link">已有账号？去登录</router-link>
        <button type="button" class="link link-btn" @click="showForgot = true">忘记密码？</button>
      </div>
    </div>
    <ForgotPasswordModal :show="showForgot" @close="showForgot = false" @success="showForgot = false" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi, captchaUrl } from '../api/request'
import ThemeToggle from '../components/ThemeToggle.vue'
import CoverSettings from '../components/CoverSettings.vue'
import ForgotPasswordModal from '../components/ForgotPasswordModal.vue'
import { useToast } from '../composables/useToast'

const router = useRouter()
const showForgot = ref(false)
const { show: showToast } = useToast()

const EMAIL_REG = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
const PASSWORD_REG = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&._])[A-Za-z\d@$!%*#?&._]{8,16}$/

const email = ref('')
const password = ref('')
const passwordConfirm = ref('')
const showPassword = ref(false)
const showPasswordConfirm = ref(false)
const verifyCode = ref('')
const message = ref('')
const messageType = ref('success')
const captchaSrc = ref('')
const emailChecking = ref(false)
const emailAvailable = ref(null)
let emailCheckTimer = null

const emailError = computed(() => {
  if (!email.value) return ''
  if (!EMAIL_REG.test(email.value.trim())) return '请输入有效的邮箱格式'
  return ''
})

const pwdRules = computed(() => {
  const p = password.value
  return {
    lengthMet: p.length >= 8 && p.length <= 16,
    hasNumber: /\d/.test(p),
    hasLetter: /[A-Za-z]/.test(p),
    hasSpecial: /[@$!%*#?&._]/.test(p)
  }
})

const passwordError = computed(() => {
  if (!password.value) return ''
  if (password.value.length < 8 || password.value.length > 16) return '密码长度为8-16位'
  if (!PASSWORD_REG.test(password.value)) return '需包含数字、英文字母和特殊字符'
  return ''
})

const passwordMatchError = computed(() => {
  if (!passwordConfirm.value) return ''
  if (password.value !== passwordConfirm.value) return '两次输入的密码不一致'
  return ''
})

const canSubmit = computed(() => {
  return email.value && !emailError.value && emailAvailable.value === true &&
    password.value && !passwordError.value &&
    passwordConfirm.value && !passwordMatchError.value &&
    verifyCode.value
})

async function checkEmailAvailable() {
  const e = email.value?.trim()
  if (!e || emailError.value) {
    emailAvailable.value = null
    return
  }
  emailChecking.value = true
  emailAvailable.value = null
  try {
    const res = await authApi.checkEmail(e)
    emailAvailable.value = res.data === true
  } catch {
    emailAvailable.value = null
  } finally {
    emailChecking.value = false
  }
}

watch(email, () => {
  emailAvailable.value = null
  if (emailCheckTimer) clearTimeout(emailCheckTimer)
  if (!email.value || emailError.value) return
  emailCheckTimer = setTimeout(checkEmailAvailable, 500)
})

function refreshCaptcha() {
  captchaSrc.value = captchaUrl + '?t=' + Date.now()
}

onMounted(() => {
  refreshCaptcha()
})

async function handleRegister() {
  message.value = ''
  if (!canSubmit.value) return
  try {
    const res = await authApi.register({
      email: email.value,
      password: password.value,
      verifyCode: verifyCode.value
    })
    if (res.code === 200) {
      showToast('注册成功')
      setTimeout(() => router.push('/login'), 800)
    } else {
      messageType.value = 'error'
      message.value = res.message || '注册失败'
      refreshCaptcha()
    }
  } catch (e) {
    messageType.value = 'error'
    message.value = e.message || '注册失败'
    refreshCaptcha()
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
}
.top-actions {
  position: absolute;
  top: 24px;
  right: 24px;
  display: flex;
  gap: 12px;
  align-items: center;
}
.register-card {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 48px 40px;
  width: 100%;
  max-width: 480px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border);
}
h1 {
  font-size: 24px;
  text-align: center;
  margin-bottom: 8px;
  color: var(--text);
}
.subtitle {
  text-align: center;
  color: var(--text-muted);
  margin-bottom: 32px;
  font-size: 14px;
}
.form-group {
  margin-bottom: 20px;
}
.form-group input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--bg-input);
  color: var(--text);
  font-size: 15px;
  transition: border-color 0.2s ease;
}
.form-group input::placeholder {
  color: var(--text-muted);
}
.form-group input:focus {
  outline: none;
  border-color: var(--accent);
}
.form-group input.invalid {
  border-color: var(--danger);
}
.field-error {
  color: var(--danger);
  font-size: 13px;
  margin-top: 6px;
}
.field-hint {
  color: var(--text-muted);
  font-size: 13px;
  margin-top: 6px;
}
.field-ok {
  color: var(--success);
  font-size: 13px;
  margin-top: 6px;
}
.form-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.field-label {
  display: block;
  font-size: 14px;
  color: var(--text);
  margin-bottom: 8px;
}
.input-wrap {
  position: relative;
}
.input-wrap input {
  padding-left: 40px;
}
.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: var(--text-muted);
  pointer-events: none;
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
.pwd-rules {
  list-style: none;
  margin: 10px 0 0;
  padding: 0;
}
.pwd-rules li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 6px;
}
.pwd-rules li.met {
  color: var(--success);
}
.pwd-rules li .rule-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  border: 1.5px solid currentColor;
  flex-shrink: 0;
}
.pwd-rules li.met .rule-dot {
  background: currentColor;
  border-color: currentColor;
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
.captcha-row {
  display: flex;
  gap: 12px;
}
.captcha-row input {
  flex: 1;
}
.captcha-img {
  height: 48px;
  cursor: pointer;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
}
.error { color: var(--danger); }
.success { color: var(--success); }
p { font-size: 14px; margin-bottom: 12px; }
.btn-primary {
  width: 100%;
  padding: 14px;
  background: var(--accent);
  border: none;
  border-radius: var(--radius-sm);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}
.btn-primary:hover:not(:disabled) {
  background: var(--accent-hover);
}
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.email-notice {
  font-size: 13px;
  color: var(--text-muted);
  margin: -16px 0 20px;
  text-align: center;
}
.links {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.link {
  color: var(--accent);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s ease;
}
.link:hover {
  color: var(--accent-hover);
}
.link-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  font-size: 14px;
  font-family: inherit;
}
</style>
