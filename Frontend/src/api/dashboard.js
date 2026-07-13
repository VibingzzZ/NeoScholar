import { get } from './index'

const BASE = '/dashboard'

export function getDashboardStats(userId) {
  return get(`${BASE}/stats/${userId}`)
}
