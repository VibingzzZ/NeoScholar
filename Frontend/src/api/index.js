import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const msg = error.response?.data?.message || error.message || '请求失败'
    console.error('[HTTP Error]', msg)
    return Promise.reject(error)
  }
)

export function get(url, params) {
  return http.get(url, { params }).then((res) => res.data)
}

export function post(url, data) {
  return http.post(url, data).then((res) => res.data)
}

export default http