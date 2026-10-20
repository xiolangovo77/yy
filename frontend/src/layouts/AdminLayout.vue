<template>
  <el-container class="admin-layout">
    <!-- 左侧边栏 -->
    <el-aside v-if="!compactNavigation" width="220px" class="admin-aside">
      <div class="logo-container">
        <h2>筛查评估系统</h2>
        <span class="logo-sub">管理端</span>
      </div>
      <AdminNavigation />
    </el-aside>

    <el-drawer v-if="compactNavigation" v-model="menuOpen" title="管理端导航"
      direction="ltr" size="min(280px, 86vw)" class="admin-navigation-drawer">
      <AdminNavigation @navigate="menuOpen = false" />
    </el-drawer>

    <!-- 右侧主体 -->
    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-button v-if="compactNavigation" :icon="Menu" aria-label="打开管理菜单"
            :aria-expanded="menuOpen" @click="menuOpen = true" />
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">管理端</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              <span class="user-name">{{ userStore.userInfo.doctorName || '管理员' }}</span>
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
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ArrowDown, Menu, UserFilled } from '@element-plus/icons-vue'
import AdminNavigation from '@/components/AdminNavigation.vue'
import { useMediaQuery } from '@/composables/useMediaQuery'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const compactNavigation = useMediaQuery('(max-width: 1023px)')
const menuOpen = ref(false)
watch([() => route.path, compactNavigation], () => { menuOpen.value = false })
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
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.logo-sub {
  color: #bfcbd9;
  font-size: 14px;
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
  font-size: 16px;
  color: #606266;
}

.admin-main {
  background: #f0f2f5;
  min-height: 0;
}

.admin-layout > .el-container { min-width: 0; }
.header-left { display: flex; align-items: center; gap: 12px; min-width: 0; }
.header-right { flex-shrink: 0; }
.user-name { max-width: 160px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

@media (max-width: 767px) {
  .admin-header { padding: 0 12px; height: auto; min-height: 60px; gap: 8px; }
  .header-left :deep(.el-breadcrumb) { font-size: 14px; line-height: 1.5; }
  .header-left :deep(.el-breadcrumb__item:first-child) { display: none; }
  .user-name { max-width: 80px; }
}
</style>
