<template>
  <div class="ai-consultant">
    <div class="page-header">
      <h2>智能辅导</h2>
      <p class="subtitle">与 AI 导师对话，获取个性化学习指导</p>
    </div>

    <div class="chat-container">
      <!-- 对话区域 -->
      <div class="chat-messages" ref="chatBody">
        <div v-if="messages.length === 0" class="welcome-area">
          <el-icon :size="48" color="#c7d2fe"><ChatDotRound /></el-icon>
          <h3>你好，我是 NeoScholar AI 导师</h3>
          <p>我可以根据你的学习画像，为你解答问题、讲解知识点、梳理学习思路</p>
          <div class="quick-questions">
            <span class="quick-label">试试问我：</span>
            <el-tag
              v-for="q in quickQuestions"
              :key="q"
              class="quick-tag"
              @click="sendQuick(q)"
            >
              {{ q }}
            </el-tag>
          </div>
        </div>

        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="message-row"
          :class="msg.role"
        >
          <div class="message-avatar">
            <el-avatar v-if="msg.role === 'user'" :size="36" icon="UserFilled" />
            <el-avatar v-else :size="36" style="background: #4f6ef7">
              <el-icon :size="20"><Sunny /></el-icon>
            </el-avatar>
          </div>
          <div class="message-body">
            <div class="message-sender">
              {{ msg.role === 'user' ? '我' : 'AI 导师' }}
              <span class="message-time">{{ msg.time }}</span>
            </div>
            <div class="message-content" v-html="formatContent(msg.content)"></div>
          </div>
        </div>

        <!-- 流式输出 -->
        <div v-if="loading && currentReply" class="message-row assistant">
          <div class="message-avatar">
            <el-avatar :size="36" style="background: #4f6ef7">
              <el-icon :size="20"><Sunny /></el-icon>
            </el-avatar>
          </div>
          <div class="message-body">
            <div class="message-sender">AI 导师 <span class="typing-dot">●</span></div>
            <div class="message-content">{{ currentReply }}</div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="输入你的问题..."
          :disabled="loading"
          resize="none"
          @keydown.enter.exact.prevent="handleSend"
        />
        <div class="input-actions">
          <span class="input-hint">Enter 发送，Shift+Enter 换行</span>
          <el-button
            v-if="loading"
            type="danger"
            plain
            size="small"
            @click="stopStreaming"
          >
            停止生成
          </el-button>
          <el-button
            v-else
            type="primary"
            :disabled="!inputText.trim()"
            @click="handleSend"
          >
            <el-icon><Promotion /></el-icon> 发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { ChatDotRound, Sunny, UserFilled, Promotion } from '@element-plus/icons-vue'
import { useChat } from '@/composables/useChat'

const { messages, loading, currentReply, sendMessage, stopStreaming } = useChat()

const chatBody = ref(null)
const inputText = ref('')

const quickQuestions = [
  '如何理解 Spring 的依赖注入？',
  '能帮我梳理一下二叉树遍历的思路吗？',
  '什么是微服务架构的优缺点？',
  '如何优化数据库查询性能？'
]

const chatId = 'user-' + Date.now()

function handleSend() {
  if (!inputText.value.trim() || loading.value) return
  const text = inputText.value.trim()
  inputText.value = ''
  sendMessage(text, chatId)
}

function sendQuick(q) {
  inputText.value = q
  handleSend()
}

function formatContent(text) {
  return text
    .replace(/\n/g, '<br>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
}

watch(
  () => messages.value.length,
  async () => {
    await nextTick()
    if (chatBody.value) {
      chatBody.value.scrollTop = chatBody.value.scrollHeight
    }
  }
)

watch(currentReply, async () => {
  await nextTick()
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
})
</script>

<style scoped lang="scss">
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 180px);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;

  .welcome-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 320px;
    text-align: center;

    h3 {
      margin-top: 16px;
      font-size: 18px;
      color: #374151;
    }

    p {
      margin-top: 8px;
      font-size: 14px;
      color: #9ca3af;
      max-width: 400px;
    }

    .quick-questions {
      margin-top: 24px;

      .quick-label {
        display: block;
        font-size: 13px;
        color: #9ca3af;
        margin-bottom: 10px;
      }

      .quick-tag {
        margin: 4px 6px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background: #eef2ff;
          color: #4f6ef7;
          border-color: #4f6ef7;
        }
      }
    }
  }

  .message-row {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;

    &.user {
      flex-direction: row-reverse;

      .message-body {
        align-items: flex-end;

        .message-content {
          background: #4f6ef7;
          color: #fff;
          border-radius: 14px 4px 14px 14px;
        }
      }
    }

    .message-body {
      display: flex;
      flex-direction: column;
      max-width: 70%;

      .message-sender {
        font-size: 12px;
        color: #9ca3af;
        margin-bottom: 4px;

        .message-time {
          margin-left: 8px;
          font-size: 11px;
          color: #d1d5db;
        }

        .typing-dot {
          color: #4f6ef7;
          animation: blink 1s infinite;
          margin-left: 4px;
        }
      }

      .message-content {
        padding: 12px 16px;
        font-size: 14px;
        line-height: 1.6;
        background: #f3f4f6;
        border-radius: 4px 14px 14px 14px;
        word-break: break-word;

        :deep(code) {
          background: rgba(79, 110, 247, 0.1);
          color: #4f6ef7;
          padding: 2px 6px;
          border-radius: 4px;
          font-size: 13px;
        }
      }
    }
  }
}

.chat-input-area {
  border-top: 1px solid #f3f4f6;
  padding: 16px 24px;

  :deep(.el-textarea__inner) {
    border-radius: 10px;
    background: #f9fafb;
    font-size: 14px;
    border: 1px solid #e5e7eb;

    &:focus {
      border-color: #4f6ef7;
      background: #fff;
    }
  }

  .input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;

    .input-hint {
      font-size: 12px;
      color: #d1d5db;
    }
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>