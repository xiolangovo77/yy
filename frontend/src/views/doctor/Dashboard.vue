<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">我的任务</h1>
      <p>仅显示分配给您的评估任务，儿童的负责医生由管理员维护。</p>
    </div>

    <div class="search-bar">
      <el-select v-model="search.taskStatus" placeholder="任务状态" clearable style="width: 140px">
        <el-option label="未开始" :value="0" />
        <el-option label="进行中" :value="1" />
        <el-option label="已完成" :value="2" />
      </el-select>
      <el-input v-model="search.childId" placeholder="儿童ID" clearable style="width: 200px" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="任务ID" width="180" show-overflow-tooltip />
        <el-table-column prop="taskNo" label="任务编号" width="160" />
        <el-table-column label="关联儿童" width="140">
          <template #default="{ row }">{{ childNameMap[row.childId] || row.childId }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.taskStatus)" size="small">
              {{ statusText(row.taskStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前步骤" width="100">
          <template #default="{ row }">
            {{ row.currentStep === 2 ? '阶段二' : '阶段一' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <!-- 未开始：可以开始任务 -->
            <el-button
              v-if="row.taskStatus === 0"
              type="primary"
              size="small"
              :loading="row._starting"
              @click="handleStart(row)"
            >
              开始任务
            </el-button>
            <!-- 进行中：继续评估 -->
            <el-button
              v-if="row.taskStatus === 1"
              type="warning"
              size="small"
              @click="handleAssess(row)"
            >
              继续评估
            </el-button>
            <!-- 已完成：查看 -->
            <el-button v-if="row.taskStatus === 2" :type="row.feedbackSubmitted ? 'success' : 'primary'" size="small" @click="handleAssess(row)">
              {{ row.feedbackSubmitted ? '查看评价' : '填写评价' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
      <div style="display: flex; align-items: center; justify-content: flex-end; gap: 8px">
        <span>共 {{ pagination.total }} 条</span>
        <el-select v-model="pagination.pageSize" style="width: 100px" @change="fetchData">
          <el-option :value="5" label="5条/页" />
          <el-option :value="10" label="10条/页" />
          <el-option :value="15" label="15条/页" />
          <el-option :value="20" label="20条/页" />
        </el-select>
        <el-button :disabled="pagination.current <= 1" @click="pagination.current--; fetchData()">上一页</el-button>
        <span>{{ pagination.current }} / {{ Math.ceil(pagination.total / pagination.pageSize) || 1 }}</span>
        <el-button :disabled="pagination.current >= Math.ceil(pagination.total / pagination.pageSize)" @click="pagination.current++; fetchData()">下一页</el-button>
      </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTaskList, startTask } from '@/api/task'
import { getAllChildOptions } from '@/api/child'
import { ElMessage } from 'element-plus'

const router = useRouter()
const childNameMap = ref({})

const loading = ref(false)
const tableData = ref([])

const search = reactive({ taskStatus: undefined, childId: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 1 })

function statusTag(status) {
  if (status === 0) return 'info'
  if (status === 1) return ''
  return 'success'
}

function statusText(status) {
  if (status === 0) return '未开始'
  if (status === 1) return '进行中'
  if (status === 2) return '已完成'
  return '未知'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getTaskList({
      current: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'id',
      sortOrder: 'descend',
      taskStatus: search.taskStatus,
      childId: search.childId || undefined
    })
    tableData.value = (res.data.records || []).map(row => ({ ...row, _starting: false }))
    pagination.total = res.data.total || 0
  } catch (e) {} finally { loading.value = false }
}

function resetSearch() { search.taskStatus = undefined; search.childId = ''; fetchData() }

async function handleStart(row) {
  row._starting = true
  try {
    await startTask(row.id)
    ElMessage.success('任务已开始，请进行评估')
    // 开始成功后直接进入评估页面
    router.push(`/doctor/assessment/${row.id}`)
  } catch (e) {} finally { row._starting = false }
}

function handleAssess(row) {
  router.push(`/doctor/assessment/${row.id}`)
}

async function loadChildNames() {
  try {
    const children = await getAllChildOptions()
    const cm = {}
    ;children.forEach(c => { cm[c.id] = c.childName })
    childNameMap.value = cm
  } catch (e) {}
}

onMounted(async () => {
  await loadChildNames()
  fetchData()
})
</script>
