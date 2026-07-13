import { post } from './index'

const BASE = '/auth'

export function login(username, password) {
  return post(`${BASE}/login`, { username, password })
}

export function register(username, password) {
  return post(`${BASE}/register`, { username, password })
}
