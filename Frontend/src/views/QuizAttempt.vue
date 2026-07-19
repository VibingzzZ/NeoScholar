<template>
  <div class="quiz-attempt">
    <div class="page-header">
      <h2>测验练习</h2>
      <p class="subtitle">完成测验后 AI 将自动评分并给出反馈</p>
    </div>

    <!-- 空状态：未指定资源 -->
    <div class="card-panel empty-state" v-if="!resourceId">
      <el-icon :size="56" color="#d1d5db"><Edit /></el-icon>
      <h3>请从学习路径中选择测验</h3>
      <p>进入「学习路径」页面，找到带有 <el-tag type="warning" size="small">练习题</el-tag> 标签的节点，点击「开始测验」按钮</p>
      <el-button type="primary" style="margin-top: 16px" @click="$router.push('/learning-path')">
        前往学习路径
      </el-button>
    </div>

    <!-- 答题阶段 -->
    <div class="card-panel quiz-card" v-if="resourceId && !submitted">
      <div class="quiz-header">
        <h3>{{ resourceTitle }}</h3>
        <el-tag type="warning">AI 生成测验</el-tag>
      </div>

      <!-- 只显示题目（不含答案） -->
      <div class="quiz-content" v-html="renderedQuestions"></div>

      <!-- 作答区 -->
      <div class="answer-section">
        <h4>你的作答</h4>
        <el-input
          v-model="answerText"
          type="textarea"
          :rows="8"
          placeholder="请在此输入你的答案，请逐题标注题号..."
          :disabled="submitting"
        />
        <div class="answer-actions">
          <span class="hint">提交后 AI 将批改评分，并展示参考答案与解析</span>
          <el-button
            type="primary"
            :loading="submitting"
            :disabled="!answerText.trim()"
            @click="submitQuiz"
          >
            提交并获取评分
          </el-button>
        </div>
      </div>
    </div>

    <!-- 评分结果阶段 -->
    <div v-if="submitted">
      <!-- 评分摘要 -->
      <div class="card-panel score-summary">
        <div class="score-header">
          <div class="score-circle" :class="scoreLevel">
            <span class="score-num">{{ earnedScore }}</span>
            <span class="score-divider">/</span>
            <span class="score-total">{{ totalScore }}</span>
          </div>
          <div class="score-meta">
            <h3>{{ scoreText }}</h3>
            <p class="score-feedback">{{ feedback }}</p>
          </div>
        </div>
      </div>

      <!-- 知识点分析 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <div class="card-panel">
            <div class="card-title">
              <el-icon color="#10b981"><CircleCheckFilled /></el-icon> 已掌握知识点
            </div>
            <div class="topic-list">
              <el-tag v-for="t in masteredTopics" :key="t" type="success" effect="plain" class="topic-tag">{{ t }}</el-tag>
              <span v-if="!masteredTopics.length" class="empty-text">暂无数据</span>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="card-panel">
            <div class="card-title">
              <el-icon color="#f59e0b"><WarningFilled /></el-icon> 薄弱知识点
            </div>
            <div class="topic-list">
              <el-tag v-for="t in weakTopics" :key="t" type="warning" effect="plain" class="topic-tag">{{ t }}</el-tag>
              <span v-if="!weakTopics.length" class="empty-text">暂无数据</span>
            </div>
          </div>
        </el-col>
      </el-row>


      <!-- 逐题评分 -->
      <div class="card-panel" style="margin-top: 20px" v-if="itemScores.length">
        <div class="card-title">逐题评分详情</div>
        <div class="item-score-list">
          <div v-for="item in itemScores" :key="item.itemIndex" class="item-score-row">
            <div class="item-header">
              <span class="item-index">第{{ item.itemIndex }}题</span>
              <span class="item-points">{{ item.earned }} / {{ item.maxScore }} 分</span>
              <el-progress
                :percentage="item.maxScore > 0 ? Math.round(item.earned / item.maxScore * 100) : 0"
                :stroke-width="6"
                :color="item.maxScore > 0 && item.earned / item.maxScore >= 0.7 ? '#10b981' : '#f59e0b'"
                style="width: 100px"
              />
            </div>
            <p class="item-correct" v-if="item.correctAnswer">
              <span class="label">正确答案：</span>{{ item.correctAnswer }}
            </p>
            <p class="item-analysis" v-if="item.analysis">
              <span class="label">解析：</span>{{ item.analysis }}
            </p>
            <p class="item-comment">
              <span class="label">评语：</span>{{ item.comment }}
            </p>
          </div>
        </div>
      </div>

      <!-- 改进建议 -->
      <div class="card-panel" style="margin-top: 20px" v-if="suggestions.length">
        <div class="card-title">
          <el-icon color="#4f6ef7"><Sunny /></el-icon> 改进建议
        </div>
        <div class="suggest-list">
          <div v-for="(s, i) in suggestions" :key="i" class="suggest-row">
            <span class="suggest-num">{{ i + 1 }}</span>
            <span>{{ s }}</span>
          </div>
        </div>
      </div>

      <!-- 操作 -->
      <div class="quiz-footer-actions">
        <el-button @click="resetQuiz" plain>重新作答</el-button>
        <el-button type="primary" @click="$router.push('/learning-path')">
          返回学习路径
        </el-button>
        <el-button type="primary" plain @click="$router.push('/consultant')">
          找 AI 导师进一步辅导
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, WarningFilled, Sunny } from '@element-plus/icons-vue'
import { getPathResources } from '@/api/path'
import { submitQuiz as submitQuizApi } from '@/api/quiz'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const resourceId = ref(Number(route.query.resourceId) || 0)
const resourceTitle = ref('')
const quizContent = ref('')       // 题目内容（纯题目，无答案）
const answerText = ref('')
const submitting = ref(false)
const submitted = ref(false)

