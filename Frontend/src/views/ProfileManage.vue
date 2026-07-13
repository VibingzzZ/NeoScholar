<template>
  <div class="profile-manage">
    <div class="page-header">
      <h2>学生画像管理</h2>
      <p class="subtitle">管理你的学习画像，让系统更懂你</p>
    </div>

    <el-row :gutter="24">
      <!-- 画像列表 -->
      <el-col :span="10">
        <div class="card-panel">
          <div class="card-title" style="display: flex; justify-content: space-between; align-items: center">
            <span>画像列表</span>
            <el-button type="primary" size="small" @click="showCreateDialog">新建画像</el-button>
          </div>

          <div class="profile-list">
            <div
              v-for="profile in profiles"
              :key="profile.id"
              class="profile-item"
              :class="{ active: currentId === profile.id }"
              @click="selectProfile(profile)"
            >
              <div class="profile-avatar">
                <el-avatar :size="40" icon="UserFilled" />
              </div>
              <div class="profile-info">
                <div class="profile-name">画像 #{{ profile.id }}</div>
                <div class="profile-major">{{ profile.majorOrField || '未设置专业' }}</div>
                <div class="profile-time">{{ profile.updateAt }}</div>
              </div>
              <el-icon class="arrow-right"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 画像详情/编辑 -->
      <el-col :span="14">
        <div class="card-panel" v-if="currentProfile">
          <div class="card-title">画像详情</div>
          <el-form :model="editForm" label-width="110px" label-position="left">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="专业/领域">
                  <el-input v-model="editForm.majorOrField" placeholder="例如：计算机科学与技术" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="思维风格">
                  <el-select v-model="editForm.cognitiveStyle" style="width: 100%">
                    <el-option label="逻辑分析型" value="逻辑分析型" />
                    <el-option label="直觉创新型" value="直觉创新型" />
                    <el-option label="实践操作型" value="实践操作型" />
                    <el-option label="理论抽象型" value="理论抽象型" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="学习目标">
              <el-input
                v-model="editForm.learningGoal"
                type="textarea"
                :rows="3"
                placeholder="描述你的学习目标..."
              />
            </el-form-item>

            <el-form-item label="知识基础">
              <el-input
                v-model="editForm.knowledgeBase"
                type="textarea"
                :rows="3"
                placeholder="描述你目前已掌握的知识..."
              />
            </el-form-item>

            <el-form-item label="常见错误">
              <el-input
                v-model="editForm.commonMistakes"
                type="textarea"
                :rows="2"
                placeholder="记录你常犯的错误类型..."
              />
            </el-form-item>

            <el-form-item label="交互偏好">
              <el-radio-group v-model="editForm.interactionPreference">
                <el-radio value="详细讲解">详细讲解</el-radio>
                <el-radio value="简洁提示">简洁提示</el-radio>
                <el-radio value="引导式提问">引导式提问</el-radio>
                <el-radio value="案例驱动">案例驱动</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存画像</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card-panel empty-state" v-else>
          <el-icon :size="48" color="#d1d5db"><UserFilled /></el-icon>
          <p>请从左侧选择一个画像查看详情</p>
        </div>
      </el-col>
    </el-row>

    <!-- 新建画像对话框 -->
    <el-dialog v-model="dialogVisible" title="新建学生画像" width="500px" :close-on-click-modal="false">
      <el-form :model="newForm" label-width="90px">
        <el-form-item label="专业/领域" required>
          <el-input v-model="newForm.majorOrField" placeholder="例如：软件工程" />
        </el-form-item>
        <el-form-item label="思维风格">
          <el-select v-model="newForm.cognitiveStyle" style="width: 100%">
            <el-option label="逻辑分析型" value="逻辑分析型" />
            <el-option label="直觉创新型" value="直觉创新型" />
            <el-option label="实践操作型" value="实践操作型" />
            <el-option label="理论抽象型" value="理论抽象型" />
          </el-select>
        </el-form-item>
        <el-form-item label="学习目标">
          <el-input v-model="newForm.learningGoal" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createNewProfile">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, ArrowRight } from '@element-plus/icons-vue'
