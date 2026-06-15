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
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Check, MapLocation } from '@element-plus/icons-vue'

const activeTab = ref('1')
const selectedNodeIndex = ref(0)

const learningPaths = ref([
  {
    id: 1,
    userId: 1,
    pathName: 'Spring Boot 微服务专属学习路径',
    currentNodeIndex: 2,
    nodesJson: JSON.stringify([
      { stepOrder: 1, title: 'Java 基础巩固', description: '回顾 Java 核心语法、面向对象编程、集合框架与异常处理机制，为后续框架学习打好基础。', estimatedDays: 5 },
      { stepOrder: 2, title: 'Spring 框架入门', description: '学习 Spring IoC 容器、依赖注入、AOP 面向切面编程等核心概念，理解 Spring 的设计思想。', estimatedDays: 7 },
      { stepOrder: 3, title: 'Spring Boot 核心', description: '掌握 Spring Boot 自动配置、起步依赖、配置文件管理，学习构建 RESTful API 的最佳实践。', estimatedDays: 10 },
      { stepOrder: 4, title: '数据库与 MyBatis-Plus', description: '深入学习 MySQL 数据库设计与优化，结合 MyBatis-Plus 实现高效的 ORM 数据访问层。', estimatedDays: 8 },
      { stepOrder: 5, title: '微服务架构设计', description: '学习 Spring Cloud 微服务治理，包括服务注册发现、配置中心、网关路由、熔断降级等核心组件。', estimatedDays: 14 },
      { stepOrder: 6, title: '项目实战', description: '基于所学的技术栈，从零搭建一个完整的微服务项目，涵盖用户、订单、商品等核心业务模块。', estimatedDays: 15 }
    ])
  },
  {
    id: 2,
    userId: 1,
    pathName: '数据结构与算法精讲专属学习路径',
    currentNodeIndex: 0,
    nodesJson: JSON.stringify([
      { stepOrder: 1, title: '复杂度分析', description: '学习时间复杂度和空间复杂度的分析方法，掌握大 O 表示法的实际应用。', estimatedDays: 3 },
      { stepOrder: 2, title: '数组与链表', description: '深入理解数组和链表的底层实现，对比两者在不同场景下的性能差异。', estimatedDays: 4 },
      { stepOrder: 3, title: '栈与队列', description: '学习栈和队列的 ADT 定义及实现，掌握单调栈、优先队列等进阶技巧。', estimatedDays: 4 },
      { stepOrder: 4, title: '树与图', description: '掌握二叉树、平衡树、图的遍历算法，理解 DFS/BFS 在树和图问题中的应用。', estimatedDays: 10 },
      { stepOrder: 5, title: '动态规划', description: '从经典 DP 问题入手，学习记忆化搜索、状态转移方程的推导与优化策略。', estimatedDays: 12 }
    ])
  }
])

const currentPath = computed(() =>
  learningPaths.value.find((p) => String(p.id) === activeTab.value)
)

const currentNodeList = computed(() => {
  if (!currentPath.value) return []
  return JSON.parse(currentPath.value.nodesJson)
})

const progressPercent = computed(() => {
  if (!currentPath.value || currentNodeList.value.length === 0) return 0
  return Math.round((currentPath.value.currentNodeIndex / currentNodeList.value.length) * 100)
})

const totalDays = computed(() =>
  currentNodeList.value.reduce((sum, n) => sum + n.estimatedDays, 0)
)

function selectNode(index) {
  selectedNodeIndex.value = selectedNodeIndex.value === index ? -1 : index
}

function completeNode(index) {
  if (currentPath.value && index === currentPath.value.currentNodeIndex) {
    currentPath.value.currentNodeIndex++
    ElMessage.success('节点已完成，继续加油！')
  }
}

function regeneratePath() {
  ElMessage.info('正在重新生成学习路径...')
}
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