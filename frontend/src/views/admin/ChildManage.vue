<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">儿童基本信息管理</h1>
    </div>

    <div class="search-bar">
      <el-input v-model="search.childName" placeholder="儿童姓名" clearable style="width: 180px" />
      <el-input v-model="search.childNo" placeholder="儿童编号" clearable style="width: 180px" />
      <el-input v-model="search.assessType" placeholder="评估方式" clearable style="width: 200px" />
      <el-select v-model="search.doctorId" placeholder="负责医生" clearable filterable style="width: 200px" :loading="doctorsLoading" @visible-change="refreshDoctors">
        <el-option v-for="doctor in doctorOptions" :key="doctor.id" :value="doctor.id" :label="doctorLabel(doctor)" />
      </el-select>
      <el-button type="primary" @click="searchChildren">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" @click="handleAdd">新增儿童</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
        <el-table-column prop="childNo" label="儿童编号" width="140" />
        <el-table-column prop="childName" label="姓名" width="120" />
        <el-table-column label="负责医生" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.doctorName || (row.doctorId ? row.doctorId : '待绑定') }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="assessType" label="评估方式" width="200" show-overflow-tooltip />
        <el-table-column label="视频" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="videoIds.has(row.id) ? 'success' : 'info'" size="small">
              {{ videoIds.has(row.id) ? '有' : '无' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报告" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="reportIds.has(row.id) ? 'success' : 'info'" size="small">
              {{ reportIds.has(row.id) ? '有' : '无' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="270" :fixed="isMobile ? false : 'right'">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-tooltip :content="row.taskPublished ? '仅未开始且未删除的评估任务允许转接' : '当前无任务，可通过编辑设置负责医生'" :disabled="row.canTransferDoctor === true" placement="top">
              <span class="transfer-action">
                <el-button type="warning" size="small" :disabled="row.canTransferDoctor !== true" @click="handleTransfer(row)">转接医生</el-button>
              </span>
            </el-tooltip>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
      <div class="pagination-bar">
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

    <el-dialog
      v-model="transferVisible"
      title="转接医生"
      width="500px"
      destroy-on-close
      :close-on-click-modal="false"
      :close-on-press-escape="!transferring"
      :show-close="!transferring"
    >
      <el-form ref="transferFormRef" :model="transferForm" :rules="transferRules" label-width="100px">
        <el-form-item label="儿童">
          <span>{{ transferChild?.childName }}（{{ transferChild?.childNo }}）</span>
        </el-form-item>
        <el-form-item label="当前医生">
          <span>{{ transferChild?.doctorName || transferChild?.doctorId || '待绑定' }}</span>
        </el-form-item>
        <el-form-item label="转接至" prop="doctorId">
          <el-select
            v-model="transferForm.doctorId"
            placeholder="请选择新的负责医生"
            filterable
            style="width: 100%"
            :loading="doctorsLoading"
            :disabled="transferring"
            no-data-text="暂无可选医生，请先在医生管理中添加"
            @visible-change="refreshDoctors"
          >
            <el-option
              v-for="doctor in doctorOptions"
              :key="doctor.id"
              :value="doctor.id"
              :label="doctorLabel(doctor)"
              :disabled="doctor.status !== 1 || String(doctor.id) === String(transferChild?.doctorId)"
            />
          </el-select>
        </el-form-item>
        <el-alert
          title="仅未开始的评估任务允许转接，确认后该任务交由新医生负责。进行中、已完成或已删除的任务均不能转接。"
          type="info"
          :closable="false"
          show-icon
        />
      </el-form>
      <template #footer>
        <el-button :disabled="transferring" @click="transferVisible = false">取消</el-button>
        <el-button type="primary" :loading="transferring" :disabled="doctorsLoading" @click="submitTransfer">确认转接</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑儿童信息' : '新增儿童'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="儿童编号" prop="childNo">
          <el-input v-model="form.childNo" placeholder="请输入儿童编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="childName">
          <el-input v-model="form.childName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="负责医生" prop="doctorId">
          <el-select v-model="form.doctorId" placeholder="请选择负责医生" filterable style="width: 100%" :disabled="isEdit && !editDoctorChangeAllowed" :loading="doctorsLoading" @visible-change="refreshDoctors">
            <el-option v-for="doctor in doctorOptions" :key="doctor.id" :value="doctor.id" :label="doctorLabel(doctor)" :disabled="doctor.status !== 1" />
          </el-select>
          <div class="binding-hint">当前无任务时可设置医生；有任务时仅未开始允许更换医生，进行中或已完成均不可更换。</div>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="0" :max="200" style="width: 100%" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
            <el-option label="未提供" value="未提供" />
          </el-select>
        </el-form-item>
        <el-form-item label="评估方式" prop="assessType">
          <el-input v-model="form.assessType" placeholder="如：短视频结构化观察与分析" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMediaQuery } from '@/composables/useMediaQuery'
import { getChildList, addChild, updateChild, deleteChild, transferChildDoctor } from '@/api/child'
import { getAllDoctorOptions } from '@/api/doctor'
import { getVideoList } from '@/api/video'
import { getReportList } from '@/api/report'

const isMobile = useMediaQuery('(max-width: 767px)')

const loading = ref(false)
const videoIds = ref(new Set())
const reportIds = ref(new Set())
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editDoctorChangeAllowed = ref(true)
const submitting = ref(false)
const formRef = ref(null)
const transferVisible = ref(false)
const transferring = ref(false)
const transferChild = ref(null)
const transferFormRef = ref(null)
const transferForm = reactive({ doctorId: null })
const transferRules = {
  doctorId: [{ required: true, message: '请选择新的负责医生', trigger: 'change' }]
}

const search = reactive({ childName: '', childNo: '', assessType: '', doctorId: undefined })
const doctorOptions = ref([])
const doctorsLoading = ref(false)

const pagination = reactive({ current: 1, pageSize: 10, total: 1 })

const form = reactive({
  doctorId: null, id: null, childNo: '', childName: '', age: null, gender: '未提供', assessType: '短视频结构化观察与分析', remark: ''
})

const rules = {
  doctorId: [{ required: true, message: '请选择负责医生', trigger: 'change' }],
  childNo: [
    { required: true, message: '请输入儿童编号', trigger: 'blur' },
    { max: 32, message: '不超过32位', trigger: 'blur' }
  ],
  childName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 64, message: '不超过64位', trigger: 'blur' }
  ],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getChildList({
      current: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'id',
      sortOrder: 'descend',
      childName: search.childName || undefined,
      childNo: search.childNo || undefined,
      doctorId: search.doctorId || undefined,
      assessType: search.assessType || undefined
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {} finally { loading.value = false }
}

function resetSearch() {
  search.childName = ''
  search.childNo = ''
  search.assessType = ''
  search.doctorId = undefined
  searchChildren()
}

function resetForm() {
  editDoctorChangeAllowed.value = true
  form.doctorId = null
  form.id = null; form.childNo = ''; form.childName = ''
  form.age = null; form.gender = '未提供'
  form.assessType = '短视频结构化观察与分析'; form.remark = ''
}

function handleAdd() { isEdit.value = false; resetForm(); dialogVisible.value = true }

function handleEdit(row) {
  isEdit.value = true
  editDoctorChangeAllowed.value = row.canChangeDoctor === true
  form.doctorId = row.doctorId || null
  form.id = row.id; form.childNo = row.childNo; form.childName = row.childName
  form.age = row.age; form.gender = row.gender
  form.assessType = row.assessType; form.remark = row.remark || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const data = {
      doctorId: form.doctorId,
      childNo: form.childNo, childName: form.childName,
      age: form.age, gender: form.gender,
      assessType: form.assessType, remark: form.remark
    }
    if (isEdit.value) {
      await updateChild({ id: form.id, ...data })
      ElMessage.success('编辑成功')
    } else {
      await addChild(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {} finally { submitting.value = false }
}

function handleTransfer(row) {
  if (row.canTransferDoctor !== true) {
    ElMessage.warning('仅未开始且未删除的评估任务允许转接医生')
    return
  }
  transferChild.value = { ...row }
  transferForm.doctorId = null
  transferFormRef.value?.clearValidate()
  transferVisible.value = true
  refreshDoctors()
}

async function submitTransfer() {
  if (transferring.value) return
  const valid = await transferFormRef.value.validate().catch(() => false)
  if (!valid || transferring.value) return
  if (String(transferForm.doctorId) === String(transferChild.value.doctorId)) {
    ElMessage.warning('请选择其他医生进行转接')
    return
  }
  transferring.value = true
  try {
    await transferChildDoctor({ id: transferChild.value.id, doctorId: transferForm.doctorId })
    ElMessage.success('转接成功，未开始的任务已转交给新医生')
    transferVisible.value = false
    pagination.current = 1
    await fetchData()
  } catch (e) {} finally { transferring.value = false }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该儿童信息吗？', '警告', {
    confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error'
  }).then(async () => {
    try {
      await deleteChild({ id: row.id })
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {}
  }).catch(() => {})
}

async function loadAssociations() {
  try {
    const [vRes, rRes] = await Promise.all([
      getVideoList({ current: 1, pageSize: 20 }),
      getReportList({ current: 1, pageSize: 20 })
    ])
    videoIds.value = new Set((vRes.data.records || []).map(v => v.childId))
    reportIds.value = new Set((rRes.data.records || []).map(r => r.childId))
  } catch (e) {}
}

function doctorLabel(doctor) {
  return `${doctor.doctorName}（${doctor.hospital || doctor.id}）${doctor.status === 1 ? '' : ' · 已禁用'}`
}

async function refreshDoctors(visible = true) {
  if (!visible || doctorsLoading.value) return
  doctorsLoading.value = true
  try {
    doctorOptions.value = await getAllDoctorOptions({ role: 0 })
  } catch (e) { doctorOptions.value = [] } finally { doctorsLoading.value = false }
}

function searchChildren() {
  pagination.current = 1
  fetchData()
}

onMounted(async () => {
  await Promise.all([loadAssociations(), refreshDoctors()])
  fetchData()
})
</script>

<style scoped>
.binding-hint { color: #909399; font-size: 14px; line-height: 1.6; margin-top: 6px; }
.transfer-action { display: inline-block; margin: 0 12px; }
</style>