import { listProfiles, createProfile, updateProfile } from '@/api/profile'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userId = computed(() => userStore.user?.id || 1)

const currentId = ref(null)
const currentProfile = ref(null)
const dialogVisible = ref(false)

const editForm = reactive({
  majorOrField: '',
  learningGoal: '',
  knowledgeBase: '',
  cognitiveStyle: '',
  commonMistakes: '',
  interactionPreference: ''
})

const newForm = reactive({
  majorOrField: '',
  cognitiveStyle: '逻辑分析型',
  learningGoal: ''
})

const profiles = ref([])

// 加载画像列表
async function loadProfiles() {
  try {
    const res = await listProfiles(userId.value)
    if (res && res.code === 200 && res.data) {
      profiles.value = res.data
    }
  } catch (e) {
    console.error('加载画像列表失败:', e)
  }
}

onMounted(() => {
  loadProfiles()
})

function selectProfile(profile) {
  currentId.value = profile.id
  currentProfile.value = profile
  Object.assign(editForm, {
    majorOrField: profile.majorOrField || '',
    learningGoal: profile.learningGoal || '',
    knowledgeBase: profile.knowledgeBase || '',
    cognitiveStyle: profile.cognitiveStyle || '',
    commonMistakes: profile.commonMistakes || '',
    interactionPreference: profile.interactionPreference || ''
  })
}

async function saveProfile() {
  if (!currentProfile.value) return
  try {
    const data = { ...editForm, id: currentProfile.value.id, userId: currentProfile.value.userId }
    const res = await updateProfile(data)
    if (res && res.code === 200) {
      Object.assign(currentProfile.value, { ...editForm, updateAt: new Date().toISOString().slice(0, 10) })
      ElMessage.success('画像保存成功')
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败，请检查网络')
  }
}

function resetForm() {
  if (currentProfile.value) {
    selectProfile(currentProfile.value)
  }
}

function showCreateDialog() {
  newForm.majorOrField = ''
  newForm.cognitiveStyle = '逻辑分析型'
  newForm.learningGoal = ''
  dialogVisible.value = true
}

async function createNewProfile() {
  if (!newForm.majorOrField) {
    ElMessage.warning('请输入专业/领域')
    return
  }
  try {
    const data = {
      userId: userId.value,
      majorOrField: newForm.majorOrField,
      learningGoal: newForm.learningGoal,
      knowledgeBase: '',
      cognitiveStyle: newForm.cognitiveStyle,
      commonMistakes: '',
      interactionPreference: '详细讲解'
    }
    const res = await createProfile(data)
    if (res && res.code === 200) {
      dialogVisible.value = false
      ElMessage.success('画像创建成功')
      await loadProfiles()
    } else {
      ElMessage.error(res?.message || '创建失败')
    }
  } catch (e) {
    ElMessage.error('创建失败，请检查网络')
  }
}
</script>

<style scoped lang="scss">
.profile-list {
  .profile-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 14px 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: background 0.2s;
    margin-bottom: 4px;

    &:hover {
      background: #f9fafb;
    }

    &.active {
      background: #eef2ff;
      border: 1px solid #c7d2fe;

      .arrow-right {
        opacity: 1;
      }
    }

    .profile-info {
      flex: 1;
      min-width: 0;

      .profile-name {
        font-weight: 600;
        font-size: 14px;
        color: #1f2937;
      }

      .profile-major {
        font-size: 12px;
        color: #6b7280;
        margin-top: 2px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .profile-time {
        font-size: 11px;
        color: #9ca3af;
        margin-top: 2px;
      }
    }

    .arrow-right {
      color: #9ca3af;
      opacity: 0;
      transition: opacity 0.2s;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #9ca3af;

  p {
    margin-top: 12px;
    font-size: 14px;
  }
}
</style>