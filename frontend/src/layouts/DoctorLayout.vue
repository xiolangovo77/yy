<template>
  <el-container class="doctor-layout">
    <!-- 顶部导航 -->
    <el-header class="doctor-header">
      <div class="header-left">
        <h2 class="system-title">儿童早期疾病筛查评估系统</h2>
      </div>
      <div class="header-nav">
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          router
          background-color="#409EFF"
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          <el-menu-item index="/doctor/introduction">
            <el-icon><Document /></el-icon>
            <span>项目介绍</span>
          </el-menu-item>
          <el-menu-item index="/doctor/dashboard">
            <el-icon><List /></el-icon>
            <span>我的任务</span>
          </el-menu-item>
          <el-menu-item index="/doctor/profile">
            <el-icon><UserFilled /></el-icon>
            <span>个人主页</span>
          </el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <span class="welcome-text">欢迎，{{ userStore.userInfo.doctorName }}</span>
        <el-button type="warning" size="small" @click="handleLogout">退出</el-button>
      </div>
    </el-header>

    <!-- 内容区 -->
    <el-main class="doctor-main">
      <router-view />
    </el-main>
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

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.doctor-layout {
  height: 100%;
  flex-direction: column;
}

.doctor-header {
  display: flex;
  align-items: center;
  background: #409EFF;
  padding: 0 20px;
  height: 60px;
}

.system-title {
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  white-space: nowrap;
}

.header-nav {
  flex: 1;
  min-width: 0;
  margin: 0 30px;
}

.header-nav .el-menu {
  border-bottom: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.welcome-text {
  color: #fff;
  font-size: 16px;
}

.doctor-main {
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}
</style>
