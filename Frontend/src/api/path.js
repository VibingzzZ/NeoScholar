import { get, post } from './index'
import http from './index'

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

/** 为学习路径生成配套资源（PPT大纲 + 练习题） */
export function generateResources(pathId) {
  return post(`${BASE}/${pathId}/generate-resources`)
}

/** 获取学习路径下的所有生成资源（PPT、测验等） */
export function getPathResources(pathId) {
  return get(`${BASE}/${pathId}/resources`)
}

/** 下载学习资源文件（返回 Blob） */
export async function downloadResource(resourceId, fileName) {
  const response = await http.get(`${BASE}/resource/${resourceId}/download`, {
    responseType: 'blob'
  })
  const url = window.URL.createObjectURL(new Blob([response.data]))
  const link = document.createElement('a')
  link.href = url
  link.download = fileName || 'resource.txt'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}