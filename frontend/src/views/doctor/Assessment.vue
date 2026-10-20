<template>
  <div class="page-container">
    <div class="page-header">
      <el-page-header @back="goBack" content="评估工作台" />
    </div>

    <el-card shadow="never" style="margin-bottom: 20px">
      <el-descriptions class="task-summary" :column="isMobile ? 1 : isTablet ? 2 : 4" border v-loading="taskLoading">
        <el-descriptions-item label="任务编号">{{ taskInfo.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="关联儿童">{{ childName || taskInfo.childId }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="statusTag(taskInfo.taskStatus)" size="small">
            {{ statusText(taskInfo.taskStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前阶段">
          <el-tag type="warning" size="small">
            {{ taskInfo.currentStep === 2 ? '阶段二' : '阶段一' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-steps :active="taskInfo.currentStep - 1" align-center style="margin-bottom: 30px">
      <el-step title="阶段一" description="初步筛查评估" />
      <el-step title="阶段二" description="综合诊断确认" />
    </el-steps>

    <el-alert v-if="formLoadError" :title="formLoadError" type="error" :closable="false" show-icon style="margin-bottom: 20px">
      <el-button size="small" :loading="taskLoading" @click="loadTask">重新加载</el-button>
    </el-alert>

    <!-- ==================== 阶段一：左视频 + 右表单 ==================== -->
    <el-row v-if="taskInfo.currentStep === 1" :gutter="20" class="responsive-row">
      <!-- 左侧：视频 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="never" v-loading="videosLoading">
          <template #header>
            <div class="reference-header">
              <span class="form-title">主视频</span>
              <el-tag v-if="mainVideo" size="small">{{ mainVideo.videoName }}</el-tag>
            </div>
          </template>
          <div v-if="mainVideo" class="main-video-player">
            <video :key="mainVideo.videoUrl" :src="mainVideo.videoUrl" controls playsinline preload="metadata" />
          </div>
          <el-empty v-else-if="!videosLoading" :description="videosError || '暂无以 main 开头的主视频，请联系管理员上传'" />
        </el-card>
      </el-col>

      <!-- 右侧：阶段一评估表单 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="never">
          <template #header><span class="form-title">阶段一：初步筛查评估</span></template>
          <el-form ref="form1Ref" :model="form1" :rules="form1Rules" :disabled="taskLoading || !!formLoadError || form1Submitting" label-width="120px" size="default">
            <el-form-item label="疾病判定" prop="diseaseType">
              <el-select v-model="form1.diseaseType" placeholder="请选择疾病判定结果" style="width: 100%">
                <el-option label="ASD - 自闭症谱系障碍" value="ASD" />
                <el-option label="TD - 典型发育" value="TD" />
                <el-option label="DD - 发育迟缓" value="DD" />
              </el-select>
            </el-form-item>
            <el-form-item label="诊断信心" prop="confidence">
              <el-slider
                  v-model="form1.confidence"
                  :min="0"
                  :max="10"
                  :step="1"
                  :marks="{ 0: '0', 2: '2', 4: '4', 6: '6', 8: '8', 10: '10' }"
                  show-input
              />
            </el-form-item>
            <el-form-item label="诊断依据" prop="diagnosticBasis">
              <el-input v-model="form1.diagnosticBasis" type="textarea" :rows="6"
                placeholder="请输入诊断依据..." maxlength="2000" show-word-limit />
            </el-form-item>
            <el-form-item label="补充备注">
              <el-input v-model="form1.doctorRemark" type="textarea" :rows="2"
                placeholder="选填" maxlength="500" show-word-limit />
            </el-form-item>
            <el-divider content-position="left">社交行为观察</el-divider>
            <p class="score-hint">以下四项均为1–5分，分数越高表示严重程度越高。</p>
            <el-form-item v-for="field in stageOneScores" :key="field.key" :label="field.label" :prop="field.key">
              <div class="score-field">
                <p>{{ field.description }}</p>
                <el-radio-group v-model="form1[field.key]">
                  <el-radio-button v-for="score in 5" :key="score" :value="score">{{ score }}分</el-radio-button>
                </el-radio-group>
              </div>
            </el-form-item>
            <el-form-item label="重复刻板或感觉异常行为" prop="repetitiveBehavior" class="behavior-description">
              <el-input v-model="form1.repetitiveBehavior" type="textarea" :rows="4" maxlength="2000" show-word-limit placeholder="出现何种重复动作、刻板摆弄物体、异常感觉兴趣等？未观察到时请填写“未观察到”。" />
            </el-form-item>
            <el-form-item>
              <el-button type="success" :loading="form1Submitting" size="large" @click="submitForm1">
                提交阶段一，进入阶段二
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- ==================== 阶段二：主视频、报告与评估表单 ==================== -->
    <template v-if="taskInfo.currentStep === 2 && taskInfo.taskStatus !== 2">
      <el-card shadow="never" v-loading="videosLoading" style="margin-bottom: 20px">
        <template #header>
          <div class="reference-header">
            <span class="form-title">主视频回看</span>
            <el-tag v-if="mainVideo" size="small">{{ mainVideo.videoName }}</el-tag>
          </div>
        </template>
        <div v-if="mainVideo" class="main-video-player">
          <video :key="mainVideo.videoUrl" :src="mainVideo.videoUrl" controls playsinline preload="metadata" />
        </div>
        <el-empty v-else-if="!videosLoading" :description="videosError || '暂无以 main 开头的主视频，请联系管理员上传'" :image-size="60" />
      </el-card>

      <!-- 报告查看器（全宽） -->
      <ReportViewer v-if="mdText" :md="mdText" :videos="videoList" style="margin-bottom: 20px" />
      <el-card v-else shadow="never" style="margin-bottom: 20px">
        <div v-if="reportLoading" v-loading="true" style="min-height: 120px" />
        <el-empty v-else description="该儿童暂无筛查报告" :image-size="60" />
      </el-card>

      <!-- 阶段二评估表单 -->
      <el-card shadow="never">
        <template #header><span class="form-title">阶段二：综合诊断确认</span></template>
        <el-form ref="form2Ref" :model="form2" :rules="form2Rules" :disabled="taskLoading || !!formLoadError || form2Submitting" label-width="120px" size="default">
          <el-form-item label="疾病判定" prop="diseaseType">
            <el-select v-model="form2.diseaseType" placeholder="请选择疾病判定结果" style="width: 100%">
              <el-option label="ASD - 自闭症谱系障碍" value="ASD" />
              <el-option label="TD - 典型发育" value="TD" />
              <el-option label="DD - 发育迟缓" value="DD" />
            </el-select>
          </el-form-item>
          <el-form-item label="诊断信心" prop="confidence">
            <el-slider
                v-model="form2.confidence"
                :min="0"
                :max="10"
                :step="1"
                :marks="{ 0: '0', 2: '2', 4: '4', 6: '6', 8: '8', 10: '10' }"
                show-input
            />
          </el-form-item>
          <el-form-item label="诊断依据" prop="diagnosticBasis">
            <el-input v-model="form2.diagnosticBasis" type="textarea" :rows="4"
              placeholder="请综合两个阶段的观察，详细描述诊断依据..." maxlength="2000" show-word-limit />
          </el-form-item>
          <el-form-item label="最终结论" prop="finalConclusion">
            <el-input v-model="form2.finalConclusion" type="textarea" :rows="4"
              placeholder="请输入最终诊断结论..." maxlength="2000" show-word-limit />
          </el-form-item>
          <el-form-item label="补充备注">
            <el-input v-model="form2.doctorRemark" type="textarea" :rows="2"
              placeholder="选填" maxlength="500" show-word-limit />
          </el-form-item>
          <el-divider content-position="left">报告评价评分</el-divider>
          <p class="score-hint">以下三项均为1–5分，分数越高表示相应程度越高。</p>
          <el-form-item v-for="field in stageTwoScores" :key="field.key" :label="field.label" :prop="field.key" label-width="180px">
            <el-radio-group v-model="form2[field.key]">
              <el-radio-button v-for="score in 5" :key="score" :value="score">{{ score }}分</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="success" :loading="form2Submitting" size="large" @click="submitForm2">
              提交最终评估，完成任务
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </template>

    <!-- 任务已完成 -->
    <el-card v-if="taskInfo.taskStatus === 2" shadow="never" style="margin-bottom: 20px">
      <el-result icon="success" title="评估已完成" :sub-title="taskInfo.feedbackSubmitted ? '评估与报告评价均已提交' : '请继续填写下方报告使用评价'">
        <template #extra>
          <el-button type="primary" @click="goBack">返回任务列表</el-button>
        </template>
      </el-result>
    </el-card>
    <TaskFeedback v-if="taskInfo.taskStatus === 2" :task-id="route.params.taskId" @submitted="taskInfo.feedbackSubmitted = true" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useMediaQuery } from '@/composables/useMediaQuery'
import { useRoute, useRouter } from 'vue-router'
import { getTaskById, getForm1ByTaskId, getForm2ByTaskId, submitForm1 as submitForm1Api, submitForm2 as submitForm2Api } from '@/api/task'
import { getChildById } from '@/api/child'
import { getAllChildVideos } from '@/api/video'
import { isMainVideoName } from '@/utils/video'
import { getReportList } from '@/api/report'
import ReportViewer from '@/components/ReportViewer.vue'
import TaskFeedback from '@/components/TaskFeedback.vue'
import { stageOneScores, stageTwoScores, emptyScores, scoreRules } from '@/utils/assessmentFields'

const isMobile = useMediaQuery('(max-width: 767px)')
const isTablet = useMediaQuery('(max-width: 1199px)')

const route = useRoute()
const router = useRouter()

const taskLoading = ref(false)
const childName = ref('')
const videoList = ref([]) // [{ videoName, videoUrl }]
const mainVideo = computed(() => videoList.value.find(video => isMainVideoName(video.videoName)) || null)
const videosLoading = ref(false)
const videosError = ref('')
const formLoadError = ref('')
const mdText = ref('')
const reportLoading = ref(false)
let resourceLoadVersion = 0

const form1Submitting = ref(false)
const form2Submitting = ref(false)
const form1Ref = ref(null)
const form2Ref = ref(null)

const taskInfo = reactive({
  taskNo: '', childId: '', taskStatus: 0, currentStep: 1, feedbackSubmitted: false
})

const form1 = reactive({
  diseaseType: '', confidence: 0, diagnosticBasis: '', doctorRemark: '',
  ...emptyScores(stageOneScores), repetitiveBehavior: ''
})

const form1Rules = {
  ...scoreRules(stageOneScores),
  repetitiveBehavior: [
    { required: true, whitespace: true, message: '请描述相关行为，未观察到时请注明', trigger: 'blur' },
    { max: 2000, message: '行为描述不能超过2000字', trigger: 'blur' }
  ],
  diseaseType: [{ required: true, message: '请选择疾病判定', trigger: 'change' }],
  confidence: [{ required: true, message: '请设置诊断信心', trigger: 'change' }],
  diagnosticBasis: [{ required: true, message: '请输入诊断依据', trigger: 'blur' }]
}

const form2 = reactive({
  diseaseType: '', confidence: 0, diagnosticBasis: '', finalConclusion: '', doctorRemark: '',
  ...emptyScores(stageTwoScores)
})

const form2Rules = {
  ...scoreRules(stageTwoScores),
  diseaseType: [{ required: true, message: '请选择疾病判定', trigger: 'change' }],
  confidence: [{ required: true, message: '请设置诊断信心', trigger: 'change' }],
  diagnosticBasis: [{ required: true, message: '请输入诊断依据', trigger: 'blur' }],
  finalConclusion: [{ required: true, message: '请输入最终诊断结论', trigger: 'blur' }]
}

function statusTag(s) {
  if (s === 0) return 'info'
  if (s === 1) return ''
  return 'success'
}
function statusText(s) {
  if (s === 0) return '未开始'
  if (s === 1) return '进行中'
  if (s === 2) return '已完成'
  return '未知'
}
function goBack() { router.push('/doctor/dashboard') }

async function loadChildResources(childId, stage) {
  const version = ++resourceLoadVersion
  videoList.value = []
  videosError.value = ''
  videosLoading.value = true
  mdText.value = ''
  reportLoading.value = stage === 2
  try {
    // 阶段一由接口仅返回 main 视频；阶段二完整加载报告证据视频及主视频。
    const videos = await getAllChildVideos(childId, { mainOnly: stage === 1 })
    if (version !== resourceLoadVersion) return
    videoList.value = videos.map(video => ({ videoName: video.videoName, videoUrl: video.videoUrl }))
  } catch (e) {
    if (version === resourceLoadVersion) videosError.value = '视频加载失败，请刷新页面重试'
  } finally {
    if (version === resourceLoadVersion) videosLoading.value = false
  }

  if (stage !== 2 || version !== resourceLoadVersion) return
  try {
    const rRes = await getReportList({ current: 1, pageSize: 1, childId })
    const reports = rRes.data?.records || []
    if (reports.length > 0) {
      const mdRes = await fetch(reports[0].reportUrl)
      if (mdRes.ok) {
        const text = await mdRes.text()
        if (version === resourceLoadVersion) mdText.value = text
      }
    }
  } catch (e) {
    if (version === resourceLoadVersion) mdText.value = ''
  } finally {
    if (version === resourceLoadVersion) reportLoading.value = false
  }
}

async function loadStageOne(taskId) {
  const res = await getForm1ByTaskId(taskId)
  if (res.data) {
    form1.diseaseType = res.data.diseaseType || ''
    form1.confidence = res.data.confidence ?? 0
    form1.diagnosticBasis = res.data.diagnosticBasis || ''
    form1.doctorRemark = res.data.doctorRemark || ''
    for (const field of stageOneScores) form1[field.key] = res.data[field.key] ?? null
    form1.repetitiveBehavior = res.data.repetitiveBehavior || ''
  }
}

async function loadTask() {
  const taskId = route.params.taskId
  taskLoading.value = true
  formLoadError.value = ''
  try {
    const taskRes = await getTaskById(taskId)
    Object.assign(taskInfo, taskRes.data)

    const resourceLoads = [loadStageOne(taskId)]
    if (taskInfo.childId) {
      // 视频及报告独立显示加载状态，不阻塞已提交表单的回显。
      loadChildResources(taskInfo.childId, taskInfo.currentStep)
      resourceLoads.push(getChildById(taskInfo.childId).then(res => {
        childName.value = res.data?.childName || ''
      }).catch(() => { childName.value = '' }))
    }
    await Promise.all(resourceLoads)

    // 阶段二未填写的三项直接带入阶段一结果；已有阶段二值优先保留，0 也是有效值。
    if (taskInfo.currentStep >= 2) {
      const res = await getForm2ByTaskId(taskId)
      const saved = res.data || {}
      form2.diseaseType = saved.diseaseType || form1.diseaseType
      form2.confidence = saved.confidence ?? form1.confidence
      form2.diagnosticBasis = saved.diagnosticBasis || form1.diagnosticBasis
      form2.finalConclusion = saved.finalConclusion || ''
      form2.doctorRemark = saved.doctorRemark || ''
      for (const field of stageTwoScores) form2[field.key] = saved[field.key] ?? null
    }
  } catch (e) {
    formLoadError.value = '评估表单加载失败，请重新加载后继续填写'
  } finally { taskLoading.value = false }
}

async function submitForm1() {
  if (form1Submitting.value || taskLoading.value || formLoadError.value) return
  const valid = await form1Ref.value.validate().catch(() => false)
  if (!valid || form1Submitting.value) return
  form1Submitting.value = true
  try {
    await submitForm1Api({
      taskId: route.params.taskId,
      diseaseType: form1.diseaseType,
      confidence: form1.confidence,
      diagnosticBasis: form1.diagnosticBasis,
      ...Object.fromEntries(stageOneScores.map(field => [field.key, form1[field.key]])),
      repetitiveBehavior: form1.repetitiveBehavior,
      doctorRemark: form1.doctorRemark || undefined
    })
    ElMessage.success('阶段一提交成功，进入阶段二')
    taskInfo.currentStep = 2
    await loadTask()
  } catch (e) {} finally { form1Submitting.value = false }
}

async function submitForm2() {
  if (form2Submitting.value || taskLoading.value || formLoadError.value) return
  const valid = await form2Ref.value.validate().catch(() => false)
  if (!valid || form2Submitting.value) return
  form2Submitting.value = true
  try {
    await ElMessageBox.confirm(
      '提交后评估任务将标记为已完成，并进入报告使用评价。确定提交吗？',
      '确认提交最终评估',
      { confirmButtonText: '确定提交', cancelButtonText: '再检查一下', type: 'warning' }
    )
    await submitForm2Api({
      taskId: route.params.taskId,
      diseaseType: form2.diseaseType,
      confidence: form2.confidence,
      diagnosticBasis: form2.diagnosticBasis,
      ...Object.fromEntries(stageTwoScores.map(field => [field.key, form2[field.key]])),
      finalConclusion: form2.finalConclusion,
      doctorRemark: form2.doctorRemark || undefined
    })
    ElMessage.success('评估已完成，请继续填写报告使用评价')
    taskInfo.taskStatus = 2
    await loadTask()
  } catch (e) {} finally { form2Submitting.value = false }
}

onMounted(() => loadTask())
</script>

<style scoped>
.task-summary :deep(.el-descriptions__body .el-descriptions__table .el-descriptions__cell) {
  font-size: 18px;
  line-height: 1.6;
  padding: 14px 16px;
}
.task-summary :deep(.el-tag) {
  font-size: 16px;
  height: 30px;
  padding: 0 10px;
}
.form-title {
  font-weight: 600;
  font-size: 18px;
}
.reference-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.main-video-player { background: #000; border-radius: 4px; overflow: hidden; }
.main-video-player video { display: block; width: 100%; max-height: 400px; }
.score-hint { color: #606266; font-size: 15px; margin: 0 0 20px; }
.score-field p { color: #909399; font-size: 15px; margin: 0 0 8px; line-height: 1.6; }
.behavior-description :deep(.el-form-item__label) { line-height: 1.6; height: auto; }
.reference-header { flex-wrap: wrap; }
.reference-header :deep(.el-tag) { max-width: 100%; height: auto; white-space: normal; overflow-wrap: anywhere; }
.score-field { min-width: 0; width: 100%; }
@media (max-width: 767px) {
  .task-summary :deep(.el-descriptions__body .el-descriptions__table .el-descriptions__cell) {
    font-size: 16px;
    padding: 10px;
  }
  .task-summary :deep(.el-descriptions__label) { width: 90px; }
}
</style>
