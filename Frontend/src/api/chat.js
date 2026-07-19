import { get } from './index'

const BASE_URL = '/api'

export function getChatHistory(userId) {
  return get(`/chat/history/${userId}`)
}

export function createChatStream(question, chatId, userId, onToken, onDone, onError) {
  const controller = new AbortController()
  const token = localStorage.getItem('token')

  const headers = { 'Content-Type': 'application/json' }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  fetch(`${BASE_URL}/chat/stream`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ question, chatId, userId }),
    signal: controller.signal
  })
    .then(async (response) => {
      if (!response.ok || !response.body) {
        onError(`请求失败 (${response.status})`)
        return
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''
        for (const line of lines) {
          if (line.startsWith('data:')) {
            // 只去掉 data: 前缀和末尾 \r，保留 \n 等有意义的空白字符
            let token = line.slice(5)
            if (token.endsWith('\r')) token = token.slice(0, -1)
            if (token === '[DONE]') {
              onDone()
              return
            }
            onToken(token)
          }
        }
      }
      onDone()
    })
    .catch((err) => {
      if (err.name !== 'AbortError') {
        onError(err.message || '连接失败')
      }
    })

  return controller
}
