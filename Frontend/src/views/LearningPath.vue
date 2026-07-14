<template>
  <div class="learning-path">
    <div class="page-header">
      <h2>学习路径</h2>
      <p class="subtitle">AI 为你量身定制的学习路线图</p>
    </div>

    <!-- 路径选择标签 -->
    <el-tabs v-model="activeTab" type="card" class="path-tabs">
      <el-tab-pane
        v-for="path in learningPaths"
        :key="path.id"
        :label="path.pathName"
        :name="String(path.id)"
      />
    </el-tabs>

    <!-- 当前路径详情 -->
    <div class="path-content" v-if="currentPath">
      <div class="path-info-bar">
        <div class="info-item">
          <span class="info-label">总节点</span>
          <span class="info-value">{{ currentNodeList.length }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">当前进度</span>
          <el-progress
            :percentage="progressPercent"
            :stroke-width="8"
            :color="'#4f6ef7'"
            style="width: 120px"
          />
        </div>
        <div class="info-item">
          <span class="info-label">预计总天数</span>
          <span class="info-value">{{ totalDays }} 天</span>
        </div>
        <el-button type="primary" plain @click="regeneratePath">
          <el-icon><Refresh /></el-icon> 重新生成
        </el-button>
      </div>

      <!-- 时间线 -->
      <div class="timeline-wrapper">
        <div class="timeline">
          <div
            v-for="(node, index) in currentNodeList"
            :key="node.stepOrder"
            class="timeline-node"
            :class="{
              completed: index < currentPath.currentNodeIndex,
              active: index === currentPath.currentNodeIndex,
              pending: index > currentPath.currentNodeIndex
            }"
            @click="selectNode(index)"
          >
            <div class="node-dot">
              <el-icon v-if="index < currentPath.currentNodeIndex"><Check /></el-icon>
              <span v-else>{{ node.stepOrder }}</span>
            </div>
            <div class="node-line" v-if="index < currentNodeList.length - 1"></div>
            <div class="node-card" :class="{ expanded: selectedNodeIndex === index }">
              <div class="node-title">{{ node.title }}</div>
              <div class="node-meta">{{ node.estimatedDays }} 天</div>
              <div class="node-desc" v-if="selectedNodeIndex === index">
                {{ node.description }}
                <div class="node-actions">
                  <el-button
                    v-if="index === currentPath.currentNodeIndex"
                    type="primary"
                    size="small"
                    @click.stop="completeNode(index)"
                  >
                    标记完成
                  </el-button>
                  <el-tag v-else-if="index < currentPath.currentNodeIndex" type="success" size="small">
                    已完成
                  </el-tag>
                </div>
                <!-- 节点对应资源下载 -->
                <div class="node-resources" v-if="getNodeResources(node.stepOrder).length > 0">
                  <div class="resource-label">AI 生成资源</div>
                  <div
                    v-for="res in getNodeResources(node.stepOrder)"
                    :key="res.id"
                    class="resource-item"
                  >
                    <div class="resource-info">
                      <el-tag :type="res.resourceType === 'quiz' ? 'warning' : 'primary'" size="small">
                        {{ res.resourceType === 'quiz' ? '测验' : 'PPT大纲' }}
                      </el-tag>
                      <span class="resource-title">{{ res.title }}</span>
                    </div>
                    <el-button type="primary" link size="small" @click.stop="downloadFile(res)">
                      <el-icon><Download /></el-icon> 下载
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="card-panel empty-state" v-else>
      <el-icon :size="56" color="#d1d5db"><MapLocation /></el-icon>
      <h3>还没有学习路径</h3>
      <p>前往「学生画像」完善你的画像后，系统将为你生成专属学习路径</p>
      <el-button type="primary" style="margin-top: 16px" @click="$router.push('/profile')">
        去完善画像
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Check, MapLocation, Download } from '@element-plus/icons-vue'
import { getLearningPaths, updateProgress, generatePath, getPathResources, downloadResource } from '@/api/path'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userId = computed(() => userStore.user?.id || 1)

const activeTab = ref('')
const selectedNodeIndex = ref(0)
const loading = ref(false)

const learningPaths = ref([])
const pathResources = ref([])  // 当前路径的所有学习资源

const currentPath = computed(() =>
  learningPaths.value.find((p) => String(p.id) === activeTab.value)
)

const currentNodeList = computed(() => {
  if (!currentPath.value) return []
  try {
    return typeof currentPath.value.nodesJson === 'string'
      ? JSON.parse(currentPath.value.nodesJson)
      : currentPath.value.nodesJson
  } catch {
    return []
  }
})

const progressPercent = computed(() => {
  if (!currentPath.value || currentNodeList.value.length === 0) return 0
  return Math.round((currentPath.value.currentNodeIndex / currentNodeList.value.length) * 100)
})

const totalDays = computed(() =>
  currentNodeList.value.reduce((sum, n) => sum + (n.estimatedDays || 0), 0)
)

function selectNode(index) {
  selectedNodeIndex.value = selectedNodeIndex.value === index ? -1 : index
}

