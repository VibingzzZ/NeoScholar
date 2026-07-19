import { get, post, put } from './index'

const BASE = '/user/profile'

export function mergeProfiles(id, userId) {
  return post(`${BASE}/merge/${id}/${userId}`)
}

export function getProfile(id) {
  return get(`${BASE}/detail/${id}`)
}

export function listProfiles(userId) {
  return get(`${BASE}/list/${userId}`)
}

export function updateProfile(data) {
  return put(`${BASE}/update`, data)
}

export function createProfile(data) {
  return post(`${BASE}/create`, data)
}

export function getActiveProfile(userId) {
  return get(`${BASE}/active/${userId}`)
}

export function setActiveProfile(userId, profileId) {
  return post(`${BASE}/active/${userId}/${profileId}`)
}

export function getMergeHistory(userId) {
  return get(`${BASE}/merge-history/${userId}`)
}
