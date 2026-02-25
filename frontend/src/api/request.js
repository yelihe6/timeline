import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

request.interceptors.response.use(
  res => res.data,
  err => {
    const msg = err.response?.data?.message || err.message || '请求失败'
    return Promise.reject(new Error(msg))
  }
)

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  getUser: (id) => request.get(`/auth/user/${id}`),
  updateNickname: (id, nickname) => request.put(`/auth/user/${id}/nickname`, { nickname }),
  updateUserInfo: (id, data) => request.put(`/auth/user/${id}/info`, data),
  changePassword: (id, data) => request.put(`/auth/user/${id}/password`, data),
  checkEmail: (email) => request.get('/auth/check-email', { params: { email } }),
  sendResetCode: (data) => request.post('/auth/forgot-password/send-code', data, { timeout: 30000 }),
  verifyResetCode: (data) => request.post('/auth/forgot-password/verify-code', data),
  resetPassword: (data) => request.post('/auth/forgot-password/reset', data)
}

export const eventApi = {
  list: (params) => request.get('/events', { params }),
  checkName: (params) => request.get('/events/check-name', { params }),
  add: (data) => request.post('/events', data),
  update: (id, data) => request.put(`/events/${id}`, data),
  delete: (id, params) => request.delete(`/events/${id}`, { params }),
  batchDelete: (data) => request.post('/events/batch-delete', data),
  batchDeleteByRange: (data) => request.post('/events/batch-delete-by-range', data)
}

export const captchaUrl = '/api/captcha'
