import { ref, onUnmounted } from 'vue'
import { createChatStream } from '@/api/chat'
import { useUserStore } from '@/stores/user'

export function useChat() {
  const messages = ref([])
  const loading = ref(false)
  const currentReply = ref('')
  let controller = null

  function addMessage(role, content) {
    messages.value.push({
      role,
      content,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
  }

  function sendMessage(question, chatId) {
    if (!question.trim() || loading.value) return

    addMessage('user', question)
    loading.value = true
    currentReply.value = ''

    const userStore = useUserStore()
    const userId = userStore.user?.id || 1

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

  return { messages, loading, currentReply, sendMessage, stopStreaming }
}