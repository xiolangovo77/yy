import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { test } from 'node:test'
import { compileScript, parse } from '@vue/compiler-sfc'
import * as vue from 'vue'
import validator from 'async-validator'
import * as fields from '../src/utils/assessmentFields.js'
import * as report from '../src/utils/screeningReport.js'

const Schema = validator.default || validator

// 执行组件自身逻辑，仅替换网络、路由和消息弹窗。
function setupComponent(path, props = {}, api = {}, emit = () => {}) {
  const source = readFileSync(new URL(`../src/${path}`, import.meta.url), 'utf8')
  const { descriptor } = parse(source)
  const compiled = compileScript(descriptor, { id: path }).content
  const dependencies = {
    vue: { ...vue, onMounted() {} },
    'vue-router': { useRoute: () => ({ params: { taskId: '42' } }), useRouter: () => ({ push() {} }) },
    '@/composables/useMediaQuery': { useMediaQuery: () => vue.ref(false) },
    '@/api/task': api,
    '@/api/child': {},
    '@/api/video': {},
    '@/api/report': {},
    '@/utils/video': {},
    '@/utils/assessmentFields': fields,
    '@/utils/screeningReport': report,
    '@/components/ReportViewer.vue': {},
    '@/components/TaskFeedback.vue': {}
  }
  const script = compiled
    .replace(/^import \{([^}]+)\} from ['"]([^'"]+)['"];?$/gm, (_, names, name) =>
      `const { ${names.replace(/\bas\b/g, ':')} } = dependencies[${JSON.stringify(name)}]`)
    .replace(/^import (\w+) from ['"]([^'"]+)['"];?$/gm, (_, name, path) =>
      `const ${name} = dependencies[${JSON.stringify(path)}]`)
    .replace('export default', 'return')
  const messages = []
  const component = new Function('dependencies', 'ElMessage', 'ElMessageBox', script)(
    dependencies,
    { success: text => messages.push(text), warning: text => messages.push(text) },
    { confirm: async () => {} }
  )
  const scope = vue.effectScope()
  const state = scope.run(() => component.setup(vue.reactive(props), { expose() {}, emit }))
  return { state, messages, stop: () => scope.stop() }
}

function assessment(api = {}) {
  const harness = setupComponent('views/doctor/Assessment.vue', {}, {
    getTaskById: async () => ({ data: { currentStep: 2, taskStatus: 2, feedbackSubmitted: true } }),
    getForm1ByTaskId: async () => ({ data: null }),
    getForm2ByTaskId: async () => ({ data: null }),
    ...api
  })
  harness.state.taskInfo.currentStep = 2
  harness.state.taskInfo.taskStatus = 1
  harness.state.form2Ref.value = { validate: async () => true }
  return harness
}

test('报告使用评价未填完整时，不提交阶段二，也不标记完成', async () => {
  let calls = 0
  const h = assessment({ submitForm2: async () => { calls++ } })
  try {
    h.state.feedbackRef.value = { validate: async () => false, persist: async () => { calls++ } }
    await h.state.submitForm2()
    assert.equal(calls, 0)
    assert.equal(h.state.taskInfo.taskStatus, 1)
    assert.equal(h.state.assessmentComplete.value, false)
  } finally { h.stop() }
})

test('阶段二字段校验失败时，即使评价完整也不能提交', async () => {
  let calls = 0
  const h = assessment({ submitForm2: async () => { calls++ } })
  try {
    h.state.form2Ref.value = { validate: async () => { throw new Error('缺少最终结论') } }
    h.state.feedbackRef.value = { validate: async () => true, persist: async () => { calls++ } }
    await h.state.submitForm2()
    assert.equal(calls, 0)
  } finally { h.stop() }
})

