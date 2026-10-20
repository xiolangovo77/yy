<template>
  <el-card shadow="never" v-loading="loading">
    <template #header>
      <div class="feedback-header">
        <span class="feedback-title">报告使用评价</span>
        <el-tag v-if="saved" type="success">已提交</el-tag>
        <el-tag v-else-if="!loading && !loadFailed" type="warning">待评价</el-tag>
      </div>
    </template>
    <el-alert v-if="loadFailed" title="评价加载失败，请重试" type="error" :closable="false">
      <el-button size="small" @click="loadFeedback">重新加载</el-button>
    </el-alert>
    <el-descriptions v-else-if="saved" :column="1" :direction="isMobile ? 'vertical' : 'horizontal'" border>
      <el-descriptions-item v-for="question in feedbackQuestions" :key="question.key" :label="question.label">
        {{ saved[question.key] }}
      </el-descriptions-item>
      <el-descriptions-item label="您对这个报告有哪些改进建议">
        <div class="suggestions">{{ saved.suggestions || '未填写' }}</div>
      </el-descriptions-item>
      <el-descriptions-item v-if="saved.createTime" label="提交时间">{{ saved.createTime }}</el-descriptions-item>
    </el-descriptions>
    <el-empty v-else-if="readonly && !loading" description="医生尚未提交报告评价" :image-size="60" />
    <el-form v-else-if="!readonly && !loading" ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="disabled || submitting" scroll-to-error>
      <p class="feedback-hint">{{ integrated ? '请根据本次使用体验完成以下评价，均为必填，将与阶段二评估一并提交。' : '请根据本次使用体验填写评价。每个任务提交一次。' }}</p>
      <el-form-item v-for="question in feedbackQuestions" :key="question.key" :label="question.label" :prop="question.key">
        <el-radio-group v-model="form[question.key]">
          <el-radio v-for="choice in feedbackChoices" :key="choice" :value="choice">{{ choice }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="您对这个报告有哪些改进建议" prop="suggestions">
        <el-input v-model="form.suggestions" type="textarea" :rows="4" maxlength="2000" show-word-limit :placeholder="integrated ? '请填写改进建议；如无建议，请填写“无”' : '选填：请填写您希望改进的报告内容或使用体验'" />
      </el-form-item>
      <el-button v-if="!integrated" type="primary" :loading="submitting" @click="submit">提交评价</el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useMediaQuery } from '@/composables/useMediaQuery'
import { getTaskFeedback, submitTaskFeedback } from '@/api/task'
import { feedbackQuestions, feedbackChoices } from '@/utils/assessmentFields'

const props = defineProps({
  taskId: { type: [String, Number], required: true },
  readonly: Boolean,
  integrated: Boolean,
  disabled: Boolean
})
const isMobile = useMediaQuery('(max-width: 767px)')
const emit = defineEmits(['submitted'])
const saved = ref(null)
const loading = ref(false)
const loadFailed = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ coreProblemClarity: '', judgmentImprovement: '', observationAccuracy: '', suggestions: '' })
const rules = computed(() => ({
  ...Object.fromEntries(feedbackQuestions.map(question => [question.key, [{ required: true, message: '请选择评价', trigger: 'change' }]])),
  suggestions: [
    { required: props.integrated, whitespace: true, message: '请填写改进建议，如无建议请填写“无”', trigger: 'blur' },
    { max: 2000, message: '改进建议不能超过2000字', trigger: 'blur' }
  ]
}))
let loadVersion = 0

async function loadFeedback() {
  const version = ++loadVersion
  loading.value = true
  loadFailed.value = false
  saved.value = null
  Object.assign(form, { coreProblemClarity: '', judgmentImprovement: '', observationAccuracy: '', suggestions: '' })
  try {
    const res = await getTaskFeedback(props.taskId)
    if (version === loadVersion) saved.value = res.data || null
  } catch (e) {
    if (version === loadVersion) loadFailed.value = true
  } finally {
    if (version === loadVersion) loading.value = false
  }
}

async function validate() {
  if (loading.value || loadFailed.value) {
    ElMessage.warning(loading.value ? '报告使用评价正在加载，请稍候' : '请先重新加载报告使用评价')
    return false
  }
  if (saved.value) return true
  return await formRef.value?.validate().catch(() => false) ?? false
}

async function persist() {
  if (saved.value) {
    emit('submitted')
    return
  }
  if (submitting.value || props.readonly || !await validate()) {
    throw new Error('报告使用评价尚未填写完整或正在提交')
  }
  submitting.value = true
  try {
    // 重试前核对服务端状态，避免上次保存成功但响应丢失后重复提交。
    const existing = await getTaskFeedback(props.taskId)
    if (existing.data) {
      saved.value = existing.data
      emit('submitted')
      return
    }
    await submitTaskFeedback({ taskId: props.taskId, ...form })
    saved.value = { ...form }
    emit('submitted')
  } finally { submitting.value = false }
}

async function submit() {
  if (props.disabled || submitting.value || saved.value || props.readonly || !await validate()) return
  try {
    await persist()
    ElMessage.success('评价提交成功，感谢您的反馈')
  } catch (e) {}
}

defineExpose({ validate, persist })
watch(() => props.taskId, loadFeedback, { immediate: true })
</script>

<style scoped>
.feedback-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.feedback-title { font-size: 18px; font-weight: 600; }
.feedback-hint { color: #606266; margin: 0 0 20px; }
.suggestions { white-space: pre-wrap; overflow-wrap: anywhere; }
</style>
