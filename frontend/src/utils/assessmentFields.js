export const stageOneScores = [
  { key: 'socialResponse', label: '社交回应', description: '主要看叫名、说话、主动接近时，儿童是否对人产生合理回应。' },
  { key: 'eyeContact', label: '眼神交流', description: '是否自然、适当地看向互动者。' },
  { key: 'socialReciprocity', label: '社会互惠性', description: '是否存在自然的来回互动。' },
  { key: 'socialEmotionalExpression', label: '社会情绪表达', description: '情绪表达是否与互动情境匹配。' }
]

export const stageTwoScores = [
  { key: 'reportUsefulness', label: '报告有用程度' },
  { key: 'evidenceValidity', label: '报告证据有效性' },
  { key: 'factAccuracy', label: '事实是否准确' }
]

export const feedbackQuestions = [
  { key: 'coreProblemClarity', label: '报告是否能让您快速找到患者核心问题' },
  { key: 'judgmentImprovement', label: '您认为使用系统后是否能提高判断质量/效率' },
  { key: 'observationAccuracy', label: '报告中的观察事实是否准确' }
]

export const feedbackChoices = ['符合', '一般', '不符合']

export function emptyScores(fields) {
  return Object.fromEntries(fields.map(field => [field.key, null]))
}

export function scoreRules(fields) {
  return Object.fromEntries(fields.map(field => [field.key, [
    { required: true, type: 'number', min: 1, max: 5, message: `请为${field.label}选择1–5分`, trigger: 'change' }
  ]]))
}
