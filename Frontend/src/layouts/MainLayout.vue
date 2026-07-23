<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarCollapsed ? '64px' : '220px'" class="layout-aside">
      <div class="logo-area" @click="$router.push('/dashboard')">
        <el-icon :size="28" color="#4f6ef7"><Sunny /></el-icon>
        <span v-show="!sidebarCollapsed" class="logo-text">NeoScholar</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        :collapse-transition="false"
        router
        background-color="#1a1d29"
        text-color="#9ca3af"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>学习仪表盘</template>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><UserFilled /></el-icon>
          <template #title>学生画像</template>
        </el-menu-item>
        <el-menu-item index="/learning-path">
          <el-icon><MapLocation /></el-icon>
          <template #title>学习路径</template>
        </el-menu-item>
        <el-menu-item index="/consultant">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>智能辅导</template>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer" @click="toggleSidebar">
        <el-icon :size="18"><DArrowLeft v-if="!sidebarCollapsed" /><DArrowRight v-else /></el-icon>
      </div>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="pageTitle">{{ pageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-badge :value="0" :max="99" class="notice-badge" :hidden="true">
            <el-icon :size="20"><Bell /></el-icon>
          </el-badge>
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ store.user?.username || '未登录' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人设置</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Sunny, Odometer, UserFilled, MapLocation,
  ChatDotRound, Bell, ArrowDown, DArrowLeft, DArrowRight
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const sidebarCollapsed = computed(() => store.sidebarCollapsed)
const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '')

// 应用启动时加载活跃画像
onMounted(() => {
  store.loadActiveProfile()
})

function toggleSidebar() {
  store.toggleSidebar()
}

function handleLogout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.layout-aside {
  background: #1a1d29;
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease;
  overflow: hidden;

  .logo-area {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    cursor: pointer;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);

    .logo-text {
      font-size: 17px;
      font-weight: 700;
      color: #fff;
      letter-spacing: 0.5px;
    }
  }

  .el-menu {
    flex: 1;
    border-right: none;
    padding-top: 8px;
  }

  .sidebar-footer {
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #6b7280;
    cursor: pointer;
    border-top: 1px solid rgba(255, 255, 255, 0.06);
    transition: background 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.04);
    }
  }
}

.layout-header {
  height: 60px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 24px;

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .notice-badge {
      cursor: pointer;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      font-size: 14px;
      color: #374151;
    }
  }
}

.layout-main {
  background: #f3f4f6;
  min-height: calc(100vh - 60px);
  padding: 24px;
}
</style>