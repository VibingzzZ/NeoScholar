<template>
  <div class="login-page">
    <div class="login-wrapper">
      <div class="login-banner">
        <div class="banner-content">
          <div class="banner-icon">
            <el-icon :size="48"><Sunny /></el-icon>
          </div>
          <h1>NeoScholar</h1>
          <p class="banner-desc">AI 驱动的智能学伴平台，为每位学生提供个性化学习方案</p>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon><UserFilled /></el-icon>
              <span>精准学生画像</span>
            </div>
            <div class="feature-item">
              <el-icon><MapLocation /></el-icon>
              <span>定制学习路径</span>
            </div>
            <div class="feature-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>AI 智能辅导</span>
            </div>
          </div>
        </div>
      </div>

      <div class="login-form-area">
        <div class="form-card">
          <h2>{{ isRegister ? '创建账号' : '欢迎回来' }}</h2>
          <p class="form-subtitle">{{ isRegister ? '注册新账号，开启学习之旅' : '登录你的账号，继续学习之旅' }}</p>

          <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                :placeholder="isRegister ? '请输入密码（至少6位）' : '请输入密码'"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleSubmit"
              />
            </el-form-item>

            <!-- 注册模式下显示确认密码 -->
            <el-form-item prop="confirmPassword" v-if="isRegister">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleSubmit"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="loading" class="login-btn" @click="handleSubmit">
                {{ isRegister ? '注 册' : '登 录' }}
              </el-button>
            </el-form-item>

            <div class="form-extra" v-if="!isRegister">
              <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
              <a href="#">忘记密码？</a>
            </div>
          </el-form>

          <div class="register-link">
            {{ isRegister ? '已有账号？' : '还没有账号？' }}
            <a href="#" @click.prevent="isRegister = !isRegister; form.confirmPassword = ''">
              {{ isRegister ? '去登录' : '立即注册' }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Sunny, UserFilled, MapLocation, ChatDotRound, User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { login as loginApi, register as registerApi } from '@/api/auth'

const router = useRouter()
const store = useUserStore()

const isRegister = ref(false)
const form = reactive({ username: '', password: '', confirmPassword: '' })
const rememberMe = ref(false)
const loading = ref(false)

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback(new Error('请再次输入密码'))
        } else if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

async function handleSubmit() {
  loading.value = true
  try {
    const api = isRegister.value ? registerApi : loginApi
    const res = await api(form.username, form.password)
    if (res && res.code === 200 && res.data) {
      const { userId, username, token } = res.data
      store.login({ id: userId, username, avatar: '' }, token)
      if (rememberMe.value && !isRegister.value) {
        localStorage.setItem('rememberedUser', form.username)
      }
      ElMessage.success(isRegister.value ? '注册成功！' : '登录成功，欢迎回来！')
      router.push('/dashboard')
    } else {
      ElMessage.error(res?.message || (isRegister.value ? '注册失败' : '登录失败'))
    }
  } catch (e) {
    ElMessage.error(isRegister.value ? '注册失败，请检查网络连接' : '登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
}

.login-wrapper {
  display: flex;
  width: 960px;
  min-height: 560px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(79, 110, 247, 0.15);
}

.login-banner {
  flex: 1;
  background: linear-gradient(135deg, #4f6ef7 0%, #6d8afb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 48px;

  .banner-content {
    color: #fff;
    text-align: center;

    .banner-icon {
      margin-bottom: 20px;
    }

    h1 {
      font-size: 32px;
      font-weight: 700;
      margin-bottom: 12px;
      letter-spacing: 1px;
    }

    .banner-desc {
      font-size: 14px;
      opacity: 0.85;
      line-height: 1.6;
      margin-bottom: 36px;
    }

    .feature-list {
      display: flex;
      flex-direction: column;
      gap: 16px;

      .feature-item {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 14px;
        opacity: 0.9;
      }
    }
  }
}

.login-form-area {
  flex: 1;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 48px;

  .form-card {
    width: 100%;
    max-width: 340px;

    h2 {
      font-size: 26px;
      font-weight: 700;
      color: #111827;
      margin-bottom: 8px;
    }

    .form-subtitle {
      font-size: 14px;
      color: #9ca3af;
      margin-bottom: 32px;
    }

    .login-btn {
      width: 100%;
      height: 44px;
      font-size: 15px;
      font-weight: 600;
      letter-spacing: 4px;
    }

    .form-extra {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 13px;
      margin-top: -8px;

      a {
        color: #4f6ef7;
        text-decoration: none;

        &:hover { text-decoration: underline; }
      }
    }

    .register-link {
      text-align: center;
      margin-top: 28px;
      font-size: 14px;
      color: #6b7280;

      a {
        color: #4f6ef7;
        text-decoration: none;
        font-weight: 500;

        &:hover { text-decoration: underline; }
      }
    }
  }
}
</style>