<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">儿童基本信息管理</h1>
    </div>

    <div class="search-bar">
      <el-input v-model="search.childName" placeholder="儿童姓名" clearable style="width: 180px" />
      <el-input v-model="search.childNo" placeholder="儿童编号" clearable style="width: 180px" />
      <el-input v-model="search.assessType" placeholder="评估方式" clearable style="width: 200px" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" @click="handleAdd">新增儿童</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
        <el-table-column prop="childNo" label="儿童编号" width="140" />
        <el-table-column prop="childName" label="姓名" width="120" />
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
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
import { getChildList, addChild, updateChild, deleteChild } from '@/api/child'
import { getVideoList } from '@/api/video'
import { getReportList } from '@/api/report'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const videoIds = ref(new Set())
const reportIds = ref(new Set())
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const search = reactive({ childName: '', childNo: '', assessType: '' })

const pagination = reactive({ current: 1, pageSize: 10, total: 1 })

const form = reactive({
  id: null, childNo: '', childName: '', age: null, gender: '未提供', assessType: '短视频结构化观察与分析', remark: ''
})

const rules = {
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
  fetchData()
}

function resetForm() {
  form.id = null; form.childNo = ''; form.childName = ''
  form.age = null; form.gender = '未提供'
  form.assessType = '短视频结构化观察与分析'; form.remark = ''
}

function handleAdd() { isEdit.value = false; resetForm(); dialogVisible.value = true }

function handleEdit(row) {
  isEdit.value = true
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

onMounted(async () => {
  await loadAssociations()
  fetchData()
})
</script>