async function completeNode(index) {
  if (!currentPath.value || index !== currentPath.value.currentNodeIndex) return
  const newIndex = index + 1
  try {
    const res = await updateProgress(currentPath.value.id, newIndex)
    if (res && res.code === 200) {
      currentPath.value.currentNodeIndex = newIndex
      ElMessage.success('节点已完成，继续加油！')
    } else {
      ElMessage.error(res?.message || '更新失败')
    }
  } catch (e) {
    ElMessage.error('更新进度失败，请检查网络')
  }
}

/** 获取指定节点序号对应的资源列表 */
function getNodeResources(nodeIndex) {
  return pathResources.value.filter(r => r.nodeIndex === nodeIndex)
}

/** 下载资源文件 */
async function downloadFile(resource) {
  try {
    const fileName = `${resource.title}.txt`
    await downloadResource(resource.id, fileName)
    ElMessage.success('下载成功')
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

/** 加载当前路径的资源列表 */
async function loadResources(pathId) {
  try {
    const res = await getPathResources(pathId)
    if (res && res.code === 200 && res.data) {
      pathResources.value = res.data
    } else {
      pathResources.value = []
    }
  } catch (e) {
    pathResources.value = []
  }
}

async function regeneratePath() {
  loading.value = true
  try {
    const res = await generatePath(userId.value)
    if (res && res.code === 200) {
      ElMessage.success('学习路径已重新生成')
      await loadPaths()
    } else {
      ElMessage.error(res?.message || '生成失败，请确认已创建学生画像')
    }
  } catch (e) {
    ElMessage.error('生成失败，请检查网络')
  } finally {
    loading.value = false
  }
}

async function loadPaths() {
  try {
    const res = await getLearningPaths(userId.value)
    if (res && res.code === 200 && res.data) {
      learningPaths.value = res.data
      if (res.data.length > 0 && !activeTab.value) {
        activeTab.value = String(res.data[0].id)
      }
    }
  } catch (e) {
    console.error('加载学习路径失败:', e)
  }
}

// 切换路径标签时自动加载资源
watch(activeTab, (newPathId) => {
  if (newPathId) {
    loadResources(Number(newPathId))
  }
})

onMounted(() => {
  loadPaths().then(() => {
    if (activeTab.value) {
      loadResources(Number(activeTab.value))
    }
  })
})
</script>

<style scoped lang="scss">
.path-tabs {
  margin-bottom: 20px;
}

.path-info-bar {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 10px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

  .info-item {
    display: flex;
    align-items: center;
    gap: 10px;

    .info-label {
      font-size: 13px;
      color: #6b7280;
    }

    .info-value {
      font-size: 18px;
      font-weight: 700;
      color: #1f2937;
    }
  }
}

.timeline-wrapper {
  overflow-x: auto;
  padding-bottom: 16px;
}

.timeline {
  display: flex;
  align-items: flex-start;
  min-width: max-content;
  padding: 20px 0;
}

.timeline-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  cursor: pointer;

  .node-dot {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
    z-index: 2;
    transition: all 0.3s;
    flex-shrink: 0;
  }

  .node-line {
    position: absolute;
    top: 20px;
    left: calc(50% + 20px);
    width: 180px;
    height: 3px;
    z-index: 1;
  }

  .node-card {
    margin-top: 14px;
    width: 180px;
    padding: 12px;
    border-radius: 10px;
    text-align: center;
    transition: all 0.3s;

    .node-title {
      font-size: 14px;
      font-weight: 600;
      margin-bottom: 4px;
    }

    .node-meta {
      font-size: 12px;
      color: #9ca3af;
    }

    .node-desc {
      margin-top: 10px;
      font-size: 12px;
      line-height: 1.5;
      text-align: left;
      color: #6b7280;
    }

    .node-actions {
      margin-top: 10px;
    }
  }

  &.completed {
    .node-dot {
      background: #10b981;
      color: #fff;
    }
    .node-line {
      background: #10b981;
    }
    .node-card {
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
      .node-title { color: #065f46; }
    }
  }

  &.active {
    .node-dot {
      background: #4f6ef7;
      color: #fff;
      box-shadow: 0 0 0 6px rgba(79, 110, 247, 0.15);
    }
    .node-line {
      background: #e5e7eb;
    }
    .node-card {
      background: #eef2ff;
      border: 1px solid #c7d2fe;
      .node-title { color: #3730a3; }
    }
  }

  &.pending {
    .node-dot {
      background: #f3f4f6;
      color: #9ca3af;
    }
    .node-line {
      background: #f3f4f6;
    }
    .node-card {
      background: #fafafa;
      border: 1px solid #f3f4f6;
      .node-title { color: #9ca3af; }
    }
  }
}

.node-resources {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e5e7eb;

  .resource-label {
    font-size: 11px;
    color: #9ca3af;
    margin-bottom: 6px;
  }

  .resource-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 6px 8px;
    border-radius: 6px;
    background: #f9fafb;
    margin-top: 4px;

    .resource-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .resource-title {
        font-size: 12px;
        color: #374151;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 360px;
  color: #9ca3af;

  h3 {
    margin-top: 16px;
    color: #6b7280;
  }

  p {
    margin-top: 8px;
    font-size: 14px;
  }
}
</style>