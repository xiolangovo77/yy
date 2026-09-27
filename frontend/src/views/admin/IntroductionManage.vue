<template>
  <div class="page-container introduction-manage">
    <div class="page-header"><h1 class="page-title">项目介绍管理</h1></div>
    <el-card shadow="never" v-loading="loading">
      <el-alert title="此说明供评估医生阅读。保存后，医生重新打开“项目介绍”即可看到最新内容。" type="info" :closable="false" class="notice" />
      <el-alert v-if="loadFailed" title="内容加载失败，请重新加载后再编辑" type="error" :closable="false">
        <el-button size="small" @click="loadIntroduction">重新加载</el-button>
      </el-alert>
      <el-form v-else-if="!loading" ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="saving">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="请输入面向评估医生的说明标题" />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="18" maxlength="20000" show-word-limit placeholder="请介绍项目目的、评估流程、评分含义和提交要求，支持换行分段" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存并发布</el-button>
          <el-button @click="previewVisible = true">医生端预览</el-button>
          <span v-if="updateTime" class="update-time">最近保存：{{ updateTime }}</span>
        </el-form-item>
      </el-form>
    </el-card>
    <el-dialog v-model="previewVisible" title="医生端内容预览（当前编辑内容）" width="min(900px, 92vw)">
      <IntroductionContent :title="form.title" :content="form.content" />
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getProjectIntroduction, saveProjectIntroduction } from '@/api/introduction'
import IntroductionContent from '@/components/IntroductionContent.vue'

const loading = ref(false)
const loadFailed = ref(false)
const saving = ref(false)
const previewVisible = ref(false)
const updateTime = ref('')
const formRef = ref(null)
const form = reactive({ title: '', content: '' })
const rules = {
  title: [
    { required: true, whitespace: true, message: '请输入标题', trigger: 'blur' },
    { max: 120, message: '标题不能超过120字', trigger: 'blur' }
  ],
  content: [
    { required: true, whitespace: true, message: '请输入正文', trigger: 'blur' },
    { max: 20000, message: '正文不能超过20000字', trigger: 'blur' }
  ]
}

function applyIntroduction(data) {
  form.title = data?.title || ''
  form.content = data?.content || ''
  updateTime.value = data?.updateTime || ''
}

async function loadIntroduction() {
  if (loading.value || saving.value) return
  loading.value = true
  loadFailed.value = false
  try {
    const res = await getProjectIntroduction()
    applyIntroduction(res.data)
  } catch (e) {
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

async function save() {
  if (saving.value || loading.value || loadFailed.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || saving.value) return
  saving.value = true
  try {
    const res = await saveProjectIntroduction({ title: form.title, content: form.content })
    applyIntroduction(res.data)
    ElMessage.success('项目介绍已保存并发布')
  } catch (e) {
    // 请求层统一提示错误，保留当前编辑内容以便重试。
  } finally {
    saving.value = false
  }
}

onMounted(loadIntroduction)
</script>

<style scoped>
.introduction-manage { max-width: 1080px; margin: 0 auto; }
.notice { margin-bottom: 24px; }
.update-time { color: #909399; margin-left: 16px; }
</style>
