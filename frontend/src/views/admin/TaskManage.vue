<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">评估任务管理</h1>
    </div>

    <div class="search-bar">
      <el-select v-model="search.taskStatus" placeholder="任务状态" clearable style="width: 140px">
        <el-option label="未开始" :value="0" />
        <el-option label="进行中" :value="1" />
        <el-option label="已完成" :value="2" />
      </el-select>
      <el-input v-model="search.childName" placeholder="儿童姓名" clearable style="width: 180px" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" @click="handlePublish">发布任务</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="任务ID" width="180" show-overflow-tooltip />
        <el-table-column prop="taskNo" label="任务编号" width="160" />
        <el-table-column label="关联儿童" width="140">
          <template #default="{ row }">{{ childNameMap[row.childId] || row.childId }}</template>
        </el-table-column>
        <el-table-column label="评估医生" width="120">
          <template #default="{ row }">{{ doctorNameMap[row.doctorId] || row.doctorId }}</template>
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
            <el-button type="primary" size="small" link @click="handleViewResult(row)">查看评估</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 发布任务对话框 -->
    <el-dialog v-model="publishVisible" title="发布评估任务" width="450px" :close-on-click-modal="false">
      <el-form ref="publishFormRef" :model="publishForm" :rules="publishRules" label-width="100px">
        <el-form-item label="选择儿童" prop="childId">
          <el-select v-model="publishForm.childId" placeholder="请选择儿童" filterable style="width: 100%">
            <el-option
              v-for="child in childOptions"
              :key="child.id"
              :label="`${child.childName} (${child.childNo})`"
              :value="child.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务编号" prop="taskNo">
          <el-input v-model="publishForm.taskNo" placeholder="请输入任务编号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="doPublish">确定发布</el-button>
      </template>
    </el-dialog>

    <!-- 查看评估结果对话框 -->
    <el-dialog v-model="resultVisible" title="评估结果" width="700px" destroy-on-close>
      <div v-loading="resultLoading">
        <el-descriptions v-if="form1Data.diseaseType || form2Data.diseaseType" :column="2" border size="small">
          <el-descriptions-item label="阶段一疾病判定">{{ form1Data.diseaseType || '--' }}</el-descriptions-item>
          <el-descriptions-item label="阶段一诊断信心">{{ form1Data.confidence != null ? form1Data.confidence + '%' : '--' }}</el-descriptions-item>
          <el-descriptions-item label="阶段一诊断依据" :span="2">{{ form1Data.diagnosticBasis || '--' }}</el-descriptions-item>
          <el-descriptions-item label="阶段二疾病判定">{{ form2Data.diseaseType || '--' }}</el-descriptions-item>
          <el-descriptions-item label="阶段二诊断信心">{{ form2Data.confidence != null ? form2Data.confidence + '%' : '--' }}</el-descriptions-item>
          <el-descriptions-item label="阶段二诊断依据" :span="2">{{ form2Data.diagnosticBasis || '--' }}</el-descriptions-item>
          <el-descriptions-item label="最终诊断结论" :span="2">
            <span style="font-weight: 600; color: #e6a23c">{{ form2Data.finalConclusion || '--' }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="该任务暂未开始评估" :image-size="80" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTaskList, publishTask, deleteTask, getForm1ByTaskId, getForm2ByTaskId } from '@/api/task'
import { getChildList } from '@/api/child'
import { getDoctorList } from '@/api/doctor'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const publishVisible = ref(false)
const publishing = ref(false)
const publishFormRef = ref(null)
const childOptions = ref([])

const search = reactive({ taskStatus: undefined, childName: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 1 })
const childNameMap = ref({})
const doctorNameMap = ref({})

const resultVisible = ref(false)
const resultLoading = ref(false)
const form1Data = reactive({ diseaseType: '', confidence: null, diagnosticBasis: '' })
const form2Data = reactive({ diseaseType: '', confidence: null, diagnosticBasis: '', finalConclusion: '' })

const publishForm = reactive({ childId: null, taskNo: '' })
const publishRules = {
  childId: [{ required: true, message: '请选择儿童', trigger: 'change' }],
  taskNo: [
    { required: true, message: '请输入任务编号', trigger: 'blur' },
    { max: 32, message: '不超过32位', trigger: 'blur' }
  ]
}

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

async function loadNameMaps() {
  try {
    const [childRes, doctorRes] = await Promise.all([
      getChildList({ current: 1, pageSize: 20 }),
      getDoctorList({ current: 1, pageSize: 20 })
    ])
    const cm = {}
    ;(childRes.data.records || []).forEach(c => { cm[c.id] = c.childName })
    childNameMap.value = cm

    const dm = {}
    ;(doctorRes.data.records || []).forEach(d => { dm[d.id] = d.doctorName })
    doctorNameMap.value = dm
  } catch (e) {}
}

async function fetchData() {
  loading.value = true
  try {
    let childId = undefined
    if (search.childName) {
      const match = Object.entries(childNameMap.value).find(([, name]) => name.includes(search.childName))
      childId = match ? match[0] : '__none__'
    }

    const res = await getTaskList({
      current: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'id',
      sortOrder: 'descend',
      taskStatus: search.taskStatus,
      childId
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {} finally { loading.value = false }
}

function resetSearch() { search.taskStatus = undefined; search.childName = ''; fetchData() }

async function handlePublish() {
  // 儿童列表已在 loadNameMaps 中加载
  if (childOptions.value.length === 0) {
    try {
      const res = await getChildList({ current: 1, pageSize: 20 })
      childOptions.value = res.data?.records || []
    } catch (e) { childOptions.value = [] }
  }
  publishForm.childId = null
  publishForm.taskNo = ''
  publishVisible.value = true
}

async function doPublish() {
  const valid = await publishFormRef.value.validate().catch(() => false)
  if (!valid) return
  publishing.value = true
  try {
    await publishTask({ childId: publishForm.childId, taskNo: publishForm.taskNo })
    ElMessage.success('任务发布成功')
    publishVisible.value = false
    fetchData()
  } catch (e) {} finally { publishing.value = false }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该评估任务吗？关联的表单也将被删除。', '警告', {
    confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error'
  }).then(async () => {
    try {
      await deleteTask({ id: row.id })
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {}
  }).catch(() => {})
}

async function handleViewResult(row) {
  resultVisible.value = true
  resultLoading.value = true
  form1Data.diseaseType = ''; form1Data.confidence = null; form1Data.diagnosticBasis = ''
  form2Data.diseaseType = ''; form2Data.confidence = null; form2Data.diagnosticBasis = ''; form2Data.finalConclusion = ''
  try {
    const [f1, f2] = await Promise.all([
      getForm1ByTaskId(row.id).catch(() => ({ data: null })),
      getForm2ByTaskId(row.id).catch(() => ({ data: null }))
    ])
    if (f1.data) Object.assign(form1Data, f1.data)
    if (f2.data) Object.assign(form2Data, f2.data)
  } catch (e) {} finally { resultLoading.value = false }
}

onMounted(async () => {
  await loadNameMaps()
  try {
    const res = await getChildList({ current: 1, pageSize: 20 })
    childOptions.value = res.data?.records || []
  } catch (e) {}
  fetchData()
})
</script>
