import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getActiveProfile } from '@/api/profile'

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 恢复登录态
  const savedToken = localStorage.getItem('token')
  const savedUser = (() => {
    try { return JSON.parse(localStorage.getItem('user') || 'null') } catch { return null }
  })()

  const user = ref(savedUser)
  const token = ref(savedToken || '')
  const sidebarCollapsed = ref(false)
  const activeProfile = ref(null)

  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => user.value?.id || 1)

  function login(userInfo, accessToken) {
    user.value = userInfo
    token.value = accessToken
    localStorage.setItem('token', accessToken)
    localStorage.setItem('user', JSON.stringify(userInfo))
  }

  function logout() {
    user.value = null
    token.value = ''
    activeProfile.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  async function loadActiveProfile() {
    try {
      const res = await getActiveProfile(userId.value)
      if (res && res.code === 200 && res.data) {
        activeProfile.value = res.data
      } else {
        activeProfile.value = null
      }
    } catch {
      activeProfile.value = null
    }
  }

  function setActiveProfileLocal(profile) {
    activeProfile.value = profile
  }

  return {
    user, token, sidebarCollapsed, activeProfile,
    isLoggedIn, userId,
    login, logout, toggleSidebar,
    loadActiveProfile, setActiveProfileLocal
  }
})