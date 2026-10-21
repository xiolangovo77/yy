// 接口和数据库仍使用原来的字符串字段；新记录采用可直接阅读的固定格式。
export function serializeRepetitiveBehavior(present, severity) {
  if (present === false) return '无'
  if (present !== true) throw new Error('请选择有无重复刻板或感觉异常行为')
  if (!Number.isInteger(severity) || severity < 1 || severity > 5) {
    throw new Error('请选择1–5分的严重程度')
  }
  return `有，严重程度：${severity}分`
}

export function parseRepetitiveBehavior(value) {
  const text = typeof value === 'string' ? value.trim() : ''
  if (text === '无') return { present: false, severity: null, legacyText: '' }
  const match = /^有，严重程度：([1-5])分$/.exec(text)
  if (match) return { present: true, severity: Number(match[1]), legacyText: '' }
  // 历史自由文本仅作参考，不据此猜测是否存在行为或严重程度。
  return { present: null, severity: null, legacyText: text }
}
