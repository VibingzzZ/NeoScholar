import { get, post } from './index'

const BASE = '/user/profile'

export function mergeProfiles(id, userId) {
  return post(`${BASE}/merge/${id}/${userId}`)
}

export function getProfile(id) {
  return post(`${BASE}/detail/${id}`)
}

export function listProfiles(userId) {
  return get(`${BASE}/list/${userId}`)
}

export function updateProfile(data) {
  return post(`${BASE}/update`, data)
}

export function createProfile(data) {
  return post(`${BASE}/create`, data)
}
