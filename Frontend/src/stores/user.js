import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref('')
  const sidebarCollapsed = ref(false)

  const isLoggedIn = computed(() => !!user.value)

  function login(userInfo, accessToken) {
    user.value = userInfo
    token.value = accessToken
    localStorage.setItem('token', accessToken)
  }

  function logout() {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return { user, token, sidebarCollapsed, isLoggedIn, login, logout, toggleSidebar }
})