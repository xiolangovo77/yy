<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">管理首页</h1>
    </div>

    <!-- 统计卡片 -->
    <el-row class="responsive-row" :gutter="20" style="margin-bottom: 20px">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card" v-loading="statsLoading">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6f7ff">
              <el-icon :size="32" color="#1890ff"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statsError ? '--' : stats.doctorCount }}</div>
              <div class="stat-label">医生总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card" v-loading="statsLoading">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fff7e6">
              <el-icon :size="32" color="#fa8c16"><Avatar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statsError ? '--' : stats.childCount }}</div>
              <div class="stat-label">儿童总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card" v-loading="statsLoading">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f6ffed">
              <el-icon :size="32" color="#52c41a"><List /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statsError ? '--' : stats.taskCount }}</div>
              <div class="stat-label">评估任务总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计加载失败提示 -->
    <el-alert
      v-if="statsError"
      title="统计数据加载失败，请检查后端服务是否已启动"
      type="error"
      :closable="false"
      show-icon
      style="margin-bottom: 20px"
    />

    <!-- 快捷入口 -->
    <el-card shadow="never">
      <template #header><span style="font-weight: 600">快捷操作</span></template>
      <el-row class="responsive-row" :gutter="20">
        <el-col :xs="12" :sm="8" :lg="4" v-for="item in quickLinks" :key="item.path">
          <el-card shadow="hover" class="quick-card" @click="$router.push(item.path)">
            <div class="quick-icon">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
            <div class="quick-label">{{ item.label }}</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDoctorList } from '@/api/doctor'
import { getChildList } from '@/api/child'
import { getTaskList } from '@/api/task'
import { Avatar, Document, List, UserFilled, VideoCameraFilled } from '@element-plus/icons-vue'

const stats = ref({
  doctorCount: 0,
  childCount: 0,
  taskCount: 0
})
const statsLoading = ref(true)
const statsError = ref(false)

const quickLinks = [
  { path: '/admin/doctors', label: '医生管理', icon: UserFilled },
  { path: '/admin/children', label: '儿童管理', icon: Avatar },
  { path: '/admin/videos', label: '视频管理', icon: VideoCameraFilled },
  { path: '/admin/reports', label: '报告管理', icon: Document },
  { path: '/admin/tasks', label: '任务管理', icon: List }
]

onMounted(async () => {
  statsLoading.value = true
  statsError.value = false
  try {
    const [doctorRes, childRes, taskRes] = await Promise.all([
      getDoctorList({ current: 1, pageSize: 1 }),
      getChildList({ current: 1, pageSize: 1 }),
      getTaskList({ current: 1, pageSize: 1 })
    ])
    stats.value.doctorCount = doctorRes.data.total ?? 0
    stats.value.childCount = childRes.data.total ?? 0
    stats.value.taskCount = taskRes.data.total ?? 0
  } catch (e) {
    statsError.value = true
  } finally {
    statsLoading.value = false
  }
})
</script>

<style scoped>
.stat-card {
  cursor: default;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 30px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 16px;
  color: #909399;
  margin-top: 4px;
}

.quick-card {
  cursor: pointer;
  text-align: center;
  transition: transform 0.2s;
}

.quick-card:hover {
  transform: translateY(-4px);
}

.quick-icon {
  color: #409EFF;
  margin-bottom: 8px;
}

.quick-label {
  font-size: 16px;
  color: #606266;
}
</style>
