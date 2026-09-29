<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">报告表管理</h1>
    </div>

    <div class="search-bar">
      <el-input v-model="search.childName" placeholder="儿童姓名" clearable style="width: 180px" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" @click="handleAdd">新增报告</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
        <el-table-column prop="reportNo" label="报告编号" width="160" />
        <el-table-column label="关联儿童" width="140">
          <template #default="{ row }">{{ childNameMap[row.childId] || row.childId }}</template>
        </el-table-column>
        <el-table-column prop="reportUrl" label="报告URL" min-width="300" show-overflow-tooltip />
        <el-table-column label="上传医生" width="120">
          <template #default="{ row }">{{ doctorNameMap[row.uploadDoctorId] || row.uploadDoctorId }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handlePreview(row)">预览</el-button>
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
      :title="isEdit ? '编辑报告' : '新增报告'"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="关联儿童" prop="childId">
          <el-select v-model="form.childId" placeholder="请选择儿童" filterable style="width: 100%">
            <el-option v-for="c in childOptions" :key="c.id" :label="`${c.childName} (${c.childNo})`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="报告编号" prop="reportNo">
          <el-input v-model="form.reportNo" placeholder="请输入报告编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="MD报告文件" v-if="!isEdit">
          <el-upload
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :file-list="fileList"
            accept=".md"
          >
            <el-button type="primary">选择MD文件</el-button>
            <template #tip>
              <div class="el-upload__tip">仅支持 .md 文件，最大10MB</div>
            </template>
          </el-upload>
          <div v-if="uploading" style="margin-top: 8px">
            <el-progress :percentage="100" :indeterminate="true" />
            <span style="font-size: 14px; color: #909399">正在上传中...</span>
          </div>
        </el-form-item>
        <el-form-item label="报告URL" v-if="isEdit" prop="reportUrl">
          <el-input v-model="form.reportUrl" placeholder="报告存储地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 报告预览对话框 -->
    <el-dialog v-model="previewVisible" title="报告预览" width="900px" top="5vh" @closed="previewHtml = ''">
      <div v-loading="previewLoading" style="min-height: 200px">
        <div v-if="previewError" style="color: #d93025; padding: 20px">
          <h4>报告加载失败</h4>
          <p>{{ previewError }}</p>
        </div>
        <div
          v-else-if="previewHtml"
          class="markdown-body"
          v-html="previewHtml"
        />
        <div v-else-if="!previewLoading" style="text-align: center; color: #999; padding: 40px">
          暂无可预览内容
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getReportList, addReport, updateReport, deleteReport } from '@/api/report'
import { uploadMD } from '@/api/common'
import { getChildList } from '@/api/child'
import { getDoctorList } from '@/api/doctor'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const formRef = ref(null)
const fileList = ref([])

const previewVisible = ref(false)
const previewUrl = ref('')
const previewLoading = ref(false)
const previewHtml = ref('')
const previewError = ref('')

const search = reactive({ childName: '' })
const childNameMap = ref({})
const doctorNameMap = ref({})
const childOptions = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 1 })

const form = reactive({ id: null, childId: '', reportNo: '', reportUrl: '' })
const selectedFile = ref(null)

const rules = {
  childId: [{ required: true, message: '请输入儿童ID', trigger: 'blur' }],
  reportNo: [{ required: true, message: '请输入报告编号', trigger: 'blur' }],
  reportUrl: [{ required: true, message: '请输入报告URL', trigger: 'blur' }]
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
    childOptions.value = childRes.data.records || []

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

    const res = await getReportList({
      current: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'id',
      sortOrder: 'descend',
      childId
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {} finally { loading.value = false }
}

function resetSearch() { search.childName = ''; fetchData() }

function resetForm() {
  form.id = null; form.childId = ''; form.reportNo = ''; form.reportUrl = ''
  selectedFile.value = null; fileList.value = []
}

function handleAdd() { isEdit.value = false; resetForm(); dialogVisible.value = true }

function handleEdit(row) {
  isEdit.value = true
  form.id = row.id; form.childId = row.childId; form.reportNo = row.reportNo; form.reportUrl = row.reportUrl
  selectedFile.value = null; fileList.value = []
  dialogVisible.value = true
}

function handleFileChange(file) {
  selectedFile.value = file.raw
}

async function handleSubmit() {
  if (!isEdit.value) {
    if (!selectedFile.value) { ElMessage.warning('请选择MD文件'); return }
    if (!form.childId) { ElMessage.warning('请输入关联儿童ID'); return }
    if (!form.reportNo) { ElMessage.warning('请输入报告编号'); return }

    uploading.value = true; submitting.value = true
    try {
      const uploadRes = await uploadMD(selectedFile.value)
      form.reportUrl = uploadRes.data
      await addReport({ childId: form.childId, reportNo: form.reportNo, reportUrl: form.reportUrl })
      ElMessage.success('上传并保存成功')
      dialogVisible.value = false
      fetchData()
    } catch (e) {} finally { uploading.value = false; submitting.value = false }
  } else {
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return
    submitting.value = true
    try {
      await updateReport({ id: form.id, childId: form.childId, reportNo: form.reportNo, reportUrl: form.reportUrl })
      ElMessage.success('编辑成功')
      dialogVisible.value = false
      fetchData()
    } catch (e) {} finally { submitting.value = false }
  }
}

async function handlePreview(row) {
  previewVisible.value = true
  previewLoading.value = true
  previewHtml.value = ''
  previewError.value = ''
  try {
    const res = await fetch(row.reportUrl)
    if (!res.ok) throw new Error('请求失败，状态码：' + res.status)
    const mdText = await res.text()
    previewHtml.value = marked.parse(mdText)
  } catch (e) {
    previewError.value = e.message
  } finally {
    previewLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该报告吗？', '警告', {
    confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error'
  }).then(async () => {
    try {
      await deleteReport({ id: row.id })
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {}
  }).catch(() => {})
}

onMounted(async () => {
  await loadNameMaps()
  fetchData()
})
</script>

<style scoped>
.markdown-body {
  padding: 24px;
  background: #fff;
  border-radius: 4px;
  line-height: 1.7;
  font-size: 16px;
  color: #303133;
  max-height: 600px;
  overflow-y: auto;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.markdown-body :deep(h1) { font-size: 26px; border-bottom: 1px solid #eee; padding-bottom: 8px; }
.markdown-body :deep(h2) { font-size: 22px; }
.markdown-body :deep(h3) { font-size: 18px; }

.markdown-body :deep(p) { margin-bottom: 12px; }

.markdown-body :deep(ul),
.markdown-body :deep(ol) { padding-left: 24px; margin-bottom: 12px; }

.markdown-body :deep(li) { margin-bottom: 4px; }

.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 16px;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid #ddd;
  padding: 8px 12px;
  text-align: left;
}

.markdown-body :deep(th) { background: #f5f7fa; font-weight: 600; }

.markdown-body :deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: Consolas, monospace;
  font-size: 15px;
}

.markdown-body :deep(pre) {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
  margin-bottom: 16px;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid #409EFF;
  padding: 8px 16px;
  margin: 12px 0;
  background: #f0f7ff;
  color: #606266;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid #eee;
  margin: 20px 0;
}
</style>
