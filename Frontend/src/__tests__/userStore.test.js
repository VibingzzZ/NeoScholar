import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('initial state should be logged out', () => {
    const store = useUserStore()
    expect(store.isLoggedIn).toBe(false)
    expect(store.token).toBe('')
  })

  it('login should set user and token', () => {
    const store = useUserStore()
    store.login({ id: 1, username: 'demo' }, 'test-token-123')
    expect(store.isLoggedIn).toBe(true)
    expect(store.token).toBe('test-token-123')
    expect(localStorage.getItem('token')).toBe('test-token-123')
  })

  it('logout should clear user and token', () => {
    const store = useUserStore()
    store.login({ id: 1, username: 'demo' }, 'test-token')
    store.logout()
    expect(store.isLoggedIn).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('should restore state from localStorage', () => {
    localStorage.setItem('token', 'saved-token')
    localStorage.setItem('user', JSON.stringify({ id: 2, username: 'saved' }))
    const store = useUserStore()
    expect(store.token).toBe('saved-token')
    expect(store.user?.username).toBe('saved')
  })
})
