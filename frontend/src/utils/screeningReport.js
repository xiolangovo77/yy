import { Marked } from 'marked'

const titles = ['基本信息与资料范围', '主要观察与筛查依据', '相对保留能力', '综合筛查意见', '主要鉴别与证据限制', '建议进一步评估']
const numbers = ['一', '二', '三', '四', '五', '六']
const escapeHtml = text => String(text).replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]))

// 报告只是展示数据：保留段落、强调、列表等格式，不执行 HTML 或嵌入外部资源。
const markdown = new Marked({
  gfm: true,
  renderer: {
    html({ text }) { return escapeHtml(text) },
    link({ tokens }) { return this.parser.parseInline(tokens) },
    image({ text }) { return escapeHtml(text) }
  }
})

export function parseDoctorOverview(md = '') {
  const sections = titles.map((title, index) => ({ number: index + 1, title, html: '' }))
  const tokens = markdown.lexer(md.replace(/\r\n?/g, '\n'))
  const start = tokens.findIndex(token => token.type === 'heading' && token.depth === 2
    && /^综合筛查结论\s*[（(]医生速览[）)]$/.test(token.text.trim()))
  if (start < 0) return sections
  let current = null
  const content = titles.map(() => [])
  for (const token of tokens.slice(start + 1)) {
    if (token.type === 'heading' && token.depth <= 2) break
    if (token.type === 'heading' && token.depth === 3) {
      const match = token.text.match(/^([一二三四五六1-6])[、.．）)]\s*(.*)$/)
      const index = match ? (/\d/.test(match[1]) ? Number(match[1]) - 1 : numbers.indexOf(match[1])) : -1
      current = index >= 0 ? index : null
      if (current !== null && match[2].trim()) sections[current].title = match[2].trim()
    } else if (current !== null && token.type !== 'hr') {
      content[current].push(token.raw)
    }
  }
  return sections.map((section, index) => ({ ...section, html: markdown.parse(content[index].join('')).trim() }))
}

export function parseReportTimeline(md = '') {
  const block = md.match(/<!--\s*REPORT_DATA_START\s*-->([\s\S]*?)<!--\s*REPORT_DATA_END\s*-->/)
  let jsonError = false
  if (block) {
    try {
      const json = block[1].match(/```json\s*([\s\S]*?)```/i)
      const data = JSON.parse(json ? json[1] : block[1].trim())
      if (!data || (data.timeline != null && !Array.isArray(data.timeline))) throw new Error('Invalid timeline')
      const events = (data.timeline || []).filter(event => event && typeof event === 'object' && !Array.isArray(event))
      if (events.length) return { events, error: '' }
    } catch {
      jsonError = true
    }
  }
  // 新版医生速览直接使用 Markdown 表格；旧 JSON 缺失、为空或损坏时尝试读取表格。
  const events = parseTimelineTable(md)
  if (events.length) return { events, error: '' }
  if (jsonError) return { events: [], error: '时间轴数据解析失败，仍可阅读上方结论。' }
  return { events: [], error: block ? '' : '报告未提供可识别的时间轴数据，仍可阅读上方结论。' }
}

function inlineText(tokens = []) {
  return tokens.map(token => token.tokens ? inlineText(token.tokens) : token.text || '').join('').trim()
}

function firstLink(tokens = []) {
  for (const token of tokens) {
    if (token.type === 'link') return token.href
    if (token.tokens) {
      const href = firstLink(token.tokens)
      if (href) return href
    }
  }
  return ''
}

function videoLocation(cell) {
  const locationText = inlineText(cell?.tokens)
  const href = firstLink(cell?.tokens)
  if (!href) return { video_uri: '', video_mode: 'missing', location_text: locationText, start_s: null, end_s: null }
  const hash = href.indexOf('#')
  const uri = hash < 0 ? href : href.slice(0, hash)
  const fragment = hash < 0 ? '' : href.slice(hash + 1)
  const time = /(?:^|&)t=([^&]*)/.exec(fragment)
  if (!time) return { video_uri: uri, video_mode: 'full', location_text: locationText, start_s: null, end_s: null }
  const range = /^(\d+(?:\.\d+)?)(?:,(\d+(?:\.\d+)?))?$/.exec(time[1])
  const start = range ? Number(range[1]) : NaN
  const end = range?.[2] == null ? null : Number(range[2])
  if (!Number.isFinite(start) || (end !== null && (!Number.isFinite(end) || end < start))) {
    return { video_uri: uri, video_mode: 'invalid', location_text: locationText, start_s: null, end_s: null }
  }
  return { video_uri: uri, video_mode: 'clip', location_text: locationText, start_s: start, end_s: end }
}

function parseTimelineTable(md) {
  const tokens = markdown.lexer(md.replace(/\r\n?/g, '\n'))
  const events = []
  let sectionDepth = null
  for (const token of tokens) {
    if (token.type === 'heading') {
      if (/可审计\s*(?:视频\s*)?时间轴/.test(token.text)) sectionDepth = token.depth
      else if (sectionDepth !== null && token.depth <= sectionDepth) sectionDepth = null
    }
    if (sectionDepth === null || token.type !== 'table') continue
    const headers = token.header.map(cell => inlineText(cell.tokens).replace(/\s/g, ''))
    const columns = ['证据ID', '任务', '事件', '视频定位', '自动指标', '解释', '字段来源'].map(name => headers.indexOf(name))
    if (columns.some(index => index < 0)) continue
    for (const row of token.rows) {
      const cells = columns.map(index => row[index])
      const [id, task, label, , metric, interpretation, source] = cells.map(cell => inlineText(cell?.tokens))
      if (!id && !label) continue
      events.push({ id, task, label, metric, interpretation, source, ...videoLocation(cells[3]) })
    }
  }
  return events
}

export const taskZh = { action1: 'Task1 呼名反应', action2: 'Task2 玩具分享', action3: 'Task3 玩具取回', action4: '动作四·年龄提问', action5: '动作五·语言问答', action6: 'Task6 模仿', dialog: '语言/沟通任务' }

export function formatTime(value) {
  if (value == null || value === '' || !Number.isFinite(Number(value)) || Number(value) < 0) return '—'
  const tenths = Math.round(Number(value) * 10)
  return `${String(Math.floor(tenths / 600)).padStart(2, '0')}:${((tenths % 600) / 10).toFixed(1).padStart(4, '0')}`
}

function basename(value = '') {
  const name = String(value).split(/[?#]/)[0].replace(/\\/g, '/').split('/').pop()
  try { return decodeURIComponent(name).toLowerCase() } catch { return name.toLowerCase() }
}

export function findVideo(videos, uri) {
  const name = basename(uri)
  if (!name) return null
  return videos.find(video => video.videoUrl && (basename(video.videoName) === name || basename(video.videoUrl) === name)) || null
}
