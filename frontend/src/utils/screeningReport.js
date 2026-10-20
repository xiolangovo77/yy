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
  if (!block) return { events: [], error: '报告未提供时间轴数据，仍可阅读上方结论。' }
  try {
    const json = block[1].match(/```json\s*([\s\S]*?)```/i)
    const data = JSON.parse(json ? json[1] : block[1].trim())
    if (!data || (data.timeline != null && !Array.isArray(data.timeline))) throw new Error('Invalid timeline')
    return { events: (data.timeline || []).filter(event => event && typeof event === 'object' && !Array.isArray(event)), error: '' }
  } catch {
    return { events: [], error: '时间轴数据解析失败，仍可阅读上方结论。' }
  }
}

export const taskZh = { action1: 'Task1 呼名反应', action2: 'Task2 玩具分享', action3: 'Task3 玩具取回', action6: 'Task6 模仿', dialog: '语言/沟通任务' }

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
