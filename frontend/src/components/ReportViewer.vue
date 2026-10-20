<template>
  <div class="report-viewer">
    <section class="report-block" aria-label="综合筛查结论">
      <header class="rv-header">
        <div>
          <span class="eyebrow">筛查报告 · 医生复核</span>
          <h2>综合筛查结论（医生速览）</h2>
          <p>六项结论概览，结合原始视频核对关键证据。</p>
        </div>
        <span class="report-badge">辅助筛查</span>
      </header>

      <div class="rv-main">
        <div class="overview-grid">
          <section v-for="section in overview.slice(0, 4)" :key="section.number"
            class="overview-card" :class="{ wide: section.number <= 2, conclusion: section.number === 4 }">
            <div class="section-heading">
              <span class="section-number">{{ String(section.number).padStart(2, '0') }}</span>
              <h3>{{ section.title }}</h3>
            </div>
            <div v-if="section.html" class="markdown-body" v-html="section.html"></div>
            <p v-else class="empty-text">报告中暂无此模块内容。</p>
          </section>

          <section class="overview-card wide combined-overview" aria-label="鉴别、证据限制与进一步评估">
            <div v-for="section in overview.slice(4)" :key="section.number" class="combined-section">
              <div class="section-heading">
                <span class="section-number">{{ String(section.number).padStart(2, '0') }}</span>
                <h3>{{ section.title }}</h3>
              </div>
              <div v-if="section.html" class="markdown-body" v-html="section.html"></div>
              <p v-else class="empty-text">报告中暂无此模块内容。</p>
            </div>
          </section>
        </div>
      </div>
    </section>

    <section class="report-block" aria-label="证据回看">
      <header class="rv-header">
        <div>
          <span class="eyebrow">原始证据 · 视频核对</span>
          <h2>证据回看</h2>
          <p>通过可审计时间轴定位事件，结合原始视频核对观察依据。</p>
        </div>
      </header>
      <div class="evidence-grid">
        <section class="panel timeline-panel">
          <div class="panel-heading">
            <h3>可审计时间轴</h3>
            <span class="count">{{ filteredTimeline.length }} / {{ timeline.length }} 条证据</span>
          </div>
          <p class="panel-description">点击事件，定位到对应任务视频的起始时间。</p>
          <input v-model="search" type="search" aria-label="搜索时间轴" placeholder="搜索任务、证据或来源">
          <p v-if="timelineError" class="feedback" role="status">{{ timelineError }}</p>
          <div v-else class="timeline">
            <button v-for="(event, index) in filteredTimeline" :key="event.id || index" type="button"
              class="event" :class="{ active: selectedEvent === event }"
              :aria-pressed="selectedEvent === event" @click="loadEventVideo(event)">
              <span class="event-top">
                <strong>{{ event.label || event.id }}</strong>
                <span class="event-time">{{ formatTime(event.start_s) }}–{{ formatTime(event.end_s) }}</span>
              </span>
              <span class="event-task">{{ taskZh[event.task] || event.task }} · {{ event.id }}</span>
              <span v-if="event.metric" class="event-metric">{{ event.metric }}</span>
              <span class="event-description">{{ event.interpretation }}</span>
              <span class="event-source">来源：{{ event.source || '未提供' }}<br>视频：{{ event.video_uri || '未提供' }}</span>
            </button>
            <p v-if="!filteredTimeline.length" class="empty-text">
              {{ timeline.length ? '没有匹配事件，请尝试其他关键词。' : '报告中暂无可审计时间轴。' }}
            </p>
          </div>
        </section>

        <section class="panel video-panel">
          <div class="panel-heading"><h3>视频与时间定位</h3></div>
          <p class="panel-description">选择任务视频，或从时间轴回看证据。</p>
          <select :value="selectedVideoUrl" aria-label="选择任务视频" :disabled="!availableVideos.length"
            @change="selectVideo($event.target.value)">
            <option v-if="!availableVideos.length" value="">暂无关联视频</option>
            <option v-for="video in availableVideos" :key="video.videoUrl" :value="video.videoUrl">
              {{ video.videoName || '任务视频' }}
            </option>
          </select>
          <video v-if="selectedVideoUrl" :key="selectedVideoUrl" ref="videoRef" :src="selectedVideoUrl"
            controls playsinline preload="metadata" @loadedmetadata="applyPendingSeek" @timeupdate="onTimeUpdate"
            @error="onVideoError"></video>
          <div v-else class="video-empty">暂无可播放的视频</div>
          <div class="video-meta"><span>{{ currentVideo?.videoName || '尚未加载视频' }}</span><span>{{ formatTime(currentTime) }}</span></div>
          <div v-if="selectedEvent" class="selected-evidence">
            <span class="eyebrow">当前定位证据</span>
            <strong>{{ selectedEvent.label }}</strong>
            <span>{{ formatTime(selectedEvent.start_s) }}–{{ formatTime(selectedEvent.end_s) }}</span>
          </div>
          <p v-if="videoMessage" class="feedback" role="status">{{ videoMessage }}</p>
        </section>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { findVideo, formatTime, parseDoctorOverview, parseReportTimeline, taskZh } from '@/utils/screeningReport'

