import { ref, onUnmounted, onMounted } from 'vue'
import { createChatStream, getChatHistory } from '@/api/chat'
import { useUserStore } from '@/stores/user'

export function useChat() {
  const messages = ref([])
  const loading = ref(false)
  const currentReply = ref('')
  let controller = null

  function addMessage(role, content, time) {
    messages.value.push({
      role,
      content,
      time: time || new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
  }

  /** 从后端加载历史消息 */
  async function loadHistory(userId) {
    try {
      const res = await getChatHistory(userId)
      if (res && res.code === 200 && res.data) {
        const historyMsgs = res.data.map(m => ({
          role: m.role,
          content: m.content,
          time: m.createdAt
            ? new Date(m.createdAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
            : ''
        }))
        // 替换而非追加，避免重复
        messages.value = historyMsgs
      }
    } catch (e) {
      console.error('加载聊天历史失败:', e)
    }
  }

  const userStore = useUserStore()
  // 使用稳定的 chatId，基于 userId + 日期，同一天同一用户保持同一会话
  const chatId = computedChatId(userStore.user?.id)

  onMounted(() => {
    if (userStore.user?.id) {
      loadHistory(userStore.user.id)
    }
  })

  function sendMessage(question) {
    if (!question.trim() || loading.value) return

    const userId = userStore.user?.id || 1
    addMessage('user', question)
    loading.value = true
    currentReply.value = ''

    controller = createChatStream(
      question,
      chatId,
      userId,
      (token) => {
        currentReply.value += token
      },
      () => {
        if (currentReply.value) {
          addMessage('assistant', currentReply.value)
        }
        currentReply.value = ''
        loading.value = false
      },
      (err) => {
        addMessage('assistant', `抱歉，连接出现问题：${err}`)
        currentReply.value = ''
        loading.value = false
      }
    )
  }

  function stopStreaming() {
    controller?.abort()
    if (currentReply.value) {
      addMessage('assistant', currentReply.value)
    }
    currentReply.value = ''
    loading.value = false
  }

  onUnmounted(() => {
    controller?.abort()
  })

  return { messages, loading, currentReply, sendMessage, stopStreaming, chatId }
}

/** 基于 userId 生成稳定的 chatId（按天轮换，同一天同一个会话） */
function computedChatId(userId) {
  const today = new Date().toISOString().slice(0, 10) // yyyy-MM-dd
  return `user-${userId || 0}-${today}`
}