// 评分结果
const earnedScore = ref(0)
const totalScore = ref(100)
const feedback = ref('')
const itemScores = ref([])
const masteredTopics = ref([])
const weakTopics = ref([])
const suggestions = ref([])

// Markdown → HTML（增强版，处理代码块和粗体）
function renderMarkdown(text) {
  if (!text) return ''
  let html = text
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  // 代码块（必须在行内代码之前处理）
  html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, (_, lang, code) => {
    const escaped = code.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    return `<pre><code>${escaped}</code></pre>`
  })
  // 行内代码
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')
  // 粗体/斜体
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')
  // 标题
  html = html.replace(/^#### (.+)$/gm, '<h5>$1</h5>')
  html = html.replace(/^### (.+)$/gm, '<h4>$1</h4>')
  html = html.replace(/^## (.+)$/gm, '<h3>$1</h3>')
  html = html.replace(/^# (.+)$/gm, '<h2>$1</h2>')
  // 水平线
  html = html.replace(/^---$/gm, '<hr>')
  // 列表项
  html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
  html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')
  // 表格（简单处理）
  html = html.replace(/\|(.+)\|/g, (match) => {
    const cells = match.split('|').filter(c => c.trim()).map(c => `<td>${c.trim()}</td>`).join('')
    return `<tr>${cells}</tr>`
  })
  html = html.replace(/(<tr>.*<\/tr>\n?)+/g, '<table>$&</table>')
  // 段落
  html = html.replace(/\n\n/g, '</p><p>')
  html = html.replace(/\n/g, '<br>')
  return '<p>' + html + '</p>'
}

// 兜底：自动截掉答案相关内容（分隔线 / 正确答案 / 参考答案 / 解析等）
const safeQuizContent = computed(() => {
  let raw = quizContent.value
  if (!raw) return ''
  // 1) 分隔线截断
  const dIdx = raw.indexOf('=====答案分隔线=====')
  if (dIdx >= 0) raw = raw.substring(0, dIdx)
  // 2) 按"正确答案"关键词截断（取第一个匹配位置）
  const patterns = ['\n正确答案', '\n参考答案', '\n解析：', '\n## 参考']
  for (const p of patterns) {
    const i = raw.indexOf(p)
    if (i >= 0) { raw = raw.substring(0, i); break }
  }
  return raw.trim()
})

const renderedQuestions = computed(() => renderMarkdown(safeQuizContent.value))

const scoreLevel = computed(() => {
  const rate = totalScore.value > 0 ? earnedScore.value / totalScore.value : 0
  if (rate >= 0.85) return 'excellent'
  if (rate >= 0.7) return 'good'
  if (rate >= 0.5) return 'medium'
  return 'poor'
})

const scoreText = computed(() => {
  const rate = totalScore.value > 0 ? earnedScore.value / totalScore.value : 0
  if (rate >= 0.85) return '太棒了！表现优秀！'
  if (rate >= 0.7) return '做得不错，继续加油！'
  if (rate >= 0.5) return '还有提升空间，针对性复习一下'
  return '需要加强基础，建议回顾相关课程内容'
})

onMounted(async () => {
  if (!resourceId.value) {
    return
  }
  try {
    const pathId = Number(route.query.pathId) || 0
    if (!pathId) {
      ElMessage.warning('缺少学习路径参数')
      return
    }
    const res = await getPathResources(pathId)
    if (res && res.code === 200 && res.data) {
      const resource = res.data.find(r => r.id === resourceId.value)
      if (resource) {
        resourceTitle.value = resource.title
        quizContent.value = resource.contentText || ''
      } else {
        ElMessage.warning('未找到该测验资源')
      }
    }
  } catch (e) {
    ElMessage.error('加载测验内容失败')
  }
})

async function submitQuiz() {
  if (!answerText.value.trim()) return
  submitting.value = true
  try {
    const res = await submitQuizApi(
      userStore.user?.id || 1,
      resourceId.value,
      { answer: answerText.value.trim() }
    )
    if (res && res.code === 200 && res.data) {
      const d = res.data
      earnedScore.value = d.earnedScore || 0
      totalScore.value = d.totalScore || 100
      feedback.value = d.feedback || ''

      // 解析详细评分
      if (d.scoresJson) {
        try {
          const detail = JSON.parse(d.scoresJson)
          itemScores.value = detail.itemScores || []
          masteredTopics.value = detail.masteredTopics || []
          weakTopics.value = detail.weakTopics || []
          suggestions.value = detail.suggestions || []
        } catch {}
      }
      submitted.value = true
      ElMessage.success('评分完成！')
    } else {
      ElMessage.error(res?.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('评分服务暂时不可用，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function resetQuiz() {
  submitted.value = false
  answerText.value = ''
  earnedScore.value = 0
  totalScore.value = 100
  feedback.value = ''
  itemScores.value = []
  masteredTopics.value = []
  weakTopics.value = []
  suggestions.value = []
}
</script>

<style scoped lang="scss">
.quiz-card {
  .quiz-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f3f4f6;

    h3 { font-size: 18px; color: #1f2937; }
  }

  .quiz-content {
    background: #f9fafb;
    padding: 20px;
    border-radius: 8px;
    line-height: 1.8;
    font-size: 14px;
    color: #374151;

    :deep(h2), :deep(h3), :deep(h4) { color: #1f2937; margin: 16px 0 8px; }
    :deep(code) { background: #eef2ff; color: #4f6ef7; padding: 2px 6px; border-radius: 4px; font-size: 13px; }
    :deep(pre) { background: #1f2937; color: #e5e7eb; padding: 16px; border-radius: 8px; overflow-x: auto; }
    :deep(li) { margin-left: 20px; }
  }

  .answer-section {
    margin-top: 24px;

    h4 { font-size: 15px; color: #374151; margin-bottom: 10px; }

    :deep(.el-textarea__inner) {
      border-radius: 8px;
      font-size: 14px;
      line-height: 1.6;
    }

    .answer-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 12px;

      .hint { font-size: 12px; color: #9ca3af; }
    }
  }
}

// ========== 评分结果 ==========
.score-summary {
  .score-header {
    display: flex;
    align-items: center;
    gap: 24px;
  }

  .score-circle {
    display: flex;
    align-items: baseline;
    justify-content: center;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    border: 4px solid;
    flex-shrink: 0;
    padding-top: 22px;

    .score-num { font-size: 40px; font-weight: 800; }
    .score-divider { font-size: 18px; margin: 0 2px; color: #d1d5db; }
    .score-total { font-size: 18px; color: #9ca3af; }

    &.excellent { border-color: #10b981; .score-num { color: #059669; } }
    &.good { border-color: #4f6ef7; .score-num { color: #4f6ef7; } }
    &.medium { border-color: #f59e0b; .score-num { color: #d97706; } }
    &.poor { border-color: #ef4444; .score-num { color: #dc2626; } }
  }

  .score-meta {
    h3 { font-size: 20px; color: #1f2937; margin-bottom: 6px; }
    .score-feedback { font-size: 14px; color: #6b7280; line-height: 1.6; }
  }
}

.topic-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;

  .topic-tag { margin: 0; }
  .empty-text { font-size: 13px; color: #9ca3af; }
}

.item-score-list {
  .item-score-row {
    padding: 14px 0;
    border-bottom: 1px solid #f3f4f6;

    &:last-child { border-bottom: none; }

    .item-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 6px;

      .item-index { font-weight: 600; font-size: 14px; color: #1f2937; }
      .item-points { font-size: 13px; color: #4f6ef7; font-weight: 500; }
    }

    .item-correct { font-size: 13px; color: #059669; padding-left: 4px; margin-top: 4px; }
    .item-analysis { font-size: 13px; color: #4f6ef7; padding-left: 4px; margin-top: 2px; }
    .item-comment { font-size: 13px; color: #6b7280; padding-left: 4px; margin-top: 2px; }
    .label { font-weight: 600; font-size: 12px; }
  }
}

.suggest-list {
  .suggest-row {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 10px 0;
    border-bottom: 1px solid #f3f4f6;
    font-size: 13px;
    color: #6b7280;
    line-height: 1.5;

    .suggest-num {
      width: 22px; height: 22px;
      border-radius: 50%;
      background: #eef2ff;
      color: #4f6ef7;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 600;
      flex-shrink: 0;
    }
  }
}

.quiz-footer-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  justify-content: center;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 360px;
  color: #9ca3af;

  h3 { margin-top: 16px; color: #6b7280; }
  p { margin-top: 8px; font-size: 14px; text-align: center; max-width: 420px; }
}
</style>
