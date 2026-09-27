<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">视频管理</h1>
    </div>

    <div class="search-bar">
      <el-input v-model="search.childName" placeholder="儿童姓名" clearable style="width: 180px" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" @click="handleAdd">新增视频</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
        <el-table-column prop="videoName" label="视频名称" width="160" show-overflow-tooltip />
        <el-table-column label="关联儿童" width="140">
          <template #default="{ row }">{{ childNameMap[row.childId] || row.childId }}</template>
        </el-table-column>
        <el-table-column prop="videoUrl" label="视频URL" min-width="300" show-overflow-tooltip />
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑视频' : '新增视频'"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="关联儿童" prop="childId">
          <el-select v-model="form.childId" placeholder="请选择儿童" filterable style="width: 100%">
            <el-option v-for="c in childOptions" :key="c.id" :label="`${c.childName} (${c.childNo})`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="视频名称" prop="videoName">
          <el-input v-model="form.videoName" placeholder="默认 main.mp4" maxlength="128" />
          <div class="el-upload__tip">以 main 开头的视频为主视频（不区分大小写），每个儿童只能有一个。其他视频请使用其他名称，如 action1.mp4。</div>
        </el-form-item>
        <el-form-item label="视频文件" v-if="!isEdit">
          <el-upload
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :file-list="fileList"
            accept=".mp4"
          >
            <el-button type="primary">选择MP4文件</el-button>
            <template #tip>
              <div class="el-upload__tip">仅支持 .mp4 文件，最大500MB</div>
            </template>
          </el-upload>
          <div v-if="uploading" style="margin-top: 8px">
            <el-progress :percentage="100" :indeterminate="true" />
            <span style="font-size: 12px; color: #909399">正在上传中...</span>
          </div>
        </el-form-item>
        <el-form-item label="视频URL" v-if="isEdit" prop="videoUrl">
          <el-input v-model="form.videoUrl" placeholder="视频存储地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 视频预览对话框 -->
    <el-dialog v-model="previewVisible" title="视频预览" width="800px" @closed="handlePreviewClosed">
      <video v-if="previewUrl" ref="videoRef" :src="previewUrl" controls style="width: 100%; max-height: 500px" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getVideoList, addVideo, updateVideo, deleteVideo } from '@/api/video'
import { uploadMP4 } from '@/api/common'
import { getChildList } from '@/api/child'
import { getDoctorList } from '@/api/doctor'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DEFAULT_VIDEO_NAME, isMainVideoName } from '@/utils/video'

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
const videoRef = ref(null)

const search = reactive({ childName: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 1 })

const form = reactive({ id: null, childId: '', videoName: DEFAULT_VIDEO_NAME, videoUrl: '' })
const selectedFile = ref(null)

const childNameMap = ref({})
const doctorNameMap = ref({})
const childOptions = ref([])

const rules = {
  childId: [{ required: true, message: '请选择儿童', trigger: 'change' }],
  videoName: [
    { required: true, message: '请输入视频名称', trigger: 'blur' },
    { max: 128, message: '视频名称不能超过128位', trigger: 'blur' }
  ],
  videoUrl: [{ required: true, message: '请输入视频URL', trigger: 'blur' }]
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
    // 按姓名搜索时，先找到匹配的 childId
    let childId = undefined
    if (search.childName) {
      const match = Object.entries(childNameMap.value).find(([, name]) => name.includes(search.childName))
      childId = match ? match[0] : '__none__'
    }

    const res = await getVideoList({
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
  form.id = null; form.childId = ''; form.videoName = DEFAULT_VIDEO_NAME; form.videoUrl = ''
  selectedFile.value = null; fileList.value = []
}

function handleAdd() { isEdit.value = false; resetForm(); dialogVisible.value = true }

function handleEdit(row) {
  isEdit.value = true
  form.id = row.id; form.childId = row.childId; form.videoName = row.videoName; form.videoUrl = row.videoUrl
  selectedFile.value = null; fileList.value = []
  dialogVisible.value = true
}

function handleFileChange(file) { selectedFile.value = file.raw }

async function handleSubmit() {
  if (submitting.value) return
  form.videoName = String(form.videoName || '').trim()
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || submitting.value) return
  if (!isEdit.value && !selectedFile.value) {
    ElMessage.warning('请选择视频文件')
    return
  }
  submitting.value = true
  try {
    // 上传文件前先提示重复主视频，后端还会在保存时做并发校验。
    if (isMainVideoName(form.videoName)) {
      const res = await getVideoList({ childId: form.childId, mainOnly: true, current: 1, pageSize: 20 })
      const duplicate = (res.data?.records || []).some(video => String(video.id) !== String(form.id))
      if (duplicate) {
        ElMessage.warning('该儿童已有以 main 开头的主视频，请编辑原视频或使用其他名称')
        return
      }
    }
    if (!isEdit.value) {
      uploading.value = true
      const uploadRes = await uploadMP4(selectedFile.value)
      form.videoUrl = uploadRes.data
      await addVideo({ childId: form.childId, videoUrl: form.videoUrl, videoName: form.videoName })
      ElMessage.success('上传并保存成功')
    } else {
      await updateVideo({ id: form.id, childId: form.childId, videoUrl: form.videoUrl, videoName: form.videoName })
      ElMessage.success('编辑成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {} finally { uploading.value = false; submitting.value = false }
}

function handlePreview(row) {
  previewUrl.value = row.videoUrl
  previewVisible.value = true
}

function handlePreviewClosed() {
  if (videoRef.value) {
    videoRef.value.pause()
    videoRef.value.removeAttribute('src')
    videoRef.value.load()
  }
  previewUrl.value = ''
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该视频吗？', '警告', {
    confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error'
  }).then(async () => {
    try {
      await deleteVideo({ id: row.id })
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
