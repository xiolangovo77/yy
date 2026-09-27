<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">医生账号管理</h1>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="search.doctorName" placeholder="医生姓名" clearable style="width: 180px" />
      <el-input v-model="search.account" placeholder="登录账号" clearable style="width: 180px" />
      <el-select v-model="search.role" placeholder="角色" clearable style="width: 140px">
        <el-option label="全部" :value="undefined" />
        <el-option label="管理员" :value="1" />
        <el-option label="普通医生" :value="0" />
      </el-select>
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" @click="handleAdd">新增医生</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
        <el-table-column prop="doctorName" label="姓名" width="120" />
        <el-table-column prop="account" label="账号" width="140" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="hospital" label="医院" min-width="160" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role == 1 ? 'danger' : 'primary'" size="small">
              {{ row.role == 1 ? '管理员' : '普通医生' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="table-container" style="margin-top: 16px; display: flex; align-items: center; justify-content: flex-end; gap: 8px">
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑医生' : '新增医生'"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="doctorName">
          <el-input v-model="form.doctorName" placeholder="请输入医生姓名" />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="form.title" placeholder="如：主任医师（选填）" />
        </el-form-item>
        <el-form-item label="医院" prop="hospital">
          <el-input v-model="form.hospital" placeholder="请输入所在医院" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号（选填）" />
        </el-form-item>
        <el-form-item label="登录账号" prop="account">
          <el-input v-model="form.account" placeholder="请输入登录账号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" :placeholder="isEdit ? '留空则不修改密码' : '请输入密码（留空默认为123456）'" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio :value="0">普通医生</el-radio>
            <el-radio :value="1">管理员</el-radio>
          </el-radio-group>
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
import { getDoctorList, addDoctor, updateDoctor, updateDoctorStatus, deleteDoctor } from '@/api/doctor'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const search = reactive({
  doctorName: '',
  account: '',
  role: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 1
})

const form = reactive({
  id: null,
  doctorName: '',
  title: '',
  hospital: '',
  phone: '',
  account: '',
  password: '',
  role: 0
})

const rules = {
  doctorName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 20, message: '姓名不能超过20位', trigger: 'blur' }
  ],
  // title 后端不校验，前端也不强制必填
  hospital: [
    { required: true, message: '请输入医院', trigger: 'blur' },
    { max: 64, message: '医院名称不能超过64位', trigger: 'blur' }
  ],
  // phone 后端不校验，前端也不强制必填
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { max: 50, message: '账号不能超过50位', trigger: 'blur' }
  ],
  password: [{
    validator: (rule, value, callback) => {
      if (value && (value.length < 6 || value.length > 15)) {
        callback(new Error('密码长度需在 6-15 位之间'))
      } else {
        callback()
      }
    }, trigger: 'blur'
  }],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getDoctorList({
      current: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'id',
      sortOrder: 'descend',
      doctorName: search.doctorName || undefined,
      account: search.account || undefined,
      role: search.role
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  search.doctorName = ''
  search.account = ''
  search.role = undefined
  fetchData()
}

function resetForm() {
  form.id = null
  form.doctorName = ''
  form.title = ''
  form.hospital = ''
  form.phone = ''
  form.account = ''
  form.password = ''
  form.role = 0
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  isEdit.value = true
  form.id = row.id
  form.doctorName = row.doctorName || ''
  form.title = row.title || ''
  form.hospital = row.hospital || ''
  form.phone = row.phone || ''
  form.account = row.account || ''
  form.password = ''
  form.role = row.role
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateDoctor({
        id: form.id,
        doctorName: form.doctorName,
        title: form.title,
        hospital: form.hospital,
        phone: form.phone,
        account: form.account,
        password: form.password,
        role: form.role
      })
      ElMessage.success('编辑成功')
    } else {
      await addDoctor({
        doctorName: form.doctorName,
        title: form.title,
        hospital: form.hospital,
        phone: form.phone,
        account: form.account,
        password: form.password,
        role: form.role
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

function handleToggleStatus(row) {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}该医生账号吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateDoctorStatus({
        id: row.id,
        status: row.status === 1 ? 0 : 1
      })
      ElMessage.success(`${action}成功`)
      fetchData()
    } catch (e) {
      // handled by interceptor
    }
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该医生账号吗？此操作不可恢复。', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
      await deleteDoctor({ id: row.id })
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      // handled by interceptor
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>
