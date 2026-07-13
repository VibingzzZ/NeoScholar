import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 恢复登录态
  const savedToken = localStorage.getItem('token')
  const savedUser = (() => {
    try { return JSON.parse(localStorage.getItem('user') || 'null') } catch { return null }
  })()

  const user = ref(savedUser)
  const token = ref(savedToken || '')
  const sidebarCollapsed = ref(false)

  const isLoggedIn = computed(() => !!token.value)

  function login(userInfo, accessToken) {
    user.value = userInfo
    token.value = accessToken
    localStorage.setItem('token', accessToken)
    localStorage.setItem('user', JSON.stringify(userInfo))
  }

  function logout() {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return { user, token, sidebarCollapsed, isLoggedIn, login, logout, toggleSidebar }
})