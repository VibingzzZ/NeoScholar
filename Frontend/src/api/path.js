import { get, post } from './index'

const BASE = '/learning-path'

export function getLearningPaths(userId) {
  return get(`${BASE}/list/${userId}`)
}

export function getPathDetail(pathId) {
  return get(`${BASE}/detail/${pathId}`)
}

export function generatePath(userId) {
  return post(`${BASE}/generate/${userId}`)
}

export function updateProgress(pathId, nodeIndex) {
  return post(`${BASE}/progress`, { pathId, nodeIndex })
}