const props = defineProps({
  md: { type: String, default: '' },
  videos: { type: Array, default: () => [] }
})

const overview = computed(() => parseDoctorOverview(props.md))
const timelineData = computed(() => parseReportTimeline(props.md))
const timeline = computed(() => timelineData.value.events)
const timelineError = computed(() => timelineData.value.error)
const search = ref('')
const filteredTimeline = computed(() => {
  const query = search.value.trim().toLowerCase()
  return timeline.value.filter(event =>
    `${taskZh[event.task] || ''} ${JSON.stringify(event)}`.toLowerCase().includes(query))
})

const videoRef = ref(null)
const availableVideos = computed(() => props.videos.filter(video => video.videoUrl))
const selectedVideoUrl = ref('')
const currentVideo = computed(() => availableVideos.value.find(video => video.videoUrl === selectedVideoUrl.value))
const selectedEvent = ref(null)
const currentTime = ref(0)
const videoMessage = ref('')
let pendingSeek = null

function selectVideo(url) {
  pendingSeek = null
  selectedEvent.value = null
  currentTime.value = 0
  videoMessage.value = ''
  selectedVideoUrl.value = url
}

async function loadEventVideo(event) {
  const hit = findVideo(availableVideos.value, event.video_uri)
  if (!hit) {
    videoMessage.value = `未匹配到视频：${event.video_uri || '未提供视频路径'}，请检查该儿童的关联视频。`
    return
  }
  const start = Number(event.start_s)
  if (event.start_s == null || !Number.isFinite(start) || start < 0) {
    videoMessage.value = '该事件没有有效的起始时间，无法定位。'
    return
  }
  videoMessage.value = ''
  selectedEvent.value = event
  pendingSeek = start
  if (selectedVideoUrl.value !== hit.videoUrl) currentTime.value = 0
  selectedVideoUrl.value = hit.videoUrl
  await nextTick()
  // 切换媒体后等待元数据，避免把时间写入上一个视频或丢失定位。
  applyPendingSeek()
}

function applyPendingSeek() {
  const video = videoRef.value
  if (!video || video.readyState < 1 || pendingSeek == null) return
  const target = pendingSeek
  pendingSeek = null
  if (Number.isFinite(video.duration) && target > video.duration) {
    videoMessage.value = '证据起始时间超出视频时长，请核对关联视频。'
    return
  }
  try {
    video.currentTime = target
    currentTime.value = target
    video.play().catch(() => {})
  } catch {
    videoMessage.value = '视频暂时无法定位，请等待加载后重试。'
  }
}

function onTimeUpdate() { currentTime.value = videoRef.value?.currentTime || 0 }
function onVideoError() {
  pendingSeek = null
  videoMessage.value = '视频加载失败，请检查视频地址或稍后重试。'
}

watch(() => props.md, () => {
  search.value = ''
  selectVideo('')
}, { immediate: true })

watch([availableVideos, timeline], () => {
  if (!currentVideo.value) {
    const first = timeline.value.map(event => findVideo(availableVideos.value, event.video_uri)).find(Boolean)
      || availableVideos.value[0]
    selectVideo(first?.videoUrl || '')
  }
}, { immediate: true })
</script>

