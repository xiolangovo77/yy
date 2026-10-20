<template>
  <el-menu
    :default-active="route.path"
    router
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
    @select="$emit('navigate')"
  >
    <el-menu-item v-for="item in items" :key="item.path" :index="item.path">
      <el-icon><component :is="item.icon" /></el-icon>
      <span>{{ item.label }}</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { Avatar, DataAnalysis, Document, List, UserFilled, VideoCameraFilled } from '@element-plus/icons-vue'

defineEmits(['navigate'])
const route = useRoute()
const items = [
  { path: '/admin/dashboard', label: '管理首页', icon: DataAnalysis },
  { path: '/admin/doctors', label: '医生账号管理', icon: UserFilled },
  { path: '/admin/children', label: '儿童信息管理', icon: Avatar },
  { path: '/admin/videos', label: '视频管理', icon: VideoCameraFilled },
  { path: '/admin/reports', label: '报告表管理', icon: Document },
  { path: '/admin/tasks', label: '评估任务管理', icon: List },
  { path: '/admin/introduction', label: '项目介绍管理', icon: Document }
]
</script>

<style scoped>
.el-menu { border-right: none; }
</style>
