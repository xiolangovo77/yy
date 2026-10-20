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
          :ellipsis="false"
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
import { Document, List, UserFilled } from '@element-plus/icons-vue'

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
  min-height: 0;
}

@media (max-width: 1199px) {
  .doctor-header { height: auto; flex-wrap: wrap; gap: 8px 12px; padding: 12px 16px 0; }
  .header-left { flex: 1; min-width: 0; }
  .system-title { white-space: normal; font-size: 18px; }
  .header-nav { flex: 0 0 100%; order: 3; margin: 0; }
  .header-nav .el-menu { --el-menu-horizontal-height: 48px; }
  .header-nav :deep(.el-menu-item) { flex: 1; justify-content: center; padding: 0 8px; }
  .welcome-text { max-width: 160px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
}

@media (max-width: 767px) {
  .doctor-header { padding: 12px 12px 0; }
  .system-title { font-size: 17px; }
  .header-right { gap: 8px; }
  .welcome-text { display: none; }
  .header-nav :deep(.el-menu-item) { min-width: 0; padding: 0 4px; font-size: 14px; }
  .header-nav :deep(.el-menu-item .el-icon) { width: 18px; font-size: 18px; margin-right: 4px; }
}
</style>
