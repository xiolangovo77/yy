import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { test } from 'node:test'
import { compileScript, parse } from '@vue/compiler-sfc'
import { effectScope, nextTick, reactive } from 'vue'
import { parseDoctorOverview, parseReportTimeline, findVideo, formatTime } from '../src/utils/screeningReport.js'

const md = readFileSync(new URL('../../llm_v9_10cases_20260805_202315/case_00072.md', import.meta.url), 'utf8')

test('样例的六项正文完整展示，且不混入后续家长版和审计内容', () => {
  const sections = parseDoctorOverview(md)
  assert.equal(sections.length, 6)
  assert.ok(sections.every(section => section.html.length > 0))
  assert.match(sections[0].html, /受评儿童，男，4岁/)
  assert.match(sections[1].html, /社交互动/)
  assert.match(sections[1].html, /沟通/)
  assert.match(sections[1].html, /面部定向与情绪反应/)
  assert.match(sections[3].html, /DD.*筛查倾向/)
  assert.equal((sections[4].html.match(/<li>/g) || []).length, 6)
  assert.equal((sections[5].html.match(/<li>/g) || []).length, 3)
  assert.match(sections[5].html, /结论边界/)
  assert.doesNotMatch(sections.map(section => section.html).join(''), /家长沟通版摘要|REPORT_DATA|模型概率/)
})

test('正文独立于机器数据解析，缺失模块保持明确的空状态', () => {
  const bodyOnly = md.split('<!-- REPORT_DATA_START -->')[0]
  assert.deepEqual(parseDoctorOverview(bodyOnly), parseDoctorOverview(md))
  assert.ok(parseReportTimeline(bodyOnly).error)
  assert.ok(parseDoctorOverview('').every(section => section.html === ''))
  const partial = '## 综合筛查结论(医生速览)\r\n\r\n### 4. 综合筛查意见\r\n\r\n**示例意见**\r\n\r\n## 下一节\r\n不应展示'
  const sections = parseDoctorOverview(partial)
  assert.match(sections[3].html, /<strong>示例意见<\/strong>/)
  assert.equal(sections.filter(section => section.html).length, 1)
  assert.doesNotMatch(sections[3].html, /不应展示/)
})

test('报告中的 HTML、脚本链接和图片不能变为活动内容', () => {
  const sample = '## 综合筛查结论（医生速览）\n\n### 一、基本信息与资料范围\n\n<script>alert(1)</script>\n\n[危险链接](javascript:alert%281%29)\n\n![图片](https://example.com/tracker.png)\n'
  const html = parseDoctorOverview(sample)[0].html
  assert.doesNotMatch(html, /<script|<img|href=|javascript:/i)
  assert.match(html, /&lt;script&gt;/)
})

test('保留全部审计事件及原始定位，不按家长可见性过滤', () => {
  const { events, error } = parseReportTimeline(md)
  assert.equal(error, '')
  assert.equal(events.length, 7)
  assert.equal(events[0].start_s, 2.63)
  assert.equal(events[0].video_uri, 'videos/00072/action1.mp4')
  assert.equal(events.filter(event => !event.parent_visible).length, 6)
  assert.ok(parseReportTimeline('<!-- REPORT_DATA_START -->bad<!-- REPORT_DATA_END -->').error)
  assert.ok(parseReportTimeline('<!-- REPORT_DATA_START -->{"timeline":{}}<!-- REPORT_DATA_END -->').error)
})

test('视频按文件名匹配，支持签名地址，缺失时不误用其他视频', () => {
  const videos = [
    { videoName: 'action1.mp4', videoUrl: 'https://media.example/a.mp4' },
    { videoName: '语言问答', videoUrl: 'https://media.example/dialog.mp4?token=example' }
  ]
  assert.equal(findVideo(videos, 'videos/00072/action1.mp4'), videos[0])
  assert.equal(findVideo(videos, 'videos/00072/dialog.mp4'), videos[1])
  assert.equal(findVideo(videos, 'videos/00072/action6.mp4'), null)
  assert.equal(findVideo(videos, ''), null)
})

test('时间显示处理分钟进位及无效数据', () => {
  assert.equal(formatTime(2.63), '00:02.6')
  assert.equal(formatTime(59.99), '01:00.0')
  assert.equal(formatTime(0), '00:00.0')
  assert.equal(formatTime(null), '—')
  assert.equal(formatTime('invalid'), '—')
})

test('视频异步加载后定位，未匹配视频不改变播放器，切换报告清除旧状态', async () => {
  const source = readFileSync(new URL('../src/components/ReportViewer.vue', import.meta.url), 'utf8')
  const { descriptor } = parse(source)
  const script = compileScript(descriptor, { id: 'report-viewer-test' }).content
    .replace(/from 'vue'/g, `from '${import.meta.resolve('vue')}'`)
    .replace(/from '@\/utils\/screeningReport'/g, `from '${new URL('../src/utils/screeningReport.js', import.meta.url).href}'`)
  const { default: component } = await import(`data:text/javascript;base64,${Buffer.from(script).toString('base64')}`)
  const props = reactive({ md, videos: [] })
  const scope = effectScope()
  try {
    const state = scope.run(() => component.setup(props, { expose() {} }))
    assert.equal(state.selectedVideoUrl.value, '')
    props.videos = [
      { videoName: 'action1.mp4', videoUrl: '/action1.mp4' },
      { videoName: 'dialog.mp4', videoUrl: '/dialog.mp4' }
    ]
    await nextTick()
    assert.equal(state.selectedVideoUrl.value, '/action1.mp4')
    let playCount = 0
    const player = { readyState: 0, duration: 60, currentTime: 0, play() { playCount++; return Promise.resolve() } }
    state.videoRef.value = player
    const event = state.timeline.value[0]
    await state.loadEventVideo(event)
    assert.equal(player.currentTime, 0)
    player.readyState = 1
    state.applyPendingSeek()
    assert.equal(player.currentTime, 2.63)
    assert.equal(playCount, 1)
    await state.loadEventVideo({ ...event, video_uri: 'missing.mp4' })
    assert.equal(state.selectedVideoUrl.value, '/action1.mp4')
    assert.equal(player.currentTime, 2.63)
    assert.match(state.videoMessage.value, /未匹配到视频/)
    // 模拟 Vue 在视频地址变化后挂载的新播放器，等待其元数据事件。
    const nextPlayer = { ...player, readyState: 0, currentTime: 0 }
    state.videoRef.value = nextPlayer
    await state.loadEventVideo(state.timeline.value[2])
    assert.equal(state.selectedVideoUrl.value, '/dialog.mp4')
    assert.equal(nextPlayer.currentTime, 0)
    nextPlayer.readyState = 1
    state.applyPendingSeek()
    assert.equal(nextPlayer.currentTime, 3.059)
    props.md = ''
    await nextTick()
    assert.equal(state.selectedEvent.value, null)
    assert.equal(state.timeline.value.length, 0)
    assert.ok(state.overview.value.every(section => !section.html))
  } finally {
    scope.stop()
  }
})