test('按现有接口先提交阶段二再提交评价，评价失败后保留页面且仅重试评价', async () => {
  const calls = []
  const h = assessment({ submitForm2: async data => {
    calls.push('form2')
    assert.equal(data.taskId, '42')
    assert.equal('doctorRemark' in data, false)
  } })
  let fail = true
  try {
    h.state.feedbackRef.value = {
      validate: async () => true,
      persist: async () => {
        calls.push('feedback')
        assert.equal(h.state.taskInfo.taskStatus, 2)
        if (fail) throw new Error('网络失败')
        h.state.taskInfo.feedbackSubmitted = true
      }
    }
    await h.state.submitForm2()
    assert.deepEqual(calls, ['form2', 'feedback'])
    assert.equal(h.state.assessmentComplete.value, false)
    assert.equal(h.state.form2Submitting.value, false)
    assert.match(h.messages.at(-1), /请重试/)
    fail = false
    await h.state.submitForm2()
    assert.deepEqual(calls, ['form2', 'feedback', 'feedback'])
    assert.equal(h.state.assessmentComplete.value, true)
  } finally { h.stop() }
})

test('阶段二保存失败时不调用评价接口', async () => {
  let feedbackCalls = 0
  const h = assessment({ submitForm2: async () => { throw new Error('保存失败') } })
  try {
    h.state.feedbackRef.value = { validate: async () => true, persist: async () => { feedbackCalls++ } }
    await h.state.submitForm2()
    assert.equal(feedbackCalls, 0)
    assert.equal(h.state.taskInfo.taskStatus, 1)
    assert.equal(h.state.form2Submitting.value, false)
  } finally { h.stop() }
})

test('重新进入已保存评估但未评价的任务，仍需在阶段二完成评价', async () => {
  const h = assessment({
    getTaskById: async () => ({ data: { currentStep: 2, taskStatus: 2, feedbackSubmitted: false } }),
    getForm2ByTaskId: async () => ({ data: { diseaseType: 'TD', confidence: 0, finalConclusion: '保留原意见' } })
  })
  try {
    await h.state.loadTask()
    assert.equal(h.state.assessmentComplete.value, false)
    assert.equal(h.state.form2.diseaseType, 'TD')
    assert.equal(h.state.form2.confidence, 0)
    assert.equal(h.state.form2.finalConclusion, '保留原意见')
  } finally { h.stop() }
})

test('阶段二使用评价的三道选择题及改进建议均必填，空白建议不通过', async () => {
  const h = setupComponent('components/TaskFeedback.vue', { taskId: '42', integrated: true }, {
    getTaskFeedback: async () => ({ data: null })
  })
  try {
    const schema = new Schema(h.state.rules.value)
    await assert.rejects(schema.validate({ ...h.state.form }))
    Object.assign(h.state.form, {
      coreProblemClarity: '符合', judgmentImprovement: '一般', observationAccuracy: '符合', suggestions: '   '
    })
    await assert.rejects(schema.validate({ ...h.state.form }))
    h.state.form.suggestions = '无'
    await schema.validate({ ...h.state.form })
  } finally { h.stop() }
})

test('评价保存成功但响应丢失时，重试读取已有评价，不重复提交', async () => {
  let saved = null
  let posts = 0
  let submitted = false
  const h = setupComponent('components/TaskFeedback.vue', { taskId: '42', integrated: true }, {
    getTaskFeedback: async () => ({ data: saved }),
    submitTaskFeedback: async data => { posts++; saved = data; throw new Error('响应丢失') }
  }, () => { submitted = true })
  try {
    await h.state.loadFeedback()
    Object.assign(h.state.form, {
      coreProblemClarity: '符合', judgmentImprovement: '一般', observationAccuracy: '符合', suggestions: '无'
    })
    h.state.formRef.value = { validate: async () => true }
    await assert.rejects(h.state.persist())
    assert.equal(h.state.form.suggestions, '无')
    assert.equal(h.state.submitting.value, false)
    await h.state.persist()
    assert.equal(posts, 1)
    assert.equal(submitted, true)
    assert.equal(h.state.saved.value.suggestions, '无')
  } finally { h.stop() }
})

test('阶段二报告只展示前五模块，第六模块正文不会串入第五模块', () => {
  const md = '## 综合筛查结论（医生速览）\n\n### 五、主要鉴别与证据限制\n\n保留的第五模块\n\n### 六、建议进一步评估\n\n移除的第六模块\n'
  const h = setupComponent('components/ReportViewer.vue', { md, videos: [] })
  try {
    assert.equal(h.state.overview.value.length, 5)
    assert.match(h.state.overview.value[4].html, /保留的第五模块/)
    assert.doesNotMatch(h.state.overview.value.map(section => section.html).join(''), /移除的第六模块/)
  } finally { h.stop() }
})
