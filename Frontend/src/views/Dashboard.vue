<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>学习仪表盘</h2>
      <p class="subtitle">欢迎回来！来看看你今日的学习概览</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">学习路径数</div>
          <div class="stat-value">{{ stats.pathCount }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">已完成节点</div>
          <div class="stat-value">{{ stats.completedNodes }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">学习天数</div>
          <div class="stat-value">{{ stats.activeDays }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">AI 辅导次数</div>
          <div class="stat-value">{{ stats.aiConsultCount }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 活跃画像卡片 -->
    <div class="card-panel profile-summary-panel" v-if="stats.activeProfile" style="margin-top: 24px">
      <div class="profile-summary-header">
        <div class="card-title" style="border: none; padding-bottom: 0; margin-bottom: 0; display: flex; align-items: center; gap: 8px">
          当前学习画像
          <el-tag
            :type="stats.activeProfile.source === 'ai_chat' ? 'success' : stats.activeProfile.source === 'merge' ? 'warning' : 'info'"
            size="small"
            effect="plain"
          >
            {{ stats.activeProfile.source === 'ai_chat' ? 'AI 提取' : stats.activeProfile.source === 'merge' ? '合并生成' : '手动创建' }}
          </el-tag>
        </div>
        <el-button type="primary" link size="small" @click="$router.push('/profile')">
          管理画像 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="profile-field">
            <span class="pf-label">专业/领域</span>
            <span class="pf-value">{{ stats.activeProfile.majorOrField || '未设置' }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="profile-field">
            <span class="pf-label">学习目标</span>
            <span class="pf-value">{{ stats.activeProfile.learningGoal || '未设置' }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="profile-field">
            <span class="pf-label">画像摘要</span>
            <span class="pf-value">{{ stats.activeProfile.summary || '暂无摘要' }}</span>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 学习活跃热力图 -->
    <div class="card-panel heatmap-panel">
      <div class="heatmap-header">
        <div class="card-title" style="border: none; padding-bottom: 0; margin-bottom: 0">学习活跃度</div>
        <div class="heatmap-stats">
          <div class="heatmap-stat-item">
            <span class="stat-num">{{ streakDays }}</span>
            <span class="stat-desc">连续学习</span>
          </div>
          <div class="heatmap-stat-item">
            <span class="stat-num">{{ totalActiveDays }}</span>
            <span class="stat-desc">累计活跃</span>
          </div>
          <div class="heatmap-stat-item">
            <span class="stat-num">{{ avgPerWeek }}</span>
            <span class="stat-desc">次 / 周</span>
          </div>
        </div>
      </div>
      <div class="heatmap-wrapper">
        <div class="heatmap-month-row">
          <span
            v-for="(label, mi) in monthLabelPositions"
            :key="mi"
            :style="{ left: label.offset + 'px' }"
          >{{ label.name }}</span>
        </div>
        <div class="heatmap-body">
          <div class="heatmap-sidebar">
            <span v-for="day in dayLabels" :key="day">{{ day }}</span>
          </div>
          <div class="heatmap-grid">
            <div
              v-for="(week, wi) in heatmapData"
              :key="wi"
              class="heatmap-week"
            >
              <div
                v-for="(day, di) in week"
                :key="di"
                class="heatmap-cell"
                :class="day.level"
                :title="day.date"
                @mouseenter="showTooltip($event, day)"
                @mouseleave="hideTooltip"
              />
            </div>
          </div>
        </div>
        <div class="heatmap-footer">
          <span class="heatmap-legend-label">少</span>
          <div class="heatmap-legend">
            <div class="heatmap-cell level-0" />
            <div class="heatmap-cell level-1" />
            <div class="heatmap-cell level-2" />
            <div class="heatmap-cell level-3" />
            <div class="heatmap-cell level-4" />
          </div>
          <span class="heatmap-legend-label">多</span>
        </div>
      </div>
    </div>

    <!-- Tooltip -->
    <teleport to="body">
      <div
        v-show="tooltip.visible"
        class="heatmap-tooltip"
        :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }"
      >
        <strong>{{ tooltip.count }}</strong> 次学习活动
        <br><span class="tooltip-date">{{ tooltip.date }}</span>
      </div>
    </teleport>

    <!-- 学习评估 -->
    <div class="card-panel assessment-panel" style="margin-top: 24px" v-if="assessment">
      <div class="card-title">学习效果评估</div>
      <el-row :gutter="24">
        <el-col :span="10">
          <!-- 综合评分 -->
          <div class="overall-score">
            <div class="score-circle" :class="levelClass">
              <span class="score-num">{{ assessment.overallScore }}</span>
              <span class="score-label">{{ assessment.overallLevel }}</span>
            </div>
          </div>
          <!-- 学习行为 -->
          <div class="behavior-stats">
            <div class="behavior-item">
              <span class="b-num">{{ assessment.behavior?.totalStudyDays || 0 }}</span>
              <span class="b-label">学习天数</span>
            </div>
            <div class="behavior-item">
              <span class="b-num">{{ assessment.behavior?.totalQuizzes || 0 }}</span>
              <span class="b-label">测验次数</span>
            </div>
            <div class="behavior-item">
              <span class="b-num">{{ assessment.behavior?.avgQuizScore || 0 }}%</span>
              <span class="b-label">平均得分</span>
            </div>
            <div class="behavior-item">
              <span class="b-num">{{ assessment.behavior?.engagementLevel || '-' }}</span>
              <span class="b-label">参与度</span>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <!-- 雷达图 -->
          <div class="radar-container" v-if="assessment.dimensionScores">
            <svg viewBox="-100 -100 200 200" class="radar-svg">
              <!-- 背景网格 -->
              <polygon
                v-for="level in 4" :key="level"
                :points="getRadarPoints(level * 25)"
                fill="none" :stroke="level === 4 ? '#c7d2fe' : '#e5e7eb'" stroke-width="1"
              />
              <!-- 轴线 -->
              <line v-for="(_, i) in dimLabels" :key="'axis'+i"
                :x1="0" :y1="0"
                :x2="getAxisPoint(i, 100).x" :y2="getAxisPoint(i, 100).y"
                stroke="#e5e7eb" stroke-width="1"
              />
              <!-- 数据区域 -->
              <polygon
                :points="getRadarPointsFromScores()"
                fill="rgba(79,110,247,0.2)" stroke="#4f6ef7" stroke-width="2"
              />
              <!-- 数据点 -->
              <circle
                v-for="(score, i) in dimScoresList" :key="'dot'+i"
                :cx="getAxisPoint(i, score).x" :cy="getAxisPoint(i, score).y"
                r="4" fill="#4f6ef7"
              />
              <!-- 标签 -->
              <text
                v-for="(label, i) in dimLabels" :key="'label'+i"
                :x="getAxisPoint(i, 115).x" :y="getAxisPoint(i, 115).y"
                text-anchor="middle" dominant-baseline="middle"
                font-size="11" fill="#6b7280"
              >{{ label }}</text>
            </svg>
          </div>
        </el-col>
        <el-col :span="6">
          <!-- 改进建议 -->
          <div class="suggestions-box">
            <div class="suggest-title">改进建议</div>
            <div v-for="(s, i) in (assessment.suggestions || [])" :key="i" class="suggest-item">
              <el-icon color="#4f6ef7"><Check /></el-icon>
              <span>{{ s }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 最近学习路径 + 快捷入口 -->
    <el-row :gutter="20" style="margin-top: 24px">
      <el-col :span="16">
        <div class="card-panel">
          <div class="card-title">我的学习路径</div>
          <el-table :data="paths" style="width: 100%" stripe>
            <el-table-column prop="name" label="路径名称" min-width="200" />
            <el-table-column prop="progress" label="进度" width="180">
              <template #default="{ row }">
                <el-progress :percentage="row.progress" :stroke-width="8" color="#4f6ef7" />
              </template>
            </el-table-column>
            <el-table-column prop="nodes" label="总节点" width="100" align="center" />
            <el-table-column prop="updateTime" label="最近更新" width="140" />
            <el-table-column label="操作" width="100" align="center">
              <template #default>
                <el-button type="primary" link size="small" @click="$router.push('/learning-path')">继续学习</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="card-panel quick-actions">
          <div class="card-title">快捷操作</div>
          <div class="action-grid">
            <div class="action-item" @click="$router.push('/profile')">
              <el-icon :size="24"><UserFilled /></el-icon>
              <span>学生画像</span>
            </div>
            <div class="action-item" @click="$router.push('/learning-path')">
              <el-icon :size="24"><MapLocation /></el-icon>
              <span>学习路径</span>
            </div>
            <div class="action-item" @click="$router.push('/consultant')">
              <el-icon :size="24"><ChatDotRound /></el-icon>
              <span>智能辅导</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { UserFilled, MapLocation, ChatDotRound, ArrowRight } from '@element-plus/icons-vue'
import { getDashboardStats, getAssessment } from '@/api/dashboard'
import { getLearningPaths } from '@/api/path'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// ==================== 统计数据 ====================
const stats = ref({ pathCount: 0, completedNodes: 0, activeDays: 0, aiConsultCount: 0, dailyActivities: [] })

// ==================== 热力图数据 ====================
const dayLabels = ['周一', '周三', '周五', '周日']

const tooltip = ref({ visible: false, x: 0, y: 0, count: 0, date: '' })

function showTooltip(e, day) {
  const rect = e.target.getBoundingClientRect()
  tooltip.value = {
    visible: true,
    x: rect.left + rect.width / 2,
    y: rect.top - 8,
    count: day.count,
    date: day.date
  }
}

function hideTooltip() {
  tooltip.value.visible = false
}

function generateHeatmapData(activityMap) {
  const now = new Date()
  const yearAgo = new Date(now)
  yearAgo.setFullYear(yearAgo.getFullYear() - 1)
  yearAgo.setDate(yearAgo.getDate() + 1)

  // 转换为网格：每列是一个周
  const startDay = yearAgo.getDay()
  const offset = startDay === 0 ? 6 : startDay - 1

  const weeks = []
  const firstDate = new Date(yearAgo)
  firstDate.setDate(firstDate.getDate() - offset)

  const dayMap = ['日', '一', '二', '三', '四', '五', '六']

  for (let i = 0; i < 53; i++) {
    const week = []
    for (let j = 0; j < 7; j++) {
      const d = new Date(firstDate)
      d.setDate(d.getDate() + i * 7 + j)
      const key = d.toISOString().slice(0, 10)
      const count = d > now ? 0 : (activityMap.get(key) || 0)
      let level = 'level-0'
      if (count > 0 && count <= 3) level = 'level-1'
      else if (count <= 7) level = 'level-2'
      else if (count <= 12) level = 'level-3'
      else if (count > 12) level = 'level-4'

      week.push({
        date: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} (周${dayMap[d.getDay()]})`,
        count,
        level
      })
    }
    weeks.push(week)
  }

  return weeks
}

const heatmapData = ref([])

// 计算每个月份标签的偏移位置
const CELL_WIDTH = 14
const CELL_GAP = 3
const STEPS_PER_WEEK = CELL_WIDTH + CELL_GAP

const monthLabelPositions = computed(() => {
  const monthMap = new Map()
  for (let wi = 0; wi < heatmapData.value.length; wi++) {
    const week = heatmapData.value[wi]
    for (const cell of week) {
      const parts = cell.date.split('-')
      const day = parseInt(parts[2], 10)
      if (day === 1) {
        const year = parseInt(parts[0], 10)
        const month = parseInt(parts[1], 10)
        const key = `${year}-${month}`
        if (!monthMap.has(key)) {
          monthMap.set(key, { weekIndex: wi, year, month })
        }
        break
      }
    }
  }

  const monthEntries = Array.from(monthMap.values())
  const monthCount = {}
  for (const m of monthEntries) {
    monthCount[m.month] = (monthCount[m.month] || 0) + 1
  }

  const labels = []
  for (let i = 0; i < monthEntries.length; i++) {
    const m = monthEntries[i]
    const isLast = monthEntries.map(x => x.month).lastIndexOf(m.month) === i
    if (monthCount[m.month] <= 1 || isLast) {
      labels.push({ name: `${m.month}月`, offset: m.weekIndex * STEPS_PER_WEEK + CELL_WIDTH / 2 })
    }
  }
  return labels
})

const streakDays = computed(() => {
  let streak = 0
  for (let i = heatmapData.value.length - 1; i >= 0; i--) {
    const week = heatmapData.value[i]
    for (let j = week.length - 1; j >= 0; j--) {
      if (week[j].count > 0) streak++
      else return streak
    }
  }
  return streak
})

const totalActiveDays = computed(() => {
  let total = 0
  for (const week of heatmapData.value) {
    for (const day of week) {
      if (day.count > 0) total++
    }
  }
  return total
})

const avgPerWeek = computed(() => {
  const total = totalActiveDays.value
  return total > 0 ? Math.round(total / 52) : 0
})

// ==================== 学习评估 ====================
const assessment = ref(null)

const dimLabels = ['知识掌握', '学习投入', '实践能力', '进步速度']

const dimScoresList = computed(() => {
  if (!assessment.value?.dimensionScores) return [0, 0, 0, 0]
  return dimLabels.map(l => assessment.value.dimensionScores[l] || 0)
})

const levelClass = computed(() => {
  const lv = assessment.value?.overallLevel
  if (lv === '优秀') return 'level-excellent'
  if (lv === '良好') return 'level-good'
  if (lv === '中等') return 'level-medium'
  return 'level-needs-work'
})

function getAxisPoint(index, value) {
  const angle = (Math.PI * 2 * index) / dimLabels.length - Math.PI / 2
  const r = (value / 100) * 85
  return { x: Math.cos(angle) * r, y: Math.sin(angle) * r }
}

function getRadarPoints(level) {
  return dimLabels.map((_, i) => {
    const p = getAxisPoint(i, level)
    return `${p.x},${p.y}`
  }).join(' ')
}

function getRadarPointsFromScores() {
  return dimScoresList.value.map((score, i) => {
    const p = getAxisPoint(i, score)
    return `${p.x},${p.y}`
  }).join(' ')
}

// ==================== 学习路径 ====================
const paths = ref([])

// ==================== 数据加载 ====================
onMounted(async () => {
  const userId = userStore.user?.id || 1
  try {
    const statsData = await getDashboardStats(userId)
    if (statsData && statsData.code === 200) {
      stats.value = statsData.data
      // 构建 activity map 用于热力图
      const activityMap = new Map()
      if (statsData.data.dailyActivities) {
        for (const act of statsData.data.dailyActivities) {
          activityMap.set(act.date, act.count)
        }
      }
      heatmapData.value = generateHeatmapData(activityMap)
    }
  } catch (e) {
    console.error('获取 Dashboard 数据失败:', e)
  }

  try {
    const pathData = await getLearningPaths(userId)
    if (pathData && pathData.code === 200 && pathData.data) {
      paths.value = pathData.data.map(p => ({
        id: p.id,
        name: p.pathName,
        nodes: p.nodesJson ? JSON.parse(p.nodesJson).length : 0,
        progress: p.nodesJson ? Math.round((p.currentNodeIndex / JSON.parse(p.nodesJson).length) * 100) : 0,
        updateTime: p.updatedAt ? p.updatedAt.slice(0, 10) : ''
      }))
    }
  } catch (e) {
    console.error('获取学习路径数据失败:', e)
  }

  // 加载学习评估数据
  try {
    const assessData = await getAssessment(userId)
    if (assessData && assessData.code === 200 && assessData.data) {
      assessment.value = assessData.data
    }
  } catch (e) {
    console.error('获取学习评估数据失败:', e)
  }
})
</script>

<style scoped lang="scss">
.stats-row {
  .stat-card {
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
  }
}

// ==================== 热力图 ====================
.heatmap-panel {
  margin-top: 24px;
  overflow: hidden;
}

.heatmap-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.heatmap-stats {
  display: flex;
  gap: 28px;
}

.heatmap-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;

  .stat-num {
    font-size: 20px;
    font-weight: 700;
    color: #4f6ef7;
    line-height: 1.2;
  }

  .stat-desc {
    font-size: 12px;
    color: #9ca3af;
    margin-top: 2px;
  }
}

.heatmap-wrapper {
  position: relative;
}

.heatmap-month-row {
  position: relative;
  height: 16px;
  margin-bottom: 4px;
  padding-left: 34px;
  overflow: hidden;

  span {
    position: absolute;
    font-size: 10px;
    color: #9ca3af;
    white-space: nowrap;
    top: 0;
    transform: translateX(-50%);
  }
}

.heatmap-body {
  display: flex;
  gap: 6px;
}

.heatmap-sidebar {
  position: relative;
  width: 28px;

  span {
    position: absolute;
    font-size: 10px;
    color: #9ca3af;
    line-height: 14px;
    height: 14px;

    &:nth-child(1) { top: 0px; }
    &:nth-child(2) { top: 34px; }
    &:nth-child(3) { top: 68px; }
    &:nth-child(4) { top: 102px; }
  }
}

.heatmap-grid {
  display: flex;
  gap: 3px;
  flex: 1;
  overflow-x: hidden;
}

.heatmap-week {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.heatmap-cell {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  background: #ebedf0;
  cursor: pointer;
  transition: all 0.15s;
  position: relative;

  &:hover {
    transform: scale(1.3);
    outline: 1px solid rgba(79, 110, 247, 0.3);
    outline-offset: 1px;
  }
}

.level-0 { background: #ebedf0; }
.level-1 { background: #c6d2ff; }
.level-2 { background: #8ba4ff; }
.level-3 { background: #4f6ef7; }
.level-4 { background: #2d4ad0; }

.heatmap-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  margin-top: 10px;
  padding-left: 24px;
  position: relative;
}

.heatmap-legend-label {
  font-size: 11px;
  color: #9ca3af;
}

.heatmap-legend {
  display: flex;
  gap: 3px;
}

// ==================== Tooltip ====================
.heatmap-tooltip {
  position: fixed;
  transform: translate(-50%, -100%);
  background: #1f2937;
  color: #fff;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.5;
  white-space: nowrap;
  z-index: 9999;
  pointer-events: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

  &::after {
    content: '';
    position: absolute;
    bottom: -6px;
    left: 50%;
    transform: translateX(-50%);
    border-left: 6px solid transparent;
    border-right: 6px solid transparent;
    border-top: 6px solid #1f2937;
  }

  .tooltip-date {
    font-size: 11px;
    color: #9ca3af;
  }
}

// ==================== 画像摘要面板 ====================
.profile-summary-panel {
  .profile-summary-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .profile-field {
    padding: 12px;
    background: #f9fafb;
    border-radius: 8px;
    min-height: 64px;

    .pf-label {
      display: block;
      font-size: 11px;
      color: #9ca3af;
      margin-bottom: 4px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .pf-value {
      font-size: 14px;
      color: #1f2937;
      line-height: 1.4;
      word-break: break-word;
    }
  }
}

// ==================== 快捷操作 ====================
.quick-actions {
  .action-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;

    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      padding: 20px 12px;
      background: #f9fafb;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.2s;
      font-size: 13px;
      color: #4b5563;

      &:hover {
        background: #eef2ff;
        color: #4f6ef7;
      }
    }
  }
}

// ==================== 学习评估 ====================
.assessment-panel {
  overflow: hidden;
}

.overall-score {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;

  .score-circle {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 4px solid;

    .score-num {
      font-size: 36px;
      font-weight: 800;
      line-height: 1;
    }

    .score-label {
      font-size: 12px;
      margin-top: 4px;
    }

    &.level-excellent { border-color: #10b981; .score-num { color: #059669; } }
    &.level-good { border-color: #4f6ef7; .score-num { color: #4f6ef7; } }
    &.level-medium { border-color: #f59e0b; .score-num { color: #d97706; } }
    &.level-needs-work { border-color: #ef4444; .score-num { color: #dc2626; } }
  }
}

.behavior-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;

  .behavior-item {
    text-align: center;
    padding: 10px;
    background: #f9fafb;
    border-radius: 8px;

    .b-num {
      display: block;
      font-size: 22px;
      font-weight: 700;
      color: #4f6ef7;
    }

    .b-label {
      font-size: 11px;
      color: #9ca3af;
      margin-top: 2px;
    }
  }
}

.radar-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;

  .radar-svg {
    width: 220px;
    height: 220px;
  }
}

.suggestions-box {
  .suggest-title {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 12px;
  }

  .suggest-item {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    padding: 8px 0;
    font-size: 12px;
    color: #6b7280;
    line-height: 1.5;
    border-bottom: 1px solid #f3f4f6;

    .el-icon {
      margin-top: 2px;
      flex-shrink: 0;
    }
  }
}
</style>