<style scoped>
.report-viewer { --line: #e1e8ef; --muted: #64748b; --accent: #315c9e; color: #253449; display: flex; flex-direction: column; gap: 56px; }
.report-block { min-width: 0; background: #f5f7fa; border: 1px solid var(--line); border-radius: 14px; overflow: hidden; }
.report-viewer *, .report-viewer *::before, .report-viewer *::after { box-sizing: border-box; }
.rv-header { display: flex; justify-content: space-between; align-items: center; gap: 16px; padding: 24px; background: #fff; border-bottom: 1px solid var(--line); }
.eyebrow { color: var(--accent); font-size: 14px; font-weight: 600; }
.rv-header h2 { margin: 8px 0; font-size: 24px; line-height: 1.4; }
.rv-header p, .panel-description { color: var(--muted); font-size: 15px; line-height: 1.6; margin: 0; }
.report-badge { flex-shrink: 0; color: var(--accent); background: #edf3fb; border-radius: 20px; padding: 6px 12px; font-size: 14px; }
.rv-main { padding: 20px; }
.overview-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.overview-card, .panel { min-width: 0; background: #fff; border: 1px solid var(--line); border-radius: 10px; padding: 20px; }
.overview-card.wide { grid-column: 1 / -1; }
.overview-card.conclusion { background: #f0f6ff; border-color: #cbdcf4; }
.combined-overview { display: grid; grid-template-columns: 1fr; gap: 0; }
.combined-section { min-width: 0; }
.combined-section + .combined-section { margin-top: 24px; padding-top: 24px; border-top: 1px solid var(--line); }
.section-heading { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.section-number { display: grid; place-items: center; width: 30px; height: 30px; flex-shrink: 0; border-radius: 8px; color: var(--accent); background: #eaf1fb; font-size: 15px; font-weight: 700; }
h3 { font-size: 18px; line-height: 1.5; margin: 0; }
.markdown-body { font-size: 16px; line-height: 1.9; overflow-wrap: anywhere; }
.markdown-body :deep(p) { margin: 0 0 12px; }
.markdown-body :deep(ul), .markdown-body :deep(ol) { padding-left: 22px; margin: 0 0 12px; }
.markdown-body :deep(li) { margin-bottom: 6px; }
.markdown-body :deep(strong) { color: #1d3555; font-weight: 600; }
.markdown-body :deep(blockquote) { margin: 16px 0 0; padding: 10px 14px; border-left: 3px solid #c8d7ea; background: #f6f8fb; color: var(--muted); font-size: 15px; }
.markdown-body :deep(:last-child) { margin-bottom: 0; }
.markdown-body :deep(table), .markdown-body :deep(pre) { display: block; max-width: 100%; overflow-x: auto; }
.markdown-body :deep(img) { max-width: 100%; height: auto; }
.evidence-grid { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); align-items: start; gap: 16px; padding: 20px; }
.panel-heading { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 8px; }
.count { color: var(--muted); font-size: 14px; white-space: nowrap; }
.report-viewer input, .report-viewer select { width: 100%; min-width: 0; border: 1px solid #d5dfeb; border-radius: 7px; padding: 9px 10px; font: inherit; font-size: 15px; color: inherit; background: #fff; margin: 14px 0; }
.report-viewer :is(input, select, button):focus-visible { outline: 2px solid var(--accent); outline-offset: 2px; }
.timeline { display: flex; flex-direction: column; gap: 10px; max-height: 520px; overflow-y: auto; padding: 2px; }
.event { display: block; width: 100%; text-align: left; color: inherit; font: inherit; background: #fff; border: 1px solid var(--line); border-left: 3px solid #c1d3eb; border-radius: 8px; padding: 12px; cursor: pointer; overflow-wrap: anywhere; }
.event:hover, .event.active { background: #f0f6ff; border-color: #9bb9e3; }
.event-top { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 6px; font-size: 16px; }
.event-time { font-size: 14px; color: var(--accent); font-variant-numeric: tabular-nums; }
.event-task, .event-source { display: block; color: var(--muted); font-size: 14px; line-height: 1.6; margin-top: 6px; }
.event-metric, .event-description { display: block; font-size: 15px; line-height: 1.7; margin-top: 6px; }
.event-source { padding-top: 8px; border-top: 1px dashed var(--line); }
.video-panel video, .video-empty { width: 100%; aspect-ratio: 16 / 9; max-height: 450px; border-radius: 8px; background: #151e2c; }
.video-empty { display: grid; place-items: center; color: #b7c3d4; font-size: 16px; }
.video-meta { display: flex; justify-content: space-between; gap: 12px; margin-top: 10px; font-size: 14px; color: var(--muted); overflow-wrap: anywhere; }
.selected-evidence { display: flex; flex-direction: column; gap: 7px; margin-top: 18px; padding: 14px; border-radius: 8px; background: #f0f6ff; font-size: 15px; }
.feedback { background: #fff8eb; color: #866126; padding: 12px; border-radius: 7px; font-size: 15px; line-height: 1.6; overflow-wrap: anywhere; }
.empty-text { font-size: 16px; color: var(--muted); line-height: 1.7; }
@media (max-width: 1000px) { .evidence-grid { grid-template-columns: 1fr; } }
@media (max-width: 640px) {
  .report-viewer { gap: 36px; }
  .rv-header { padding: 18px; align-items: flex-start; }
  .rv-header h2 { font-size: 21px; }
  .report-badge { display: none; }
  .rv-main { padding: 12px; }
  .overview-grid { grid-template-columns: 1fr; }
  .combined-section + .combined-section { margin-top: 20px; padding-top: 20px; }
  .evidence-grid { padding: 12px; }
  .overview-card, .panel { padding: 16px; }
  .panel-heading { flex-wrap: wrap; }
  .report-viewer input, .report-viewer select { font-size: 16px; }
}
</style>
