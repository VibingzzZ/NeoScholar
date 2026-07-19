<template>
  <div class="profile-merge">
    <div class="page-header">
      <h2>画像合并</h2>
      <p class="subtitle">将两份学生画像智能合并，综合评估你的学习情况</p>
    </div>

    <el-row :gutter="24">
      <!-- 原始画像 -->
      <el-col :span="8">
        <div class="card-panel profile-compare">
          <div class="compare-header original">
            <el-tag type="primary" effect="dark">原始画像</el-tag>
            <el-select v-model="originalId" placeholder="选择画像" style="width: 160px; margin-left: 12px">
              <el-option
                v-for="p in availableProfiles"
                :key="p.id"
                :label="'画像 #' + p.id + ' - ' + p.majorOrField"
                :value="p.id"
              />
            </el-select>
          </div>
          <div class="profile-detail" v-if="originalProfile">
            <div class="detail-row">
              <span class="detail-label">专业/领域</span>
              <span class="detail-value">{{ originalProfile.majorOrField }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">学习目标</span>
              <span class="detail-value">{{ originalProfile.learningGoal }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">知识基础</span>
              <span class="detail-value">{{ originalProfile.knowledgeBase }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">思维风格</span>
              <span class="detail-value">{{ originalProfile.cognitiveStyle }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">常见错误</span>
              <span class="detail-value">{{ originalProfile.commonMistakes }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">交互偏好</span>
              <span class="detail-value">{{ originalProfile.interactionPreference }}</span>
            </div>
          </div>
          <div class="empty-hint" v-else>
            <el-icon :size="36"><Document /></el-icon>
            <p>请选择原始画像</p>
          </div>
        </div>
      </el-col>

      <!-- 合并按钮 -->
      <el-col :span="2" class="merge-center">
        <div class="merge-action">
          <el-button
            type="primary"
            :icon="Connection"
            circle
            size="large"
            :loading="merging"
            :disabled="!originalId || !targetId"
            @click="doMerge"
          />
          <span class="merge-label">AI 合并</span>
        </div>
      </el-col>

      <!-- 目标画像 -->
      <el-col :span="8">
        <div class="card-panel profile-compare">
          <div class="compare-header target">
            <el-tag type="success" effect="dark">目标画像</el-tag>
            <el-select v-model="targetId" placeholder="选择画像" style="width: 160px; margin-left: 12px">
              <el-option
                v-for="p in availableProfiles"
                :key="p.id"
                :label="'画像 #' + p.id + ' - ' + p.majorOrField"
                :value="p.id"
              />
            </el-select>
          </div>
          <div class="profile-detail" v-if="targetProfile">
            <div class="detail-row">
              <span class="detail-label">专业/领域</span>
              <span class="detail-value">{{ targetProfile.majorOrField }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">学习目标</span>
              <span class="detail-value">{{ targetProfile.learningGoal }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">知识基础</span>
              <span class="detail-value">{{ targetProfile.knowledgeBase }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">思维风格</span>
              <span class="detail-value">{{ targetProfile.cognitiveStyle }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">常见错误</span>
              <span class="detail-value">{{ targetProfile.commonMistakes }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">交互偏好</span>
              <span class="detail-value">{{ targetProfile.interactionPreference }}</span>
            </div>
          </div>
          <div class="empty-hint" v-else>
            <el-icon :size="36"><Document /></el-icon>
            <p>请选择目标画像</p>
          </div>
        </div>
      </el-col>

      <!-- 合并结果 -->
      <el-col :span="6">
        <div class="card-panel profile-compare result-panel" v-if="mergedResult">
          <div class="compare-header result">
            <el-tag type="warning" effect="dark">合并结果</el-tag>
          </div>
          <div class="profile-detail">
            <div class="detail-row">
              <span class="detail-label">专业/领域</span>
              <span class="detail-value highlight">{{ mergedResult.majorOrField }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">学习目标</span>
              <span class="detail-value highlight">{{ mergedResult.learningGoal }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">知识基础</span>
              <span class="detail-value highlight">{{ mergedResult.knowledgeBase }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">思维风格</span>
              <span class="detail-value highlight">{{ mergedResult.cognitiveStyle }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">常见错误</span>
              <span class="detail-value highlight">{{ mergedResult.commonMistakes }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">交互偏好</span>
              <span class="detail-value highlight">{{ mergedResult.interactionPreference }}</span>
            </div>
          </div>
          <el-button type="primary" plain style="width: 100%; margin-top: 16px" @click="applyMerge">
            应用合并结果
          </el-button>
        </div>

        <div class="card-panel result-panel empty-state" v-else>
          <el-icon :size="40" color="#d1d5db"><MagicStick /></el-icon>
          <p>AI 合并结果将在这里展示</p>
        </div>
      </el-col>
    </el-row>

    <!-- 合并历史 -->
    <div class="card-panel" style="margin-top: 24px">
      <div class="card-title">合并历史</div>
      <el-table :data="mergeHistory" style="width: 100%" stripe>
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column label="原始画像" width="120">
          <template #default="{ row }">画像 #{{ row.originalId }}</template>
        </el-table-column>
        <el-table-column label="目标画像" width="120">
          <template #default="{ row }">画像 #{{ row.targetId }}</template>
        </el-table-column>
        <el-table-column prop="resultSummary" label="合并摘要" min-width="200" />
        <el-table-column prop="time" label="合并时间" width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Connection, Document, MagicStick } from '@element-plus/icons-vue'
import { listProfiles, mergeProfiles, getProfile, getMergeHistory } from '@/api/profile'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userId = computed(() => userStore.user?.id || 1)

const originalId = ref(null)
const targetId = ref(null)
const merging = ref(false)
const mergedResult = ref(null)

const availableProfiles = ref([])

const originalProfile = computed(() =>
  availableProfiles.value.find((p) => p.id === originalId.value)
)

const targetProfile = computed(() =>
  availableProfiles.value.find((p) => p.id === targetId.value)
)

const mergeHistory = ref([])

// 保存合并前原始画像的 updateAt，用于可靠轮询
let originalUpdateAtBeforeMerge = null

// 加载可用画像列表
async function loadProfiles() {
  try {
    const res = await listProfiles(userId.value)
    if (res && res.code === 200 && res.data) {
      availableProfiles.value = res.data
    }
  } catch (e) {
    console.error('加载画像列表失败:', e)
  }
}

async function loadMergeHistory() {
  try {
    const res = await getMergeHistory(userId.value)
    if (res && res.code === 200 && res.data) {
      mergeHistory.value = res.data.map(h => ({
        id: h.id,
        originalId: h.originalId,
        targetId: h.targetId,
        resultSummary: h.resultSummary || '',
        time: h.mergedAt ? h.mergedAt.slice(0, 16).replace('T', ' ') : '',
        status: h.status || 'success'
      }))
    }
  } catch (e) {
    console.error('加载合并历史失败:', e)
  }
}

onMounted(() => {
  loadProfiles()
  loadMergeHistory()
})

async function doMerge() {
  if (!originalId.value || !targetId.value) {
    ElMessage.warning('请选择两份画像')
    return
  }
  if (originalId.value === targetId.value) {
    ElMessage.warning('请选择两份不同的画像')
    return
  }
  merging.value = true
  mergedResult.value = null

  // 保存合并前的 updateAt 用于轮询检测
  originalUpdateAtBeforeMerge = originalProfile.value?.updateAt || null

  try {
    const res = await mergeProfiles(originalId.value, targetId.value)
    if (res && res.code === 200) {
      ElMessage.success('合并任务已提交，AI 正在后台处理...')
      // 轮询获取合并结果（最多等待 30 秒）
      const mergedProfile = await pollMergedResult(originalId.value, 30)
      if (mergedProfile) {
        mergedResult.value = mergedProfile
        ElMessage.success('画像合并完成！')
        // 刷新合并历史
        await loadMergeHistory()
        await loadProfiles()
      } else {
        ElMessage.info('合并处理中，请稍后刷新画像列表查看结果')
      }
    } else {
      ElMessage.error(res?.message || '合并失败')
    }
  } catch (e) {
    ElMessage.error('合并请求失败，请检查网络')
  } finally {
    merging.value = false
    originalUpdateAtBeforeMerge = null
  }
}

// 轮询等待合并结果
async function pollMergedResult(profileId, maxSeconds) {
  for (let i = 0; i < maxSeconds; i++) {
    await new Promise(resolve => setTimeout(resolve, 1000))
    try {
      const res = await getProfile(profileId)
      if (res && res.code === 200 && res.data) {
        // 检查画像是否已被更新（对比合并前保存的 updateAt）
        if (res.data.updateAt && res.data.updateAt !== originalUpdateAtBeforeMerge) {
          return res.data
        }
      }
    } catch {}
  }
  return null
}

function applyMerge() {
  ElMessage.success('合并结果已保存到画像中')
  // 刷新画像列表
  loadProfiles()
}
</script>

<style scoped lang="scss">
.profile-compare {
  min-height: 380px;

  .compare-header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 2px solid #f3f4f6;
  }

  .profile-detail {
    .detail-row {
      display: flex;
      padding: 8px 0;
      border-bottom: 1px solid #f9fafb;
      font-size: 13px;

      .detail-label {
        width: 80px;
        flex-shrink: 0;
        color: #6b7280;
      }

      .detail-value {
        flex: 1;
        color: #374151;
        word-break: break-all;

        &.highlight {
          color: #4f6ef7;
          font-weight: 500;
        }
      }
    }
  }

  .empty-hint {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 260px;
    color: #d1d5db;

    p {
      margin-top: 10px;
      font-size: 13px;
    }
  }
}

.merge-center {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 !important;

  .merge-action {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;

    .merge-label {
      font-size: 12px;
      color: #4f6ef7;
      font-weight: 600;
    }
  }
}

.result-panel {
  border: 1px solid #c7d2fe;
  background: #fafbff;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 380px;
  color: #9ca3af;

  p {
    margin-top: 12px;
    font-size: 14px;
  }
}
</style>