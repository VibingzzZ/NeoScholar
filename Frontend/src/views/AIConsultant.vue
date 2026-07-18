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
            <div class="message-content" v-html="formatContent(currentReply)"></div>
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

// ========== 内联 Markdown → HTML 转换器 ==========
function parseMarkdown(text) {
  if (!text) return ''

  // 预处理：修复 AI 常见格式问题（#/&gt;/- 后缺空格）
  let md = text
    .replace(/^(#{1,6})([^\s#])/gm, '$1 $2')
    .replace(/^>([^\s>])/gm, '> $1')
    .replace(/^(-)([^\s-])/gm, '$1 $2')

  // 预处理：拆分表格行（无换行时 || 是行边界，如 |a|b||--|--||1|2|）
  // 必须在其他换行插入之前处理，避免表格被误拆
  md = md.replace(/\|\|/g, '|\n|')

  // 预处理：在块级 Markdown 标记前自动插入换行
  // AI 的 SSE 流式传输可能丢失 \n，导致 ---、###、> 等标记挤在段落中
  md = md
    // 水平线：[非\n|-]---[非\n|-] → 前面插入 \n\n
    // 排除 |--- 和 ---|（表格分隔行），排除 - 开头的 ---（减号）
    .replace(/([^\n|-])---([^\n|-])/g, '$1\n\n---\n$2')
    .replace(/([^\n|-])---$/gm, '$1\n\n---')
    .replace(/^---([^\n|-])/gm, '---\n$1')
    // 标题：[非\n|#]### → 前面插入 \n\n（排除 |### 表格内）
    .replace(/([^\n|#])(#{1,6}\s)/g, '$1\n\n$2')
    // 引用：[非\n|>]>  → 前面插入 \n\n（排除 |> 表格内、>> 嵌套）
    .replace(/([^\n|>])(>\s)/g, '$1\n\n$2')

  const blocks = []   // 收集顶层块级 HTML
  const placeholders = new Map()  // 占位符 → HTML
  let uid = 0
  const stash = (html) => { const key = `%%BLOCK_${uid++}%%`; placeholders.set(key, html); return key }

  // ── 1. 提取代码块（```...```）──
  md = md.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
    const escaped = code
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    return stash(`<pre><code>${escaped}</code></pre>`)
  })

  // ── 2. 提取表格（连续的 | 行）──
  md = md.replace(/(^\|.+\|\n(?:^\|[-:\s|]+\|\n)?(?:^\|.+\|\n?)+)/gm, (table) => {
    const rows = table.trim().split('\n').filter(line => line.includes('|'))
    if (rows.length < 2) return table
    // 过滤分隔行
    const dataRows = rows.filter(r => !/^\|[-:\s|]+\|$/.test(r))
    if (dataRows.length === 0) return table
    const isHeader = rows.length > dataRows.length  // 有分隔行
    const htmlRows = dataRows.map((row, i) => {
      const cells = row.split('|').filter(c => c.trim() !== '').map(c => c.trim())
      const tag = (isHeader && i === 0) ? 'th' : 'td'
      return `<tr>${cells.map(c => `<${tag}>${c}</${tag}>`).join('')}</tr>`
    })
    return stash(`<table>${htmlRows.join('')}</table>`)
  })

  // ── 3. 逐行处理块级元素 ──
  const lines = md.split('\n')
  const result = []
  let inList = false

  for (let i = 0; i < lines.length; i++) {
    let line = lines[i]

    // 空行：结束列表
    if (line.trim() === '') {
      if (inList) { result.push('</ul>'); inList = false }
      result.push('')
      continue
    }

    // 标题 h1-h6
    let m = line.match(/^(#{1,6})\s+(.+)$/)
    if (m) {
      if (inList) { result.push('</ul>'); inList = false }
      const level = m[1].length
      result.push(`<h${level}>${inline(m[2])}</h${level}>`)
      continue
    }

    // 水平线
    if (/^(-{3,}|\*{3,})$/.test(line.trim())) {
      if (inList) { result.push('</ul>'); inList = false }
      result.push('<hr>')
      continue
    }

    // 引用块
    m = line.match(/^>\s?(.+)$/)
    if (m) {
      if (inList) { result.push('</ul>'); inList = false }
      result.push(`<blockquote><p>${inline(m[1])}</p></blockquote>`)
      continue
    }

    // 无序列表
    m = line.match(/^-\s+(.+)$/)
    if (m) {
      if (!inList) { result.push('<ul>'); inList = true }
      result.push(`<li>${inline(m[1])}</li>`)
      continue
    }

    // 普通段落（跳过占位符行，它们自带 HTML 标签）
    if (inList) { result.push('</ul>'); inList = false }
    if (line.trim()) {
      if (/^%%BLOCK_\d+%%$/.test(line.trim())) {
        result.push(line.trim())  // 占位符直接输出，不用 <p> 包裹
      } else {
        result.push(`<p>${inline(line)}</p>`)
      }
    }
  }
  if (inList) result.push('</ul>')

  // ── 4. 还原占位符 ──
  let html = result.join('\n')
  for (const [key, val] of placeholders) {
    html = html.replace(key, val)
  }

  return html
}

// 行内元素转换
function inline(text) {
  if (!text) return ''
  return text
    // 图片
    .replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1">')
    // 链接
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
    // 粗体+斜体
    .replace(/\*\*\*(.+?)\*\*\*/g, '<strong><em>$1</em></strong>')
    // 粗体
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    // 斜体
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    // 行内代码（保护已转义的）
    .replace(/(?<!&|<)\`([^\`]+)\`(?!;)/g, '<code>$1</code>')
    // 删除线
    .replace(/~~(.+?)~~/g, '<del>$1</del>')
}

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
  return parseMarkdown(text)
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

        // Markdown 渲染样式
        :deep(h1) { font-size: 1.4em; font-weight: 700; margin: 0.6em 0 0.4em; line-height: 1.3; }
        :deep(h2) { font-size: 1.25em; font-weight: 700; margin: 0.6em 0 0.3em; line-height: 1.3; }
        :deep(h3) { font-size: 1.1em; font-weight: 600; margin: 0.5em 0 0.25em; line-height: 1.3; }
        :deep(h4) { font-size: 1em; font-weight: 600; margin: 0.4em 0 0.2em; }

        :deep(p) { margin: 0.4em 0; }
        :deep(strong) { font-weight: 700; color: #1f2937; }
        :deep(em) { font-style: italic; }

        :deep(ul), :deep(ol) {
          padding-left: 1.5em;
          margin: 0.3em 0;
        }
        :deep(li) { margin: 0.15em 0; }

        :deep(blockquote) {
          margin: 0.5em 0;
          padding: 6px 14px;
          border-left: 3px solid #4f6ef7;
          background: rgba(79, 110, 247, 0.06);
          border-radius: 0 6px 6px 0;
          color: #6b7280;
        }

        :deep(code) {
          background: rgba(79, 110, 247, 0.1);
          color: #4f6ef7;
          padding: 2px 6px;
          border-radius: 4px;
          font-size: 13px;
          font-family: 'Consolas', 'Courier New', monospace;
        }

        :deep(pre) {
          margin: 0.5em 0;
          padding: 12px 14px;
          background: #1f2937;
          border-radius: 8px;
          overflow-x: auto;

          code {
            background: none;
            color: #e5e7eb;
            padding: 0;
            font-size: 13px;
            line-height: 1.5;
            border-radius: 0;
          }
        }

        :deep(table) {
          width: 100%;
          margin: 0.5em 0;
          border-collapse: collapse;
          font-size: 13px;

          th, td {
            border: 1px solid #d1d5db;
            padding: 6px 12px;
            text-align: left;
          }

          th {
            background: #f3f4f6;
            font-weight: 600;
            color: #374151;
          }

          tr:nth-child(even) td {
            background: #fafbfc;
          }
        }

        :deep(hr) {
          margin: 0.8em 0;
          border: none;
          border-top: 1px solid #e5e7eb;
        }

        :deep(a) {
          color: #4f6ef7;
          text-decoration: underline;
          &:hover { color: #3b5de7; }
        }

        :deep(img) {
          max-width: 100%;
          border-radius: 6px;
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