import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动携带 JWT Token
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：处理 401 未授权
http.interceptors.response.use(
  (response) => response,
  (error) => {
    const msg = error.response?.data?.message || error.message || '请求失败'
    console.error('[HTTP Error]', msg)

    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token')
      ElMessage.error('登录已过期，请重新登录')
      window.location.href = '/login'
    }

    return Promise.reject(error)
  }
)

export function get(url, params) {
  return http.get(url, { params }).then((res) => res.data)
}

export function put(url, data) {
  return http.put(url, data).then((res) => res.data)
}

export function post(url, data) {
  return http.post(url, data).then((res) => res.data)
}

export default http