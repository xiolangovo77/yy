<template>
  <el-container class="admin-layout">
    <!-- 左侧边栏 -->
    <el-aside width="220px" class="admin-aside">
      <div class="logo-container">
        <h2>筛查评估系统</h2>
        <span class="logo-sub">管理端</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>管理首页</span>
        </el-menu-item>
        <el-menu-item index="/admin/doctors">
          <el-icon><UserFilled /></el-icon>
          <span>医生账号管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/children">
          <el-icon><Avatar /></el-icon>
          <span>儿童信息管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/videos">
          <el-icon><VideoCameraFilled /></el-icon>
          <span>视频管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/reports">
          <el-icon><Document /></el-icon>
          <span>报告表管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tasks">
          <el-icon><List /></el-icon>
          <span>评估任务管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/introduction">
          <el-icon><Document /></el-icon>
          <span>项目介绍管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">管理端</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.userInfo.doctorName || '管理员' }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await userStore.logout()
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    // 跳转到个人资料（如果没有管理员profile页面，可以提示）
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100%;
}

.admin-aside {
  background-color: #304156;
  overflow-y: auto;
}

.logo-container {
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-container h2 {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.logo-sub {
  color: #bfcbd9;
  font-size: 12px;
}

.admin-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
  height: 50px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
}

.admin-main {
  background: #f0f2f5;
  min-height: calc(100vh - 50px);
}
</style>
