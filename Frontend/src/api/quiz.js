import { get, post } from './index'

const BASE = '/quiz'

export function submitQuiz(userId, resourceId, answers) {
  return post(`${BASE}/submit`, { userId, resourceId, answers })
}

export function getQuizHistory(userId) {
  return get(`${BASE}/history/${userId}`)
}

export function getQuizDetail(attemptId) {
  return get(`${BASE}/detail/${attemptId}`)
}
